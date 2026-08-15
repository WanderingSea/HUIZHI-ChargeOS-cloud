package com.hcp.auth.service;

import com.hcp.common.security.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.hcp.common.core.constant.Constants;
import com.hcp.common.core.constant.SecurityConstants;
import com.hcp.common.core.utils.StringUtils;
import com.hcp.common.core.utils.ip.IpUtils;
import com.hcp.system.api.RemoteLogService;
import com.hcp.system.api.domain.SysLoginInfo;

/**
 * 记录日志方法
 *
 * @author JHan
 */
@Component
@RequiredArgsConstructor
public class SysRecordLogService
{
    private final RemoteLogService remoteLogService;

    /**
     * 记录登录信息
     *
     * @param phone 用户手机号
     * @param status 状态
     * @param message 消息内容
     * @return
     */
    public void recordLoginInfo(String phone, String status, String message)
    {
        SysLoginInfo loginInfo = new SysLoginInfo();
         loginInfo.setPhone(phone)
                  .setIpaddr(IpUtils.getIpAddr())
                  .setMsg(message)
                  .setMerchantId(SecurityUtils.getMerchantId());
        // 日志状态
        if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER))
        {
            loginInfo.setStatus(Constants.LOGIN_SUCCESS_STATUS);
        }
        else if (Constants.LOGIN_FAIL.equals(status))
        {
            loginInfo.setStatus(Constants.LOGIN_FAIL_STATUS);
        }
        remoteLogService.saveLoginInfo(loginInfo, SecurityConstants.INNER);
    }
}
