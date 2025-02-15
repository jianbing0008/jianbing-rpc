package com.jianbing.transport.message;

import lombok.*;

/**
 * 服务提供方回复的响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class RpcResponse {

    //请求id
    private Long requestId;

    //压缩类型，序列化方式
    private byte compressType;
    private byte serializeType;

    //响应码  1 成功  2 异常
    private byte code;

    //响应的载体
    private Object body;
}
