package com.hcp.common.swagger.config;

import cn.hutool.core.collection.CollUtil;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Collections;

@Configuration
@EnableConfigurationProperties(SwaggerProperties.class)
@ConditionalOnProperty(name = "swagger.enabled", matchIfMissing = true)
public class SwaggerAutoConfiguration {

    /**
     * 模块接口分组 —— 按当前模块的 swagger 配置生成唯一 GroupedOpenApi
     */
    @Bean
    public GroupedOpenApi groupedOpenApi(SwaggerProperties swaggerProperties) {
        String group = swaggerProperties.getGroup();
        String basePackage = swaggerProperties.getBasePackage();

        GroupedOpenApi.Builder builder = GroupedOpenApi.builder()
                .group(group)
                .displayName(swaggerProperties.getTitle());

        if (CollUtil.isNotEmpty(swaggerProperties.getBasePath())) {
            builder.pathsToMatch(swaggerProperties.getBasePath().toArray(new String[0]));
        }
        if (CollUtil.isNotEmpty(swaggerProperties.getExcludePath())) {
            builder.pathsToExclude(swaggerProperties.getExcludePath().toArray(new String[0]));
        }
        // 仅当配置了 base-package 时才限制扫描范围
        if (basePackage != null && !basePackage.isEmpty()) {
            builder.packagesToScan(basePackage);
        }
        // 兜底：包和路径都没配时，默认匹配全部路径
        if (basePackage == null || basePackage.isEmpty()) {
            if (CollUtil.isEmpty(swaggerProperties.getBasePath())) {
                builder.pathsToMatch("/**");
            }
        }

        return builder.build();
    }

    /**
     * OpenAPI 全局信息配置
     */
    @Bean
    public OpenAPI openAPI(SwaggerProperties swaggerProperties) {
        Contact contact = new Contact()
                .name(swaggerProperties.getContact().getName())
                .url(swaggerProperties.getContact().getUrl())
                .email(swaggerProperties.getContact().getEmail());

        Info info = new Info()
                .title(swaggerProperties.getTitle())
                .description(swaggerProperties.getDescription())
                .version(swaggerProperties.getVersion())
                .contact(contact);

        if (swaggerProperties.getLicense() != null && !swaggerProperties.getLicense().isEmpty()) {
            info.license(new License().name(swaggerProperties.getLicense())
                    .url(swaggerProperties.getLicenseUrl()));
        }
        if (swaggerProperties.getTermsOfServiceUrl() != null
                && !swaggerProperties.getTermsOfServiceUrl().isEmpty()) {
            info.termsOfService(swaggerProperties.getTermsOfServiceUrl());
        }

        OpenAPI openAPI = new OpenAPI().info(info);

        // 全局 Authorization 请求头安全配置
        SwaggerProperties.Authorization authorization = swaggerProperties.getAuthorization();
        if (authorization != null && authorization.getName() != null
                && !authorization.getName().isEmpty()) {
            SecurityScheme securityScheme = new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name(authorization.getName());
            openAPI.components(new Components()
                    .addSecuritySchemes(authorization.getName(), securityScheme));
            openAPI.security(Collections.singletonList(
                    new SecurityRequirement().addList(authorization.getName())));
        }

        return openAPI;
    }
}
