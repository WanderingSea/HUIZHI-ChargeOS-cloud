package com.hcp.auth.service;

import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.hcp.common.core.constant.CacheConstants;
import com.hcp.common.core.constant.Constants;
import com.hcp.common.core.exception.ServiceException;
import com.hcp.common.redis.service.RedisService;
import com.hcp.common.security.utils.SecurityUtils;
import com.hcp.system.api.domain.SysUser;

/**
 * 登录密码方法
 *
 * @author JHan
 */
@Component
@RequiredArgsConstructor
public class SysPasswordService
{
    private final RedisService redisService;

    private int maxRetryCount = CacheConstants.PASSWORD_MAX_RETRY_COUNT;

    private Long lockTime = CacheConstants.PASSWORD_LOCK_TIME;

    private final SysRecordLogService recordLogService;

    /**
     * 登录账户密码错误次数缓存键名
     *
     * @param phone 登录手机号
     * @return 缓存键key
     */
    private String getCacheKey(String phone)
    {
        return CacheConstants.PWD_ERR_CNT_KEY + phone;
    }

    public void validate(SysUser user, String password)
    {
        String phone = user.getPhone();

        Integer retryCount = redisService.getCacheObject(getCacheKey(phone));

        if (retryCount == null)
        {
            retryCount = 0;
        }

        if (retryCount >= Integer.valueOf(maxRetryCount).intValue())
        {
            String errMsg = String.format("密码输入错误%s次，帐户锁定%s分钟", maxRetryCount, lockTime);
            recordLogService.recordLoginInfo(phone, Constants.LOGIN_FAIL,errMsg);
            throw new ServiceException(errMsg);
        }

        if (!matches(user, password))
        {
            retryCount = retryCount + 1;
            recordLogService.recordLoginInfo(phone, Constants.LOGIN_FAIL, String.format("密码输入错误%s次", retryCount));
            redisService.setCacheObject(getCacheKey(phone), retryCount, lockTime, TimeUnit.MINUTES);
            throw new ServiceException("用户不存在/密码错误");
        }
        else
        {
            clearLoginRecordCache(phone);
        }
    }

    public boolean matches(SysUser user, String rawPassword)
    {
        return SecurityUtils.matchesPassword(rawPassword, user.getPassword());
    }

    public void clearLoginRecordCache(String phone)
    {
        if (redisService.hasKey(getCacheKey(phone)))
        {
            redisService.deleteObject(getCacheKey(phone));
        }
    }
}
