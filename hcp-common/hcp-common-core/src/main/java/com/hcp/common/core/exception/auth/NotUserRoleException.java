package com.hcp.common.core.exception.auth;

import org.apache.commons.lang3.StringUtils;

/**
 * 未能通过的用户身份认证异常
 *
 * @author JHan
 */
public class NotUserRoleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NotUserRoleException(int userType) {
        super("用户身份类型[" + userType + "]无权访问");
    }

    public NotUserRoleException(int[] userTypes) {
        super("用户身份类型[" + StringUtils.join(userTypes, ",") + "]无权访问");
    }
}
