package com.hcp.common.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用户身份认证：必须具有指定用户身份类型才能进入该方法
 *
 * @author JHan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface PreAuthorizeUserRole {

    /**
     * 需要校验的用户身份类型
     */
    int[] value() default {};

    /**
     * 验证逻辑：AND | OR，默认OR（满足其中一个即可）
     */
    Logical logical() default Logical.OR;
}
