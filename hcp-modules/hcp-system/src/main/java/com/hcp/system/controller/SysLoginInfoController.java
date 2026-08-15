package com.hcp.system.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.hcp.common.core.utils.poi.ExcelUtil;
import com.hcp.common.core.web.controller.BaseController;
import com.hcp.common.core.web.domain.AjaxResult;
import com.hcp.common.log.annotation.Log;
import com.hcp.common.log.enums.BusinessType;
import com.hcp.common.security.annotation.InnerAuth;
import com.hcp.common.security.annotation.RequiresPermissions;
import com.hcp.system.api.domain.SysLoginInfo;
import com.hcp.system.service.ISysLoginInfoService;
import com.hcp.common.redis.service.RedisService;
import com.hcp.common.core.constant.CacheConstants;

/**
 * 系统访问记录
 *
 * @author JHan
 */
@RestController
@RequestMapping("/login-info")
@RequiredArgsConstructor
public class SysLoginInfoController extends BaseController
{
    private final ISysLoginInfoService loginInfoService;
    private final RedisService redisService;


    @RequiresPermissions("system:login-info:list")
    @GetMapping("/list")
    public AjaxResult list(SysLoginInfo loginInfo)
    {
        IPage<SysLoginInfo> list = loginInfoService.selectLoginInfoPage(loginInfo);
        return AjaxResult.success(list);
    }

    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    @RequiresPermissions("system:login-info:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysLoginInfo loginInfo)
    {
        List<SysLoginInfo> list = loginInfoService.selectLoginInfoList(loginInfo);
        ExcelUtil<SysLoginInfo> util = new ExcelUtil<SysLoginInfo>(SysLoginInfo.class);
        util.exportExcel(response, list, "登录日志");
    }

    @RequiresPermissions("system:login-info:remove")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public AjaxResult remove(@PathVariable Long[] infoIds)
    {
        return toAjax(loginInfoService.deleteLoginInfoByIds(infoIds));
    }

    @RequiresPermissions("system:login-info:remove")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/clean")
    public AjaxResult clean()
    {
        loginInfoService.cleanLoginInfo();
        return success();
    }

    @RequiresPermissions("system:login-info:unlock")
    @Log(title = "账户解锁", businessType = BusinessType.OTHER)
    @GetMapping("/unlock/{userName}")
    public AjaxResult unlock(@PathVariable("userName") String userName)
    {
        redisService.deleteObject(CacheConstants.PWD_ERR_CNT_KEY + userName);
        return success();
    }


    @InnerAuth
    @PostMapping
    public AjaxResult add(@RequestBody SysLoginInfo loginInfo)
    {
        return toAjax(loginInfoService.insertLoginInfo(loginInfo));
    }

    /**
     * 查询统计信息
     * @return
     */
    @GetMapping("/login-count")
    public AjaxResult loginCount()
    {
        return loginInfoService.loginCount();
    }
}
