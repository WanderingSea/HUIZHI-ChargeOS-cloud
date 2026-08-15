package com.hcp.system.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hcp.common.core.annotation.Excel;
import com.hcp.common.core.enums.MerchantTypeEnum;
import com.hcp.common.core.enums.SubjectTypeEnum;
import com.hcp.common.core.enums.UserStatusEnum;
import com.hcp.common.core.web.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
/**
 * 运营商基础信息实体类
 *
 * @author JHan
 * @date 2022-04-11
 */
@Data
@TableName("sys_tenant")
@Schema(description = "运营商基础信息实体类")
public class SysTenant extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 运营商基础信息主键id */
    @Excel(name = "运营商基础信息主键id")
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "运营商基础信息主键id")
    private Long id;

    /** 运营商编号 */
    @Excel(name = "运营商编号")
    @Schema(description = "运营商编号（全局唯一，对外展示）")
    private String merchantNo;

    /** 运营商业务类型 */
    @Excel(name = "运营商业务类型")
    @Schema(description = "运营商业务类型：1-平台自营/2-联盟运营商/3-普通商户")
    private MerchantTypeEnum merchantType;

    /** 主体名称 */
    @Excel(name = "主体名称")
    @Schema(description = "主体名称：企业填营业执照全称；个体填字号名称")
    private String merchantName;

    /** 统一社会信用代码 */
    @Excel(name = "统一社会信用代码")
    @Schema(description = "统一社会信用代码")
    private String unifiedSocialCode;

    /** 工商主体类型 */
    @Excel(name = "工商主体类型")
    @Schema(description = "工商主体类型：1-企业法人/2-个体工商户/3-个人自然人商户")
    private SubjectTypeEnum subjectType;

    /** 负责人 */
    @Excel(name = "负责人")
    @Schema(description = "负责人：企业填法定代表人；个体填经营者姓名")
    private String legalPersonName;

    /** 注册地址 */
    @Excel(name = "注册地址/经营地址")
    @Schema(description = "注册地址/经营地址")
    private String registerAddress;

    /** 营业执照图片 */
    @Excel(name = "营业执照图片")
    @Schema(description = "营业执照图片")
    private String businessLicenseImg;

    /** 平台租赁开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "平台租赁开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "平台租赁开始时间")
    private Date tenantStartTime;

    /** 平台租赁结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "平台租赁结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "平台租赁结束时间")
    private Date tenantEndTime;

    /** 所属省份编码 */
    @Excel(name = "所属省份编码")
    @Schema(description = "所属省份编码")
    private String provinceCode;

    /** 所属省份名称 */
    @Excel(name = "所属省份名称")
    @Schema(description = "所属省份名称")
    private String provinceName;

    /** 所属城市编码 */
    @Excel(name = "所属城市编码")
    @Schema(description = "所属城市编码")
    private String cityCode;

    /** 所属城市名称 */
    @Excel(name = "所属城市名称")
    @Schema(description = "所属城市名称")
    private String cityName;

    /** 协议文件地址 */
    @Excel(name = "协议文件地址")
    @Schema(description = "协议文件地址")
    private String agreementFileUrl;

    /** 租户套餐 */
    @Excel(name = "租户套餐")
    @Schema(description = "租户套餐")
    private Long tenantPackage;

    /** 租户状态（0正常 1停用） */
    @Excel(name = "租户状态", readConverterExp = "0-正常/1-停用")
    @Schema(description = "租户状态（0-正常/1-停用）")
    private UserStatusEnum status;

    /** 删除标志（0代表存在/1代表删除） */
    @Schema(description = "删除标志（0代表存在 1代表删除）")
    private Integer delFlag;

    @Schema(description = "创建者真实姓名")
    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新者真实姓名")
    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "备注")
    private String remark;

}
