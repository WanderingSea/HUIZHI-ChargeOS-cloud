package com.hcp.common.security.config;

import com.hcp.common.security.interceptor.HeaderInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置
 *
 * @author JHan
 */
public class WebMvcConfig implements WebMvcConfigurer
{
    // 不需要拦截地址
    public static final String[] excludeUrls = { "/login", "/logout", "/refresh", "/simulator/**" };

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(getHeaderInterceptor())
                .addPathPatterns("/**")    // 拦截所有请求
                .excludePathPatterns(excludeUrls)  // 排除白名单
                .order(-10);
    }

    /**
     * 自定义请求头拦截器
     */
    public HeaderInterceptor getHeaderInterceptor()
    {
        return new HeaderInterceptor();
    }
}
