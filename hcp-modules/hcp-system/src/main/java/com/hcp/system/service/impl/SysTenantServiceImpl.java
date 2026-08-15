package com.hcp.system.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.hcp.common.core.constant.CacheConstants;
import com.hcp.common.core.domain.R;
import com.hcp.common.core.enums.SubjectTypeEnum;
import com.hcp.common.core.enums.UserSourceEnum;
import com.hcp.common.core.enums.UserStatusEnum;
import com.hcp.common.core.enums.UserTypeEnum;
import com.hcp.common.core.exception.ServiceException;
import com.hcp.common.core.utils.StringUtils;
import com.hcp.common.core.utils.UserGenerator;
import com.hcp.common.core.web.domain.AjaxResult;
import com.hcp.common.message.mail.EmailUtil;
import com.hcp.common.message.sms.SmsUtil;
import com.hcp.common.mybatisplus.util.TenantUtils;
import com.hcp.common.redis.service.RedisService;
import com.hcp.common.security.utils.SecurityUtils;
import com.hcp.system.api.domain.*;
import com.hcp.system.api.domain.dto.RegisterMerchantDTO;
import com.hcp.system.api.domain.dto.RegisterMerchantSubAccountDTO;
import com.hcp.system.api.model.LoginUser;
import com.hcp.system.domain.*;
import com.hcp.system.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hcp.system.service.ISysTenantService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hcp.common.core.text.Convert;
import com.hcp.common.core.utils.ServletUtils;
import com.hcp.common.mybatisplus.constant.MybatisPageConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * 租户管理Service业务层处理
 *
 * @author JHan
 * @date 2026-08-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysTenantServiceImpl implements ISysTenantService
{

    private final SysTenantMapper sysTenantMapper;
    private final SysUserMapper sysUserMapper;
    private final SysTenantPackageMapper sysTenantPackageMapper;
    private final SysDeptMapper deptMapper;
    private final SysPostMapper sysPostMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysRoleDeptMapper sysRoleDeptMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserPostMapper userPostMapper;

    private final EmailUtil emailUtil;
    private final SmsUtil smsUtil;
    private final RedisService redisService;


    /**
     * 平台管理员注册运营商（租户 + 管理员主账号）
     * <p>
     * 在一个事务中完成：<br>
     * 1. 校验手机号唯一性 <br>
     * 2. 生成运营商编号 merchantNo（如未提供） <br>
     * 3. 生成管理员账号（XZC + 时间戳 + 随机数）<br>
     * 4. 生成随机密码 <br>
     * 5. 插入 sys_tenant <br>
     * 6. 插入 sys_user（userType = MCHT_OP, isPrimary = 1） <br>
     * 7. 短信/邮件通知管理员账号密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> registerMerchant(RegisterMerchantDTO dto) {

        // 获取平台级管理员的信息
        LoginUser loginUser = SecurityUtils.getLoginUser();

        // ==================== 第一部分：参数校验 ====================

        // 校验手机号是否已被注册
        SysUser existUser = sysUserMapper.checkUserPhoneUnique(dto.getPhone());
        if (existUser != null) {
            return R.fail("该手机号已被注册：" + dto.getPhone());
        }

        // 校验统一社会信用代码唯一性（企业法人和个体工商户必填写）
        if (dto.getSubjectType().equals(SubjectTypeEnum.INDIVIDUAL) && StringUtils.isEmpty(dto.getUnifiedSocialCode())) {
            return R.fail("个体工商户必须填写统一社会信用代码");
        }
        if (dto.getSubjectType().equals(SubjectTypeEnum.CORPORATE) && StringUtils.isEmpty(dto.getUnifiedSocialCode())) {
            return R.fail("企业法人必须填写统一社会信用代码");
        }
        if (StringUtils.isNotEmpty(dto.getUnifiedSocialCode())) {
            Long count = sysTenantMapper.selectCount(
                    SysTenant::getUnifiedSocialCode, dto.getUnifiedSocialCode());
            if (count > 0) {
                return R.fail("该统一社会信用代码已被使用：" + dto.getUnifiedSocialCode());
            }
        }

        // ==================== 第二部分：生成关键标识 ====================

        // 运营商(租户)账号
        String username = UserGenerator.genAdminAccount(
                un -> sysUserMapper.selectCount(SysUser::getUserName, un) > 0
        );
        // 运营商用户昵称
        String nickname = UserGenerator.genNickname(dto.getPhone());
        // 生成随机密码（12位）
        String password = SecurityUtils.randomPassword(12);
        // TODO 由于目前未接入短信服务，所以密码直接日志打印
        log.info("=============手机号位{}的运营商密码: {}", dto.getPhone(), password);

        // 生成运营商编号（对外展示）
        String merchantNo = UserGenerator.genMerchantNo(
                mn -> sysTenantMapper.selectCount(SysTenant::getMerchantNo, mn) > 0
        );

        // ==================== 第三部分：插入 sys_tenant ====================
        SysTenant merchant = new SysTenant();
        merchant.setMerchantNo(merchantNo)
                .setMerchantType(dto.getMerchantType())
                .setMerchantName(dto.getMerchantName())
                .setUnifiedSocialCode(dto.getUnifiedSocialCode())
                .setSubjectType(dto.getSubjectType())
                .setLegalPersonName(dto.getLegalPersonName())
                .setRegisterAddress(dto.getRegisterAddress())
                .setBusinessLicenseImg(dto.getBusinessLicenseUrl())
                .setTenantStartTime(dto.getTenantStartTime())
                .setTenantEndTime(dto.getTenantEndTime())
                .setProvinceCode(dto.getProvinceCode())
                .setCityCode(dto.getCityCode())
                .setProvinceName(dto.getProvinceName())
                .setCityName(dto.getCityName())
                .setStatus(UserStatusEnum.NORMAL)
                .setCreateBy(loginUser.getRealName())
                .setUpdateBy(loginUser.getRealName())
                .setAgreementFileUrl(dto.getAgreementFileUrl())
                .setRemark(dto.getRemark());

        // 插入数据库
        int insert = sysTenantMapper.insert(merchant);
        if (insert <= 0) {
            return R.fail("注册运营商失败");
        }

        // ==================== 第四部分：插入 sys_user ====================
        SysUser user = new SysUser();
        user.setUserName(username)
                .setNickName(nickname)
                .setPhone(dto.getPhone())
                .setRealName(dto.getRealName())
                .setMerchantId(merchant.getId())
                .setUserType(UserTypeEnum.MCHT_OP)
                .setPassword(SecurityUtils.encryptPassword(password))
                .setEmail(dto.getEmail())
                .setSex(dto.getSex())
                .setStatus(UserStatusEnum.NORMAL)
                .setUserSource(UserSourceEnum.PLATFORM)
                .setIsPrimary(1)
                .setIsSubAccount(0)
                .setCreateById(loginUser.getUserId())
                .setCreateBy(loginUser.getRealName())
                .setUpdateBy(loginUser.getRealName())
                .setRemark(dto.getRemark());

        // 插入数据库
        int insertUser = sysUserMapper.insert(user);
        if (insertUser <= 0) {
            return R.fail("注册运营商用户失败");
        }

        // ==================== 第五部分：发送凭证通知 ====================

        sendCredentials(dto.getPhone(), dto.getEmail(), username, password);

        return R.ok("运营商注册成功", username);
    }

    /**
     * 运营商注册子账户接口（员工账号）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> registerSubAccount(RegisterMerchantSubAccountDTO dto) {

      return R.ok("子账户注册成功");
    }





    /**
     * 发送账号密码通知（短信优先，失败降级到邮件）
     */
    private void sendCredentials(String phone, String email,
                                 String account, String password) {
        String message = "您的运营商管理账号已开通！账号：" + account + "，初始密码：" + password
                + "，请登录后及时修改密码。";

        // 短信优先
        String configValue = Convert.toStr(
                redisService.getCacheObject(CacheConstants.SYS_CONFIG_KEY + "sys.message.type"));
        if (!"false".equals(configValue)) {
            try {
                smsUtil.send(phone, message);
                log.info("运营商账号密码短信发送成功 -> {}", phone);
                return;
            } catch (Exception e) {
                log.warn("短信发送失败，降级使用邮件通知: {}", e.getMessage());
            }
        }

        // 邮件降级
        if (StringUtils.isNotEmpty(email)) {
            try {
                emailUtil.sendSimpleMail("运营商管理账号开通通知", message, email);
                log.info("运营商账号密码邮件发送成功 -> {}", email);
            } catch (Exception e) {
                log.error("邮件发送也失败: {}", e.getMessage());
            }
        }
    }


    /**
     * 查询租户管理
     *
     * @param id 租户管理主键
     * @return 租户管理
     */
    @Override
    public SysTenant selectSysTenantById(Long id)
    {
        return sysTenantMapper.selectById(id);
    }

    /**
     * 查询租户管理列表-分页
     *
     * @param sysTenant 租户管理
     * @return 租户管理
     */
    @Override
    public IPage<SysTenant> selectSysTenantPage(SysTenant sysTenant)
    {
        Page mpPage =new Page(Convert.toLong(ServletUtils.getParameterToInt(MybatisPageConstants.PAGE_NUM),1L)
                ,Convert.toLong(ServletUtils.getParameterToInt(MybatisPageConstants.PAGE_SIZE),10L));
        return sysTenantMapper.selectSysTenantList(mpPage,sysTenant);
    }
    /**
     * 查询租户管理列表
     *
     * @param sysTenant 租户管理
     * @return 租户管理
     */
    @Override
    public List<SysTenant> selectSysTenantList(SysTenant sysTenant) {return sysTenantMapper.selectSysTenantList(sysTenant);}

    /**
     * 新增租户管理
     *
     * @param sysTenant 租户管理
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult insertSysTenant(SysTenant sysTenant)
    {
        /*AjaxResult res = new AjaxResult();
        //新增租户开始
        if (StringUtils.isEmpty(sysTenant.getId())){
            return res.error("租户编码为空,请重新设置!");
        }
        //先判断租户编码是否存在
        if ("9999".equals(sysTenant.getId())){
            //优化租户列表里不存在9999的超级管理员租户导致查出结果为0跳过校验
            return res.error("租户编码已存在,请重新设置!");
        }
        Long tenantcount = sysTenantMapper.selectCount("id",sysTenant.getId());
        if (tenantcount > 0)
        {
            return res.error("租户编码已存在,请重新设置!");
        }
        if (StringUtils.isEmpty(sysTenant.getUserName())){
            return res.error("管理员账号为空,请重新设置!");
        }
        //先判断租户管理员设置的账号是否存在
        SysUser usercount = userMapper.checkUserNameUnique(sysTenant.getUserName());
        if (!(usercount == null))
        {
            return res.error("用户名已存在,请重新设置!");
        }
        //创建租户
        sysTenantMapper.insert(sysTenant);
        //租户创建完成后 开始创建相关基础数据
        TenantUtils.execute(sysTenant.getId(), () -> {
            //创建默认部门--部门默认名称以租户名称
            Long deptid = createDept(sysTenant);
            //创建默认岗位--岗位默认为董事长
            Long postid = createPost(sysTenant.getUserName());
            //创建默认角色--角色默认为租户名称+管理员
            Long roleid = createRole(sysTenant);
            //创建默认账号
            createUser(sysTenant,deptid,postid,roleid);
        });
        return res.success("租户创建成功!");*/
        return AjaxResult.success("租户创建成功!");
    }

    private void createUser(SysTenant sysTenant,Long deptId,Long postid,Long roleid) {
       /* SysUser user = new SysUser();
        user.setDeptId(deptId).setUserName(sysTenant.getUserName()).setNickName(sysTenant.getTenantName())
                .setUserType("00")//用户类型 00 表示各管理员账号，不允许租户修改删除 其他账号为10
                .setEmail(sysTenant.getUserEmail()).setPhonenumber(sysTenant.getUserPhone()).setRemark("租户管理员");
        //默认密码采用随机生成
        String randomPassword = SecurityUtils.randomPassword(8);//随机密码8位

        String password = SecurityUtils.encryptPassword(randomPassword);
        user.setPassword(password);
        userMapper.insert(user);
        userPostMapper.insert(new SysUserPost().setUserId(user.getUserId()).setPostId(postid));
        userRoleMapper.insert(new SysUserRole().setRoleId(roleid).setUserId(user.getUserId()));
        String configValue = Convert.toStr(redisService.getCacheObject( CacheConstants.SYS_CONFIG_KEY + "sys.message.type"));
        if ("false".equals(configValue)){
           //短信
            emailUtil.sendSimpleMail("租户管理员账号注册成功","请牢记登录密码:"+randomPassword,sysTenant.getUserEmail());
        }else {
            try {
                smsUtil.send(sysTenant.getUserPhone(),randomPassword);
            }catch (Exception e){
                log.info("短信调用失败:"+e.getMessage());
                //调用邮件通知
                emailUtil.sendSimpleMail("租户管理员账号注册成功","请牢记登录密码:"+randomPassword,sysTenant.getUserEmail());
            }
        }*/
    }

    private Long createRole(SysTenant sysTenant) {
        /*// 创建角色
        SysRole role = new SysRole();
        role.setRoleName(sysTenant.getTenantName()+"管理员").setRoleKey("admin")
                .setRoleSort("1").setDataScope("1").setMenuCheckStrictly(true).setDeptCheckStrictly(true);
        role.setCreateBy(sysTenant.getUserName());
        role.setRemark("租户管理员");
        role.setAdminRole(true);
        sysRoleMapper.insert(role);
        //根据租户套餐ids查出套餐编码塞入角色-菜单表
        createRoleMenu(sysTenant,role);
        return role.getRoleId();*/
        return null;
    }

    //目前为单套餐,跟租户绑定,解耦防止套餐变动影响多个租户
    private void createRoleMenu(SysTenant sysTenant,SysRole role)
    {
        SysTenantPackage sysTenantPackage = sysTenantPackageMapper.selectById(sysTenant.getTenantPackage());
        List<String> subMeuns = Arrays.asList(sysTenantPackage.getMenuIds().split(","));

        List<SysRoleMenu> roleMenuList = subMeuns.stream().map(menuid -> {
            SysRoleMenu entity = new SysRoleMenu();
            entity.setRoleId(role.getRoleId());
            entity.setMenuId(Convert.toLong(menuid));
            return entity;
        }).collect(Collectors.toList());
        sysRoleMenuMapper.batchRoleMenu(roleMenuList);
    }

    private Long createPost(String username) {
        SysPost post = new SysPost();
        post.setPostCode("ceo").setPostName("董事长").setPostSort("1");
        post.setCreateBy(username);
        sysPostMapper.insert(post);
        return post.getPostId();
    }

    private Long createDept(SysTenant sysTenant) {
      /*  // 创建部门
        SysDept dept = new SysDept();
        dept.setParentId(0L).setAncestors("0").setDeptName(sysTenant.getTenantName()).setOrderNum(0)
                .setLeader(sysTenant.getTenantName()+"管理员").setPhone(sysTenant.getUserPhone()).setEmail(sysTenant.getUserEmail());
        deptMapper.insert(dept);
        return dept.getDeptId();*/
        return null;
    }


    /**
     * 修改租户管理
     *
     * @param sysTenant 租户管理
     * @return 结果
     */
    @Override
    public int updateSysTenant(SysTenant sysTenant)
    {
        //判断最新的租户套餐是否改变 重新授权 租户二级管理员账号需重新分配三级账号权限
        SysTenant t_sysTenant = sysTenantMapper.selectById(sysTenant.getId());
        if(sysTenant.getTenantPackage() != null && !sysTenant.getTenantPackage().equals(t_sysTenant.getTenantPackage()))
        {
            List<SysRole> roleList = sysRoleMapper.queryAdminRole(sysTenant.getId());
            SysRole t_role = roleList.get(0);//正常逻辑下每个租户只有一个二级管理员账号
            if(t_role != null)
            {
                //删除原租户下所有的角色-菜单信息
                sysRoleMenuMapper.deleteRoleMenuByMerchantId(sysTenant.getId());
                //新增默认角色-菜单信息
                TenantUtils.execute(sysTenant.getId(), () -> {
                    createRoleMenu(sysTenant,t_role);
                });
                // TODO  此处需要优化,如果当前同时在线人数较多,会出现卡顿
                //原登租户录账号退出重登 租户二级管理员账号需重新分配三级账号权限
                Collection<String> keys = redisService.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
                for (String key : keys)
                {
                    LoginUser onlineUser = redisService.getCacheObject(key);
                    if(onlineUser.getSysUser().getMerchantId() != null && onlineUser.getSysUser().getMerchantId().equals(sysTenant.getId()))
                    {
                        redisService.deleteObject(key);
                    }
                }
            }
        }
        return sysTenantMapper.updateById(sysTenant);
    }

    /**
     * 批量删除租户管理
     *
     * @param ids 需要删除的租户管理主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSysTenantByIds(Long[] ids)
    {
        //优化删除逻辑
        //1.先删租户
        int tenantres = sysTenantMapper.deleteSysTenantByIds(ids);
        if(tenantres>0){
            //下面才会进行子模块数据的删除
            //部门模块
            deptMapper.deleteDeptByTenantId(ids);
            //职位模块
            sysPostMapper.deletePostByTenantId(ids);
            //权限
            sysRoleMapper.deleteRoleByMerchantId(ids);
            sysRoleMenuMapper.deleteRoleMenuByMerchantIds(ids);
            sysRoleDeptMapper.deleteRoleDeptByTenantId(ids);
            //账号
            sysUserMapper.deleteUserByMerchantId(ids);
            userRoleMapper.deleteUserRoleByMerchantId(ids);
            userPostMapper.deleteUserPostByMerchantId(ids);
            return 1;
        }else {
            throw new ServiceException("当前租户已被删除不存在！");
        }
    }

    /**
     * 删除租户管理信息
     *
     * @param id 租户管理主键
     * @return 结果
     */
    @Override
    public int deleteSysTenantById(Long id)
    {
        return sysTenantMapper.deleteById(id);
    }
}
