package com.hcp.system.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.system.UserInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hcp.common.core.enums.UserTypeEnum;
import com.hcp.system.domain.SysMenu;
import com.hcp.system.domain.SysTenant;
import com.hcp.system.domain.vo.UserInfoVO;
import com.hcp.system.domain.vo.UserListVO;
import com.hcp.system.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.hcp.common.core.domain.R;
import com.hcp.common.core.utils.StringUtils;
import com.hcp.common.core.utils.poi.ExcelUtil;
import com.hcp.common.core.web.controller.BaseController;
import com.hcp.common.core.web.domain.AjaxResult;
import com.hcp.common.log.annotation.Log;
import com.hcp.common.log.enums.BusinessType;
import com.hcp.common.security.annotation.InnerAuth;
import com.hcp.common.security.annotation.RequiresPermissions;
import com.hcp.common.security.utils.SecurityUtils;
import com.hcp.system.api.domain.SysDept;
import com.hcp.system.api.domain.SysRole;
import com.hcp.system.api.domain.SysUser;
import com.hcp.system.api.model.LoginUser;

/**
 * 用户信息
 *
 * @author JHan
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户管理接口")
@RequestMapping("/user")
public class SysUserController extends BaseController {

    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final ISysDeptService deptService;
    private final ISysPostService postService;
    private final ISysPermissionService permissionService;
    private final ISysConfigService configService;
    private final ISysTenantService sysTenantService;

    /**
     * 获取用户列表
     */
    @RequiresPermissions("system:user:list")
    @GetMapping("/list")
    public AjaxResult list(SysUser user)
    {
        IPage<SysUser> list = userService.selectUserPage(user);
        return AjaxResult.success(list);
    }

    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("system:user:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysUser user)
    {
        List<SysUser> list = userService.selectUserList(user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.exportExcel(response, list, "用户数据");
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @RequiresPermissions("system:user:import")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        String operName = SecurityUtils.getUsername();
        String message = userService.importUser(userList, updateSupport, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) throws IOException
    {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.importTemplateExcel(response, "用户数据");
    }

    /**
     * 获取当前用户信息
     */
    @InnerAuth
    @GetMapping("/info/{phone}")
    public R<LoginUser> info(@PathVariable("phone") String phone)
    {
        SysUser sysUser = userService.selectUserByPhone(phone);
        if (StringUtils.isNull(sysUser))
        {
            return R.fail("用户名或密码错误");
        }
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(sysUser);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(sysUser);
        // 查询租户信息
        SysTenant tenant = sysTenantService.selectSysTenantById(sysUser.getMerchantId());
        LoginUser sysUserVo = new LoginUser();
        sysUserVo.setUserType(sysUser.getUserType().getValue());
        sysUserVo.setRealName(sysUser.getRealName());
        sysUserVo.setSysUser(sysUser);
        sysUserVo.setRoles(roles);
        sysUserVo.setPermissions(permissions);
        if(tenant != null)
        {
            sysUserVo.setTenantEndTime(tenant.getTenantEndTime());
            sysUserVo.setStatus(tenant.getStatus());
        }
        return R.ok(sysUserVo);
    }

    /**
     * 注册用户信息（暂时不开启）
     */
    @InnerAuth
    @Tag(name = "", description = "暂时禁用接口")
    @PostMapping("/register")
    public R<Boolean> register(@RequestBody SysUser sysUser)
    {
        String username = sysUser.getUserName();
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return R.fail("当前系统没有开启注册功能！");
        }
        if (!userService.checkUserNameUnique(sysUser))
        {
            return R.fail("保存用户'" + username + "'失败，注册账号已存在");
        }
        return R.ok(userService.registerUser(sysUser));
    }

    /**
     * 网络 获取登录用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo() {
        //调整下查询顺序 将租户id塞入当前查询线程 --或者直接让权限过滤忽略租户查询 毕竟账号id唯一 目前选择直接忽略查询 后续此处修改
        SysUser user = userService.selectUserById(SecurityUtils.getUserId());
        // 前台返回用户信息
        UserInfoVO userInfoVO = new UserInfoVO();
        // 判断用户身份类型 返回运营商相关的信息
        if (user.getUserType() == UserTypeEnum.SYS_SUPER || user.getUserType() == UserTypeEnum.PLAT_ADMIN){
            userInfoVO.setAccountId(user.getUserId().toString())
                      .setAccountNo(null)
                      .setAccountAttribute(null)
                      .setBusinessType(null)
                      .setUserAttribute(user.getUserType().getValue())
                      .setNickName(user.getNickName())
                      .setOperatorId(null)
                      .setOperatorName(null)
                      .setIsMainAccount(null)
                      .setIsBDAccount(null)
                      .setSecurityCode(user.getPhone())
                      .setAvatar(user.getAvatar());
        } else if (user.getUserType() == UserTypeEnum.MCHT_OP || user.getUserType() == UserTypeEnum.MCHT_STAFF){
            // 根据 merchantId 查询运营商(租户)信息
            SysTenant tenant = sysTenantService.selectSysTenantById(user.getMerchantId());
            userInfoVO.setAccountId(user.getUserId().toString())
                    .setAccountNo(tenant.getMerchantNo())
                    .setAccountAttribute(tenant.getSubjectType().getValue())
                    .setBusinessType(tenant.getMerchantType().getValue())
                    .setUserAttribute(user.getUserType().getValue())
                    .setNickName(user.getNickName())
                    .setOperatorId(user.getCreateById().toString())
                    .setOperatorName(tenant.getMerchantName())
                    .setIsMainAccount(user.getIsPrimary())
                    .setIsBDAccount(user.getIsSubAccount())
                    .setSecurityCode(user.getPhone())
                    .setAvatar(user.getAvatar());
        }

        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);

        AjaxResult ajax = AjaxResult.success();
        ajax.put("body",userInfoVO);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);

        return ajax;
    }

    /**
     * 根据用户编号获取详细信息
     */
    @RequiresPermissions("system:user:query")
    @GetMapping(value = { "/", "/{userId:\\d+}" })
    public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Long userId)
    {
        // 获取用户信息 TODO
        userService.checkUserDataScope(userId);
        AjaxResult ajax = AjaxResult.success();
        List<SysRole> roles = roleService.selectRoleAll();
        ajax.put("roles", SysUser.isSuperAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        ajax.put("posts", postService.selectPostAll());
        if (StringUtils.isNotNull(userId))
        {
            SysUser sysUser = userService.selectUserById(userId);
            ajax.put(AjaxResult.DATA_TAG, sysUser);
            ajax.put("postIds", postService.selectPostListByUserId(userId));
            ajax.put("roleIds", sysUser.getRoles().stream().map(SysRole::getRoleId).collect(Collectors.toList()));
        }
        return ajax;
    }

    /**
     * 新增用户
     */
    @RequiresPermissions("system:user:add")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysUser user)
    {
        if (!userService.checkUserNameUnique(user))
        {
            return error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhone()) && !userService.checkUserPhoneUnique(user))
        {
            return error("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            return error("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setCreateBy(SecurityUtils.getUsername());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        if (!userService.checkUserNameUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhone()) && !userService.checkUserPhoneUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(userService.updateUser(user));
    }

    /**
     * 删除用户
     */
    @RequiresPermissions("system:user:remove")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds)
    {
        if (ArrayUtils.contains(userIds, SecurityUtils.getUserId()))
        {
            return error("当前用户不能删除");
        }
        return toAjax(userService.deleteUserByIds(userIds));
    }

    /**
     * 重置密码
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(userService.resetPwd(user));
    }

    /**
     * 状态修改
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(userService.updateUserStatus(user));
    }

    /**
     * 根据用户编号获取授权角色  TODO
     */
    @RequiresPermissions("system:user:query")
    @GetMapping("/authRole/{userId}")
    public AjaxResult authRole(@PathVariable("userId") Long userId)
    {
        AjaxResult ajax = AjaxResult.success();
        SysUser user = userService.selectUserById(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        ajax.put("user", user);
        ajax.put("roles", SysUser.isSuperAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        return ajax;
    }

    /**
     * 用户授权角色
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PutMapping("/authRole")
    public AjaxResult insertAuthRole(Long userId, Long[] roleIds)
    {
        userService.checkUserDataScope(userId);
        userService.insertUserAuth(userId, roleIds);
        return success();
    }

    /**
     * 获取部门树列表
     */
    @RequiresPermissions("system:user:list")
    @GetMapping("/deptTree")
    public AjaxResult deptTree(SysDept dept)
    {
        return success(deptService.selectDeptTreeList(dept));
    }


    /**
     * 用于经营管理根据用户筛选 <br>
     * 如果是平台级管理员(身份为1 2) 返回所有用户 <br>
     * 如果非管理员(除1 2之外) 只返回当前用户
     * @return
     */
    @RequiresPermissions("system:user:list")
    @GetMapping("/userList")
    public AjaxResult userList()
    {
        // 获取登录用户信息
        LoginUser loginUser = SecurityUtils.getLoginUser();
        // 获取用户身份
        Long userType = Long.valueOf(loginUser.getUserType());

        List<UserListVO> userVOList = new ArrayList<>();
        if (SysUser.isSuperAdmin(userType) || SysUser.isPlatformAdmin(userType)) {
            List<SysUser> sysUsers = userService.selectUserList(new SysUser());
            for(SysUser user:sysUsers){
                // 获取用户身份
                Long role = user.getRoleId();
                if (!SysUser.isSuperAdmin(role) && !SysUser.isPlatformAdmin(role)) {
                    userVOList.add(convertToVO(user));
                }
            }
        }else {
            userVOList.add(convertToVO(userService.selectUserById(SecurityUtils.getUserId())));
        }
        return AjaxResult.success(userVOList);
    }

    /**
     * 转换为精简VO，过滤敏感字段
     */
    private UserListVO convertToVO(SysUser user) {
        UserListVO vo = new UserListVO();
                vo.setUserId(String.valueOf(user.getUserId()))
                .setAccount(user.getUserName())
                .setNickName(user.getNickName())
                .setAvatar(user.getAvatar())
                .setSecurityCode(user.getPhone())
                .setStatus(user.getStatus().getValue())
                .setRole(user.getUserType().getValue())
                .setIsMain(user.getIsPrimary())
                .setCreateBy(String.valueOf(user.getCreateById()))
                .setAccountOwner(String.valueOf(user.getMerchantId()));
        return vo;
    }

}
