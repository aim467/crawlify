package org.crawlify.platform.exception;

import cn.dev33.satoken.exception.DisableServiceException;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.NotSafeException;
import org.crawlify.common.entity.result.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕获所有未处理的异常
    @ExceptionHandler(Exception.class)
    public R<String> handleException(Exception ex) {
        // 记录日志（可根据需求替换为实际的日志框架）
        return R.fail("系统异常：" + ex.getMessage());
    }

    // 捕获特定的业务异常（可根据需求扩展）
    @ExceptionHandler(RuntimeException.class)
    public R<String> handleRuntimeException(RuntimeException ex) {
        // 记录日志（可根据需求替换为实际的日志框架）
        ex.printStackTrace();
        return R.fail("运行时异常：" + ex.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public R handlerNotLoginException(NotLoginException nle) {
        // 不同异常返回不同状态码
        String message = "";
        if (nle.getType().equals(NotLoginException.NOT_TOKEN)) {
            message = "未提供Token";
        } else if (nle.getType().equals(NotLoginException.INVALID_TOKEN)) {
            message = "未提供有效的Token";
        } else if (nle.getType().equals(NotLoginException.TOKEN_TIMEOUT)) {
            message = "登录信息已过期，请重新登录";
        } else if (nle.getType().equals(NotLoginException.BE_REPLACED)) {
            message = "您的账户已在另一台设备上登录，如非本人操作，请立即修改密码";
        } else if (nle.getType().equals(NotLoginException.KICK_OUT)) {
            message = "已被系统强制下线";
        } else {
            message = "当前会话未登录";
        }
        // 返回给前端
        return R.fail(401, message);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler
    public R handlerNotRoleException(NotRoleException e) {
        return R.fail(403, "无此角色：" + e.getRole());
    }

    @ExceptionHandler
    public R handlerNotPermissionException(NotPermissionException e) {
        return R.fail(403, "无此权限：" + e.getCode());
    }

    @ExceptionHandler
    public R handlerDisableLoginException(DisableServiceException e) {
        return R.fail(401, "账户被封禁：" + e.getDisableTime() + "秒后解封");
    }

    @ExceptionHandler
    public R handlerNotSafeException(NotSafeException e) {
        return R.fail(401, "二级认证异常：" + e.getMessage());
    }
}