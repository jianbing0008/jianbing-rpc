package com.jianbing.channelHandler.handler;

import com.jianbing.compress.Compressor;
import com.jianbing.compress.CompressorFactory;
import com.jianbing.serialize.Serializer;
import com.jianbing.serialize.SerializerFactory;
import com.jianbing.transport.message.MessageFormatConstant;
import com.jianbing.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义协议编码器
 * <p>
 * <pre>
 * *   0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22   23   24   25   26   27   28   29
 * *   +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 * *   |<--------------------- magic (12B) ------------------>| ver|head_len |  full_length (4B) |code|ser |com |<---------- requestId (8B) ----------->|
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
 * 1B code
 * 1B serialize
 * 1B compress
 * 8B requestId
 *
 * body
 * 出站时，第一个经过的处理器
 */
@Slf4j
public class RpcResponseEncoder extends MessageToByteEncoder<RpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse, ByteBuf byteBuf) throws Exception {
        // 12B魔数
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);

        // 1B版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);

        // 2B首部长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);

        // 总长度不清楚，故将写入索引向前移动4个字节，用于跳过当前写入位置后的4个字节空间
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);

        // 3个1B类型
        byteBuf.writeByte(rpcResponse.getCode());
        byteBuf.writeByte(rpcResponse.getSerializeType());
        byteBuf.writeByte(rpcResponse.getCompressType());

        // 8B请求id
        byteBuf.writeLong(rpcResponse.getRequestId());

        // 对响应序列化
        Serializer serializer = SerializerFactory.getSerializerWrapper(rpcResponse.getSerializeType()).getSerializer();
        byte[] body = serializer.serialize(rpcResponse.getBody());

        // 压缩
        Compressor compressor = CompressorFactory.getSerializerWrapper(rpcResponse.getCompressType()).getCompressor();
        body = compressor.compress(body);


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
        if (log.isDebugEnabled()) {
            log.debug("响应---->【{}】已完成编码", rpcResponse.getRequestId());
            log.debug("response Total length written: {}", MessageFormatConstant.HEADER_LENGTH + body.length);
        }
    }

}
