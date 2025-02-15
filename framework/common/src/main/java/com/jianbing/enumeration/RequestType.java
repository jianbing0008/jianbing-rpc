package com.jianbing.enumeration;

/**
 * 标记请求类型
 */
public enum RequestType {
    REQUEST((byte) 1, "普通请求"),
    HEART_BEAT((byte) 2, "心跳检测请求");

    private byte code;
    private String desc;

    private RequestType(byte code, String desc) {
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
