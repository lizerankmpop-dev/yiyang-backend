package com.neusoft.common;

import lombok.Data;

/**
 * 统一响应封装
 * <p>
 * 企业级规范改造：
 * 1. 使用 Lombok 简化代码
 * 2. 增加 HTTP 状态码语义（200/400/401/403/404/500）
 * 3. 支持链式调用 new R<>().code(200).msg("ok").data(obj)
 */
@Data
public class R<T> {

    /** 业务状态码 */
    private int code;

    /** 提示信息 */
    private String msg;

    /** 提示信息（前端兼容字段） */
    public String getMessage() {
        return this.msg;
    }

    public void setMessage(String message) {
        this.msg = message;
    }

    /** 响应数据 */
    private T data;

    // ==================== 成功 ====================

    public static <T> R<T> ok() {
        return new R<T>().code(200).msg("success");
    }

    public static <T> R<T> ok(String msg) {
        return new R<T>().code(200).msg(msg);
    }

    public static <T> R<T> ok(T data) {
        return new R<T>().code(200).msg("success").data(data);
    }

    // ==================== 失败 ====================

    public static <T> R<T> fail(String msg) {
        return new R<T>().code(500).msg(msg);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<T>().code(code).msg(msg);
    }

    /** 400 - 参数校验失败 */
    public static <T> R<T> badRequest(String msg) {
        return new R<T>().code(400).msg(msg);
    }

    /** 401 - 未登录 */
    public static <T> R<T> unauthorized(String msg) {
        return new R<T>().code(401).msg(msg);
    }

    /** 403 - 无权限 */
    public static <T> R<T> forbidden(String msg) {
        return new R<T>().code(403).msg(msg);
    }

    /** 404 - 资源不存在 */
    public static <T> R<T> notFound(String msg) {
        return new R<T>().code(404).msg(msg);
    }

    // ==================== 链式 setter ====================

    public R<T> code(int code) {
        this.code = code;
        return this;
    }

    public R<T> msg(String msg) {
        this.msg = msg;
        return this;
    }

    public R<T> data(T data) {
        this.data = data;
        return this;
    }

    // ==================== 便捷判断 ====================

    public boolean isSuccess() {
        return this.code == 200;
    }
}
