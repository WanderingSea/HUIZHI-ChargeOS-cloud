package com.hcp.system.api.domain.dto;

import com.hcp.common.core.enums.SexEnum;
import com.hcp.common.core.validate.Phone;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * <p>
 * RegisterMerchantSubAccountDTO
 * </p>
 * 运营商注册子账户DTO
 * @author JHan
 * @Date 2026-08-10 17:05
 */
@Data
public class RegisterMerchantSubAccountDTO {

    @NotBlank(message = "登录账号不能为空")
    @Schema(description = "登录账号（手机号或自定义账号）")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Phone(message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "性别（0-男/1-女/2-未知）")
    private SexEnum sex;

    @Schema(description = "用户类型（3-运营商员工/5-普通用户）")
    private Integer userType;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "岗位ID列表")
    private Long[] postIds;

    @Schema(description = "角色ID列表")
    private Long[] roleIds;

    @Schema(description = "备注")
    private String remark;


}
