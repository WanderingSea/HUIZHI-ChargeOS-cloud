package com.hcp.common.security.utils;

import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;

import com.hcp.common.core.enums.UserTypeEnum;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.hcp.common.core.constant.SecurityConstants;
import com.hcp.common.core.constant.TokenConstants;
import com.hcp.common.core.context.SecurityContextHolder;
import com.hcp.common.core.utils.ServletUtils;
import com.hcp.common.core.utils.StringUtils;
import com.hcp.system.api.model.LoginUser;

import java.util.Objects;

/**
 * 权限获取工具类
 *
 * @author JHan
 */
public class SecurityUtils
{

    /**
     * 获取运营商(租户)ID
     */
    public static Long getMerchantId()
    {
        return SecurityContextHolder.getMerchantId();
    }

    /**
     * 设置运营商(租户)ID
     */
    public static void setMerchantId(String merchantId)
    {
         SecurityContextHolder.setMerchantId(merchantId);
    }

    /**
     * 获取该后台用户部门ID
     */
    public static Long getDeptId()
    {
        return SecurityContextHolder.getDeptId();
    }

    /**
     * 获取用户手机号
     */
    public static String getPhone()
    {
        return SecurityContextHolder.getPhone();
    }

    /**
     * 获取用户账号
     */
    public static String getUsername()
    {
        return SecurityContextHolder.getUserName();
    }

    /**
     获取用户真实姓名
     */
    public static String getRealName()
    {
        return SecurityContextHolder.getRealName();
    }

    /**
     * 获取用户身份类型
     */
    public static UserTypeEnum getUserType(){
        return SecurityContextHolder.getUserType();
    }

    /**
     * 获取用户ID
     */
    public static Long getUserId()
    {
        return SecurityContextHolder.getUserId();
    }

    /**
     * 获取用户key
     */
    public static String getUserKey()
    {
        return SecurityContextHolder.getUserKey();
    }

    /**
     * 获取登录用户信息
     */
    public static LoginUser getLoginUser()
    {
        return SecurityContextHolder.get(SecurityConstants.LOGIN_USER, LoginUser.class);
    }

    /**
     * 获取请求token
     */
    public static String getToken()
    {
        return getToken(ServletUtils.getRequest());
    }

    /**
     * 根据request获取请求token
     */
    public static String getToken(HttpServletRequest request)
    {
        // 从header获取token标识
        String token = request.getHeader(TokenConstants.AUTHENTICATION);
        return replaceTokenPrefix(token);
    }

    /**
     * 裁剪token前缀
     */
    public static String replaceTokenPrefix(String token)
    {
        // 如果前端设置了令牌前缀，则裁剪掉前缀
        if (StringUtils.isNotEmpty(token) && token.startsWith(TokenConstants.PREFIX))
        {
            token = token.replaceFirst(TokenConstants.PREFIX, "");
        }
        return token;
    }

    /**
     * 是否为平台管理员(包括SYS_SUPER和PLAT_ADMIN)
     *
     * @param userId 用户ID
     * @return 结果
     */
    public static boolean isPlatformAdmin(Long userId)
    {
        return userId != null && (Objects.equals(1L, userId) || Objects.equals(2L, userId));
    }

    /**
     * 生成BCryptPasswordEncoder密码
     *
     * @param password 密码
     * @return 加密字符串
     */
    public static String encryptPassword(String password)
    {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 随机生成n位字符串
     *
     * @param n 密码
     * @return 字符串
     */
    public static String randomStr(int n)
    {
        StringBuffer bufferStr = new StringBuffer();
        for(int i=0;i<n;i++)
        {
            bufferStr.append("0123456789ABCDEFGHIZKLMNOPQRSTUVWXYZ".charAt((int)(Math.random() * 36)));
        }
        return bufferStr.toString();
    }

    /** 密码生成使用的安全随机数生成器 */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    /** 密码字符集：大小写字母 + 数字 */
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * 后台管理级用户注册后 密码生成
     * 规则：包含大小写字母和数字，使用 SecureRandom 保证安全性
     *
     * @param length 密码长度（建议至少8位）
     * @return 随机密码字符串
     */
    public static String randomPassword(int length)
    {
        if (length < 6) {
            length = 8; // 最小长度保护
        }

        StringBuilder sb = new StringBuilder(length);

        // 保证至少包含一个大写字母、一个小写字母、一个数字
        sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(SECURE_RANDOM.nextInt(26)));
        sb.append("abcdefghijklmnopqrstuvwxyz".charAt(SECURE_RANDOM.nextInt(26)));
        sb.append("0123456789".charAt(SECURE_RANDOM.nextInt(10)));

        // 剩余位数随机填充
        for (int i = 3; i < length; i++) {
            sb.append(PASSWORD_CHARS.charAt(SECURE_RANDOM.nextInt(PASSWORD_CHARS.length())));
        }

        // 打乱顺序，避免前3位固定模式
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = SECURE_RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }

    /**
     * 判断密码是否相同
     *
     * @param rawPassword 真实密码
     * @param encodedPassword 加密后字符
     * @return 结果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword)
    {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
