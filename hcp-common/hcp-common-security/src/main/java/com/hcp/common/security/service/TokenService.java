package com.hcp.common.security.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson2.JSONObject;
import com.hcp.common.core.utils.ip.AddressUtils;
import eu.bitwalker.useragentutils.UserAgent;
import com.hcp.common.security.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.hcp.common.core.constant.CacheConstants;
import com.hcp.common.core.constant.SecurityConstants;
import com.hcp.common.core.utils.JwtUtils;
import com.hcp.common.core.utils.ServletUtils;
import com.hcp.common.core.utils.StringUtils;
import com.hcp.common.core.utils.ip.IpUtils;
import com.hcp.common.core.utils.uuid.IdUtils;
import com.hcp.common.redis.service.RedisService;
import com.hcp.system.api.model.LoginUser;


/**
 * token验证处理
 *
 * @author JHan
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenService {

    private final RedisService redisService;

    protected static final long MILLIS_SECOND = 1000;

    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    private final static long expireTime = CacheConstants.EXPIRATION;

    private final static String ACCESS_TOKEN = CacheConstants.LOGIN_TOKEN_KEY;

    private final static Long MILLIS_MINUTE_TEN = CacheConstants.REFRESH_TIME * MILLIS_MINUTE;

    /**
     * 创建token令牌
     */
    public Map<String, Object> createToken(LoginUser loginUser) {
        String token = IdUtils.fastUUID();
        // 操作者id
        Long userId = loginUser.getSysUser().getUserId();
        // 操作者手机号
        String phone = loginUser.getSysUser().getPhone();
        // 操作者账号
        String userName = loginUser.getSysUser().getUserName();
        // 操作者所属运营商id
        Long merchantId = loginUser.getSysUser().getMerchantId();
        // 操作者所属部门id
        Long deptId = loginUser.getSysUser().getDeptId();
        // 操作者身份
        Integer userType = loginUser.getUserType();

        loginUser.setToken(token)
                 .setUserId(userId)
                 .setUsername(userName)
                 .setPhone(phone)
                 .setIpaddr(IpUtils.getIpAddr());

        //添加地址信息
        setUserAgent(loginUser);
        refreshToken(loginUser);

        // Jwt存储信息
        Map<String, Object> claimsMap = new HashMap<String, Object>();
        claimsMap.put(SecurityConstants.USER_KEY, token);
        claimsMap.put(SecurityConstants.DETAILS_USER_ID, userId);
        claimsMap.put(SecurityConstants.PHONE, phone);
        //租户id
        claimsMap.put(SecurityConstants.DETAILS_MERCHANT_ID, merchantId);
        //部门id
        claimsMap.put(SecurityConstants.DETAILS_DEPT_ID, deptId);
        // 接口返回信息
        Map<String, Object> rspMap = new HashMap<String, Object>();
        rspMap.put("token", JwtUtils.createToken(claimsMap));
        rspMap.put("expiresIn", expireTime);
        rspMap.put("merchantId", merchantId);
        rspMap.put("account", userName);
        rspMap.put("securityCode", phone);
        rspMap.put("userAttribute", userType);

        return rspMap;
    }

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser()
    {
        return getLoginUser(ServletUtils.getRequest());
    }

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser(HttpServletRequest request)
    {
        // 获取请求携带的令牌
        String token = SecurityUtils.getToken(request);
        return getLoginUser(token);
    }

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser(String token)
    {
        LoginUser user = null;
        try
        {
            if (StringUtils.isNotEmpty(token))
            {
                String userKey = JwtUtils.getUserKey(token);
                user = redisService.getCacheObject(getTokenKey(userKey));
                return user;
            }
        }
        catch (Exception e)
        {
            log.error("获取用户信息异常'{}'", e.getMessage());
        }
        return user;
    }

    /**
     * 设置用户身份信息
     */
    public void setLoginUser(LoginUser loginUser)
    {
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken()))
        {
            refreshToken(loginUser);
        }
    }

    /**
     * 删除用户缓存信息
     */
    public void delLoginUser(String token)
    {
        if (StringUtils.isNotEmpty(token))
        {
            String userKey = JwtUtils.getUserKey(token);
            redisService.deleteObject(getTokenKey(userKey));
        }
    }

    /**
     * 验证令牌有效期，相差不足120分钟，自动刷新缓存
     *
     * @param loginUser
     */
    public void verifyToken(LoginUser loginUser)
    {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TEN)
        {
            refreshToken(loginUser);
        }
    }

    /**
     * 刷新令牌有效期
     *
     * @param loginUser 登录信息
     */
    public void refreshToken(LoginUser loginUser)
    {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
        // 根据uuid将loginUser缓存
        String userKey = getTokenKey(loginUser.getToken());
        redisService.setCacheObject(userKey, loginUser, expireTime, TimeUnit.MINUTES);
    }

    private String getTokenKey(String token)
    {
        return ACCESS_TOKEN + token;
    }

    /**
     * 设置用户代理信息
     *
     * @param loginUser 登录信息
     */
    public void setUserAgent(LoginUser loginUser)
    {
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) return;  // 非 HTTP 场景跳过

        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        String ip = IpUtils.getIpAddr(request);
        loginUser.setIpaddr(ip);
        loginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        loginUser.setBrowser(userAgent.getBrowser().getName());
        loginUser.setOs(userAgent.getOperatingSystem().getName());
    }
}
