package com.hcp.common.swagger.annotation;

import com.hcp.common.swagger.config.SwaggerAutoConfiguration;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnableKnife4j
@Import({SwaggerAutoConfiguration.class})
public @interface EnableCustomSwagger2 {
}
