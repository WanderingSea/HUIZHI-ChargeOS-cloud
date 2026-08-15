package com.hcp.auth.controller;

import javax.servlet.http.HttpServletRequest;
import com.hcp.auth.form.LoginBody;
import com.hcp.auth.form.RegisterBody;
import com.hcp.auth.service.SysLoginService;
import com.hcp.common.core.constant.CacheConstants;
import com.hcp.common.core.constant.Constants;
import com.hcp.common.core.domain.R;
import com.hcp.common.core.exception.CaptchaException;
import com.hcp.common.core.utils.JwtUtils;
import com.hcp.common.core.utils.StringUtils;
import com.hcp.common.core.utils.sign.Base64;
import com.hcp.common.core.web.domain.AjaxResult;
import com.hcp.common.redis.service.RedisService;
import com.hcp.common.security.annotation.Logical;
import com.hcp.common.security.annotation.PreAuthorizeUserRole;
import com.hcp.common.security.auth.AuthUtil;
import com.hcp.common.security.service.TokenService;
import com.hcp.common.security.utils.SecurityUtils;
import com.hcp.system.api.model.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * token 控制
 *
 * @author JHan
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class TokenController {

    private final TokenService tokenService;
    private final SysLoginService sysLoginService;
    private final RedisService redisService;

    /**
     * 平台后台管理登录接口：用于平台后台管理登录、运营商登录、运营商子账户登录
     */
    @PostMapping("login")
    public R<?> login(@RequestBody LoginBody form) {
        log.info("收到登录请求: phone={}, code={}, uuid={}", form.getPhone(), form.getCode(), form.getUuid());
        // 验证码已由网关 ValidateCodeFilter 校验，此处直接进行用户登录
        LoginUser userInfo = sysLoginService.loginByPhone(form.getPhone(), form.getPassword());
        // 获取登录token
        return R.ok(tokenService.createToken(userInfo));
    }

    /**
     * 校验验证码
     */
    private void checkCaptcha(String code, String uuid) throws CaptchaException {
        if (StringUtils.isEmpty(code)) {
            throw new CaptchaException("验证码不能为空");
        }
        if (StringUtils.isEmpty(uuid)) {
            throw new CaptchaException("验证码已失效");
        }
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
        String captcha = redisService.getCacheObject(verifyKey);
        redisService.deleteObject(verifyKey);

        if (captcha == null) {
            throw new CaptchaException("验证码已失效");
        }

        if (!code.equalsIgnoreCase(captcha)) {
            throw new CaptchaException("验证码错误");
        }
    }

    /**
     * 运营商注册接口：平台管理用于注册运营商账号
     */
    @PreAuthorizeUserRole(value = {1,2},logical = Logical.OR)
    @PostMapping("register")
    public R<?> register(@RequestBody RegisterBody registerBody) {
        // 用户注册
        sysLoginService.register(registerBody);
        return R.ok("运营商注册成功");
    }

    @DeleteMapping("logout")
    public R<?> logout(HttpServletRequest request)
    {
        String token = SecurityUtils.getToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            try {
                String phone = JwtUtils.getPhone(token);
                // 删除用户缓存记录
                AuthUtil.logoutByToken(token);
                // 记录用户退出日志
                sysLoginService.logout(phone);
            } catch (Exception e) {
                // token不是合法JWT，无法解析userKey，说明无有效会话，直接返回成功
                log.warn("登出时token无效，无有效会话需要清理: {}", e.getMessage());
            }
        }
        return R.ok();
    }

    @PostMapping("refresh")
    public R<?> refresh(HttpServletRequest request)
    {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser))
        {
            // 刷新令牌有效期
            tokenService.refreshToken(loginUser);
            return R.ok();
        }
        return R.ok();
    }


}
