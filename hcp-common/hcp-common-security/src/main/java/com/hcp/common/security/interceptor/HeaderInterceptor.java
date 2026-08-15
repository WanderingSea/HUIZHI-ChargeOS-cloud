package com.hcp.common.security.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hcp.common.core.enums.UserTypeEnum;
import com.hcp.common.security.auth.AuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import com.hcp.common.core.constant.SecurityConstants;
import com.hcp.common.core.context.SecurityContextHolder;
import com.hcp.common.core.utils.ServletUtils;
import com.hcp.common.core.utils.StringUtils;
import com.hcp.common.security.utils.SecurityUtils;
import com.hcp.system.api.model.LoginUser;

/**
 * 自定义请求头拦截器，将Header数据封装到线程变量中方便获取 <br>
 * 注意：此拦截器会同时验证当前用户token有效期，若过期则会自动刷新token
 *
 * @author JHan
 */
public class HeaderInterceptor implements AsyncHandlerInterceptor
{
    private final Logger logger = LoggerFactory.getLogger(HeaderInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        if (!(handler instanceof HandlerMethod))
        {
            return true;
        }
        logger.info("进到这里来了......{}",request.getRequestURI());
        SecurityContextHolder.setMerchantId(ServletUtils.getHeader(request, SecurityConstants.DETAILS_MERCHANT_ID));
        SecurityContextHolder.setDeptId(ServletUtils.getHeader(request, SecurityConstants.DETAILS_DEPT_ID));
        SecurityContextHolder.setUserId(ServletUtils.getHeader(request, SecurityConstants.DETAILS_USER_ID));
        SecurityContextHolder.setUserName(ServletUtils.getHeader(request, SecurityConstants.DETAILS_USERNAME));
        SecurityContextHolder.setPhone(ServletUtils.getHeader(request, SecurityConstants.PHONE));
        SecurityContextHolder.setUserKey(ServletUtils.getHeader(request, SecurityConstants.USER_KEY));

        // 真实姓名（可能为空）
        String realName = ServletUtils.getHeader(request, SecurityConstants.REAL_NAME);
        if (StringUtils.isNotEmpty(realName)) {
            SecurityContextHolder.setRealName(realName);
        }

        // 用户类型（可能为空，如 Swagger 文档请求）
        String userTypeStr = ServletUtils.getHeader(request, SecurityConstants.USER_TYPE);
        if (StringUtils.isNotEmpty(userTypeStr)) {
            SecurityContextHolder.setUserType(UserTypeEnum.valueOf(userTypeStr));
        }

        String token = SecurityUtils.getToken();
        if (StringUtils.isNotEmpty(token))
        {
            LoginUser loginUser = AuthUtil.getLoginUser(token);
            if (StringUtils.isNotNull(loginUser))
            {
                // 校验并自动刷新 Token 有效期
                AuthUtil.verifyLoginUserExpire(loginUser);
                // 将完整的 LoginUser 对象存入线程变量
                SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception
    {
        // 请求完成后清除线程变量，防止内存泄漏
        SecurityContextHolder.remove();
    }
}
