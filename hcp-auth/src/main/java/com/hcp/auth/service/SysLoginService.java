package com.hcp.auth.service;

import com.hcp.auth.form.RegisterBody;
import com.hcp.common.core.constant.CacheConstants;
import com.hcp.common.core.enums.UserTypeEnum;
import com.hcp.common.core.text.Convert;
import com.hcp.common.redis.service.RedisService;
import com.hcp.system.api.domain.dto.RegisterMerchantDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.hcp.common.core.constant.Constants;
import com.hcp.common.core.constant.SecurityConstants;
import com.hcp.common.core.constant.UserConstants;
import com.hcp.common.core.domain.R;
import com.hcp.common.core.enums.UserStatusEnum;
import com.hcp.common.core.exception.ServiceException;
import com.hcp.common.core.utils.StringUtils;
import com.hcp.common.core.utils.ip.IpUtils;
import com.hcp.common.security.utils.SecurityUtils;
import com.hcp.system.api.RemoteUserService;
import com.hcp.system.api.domain.SysUser;
import com.hcp.system.api.model.LoginUser;
import java.util.Date;

/**
 * 登录校验方法
 *
 * @author JHan
 */
@Component
@RequiredArgsConstructor
public class SysLoginService {

    private final RemoteUserService remoteUserService;
    private final SysPasswordService passwordService;
    private final SysRecordLogService recordLogService;
    private final RedisService redisService;

    /**
     * 平台后台管理登录实现方法
     * 根据用户身份类型拆分校验逻辑：
     * userType=1/2（系统超管/平台管理员）：仅校验用户自身状态
     * userType=4/5（运营商运营/子账号）：额外校验运营商停用/过期状态
     * userType=3（代理商运营）：仅校验用户自身状态（按需可扩展）
     *
     */
    public LoginUser loginByPhone(String phone, String password){

        //==================== 第一部分：基础校验（所有身份通用） ====================

        // 用户手机号或密码为空 错误
        if (StringUtils.isAnyBlank(phone, password))
        {
            recordLogService.recordLoginInfo(phone, Constants.LOGIN_FAIL, "用户手机号/密码必须填写");
            throw new ServiceException("用户手机号/密码必须填写");
        }

        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            recordLogService.recordLoginInfo(phone, Constants.LOGIN_FAIL, "用户密码不在指定范围");
            throw new ServiceException("用户密码不在指定范围");
        }

        // 用户手机号不在指定范围内 错误
        if (phone.length() < UserConstants.PHONE_NUM_MIN_LENGTH
                || phone.length() > UserConstants.PHONE_NUM_MAX_LENGTH)
        {
            recordLogService.recordLoginInfo(phone, Constants.LOGIN_FAIL, "用户手机号不在指定范围");
            throw new ServiceException("用户手机号不在指定范围");
        }

        // IP黑名单校验
        String blackStr = Convert.toStr(redisService.getCacheObject(CacheConstants.SYS_LOGIN_BLACKIPLIST));
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr()))
        {
            recordLogService.recordLoginInfo(phone, Constants.LOGIN_FAIL, "很遗憾，访问IP已被列入系统黑名单");
            throw new ServiceException("很遗憾，访问IP已被列入系统黑名单");
        }

        // 查询用户信息
        R<LoginUser> userResult = remoteUserService.getUserInfo(phone, SecurityConstants.INNER);

        if (StringUtils.isNull(userResult) || StringUtils.isNull(userResult.getData()))
        {
            recordLogService.recordLoginInfo(phone, Constants.LOGIN_FAIL, "登录用户不存在");
            throw new ServiceException("登录用户：" + phone + " 不存在");
        }

        if (R.FAIL == userResult.getCode())
        {
            throw new ServiceException(userResult.getMsg());
        }

        LoginUser userInfo = userResult.getData();
        SysUser user = userResult.getData().getSysUser();
        UserTypeEnum userType = user.getUserType();  // 获取用户身份类型

        //线程塞入租户ID(运营行相关用户才有)
        SecurityUtils.setMerchantId(Convert.toStr(user.getMerchantId()));

        // ==================== 第二部分：按身份类型分流校验 ====================

        // 通用校验：所有身份都要检查用户自身是否被删除/停用
        if (UserStatusEnum.DELETED.getValue().equals(user.getDelFlag()))
        {
            recordLogService.recordLoginInfo(phone, Constants.LOGIN_FAIL, "对不起，您的账号已被删除");
            throw new ServiceException("对不起，您的账号：" + phone + " 已被删除");
        }
        if (UserStatusEnum.DISABLE.equals(user.getStatus()))
        {
            recordLogService.recordLoginInfo(phone, Constants.LOGIN_FAIL, "用户已停用，请联系管理员");
            throw new ServiceException("对不起，您的账号：" + phone + " 已停用");
        }

        // 根据身份类型执行差异化校验
        if (UserTypeEnum.isPlatAdminOrHigher(userType.getValue()))
        {
            // ========== 平台级用户（userType=1/2）：仅校验用户自身，无需校验运营商 ==========
            // 系统超管 / 平台管理员 不绑定运营商，不需要检查租户状态
        }
        else if (UserTypeEnum.isMerchant(userType.getValue()) || UserTypeEnum.isMerchantStaff(userType.getValue()))
        {
            // ========== 运营商及子账号（userType=4/5）：额外校验运营商状态 ==========
            validateMerchantStatus(userInfo, phone);
        }
        else if (UserTypeEnum.isAgentOp(userType.getValue()))
        {
            // ========== 代理商运营（userType=3）：后续扩展 ==========
            // 目前仅校验用户自身状态，如需校验代理商状态可在此扩展
        }

        // ==================== 第三部分：密码校验 + 登录成功日志 ====================
        passwordService.validate(user, password);
        recordLogService.recordLoginInfo(phone, Constants.LOGIN_SUCCESS, "登录成功");

                return userInfo;
    }

    /**
     * 校验运营商（租户）状态
     * 仅 userType=4/5（运营商运营/子账号）调用
     * @param userInfo 登录用户信息
     * @param phone 手机号（用于日志记录）
     */
    private void validateMerchantStatus(LoginUser userInfo, String phone) {
        // 校验运营商是否被停用
        if (userInfo.getStatus() != null && UserStatusEnum.DISABLE.equals(userInfo.getStatus()))
        {
            recordLogService.recordLoginInfo(phone, Constants.LOGIN_FAIL, "当前运营商已被停用，请联系管理员");
            throw new ServiceException("当前运营商已被停用，请联系管理员");
        }
        // 校验运营商租赁是否过期
        if (userInfo.getTenantEndTime() != null && userInfo.getTenantEndTime().compareTo(new Date()) < 0)
        {
            recordLogService.recordLoginInfo(phone, Constants.LOGIN_FAIL, "当前运营商已超过租赁日期，请联系管理员");
            throw new ServiceException("当前运营商已超过租赁日期，请联系管理员");
        }
    }

    /**
     * 平台管理员注册运营商(租户)方法实现  <br>
     * 通过 Feign 远程调用 RemoteMerchantService.registerMerchant方法注册用户 <br>
     * 说明：只允许平台级管理员注册运营商 <br>
     * 具体流程： <br>
     * 前端 POST /register  <br>
     *     ↓ <br>
     * TokenController.register(RegisterBody) <br>
     *     ↓ <br>
     * SysLoginService.register(RegisterBody) <br>
     *     ├─ ① 必填项校验 <br>
     *     ├─ ② RegisterBody → RegisterMerchantDTO <br>
     *     ├─ ③ Feign → remoteMerchantService.registerMerchant() <br>
     *     └─ ④ 记录注册日志 <br>
     *     ↓ <br>
     * SysTenantController.register() (@InnerAuth)<br>
     *     ↓<br>
     * SysTenantService.registerTenant()<br>
     *     ├─ 生成运营商编号<br>
     *     ├─ 生成管理员账号<br>
     *     ├─ 生成随机密码<br>
     *     ├─ 插入 sys_tenant<br>
     *     ├─ 插入 sys_user<br>
     *     └─ 发送通知<br>
     *     ↓<br>
     * 返回 "运营商注册成功，登录账号：XZC608912345"<br>
     */
    public void register(RegisterBody registerBody) {

        // ==================== 第一部分：必填项校验 ====================

        if (StringUtils.isBlank(registerBody.getPhone())) {
            throw new ServiceException("管理者手机号不能为空");
        }
        if (StringUtils.isBlank(registerBody.getRealName())) {
            throw new ServiceException("管理者真实姓名不能为空");
        }
        if (StringUtils.isBlank(registerBody.getMerchantName())) {
            throw new ServiceException("主体名称不能为空");
        }
        if (StringUtils.isBlank(registerBody.getLegalPersonName())) {
            throw new ServiceException("负责人姓名不能为空");
        }
        if (StringUtils.isBlank(registerBody.getRegisterAddress())) {
            throw new ServiceException("注册地址不能为空");
        }
        if (StringUtils.isBlank(registerBody.getBusinessLicenseUrl())) {
            throw new ServiceException("营业执照图片不能为空");
        }
        if (StringUtils.isBlank(registerBody.getAgreementFileUrl())) {
            throw new ServiceException("协议文件不能为空");
        }

        // ==================== 第二部分：转换为 RegisterMerchantDTO ====================

        RegisterMerchantDTO dto = new RegisterMerchantDTO();
                            dto.setPhone(registerBody.getPhone())
                               .setRealName(registerBody.getRealName())
                               .setEmail(registerBody.getEmail())
                               .setSex(registerBody.getSex())
                               .setSubjectType(registerBody.getSubjectType())
                               .setMerchantType(registerBody.getMerchantType())
                               .setMerchantName(registerBody.getMerchantName())
                               .setUnifiedSocialCode(registerBody.getUnifiedSocialCode())
                               .setLegalPersonName(registerBody.getLegalPersonName())
                               .setRegisterAddress(registerBody.getRegisterAddress())
                               .setBusinessLicenseUrl(registerBody.getBusinessLicenseUrl())
                               .setAgreementFileUrl(registerBody.getAgreementFileUrl())
                               .setTenantStartTime(registerBody.getTenantStartTime())
                               .setTenantEndTime(registerBody.getTenantEndTime())
                               .setProvinceCode(registerBody.getProvinceCode())
                               .setProvinceName(registerBody.getProvinceName())
                               .setCityCode(registerBody.getCityCode())
                               .setCityName(registerBody.getCityName())
                               .setRemark(registerBody.getRemark());

        // ==================== 第三部分：Feign 远程调用 hcp-system 模块 ====================

        R<String> result = remoteUserService.registerMerchant(dto, SecurityConstants.INNER);

        if (R.FAIL == result.getCode()) {
            throw new ServiceException(result.getMsg());
        }

        // ==================== 第四部分：记录注册日志 ====================

        String adminAccount = result.getData();
        recordLogService.recordLoginInfo(
                registerBody.getPhone(),
                Constants.REGISTER,
                "运营商注册成功，登录账号：" + adminAccount
        );
    }

    /**
     * 后台管理级用户退出登录
     */
    public void logout(String loginName) {
        recordLogService.recordLoginInfo(loginName, Constants.LOGOUT, "退出成功");
    }

}
