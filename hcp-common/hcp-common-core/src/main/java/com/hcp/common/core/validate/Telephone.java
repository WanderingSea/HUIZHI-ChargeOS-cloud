package com.hcp.common.core.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Telephone
 * </p>
 * 自定义座机号码校验注解
 * 支持格式：0XXX-XXXXXXXX、0XX-XXXXXXXX、(0XXX)XXXXXXXX、0XXXXXXXXXXX
 * @author JHan
 * @Date 2026-08-07 15:28
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER })
@Constraint(validatedBy = { TelephoneValidator.class })
public @interface Telephone {

    String message() default "座机号码格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
