package com.jianbing.transport.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 服务调用方发起的请求内容
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RpcRequest {

    //请求id
    private Long requestId;

    //请求类型，压缩类型，序列化方式
    private byte requestType;
    private byte compressType;
    private byte serializeType;

    //具体的消息体
    private RequestPayload requestPayload;
}
