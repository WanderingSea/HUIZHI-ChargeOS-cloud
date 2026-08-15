package com.hcp.system.api.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hcp.common.core.enums.MerchantTypeEnum;
import com.hcp.common.core.enums.SexEnum;
import com.hcp.common.core.enums.SubjectTypeEnum;
import com.hcp.common.core.validate.Phone;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * <p>
 * RegisterTenantDTO
 * </p>
 * 平台管理员注册运营商传输对象（Feign 跨模块调用）
 * @author JHan
 * @Date 2026-08-10 14:59
 */
@Data
public class RegisterMerchantDTO {

    /* ========== sys_user 表相关字段 ========== */

    @NotBlank(message = "管理者手机号码不能为空")
    @Schema(description = "管理者手机号码（用于登录）")
    @Phone(message = "管理者手机号格式不正确")
    private String phone;

    @NotBlank(message = "管理者真实姓名不能为空")
    @Schema(description = "管理者真实姓名")
    private String realName;

    @Schema(description = "管理者邮箱")
    private String email;

    @Schema(description = "管理者性别（0-男/1-女/2-未知）")
    private SexEnum sex;

    /* ========== sys_tenant 表相关字段 ========== */

    @Schema(description = "运营商编号（对外展示），系统自动生成")
    private String merchantNo;

    @Schema(description = "运营商业务类型（1-平台自营/2-联盟运营商/3-普通运营商）")
    private MerchantTypeEnum merchantType;

    @NotBlank(message = "主体名称不能为空")
    @Schema(description = "主体名称：企业填营业执照全称；个体填字号名称")
    private String merchantName;

    @Schema(description = "统一社会信用代码(18位)")
    private String unifiedSocialCode;

    @Schema(description = "工商主体类型（1-企业法人/2-个体工商户/3-个人自然人商户）")
    private SubjectTypeEnum subjectType;

    @NotBlank(message = "负责人姓名不能为空")
    @Schema(description = "负责人：企业填法定代表人；个体填经营者姓名")
    private String legalPersonName;

    @NotBlank(message = "运营商注册地址不能为空")
    @Schema(description = "运营商注册地址")
    private String registerAddress;

    @NotBlank(message = "营业执照图片url不能为空")
    @Schema(description = "营业执照图片url")
    private String businessLicenseUrl;

    @Schema(description = "协议文件url")
    @NotBlank(message = "协议文件url不能为空")
    private String agreementFileUrl;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "平台租赁开始时间")
    private Date tenantStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "平台租赁结束时间")
    private Date tenantEndTime;

    @Schema(description = "省份名称")
    private String provinceName;

    @Schema(description = "省份编码")
    private String provinceCode;

    @Schema(description = "城市名称")
    private String cityName;

    @Schema(description = "城市编码")
    private String cityCode;

    @Schema(description = "备注")
    private String remark;
}
