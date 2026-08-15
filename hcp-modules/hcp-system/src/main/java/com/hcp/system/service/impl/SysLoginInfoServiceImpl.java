package com.hcp.system.service.impl;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hcp.common.core.constant.CacheConstants;
import com.hcp.common.core.text.Convert;
import com.hcp.common.core.utils.ServletUtils;
import com.hcp.common.core.web.domain.AjaxResult;
import com.hcp.common.mybatisplus.constant.MybatisPageConstants;
import com.hcp.common.redis.service.RedisService;
import com.hcp.system.domain.SysLoginCount;
import com.hcp.system.mapper.SysLoginInfoMapper;
import com.hcp.system.service.ISysLoginInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hcp.system.api.domain.SysLoginInfo;

/**
 * 系统访问日志情况信息 服务层处理
 *
 * @author JHan
 */
@Service
@RequiredArgsConstructor
public class SysLoginInfoServiceImpl implements ISysLoginInfoService
{
    private final SysLoginInfoMapper loginInfoMapper;
    private final RedisService redisService;

    /**
     * 新增系统登录日志
     *
     * @param loginInfo 访问日志对象
     */
    @Override
    public int insertLoginInfo(SysLoginInfo loginInfo)
    {
        return loginInfoMapper.insertLoginInfo(loginInfo);
    }

    /**
     * 查询系统登录日志集合
     *
     * @param loginInfo 访问日志对象
     * @return 登录记录集合
     */
    @Override
    public IPage<SysLoginInfo> selectLoginInfoPage(SysLoginInfo loginInfo)
    {
        Page mpPage =new Page(Convert.toLong(ServletUtils.getParameterToInt(MybatisPageConstants.PAGE_NUM),1L)
                ,Convert.toLong(ServletUtils.getParameterToInt(MybatisPageConstants.PAGE_SIZE),10L));
        return loginInfoMapper.selectLoginInfoList(mpPage,loginInfo);
    }

    /**
     * 查询系统登录日志集合
     *
     * @param loginInfo 访问日志对象
     * @return 登录记录集合
     */
    @Override
    public List<SysLoginInfo> selectLoginInfoList(SysLoginInfo loginInfo)
    {
        return loginInfoMapper.selectLoginInfoList(loginInfo);
    }

    /**
     * 批量删除系统登录日志
     *
     * @param infoIds 需要删除的登录日志ID
     * @return 结果
     */
    @Override
    public int deleteLoginInfoByIds(Long[] infoIds)
    {
        return loginInfoMapper.deleteLoginInfoByIds(infoIds);
    }

    /**
     * 清空系统登录日志
     */
    @Override
    public void cleanLoginInfo()
    {
        loginInfoMapper.cleanLoginInfo();
    }

    /**
     * 查询统计信息
     * @return
     */
    @Override
    public AjaxResult loginCount() {
        SysLoginCount loginCountInfo = new SysLoginCount();
        //step1 在线人数
        Collection<String> online = redisService.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        loginCountInfo.setOnlineCount(String.valueOf(online.size()));
        //step2 累积登陆人数 超级管理员统计所有
        loginCountInfo.setLoginCount(String.valueOf(loginInfoMapper.countLoginInfo()));
        //step 累积租户数
        loginCountInfo.setTenantCount(String.valueOf(loginInfoMapper.countTenant()));
        //step4 累积注册人数
        loginCountInfo.setRegisterCount(String.valueOf(loginInfoMapper.countRegister()));
        return AjaxResult.success(loginCountInfo);
    }
}
