package com.hcp.auth.form;

import com.hcp.common.core.validate.Phone;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户登录对象
 * 目前仅支持 手机号+密码 登录
 *
 * @author JHan
 */
@Data
public class LoginBody {

    /**
     * 管理者手机号
     * 目前仅支持手机号登录(后续考虑支持账号登录)
     */
    @Schema(description = "管理者手机号")
    @NotBlank(message = "管理者手机号不能为空")
    @Phone(message = "管理者手机号格式不正确")
    private String phone;

    /**
     * 管理者密码
     */
    @Schema(description = "管理者密码")
    private String password;

    /**
     * 验证码
     */
    @Schema(description = "验证码")
    private String code;

    /**
     * 验证码唯一标识
     */
    @Schema(description = "验证码唯一标识")
    private String uuid;
}
