package com.hcp.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hcp.common.core.enums.AuditStatusEnum;
import com.hcp.common.core.enums.SettleCycleEnum;
import com.hcp.common.core.enums.UserSourceEnum;
import com.hcp.common.core.enums.UserStatusEnum;
import com.hcp.common.core.web.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * SysTenantInfo
 * </p>
 * 运营商信息实体类
 * @author JHan
 * @Date 2026-08-06 17:43
 */
@Data
@TableName("sys_tenant_info")
@Schema(description = "运营商信息实体类")
public class SysTenantInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 运营商信息主键ID */
    @Schema(description = "运营商信息主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 运营商ID */
    @Schema(description = "运营商ID")
    private Long merchantId;

    /** 上级运营商ID */
    @Schema(description = "上级运营商id：平台直签填 0，预留多级扩展")
    private Long parentAgentId;

    /** 经营范围 */
    @Schema(description = "经营范围，企业建议必填")
    private String businessScope;

    /** 营业期限开始日期 */
    @Schema(description = "营业期限开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date businessTermStart;

    /** 营业期限结束日期 */
    @Schema(description = "营业期限结束日期，长期经营设为 NULL")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date businessTermEnd;

    /** 法人/经营者身份证正面身份证正面 */
    @Schema(description = "法人/经营者身份证正面")
    private String legalIdCardFront;

    /** 法人/经营者身份证反面 */
    @Schema(description = "法人/经营者身份证反面")
    private String legalIdCardBack;

    /** 法人授权委托书 */
    @Schema(description = "法人授权委托书，对接人非法人 / 经营者时必填")
    private String authorizationLetter;

    /** 主对接人姓名 */
    @Schema(description = "主对接人姓名")
    private String contactName;

    /** 主对接人手机号 */
    @Schema(description = "主对接人手机号，同时作为主账号登录手机号，全局唯一")
    private String contactPhone;

    /** 对接人微信号 */
    @Schema(description = "对接人微信号")
    private String contactWechat;

    /** 对接人邮箱 */
    @Schema(description = "对接人邮箱")
    private String contactEmail;

    /** 财务联系人姓名 */
    @Schema(description = "财务联系人姓名，对账专用")
    private String financeName;

    /** 财务联系人电话 */
    @Schema(description = "财务联系人电话，对账专用")
    private String financePhone;

    /** 结算账户类型 */
    @Schema(description = "结算账户类型：1 = 对公账户 2 = 个人银行卡（仅个体商户可选）")
    private Integer accountType;

    /** 账户开户名 */
    @Schema(description = "账户开户名：企业必须与主体全称一致；个人必须与经营者姓名一致")
    private String bankAccountName;

    /** 开户银行全称（精确到支行） */
    @Schema(description = "开户银行全称（精确到支行）")
    private String bankName;

    /** 银行账号 */
    @Schema(description = "银行账号")
    private String bankAccountNo;

    /** 开户行联行号（12 位），精准打款核验用 */
    @Schema(description = "开户行联行号（12 位），精准打款核验用")
    private String bankBranchCode;

    /** 平台服务费费率 */
    @Schema(description = "平台服务费费率")
    private BigDecimal platformFeeRate;

    /** 结算周期 */
    @Schema(description = "结算周期：1 = 日结 7 = 周结 30 = 月结")
    private SettleCycleEnum settlementCycle;

    /** 最低提现金额 */
    @Schema(description = "最低提现金额")
    private BigDecimal minWithdrawAmount;

    /** 提现手续费率 */
    @Schema(description = "提现手续费率")
    private BigDecimal withdrawFeeRate;

    /** 发票抬头 */
    @Schema(description = "发票抬头，默认同主体名称")
    private String invoiceTitle;

    /** 发票税号 */
    @Schema(description = "发票税号，默认同统一社会信用代码")
    private String invoiceTaxNo;

    /** 发票地址 */
    @Schema(description = "开票地址（开增值税专票时必填）")
    private String invoiceAddress;

    /** 发票电话 */
    @Schema(description = "开票电话（开增值税专票时必填）")
    private String invoicePhone;

    /** 发票银行信息 */
    @Schema(description = "开票银行信息（开增值税专票时必填）")
    private String invoiceBankInfo;

    /** 合作模式 */
    @Schema(description = "合作模式：1 = 普通联盟商 2 = 区域独家代理")
    private Integer cooperationMode;

    /** 管辖省份行政编码 */
    @Schema(description = "管辖省份行政编码")
    private String manageProvinceCode;

    /** 管辖省份名称 */
    @Schema(description = "管辖省份名称")
    private String manageProvinceName;

    /** 管辖城市行政编码 */
    @Schema(description = "管辖城市行政编码")
    private String manageCityCode;

    /** 管辖城市名称 */
    @Schema(description = "管辖城市名称")
    private String manageCityName;

    /** 下级商户默认费率 */
    @Schema(description = "下级商户默认费率")
    private BigDecimal lowerMerchantDefaultRate;

    /** 下级商户最低费率 */
    @Schema(description = "下级商户最低费率")
    private BigDecimal lowerMerchantRateMin;

    /** 下级商户最高费率 */
    @Schema(description = "下级商户最高费率")
    private BigDecimal lowerMerchantRateMax;

    /** 合作生效时间 */
    @Schema(description = "合作生效时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cooperationStartTime;

    /** 合作到期时间 */
    @Schema(description = "合作到期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cooperationEndTime;

    /** 合作保证金金额 */
    @Schema(description = "合作保证金金额（元）")
    private BigDecimal depositAmount;

    /** 充电设施运营备案证明 */
    @Schema(description = "充电设施运营备案证明")
    private String operationRecordImg;

    /** 承装（修、试）电力设施许可证 */
    @Schema(description = "承装（修、试）电力设施许可证")
    private String electricLicenseImg;

    /** 审核状态 */
    @Schema(description = "审核状态：0=待审核/1=审核通过/2=审核驳回")
    private AuditStatusEnum auditStatus;

    /** 审核备注 */
    @Schema(description = "审核备注")
    private String auditRemark;

    /** 平台对接商务人员姓名 */
    @Schema(description = "平台对接商务人员姓名")
    private String belongSales;

    /** 删除标志 */
    @Schema(description = "删除标志（0=存在/1=删除）")
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
}
