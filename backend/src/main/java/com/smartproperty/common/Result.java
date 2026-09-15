package com.smartproperty.common;

import java.io.Serializable;

/**
 * 统一返回结果类
 * 用于封装API接口的返回数据，保持响应格式统一
 *
 * @param <T> 返回数据的类型
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private Integer code;

    /** 返回消息 */
    private String message;

    /** 返回数据 */
    private T data;

    /** 时间戳 */
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // ===== 静态工厂方法 =====

    /** 成功（无数据） */
    public static <T> Result<T> success() {
        return success(null);
    }

    /** 成功（有数据） */
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("操作成功");
        r.setData(data);
        return r;
    }

    /** 成功（自定义消息） */
    public static <T> Result<T> success(String message, T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    /** 失败（自定义code） */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    /** 失败（默认500） */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    /** 参数错误 400 */
    public static <T> Result<T> paramError(String message) {
        return error(400, message);
    }

    /** 未授权 401 */
    public static <T> Result<T> unauthorized(String message) {
        return error(401, message);
    }

    /** 禁止访问 403 */
    public static <T> Result<T> forbidden(String message) {
        return error(403, message);
    }

    /** 资源不存在 404 */
    public static <T> Result<T> notFound(String message) {
        return error(404, message);
    }

    // ===== Getter / Setter =====

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
