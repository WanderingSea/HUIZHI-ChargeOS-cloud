package com.hcp.gateway.handler;

import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.*;

/**
 * Knife4j 网关聚合 —— 提供 /v3/api-docs/swagger-config 端点
 * <p>
 * Knife4j 前端 doc.html 会请求此端点获取所有微服务的 OpenAPI 文档地址列表
 * </p>
 */
@RestController
public class Knife4jSwaggerConfigHandler {

    @Resource
    private GatewayProperties gatewayProperties;

    @GetMapping("/v3/api-docs/custom-swagger-config")
    public Mono<Map<String, Object>> swaggerConfig() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("configUrl", "/v3/api-docs/swagger-config");
        result.put("validatorUrl", "");
        result.put("oauth2RedirectUrl", "");

        List<Map<String, String>> urls = new ArrayList<>();
        Set<String> added = new HashSet<>();

        gatewayProperties.getRoutes().forEach(route -> {
            String id = route.getId();
            if ("vctgo-auth".equalsIgnoreCase(id)) {
                return;
            }
            route.getPredicates().stream()
                    .filter(p -> "Path".equalsIgnoreCase(p.getName()))
                    .forEach(p -> {
                        if (added.add(id)) {
                            String location = p.getArgs()
                                    .get(NameUtils.GENERATED_NAME_PREFIX + "0")
                                    .replace("/**", "/v3/api-docs");
                            Map<String, String> item = new LinkedHashMap<>();
                            item.put("url", location);
                            item.put("name", id);
                            urls.add(item);
                        }
                    });
        });

        result.put("urls", urls);
        return Mono.just(result);
    }
}
