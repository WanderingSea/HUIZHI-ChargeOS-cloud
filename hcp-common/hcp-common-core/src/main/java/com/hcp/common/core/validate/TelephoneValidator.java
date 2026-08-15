package com.hcp.common.core.validate;

import com.hcp.common.core.utils.StringUtils;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * TelephoneValidator
 * </p>
 * 自定义座机号码校验注解实现
 * @author JHan
 * @Date 2026-08-07 15:29
 */
public class TelephoneValidator implements ConstraintValidator<Telephone, String> {

    /**
     * 座机正则：
     * 支持 (010)88886666、010-88886666、01088886666、0411-88886666 等
     */
    private static final String TELEPHONE_PATTERN = "^0\\d{2,3}[-]?\\d{7,8}$|^\\(0\\d{2,3}\\)\\d{7,8}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext)
    {
        if (StringUtils.isEmpty(value))
        {
            return true;
        }
        Pattern pattern = Pattern.compile(TELEPHONE_PATTERN);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }


}
