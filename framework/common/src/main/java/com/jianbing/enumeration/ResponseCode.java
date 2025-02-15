package com.jianbing.enumeration;

/**
 * 响应状态码 1-成功 2-失败
 */
public enum ResponseCode {
    SUCCESS((byte) 1, "成功"),
    FAIL((byte) 2, "失败");

    private byte code;
    private String desc;

    ResponseCode(byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public byte getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
