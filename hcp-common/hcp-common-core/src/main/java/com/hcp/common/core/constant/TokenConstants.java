package com.hcp.common.core.constant;

/**
 * Token的Key常量
 *
 * @author JHan
 */
public class TokenConstants
{
    /**
     * 令牌自定义标识
     */
    public static final String AUTHENTICATION = "Authorization";

    /**
     * 令牌前缀
     */
    public static final String PREFIX = "Bearer ";

    /**
     * 令牌秘钥
     */
    public final static String SECRET = "abcdefghijklmnopqrstuvwxyz";

    /**
     * JWT 令牌过期时间（毫秒），默认 24 小时
     * 与 Redis TTL 配合实现双重过期保障
     */
    public static final long JWT_EXPIRE_TIME = 24 * 60 * 60 * 1000L;  // 86,400,000 ms

}
