package com.hcp.auth.form;

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
 * 平台管理员注册运营商dto对象
 *
 * @author JHan
 */
@Data
public class RegisterBody {

    /* sys_user表关键数据 */
    /**
     * 管理者手机号码（用于登录 + 默认昵称）
     */
    @NotBlank(message = "手机号码不能为空")
    @Phone(message = "管理者手机号格式不正确")
    @Schema(description = "管理者手机号码")
    private String phone;

    /**
     * 管理者真实姓名
     */
    @Schema(description = "管理者真实姓名")
    @NotBlank(message = "管理者真实姓名不能为空")
    private String realName;

    /**
     * 管理者邮箱
     */
    @Schema(description = "管理者邮箱")
    private String email;

    /**
     * 管理者性别
     */
    @Schema(description = "管理者性别（0-男/1-女/2-未知）")
    private SexEnum sex;

    /* sys_tenant表关键数据 */

    @Schema(description = "工商主体类型（1-企业法人/2-个体工商户/3-个人自然人商户）")
    private SubjectTypeEnum subjectType;

    /**
     * 运营商编号(对外展示),由系统自动生成
     */
    @Schema(description = "运营商编号(对外展示),系统自动生成")
    private String merchantNo;

    /**
     * 运营商业务类型：1-平台自营/2-联盟运营商/3-普通运营商
     */
    @Schema(description = "运营商业务类型（1-平台自营/2-联盟运营商/3-普通运营商）")
    private MerchantTypeEnum merchantType;

    /**
     * 主体名称：企业填营业执照全称；个体填字号名称
     */
    @Schema(description = "主体名称：企业填营业执照全称；个体填字号名称")
    private String merchantName;

    /**
     * 统一社会信用代码
     */
    @Schema(description = "统一社会信用代码(18位)")
    private String unifiedSocialCode;

    /**
     * 负责人：企业填法定代表人；个体填经营者姓名
     */
    @Schema(description = "负责人：企业填法定代表人；个体填经营者姓名")
    @NotBlank(message = "法人姓名不能为空")
    private String legalPersonName;

    /**
     * 运营商注册地址
     */
    @Schema(description = "运营商注册地址")
    @NotBlank(message = "运营商注册地址不能为空")
    private String registerAddress;

    /**
     * 营业执照图片url
     */
    @Schema(description = "营业执照图片url")
    @NotBlank(message = "营业执照图片url不能为空")
    private String businessLicenseUrl;

    @Schema(description = "协议文件url")
    @NotBlank(message = "协议文件url不能为空")
    private String agreementFileUrl;

    /**
     * 平台租凭开始时间
     */
    @Schema(description = "平台租凭开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date tenantStartTime;

    /**
     * 平台租凭结束时间
     */
    @Schema(description = "平台租凭结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date tenantEndTime;

    /**
     * 省份名称
     */
    @Schema(description = "省份名称")
    private String provinceName;

    /**
     * 省份编码
     */
    @Schema(description = "省份编码")
    private String provinceCode;

    /**
     * 城市名称
     */
    @Schema(description = "城市名称")
    private String cityName;

    /**
     * 城市编码
     */
    @Schema(description = "城市编码")
    private String cityCode;

    @Schema(description = "备注")
    private String remark;

}
