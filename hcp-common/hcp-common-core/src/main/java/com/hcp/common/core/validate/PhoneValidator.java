package com.hcp.common.core.validate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.hcp.common.core.utils.StringUtils;

/**
 * <p>
 * PhoneValidator
 * </p>
 * 自定义手机号校验注解实现
 * 中国大陆手机号正则：^1[3-9]\d{9}$
 *
 * @author JHan
 * @Date 2026-08-07 15:26
 */
public class PhoneValidator implements ConstraintValidator<Phone,String> {

    /**
     * 中国大陆11位手机号正则
     * 支持号段: 13,14,15,16,17,18,19
     */
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext)
    {
        // 为空不校验，由 @NotBlank 负责非空校验
        if (StringUtils.isBlank(value))
        {
            return true;
        }
        return matches(value);
    }

    /**
     * 校验手机号格式
     */
    public static boolean matches(String phone)
    {
        Pattern pattern = Pattern.compile(PHONE_PATTERN);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }


}
