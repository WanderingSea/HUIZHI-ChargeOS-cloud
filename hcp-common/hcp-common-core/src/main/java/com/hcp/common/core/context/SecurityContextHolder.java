package com.hcp.common.core.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.hcp.common.core.constant.SecurityConstants;
import com.hcp.common.core.enums.UserTypeEnum;
import com.hcp.common.core.text.Convert;
import com.hcp.common.core.utils.StringUtils;

/**
 * 获取当前线程变量中的 用户信息主键id、租户(运营商)id、部门id、用户手机号、用户账号、用户真实姓名、用户身份类型、Token等信息
 * 注意： 必须在网关通过请求头的方法传入，同时在HeaderInterceptor拦截器设置值。 否则这里无法获取
 *
 * @author JHan
 */
public class SecurityContextHolder {

    private static final TransmittableThreadLocal<Map<String, Object>> THREAD_LOCAL = new TransmittableThreadLocal<>();

    public static void set(String key, Object value)
    {
        Map<String, Object> map = getLocalMap();
        map.put(key, value == null ? StringUtils.EMPTY : value);
    }

    public static String get(String key)
    {
        Map<String, Object> map = getLocalMap();
        return Convert.toStr(map.getOrDefault(key, StringUtils.EMPTY));
    }


    public static <T> T get(String key, Class<T> clazz)
    {
        Map<String, Object> map = getLocalMap();
        return StringUtils.cast(map.getOrDefault(key, null));
    }

    public static Map<String, Object> getLocalMap()
    {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null)
        {
            map = new ConcurrentHashMap<String, Object>();
            THREAD_LOCAL.set(map);
        }
        return map;
    }

    public static void setLocalMap(Map<String, Object> threadLocalMap)
    {
        THREAD_LOCAL.set(threadLocalMap);
    }

    // 获取后台管理级用户id
    public static Long getUserId()
    {
        return Convert.toLong(get(SecurityConstants.DETAILS_USER_ID), 0L);
    }

    public static void setUserId(String account)
    {
        set(SecurityConstants.DETAILS_USER_ID, account);
    }

    // 获取运营商(租户)ID
    public static Long getMerchantId()
    {
        return Convert.toLong(get(SecurityConstants.DETAILS_MERCHANT_ID), 0L);
    }

    public static void setMerchantId(String merchantId)
    {
        set(SecurityConstants.DETAILS_MERCHANT_ID, merchantId);
    }

    // 获取该后台用户部门ID
    public static Long getDeptId()
    {
        return Convert.toLong(get(SecurityConstants.DETAILS_DEPT_ID), 0L);
    }

    public static void setDeptId(String deptId)
    {
        set(SecurityConstants.DETAILS_DEPT_ID, deptId);
    }

    // 获取用户手机号
    public static String getPhone()
    {
        return get(SecurityConstants.PHONE);
    }

    public static void setPhone(String phone)
    {
        set(SecurityConstants.PHONE, phone);
    }

    // 获取用户账号
    public static String getUserName()
    {
        return get(SecurityConstants.DETAILS_USERNAME);
    }

    public static void setUserName(String username)
    {
        set(SecurityConstants.DETAILS_USERNAME, username);
    }

    // 获取用户真实姓名
    public static String getRealName()
    {
        return get(SecurityConstants.REAL_NAME);
    }

    public static void setRealName(String realName) {
        set(SecurityConstants.REAL_NAME, realName);
    }

    // 获取用户身份类型
    public static UserTypeEnum getUserType()
    {
        String value = get(SecurityConstants.USER_TYPE);
        return StringUtils.isNotEmpty(value) ? UserTypeEnum.valueOf(value) : null;
    }

    public static void setUserType(UserTypeEnum userType)
    {
        set(SecurityConstants.USER_TYPE, userType);
    }

    // 获取用户标识(即token)
    public static String getUserKey()
    {
        return get(SecurityConstants.USER_KEY);
    }

    public static void setUserKey(String userKey)
    {
        set(SecurityConstants.USER_KEY, userKey);
    }

    // 获取用户权限
    public static String getPermission()
    {
        return get(SecurityConstants.ROLE_PERMISSION);
    }

    public static void setPermission(String permissions)
    {
        set(SecurityConstants.ROLE_PERMISSION, permissions);
    }

    // 设置远程头信息
    public static void setRemoteHeader(Object account)
    {
        set(SecurityConstants.REMOTE_HEADER, account);
    }

    // 清除当前线程变量中的用户信息
    public static void remove()
    {
        THREAD_LOCAL.remove();
    }
}
