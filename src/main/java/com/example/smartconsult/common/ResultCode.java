package com.example.smartconsult.common;

/**
 * Common API result codes.
 *
 * @author lizhifu
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "请求参数错误"),
    BUSINESS_ERROR(4000, "业务处理失败"),
    SYSTEM_ERROR(500, "系统异常");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
