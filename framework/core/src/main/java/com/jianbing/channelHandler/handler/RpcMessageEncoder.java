package com.jianbing.channelHandler.handler;

import com.jianbing.transport.message.MessageFormatConstant;
import com.jianbing.transport.message.RequestPayload;
import com.jianbing.transport.message.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 自定义协议编码器
 * <p>
 * <pre>
 * *   0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22   23   24   25   26   27   28   29
 * *   +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 * *   |<--------------------- magic (12B) ------------------>| ver|head_len |  full_length (4B) | qt |ser |com |<---------- requestId (8B) ----------->|
 * *   +----------------------------------------------------------------------------+---+-------------+---------------------------------+---+---+---+---+
 * *   |                                                                                                                                                |
 * *   |                                                                     body                                                                       |
 * *   |                                                                                                                                                |
 * *   +-------------------------------------------------------------------------------------------------------------------------------------------------
 * <pre>
 * 12B magic(魔数)   --->Jianbing-RPC.getBytes()
 * 1B version(版本)   ----> 1
 * 2B header length 首部的长度
 * 4B full length 报文总长度
 * 1B serialize
 * 1B compress
 * 1B requestType
 * 8B requestId
 *
 * body
 * 出站时，第一个经过的处理器
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest, ByteBuf byteBuf) throws Exception {
        // 12B魔数
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);

        // 1B版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);

        // 2B首部长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);

        // 总长度不清楚，故将写入索引向前移动4个字节，用于跳过当前写入位置后的4个字节空间
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);

        // 3个1B类型
        byteBuf.writeByte(rpcRequest.getRequestType());
        byteBuf.writeByte(rpcRequest.getSerializeType());
        byteBuf.writeByte(rpcRequest.getCompressType());

        // 8B请求id
        byteBuf.writeLong(rpcRequest.getRequestId());

        // 写入请求体（requestPayload）
        byte[] body = getBodyBytes(rpcRequest.getRequestPayload());
        if(body != null){
            byteBuf.writeBytes(body);
        }
        // 如果是心跳检测，则body=0
        int bodyLength = body==null? 0 : body.length;


        // 先保存当前写指针的位置
        int writerIndex = byteBuf.writerIndex();
        // 将写指针的位置移动到总长度的位置上
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length
                + MessageFormatConstant.VERSION_LENGTH
                + MessageFormatConstant.HEADER_FIELD_LENGTH);
        // 将总长度写入到总长度的位置上
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
        // 将写指针归位
        byteBuf.writerIndex(writerIndex);

        // Encoder中打印写入的full_length
        log.debug("Total  length written: {}", MessageFormatConstant.HEADER_LENGTH + body.length);
    }

    /**
     * 将请求负载对象序列化为字节数组
     *
     * @param requestPayload 请求负载对象，包含需要序列化的数据
     * @return 返回序列化后的字节数组
     * @throws RuntimeException 如果序列化过程中发生IO异常，则抛出运行时异常
     */
    private byte[] getBodyBytes(RequestPayload requestPayload) {
        //心跳请求没有payLoad, 直接返回null
        if (requestPayload == null) {
            return null;
        }
        try {
            // 序列化
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(baos);
            outputStream.writeObject(requestPayload);

            // 压缩

            return baos.toByteArray();
        } catch (IOException e) {
            log.error("序列化失败", e);
            throw new RuntimeException(e);
        }
    }
}
