package com.jianbing.channelHandler.handler;

import com.jianbing.serialize.Serializer;
import com.jianbing.serialize.SerializerFactory;
import com.jianbing.transport.message.MessageFormatConstant;
import com.jianbing.transport.message.RequestPayload;
import com.jianbing.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 基于长度字段的帧解码器
 *
 *  * 自定义协议编码器
 *  * <p>
 *  * <pre>
 *  * *   0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22   23   24   25   26   27   28   29
 *  * *   +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 *  * *   |<--------------------- magic (12B) ------------------>| ver|head_len |  full_length (4B) |code|ser |com |<---------- requestId (8B) ----------->|
 *  * *   +----------------------------------------------------------------------------+---+-------------+---------------------------------+---+---+---+---+
 *  * *   |                                                                                                                                                |
 *  * *   |                                                                     body                                                                       |
 *  * *   |                                                                                                                                                |
 *  * *   +-------------------------------------------------------------------------------------------------------------------------------------------------
 *  * <pre>
 *  * 12B magic(魔数)   --->Jianbing-RPC.getBytes()
 *  * 1B version(版本)   ----> 1
 *  * 2B header length 首部的长度
 *  * 4B full length 报文总长度
 *  * 1B code
 *  * 1B serialize
 *  * 1B compress
 *  * 8B requestId
 *  *
 *  * body
 */
@Slf4j
@Builder
public class RpcResponseDecoder extends LengthFieldBasedFrameDecoder {


    /**
     * 构造函数用于初始化RPC消息解码器
     * 本构造函数调用了父类的构造函数，以设置消息解析的参数
     *Netty的解码器需要处理以下问题：
     * 跳过头部字段（lengthFieldOffset）
     * 读取长度字段（lengthFieldLength）
     * 根据长度字段值动态计算剩余需读取的字节数（lengthAdjustment）
     */
    public RpcResponseDecoder() {
        super(
                // 找到当前报文的总长度，截取报文，截取出来的报文才可以进行解析
                MessageFormatConstant.MAX_FRAME_LENGTH,  // 最大帧长度，超过maxFrameLength会直接丢弃
                MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH, // 长度字段偏移量(full_length的起始位置), 即魔术值+版本+头部长度
                MessageFormatConstant.FULL_FIELD_LENGTH, // 长度字段长度(full_length的长度)
                -(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH + MessageFormatConstant.FULL_FIELD_LENGTH), // 长度调整(负载的适配长度),将长度字段值（full_length）从“总长度”转换为“Body长度”
                0 //initialBytesToStrip 是 LengthFieldBasedFrameDecoder 中用于控制解码后是否跳过报文头部的参数。
        );
    }
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf byteBuf) {
            return DecodeFrame(byteBuf);
        }
        return null;
    }

    private Object DecodeFrame(ByteBuf byteBuf) {
        // 1、解析魔术
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != MessageFormatConstant.MAGIC[i]) {
                throw new IllegalArgumentException("不合法的magic code: " + Arrays.toString(magic));
            }
        }
        // 2、解析版本
        byte version = byteBuf.readByte();
        if (version > MessageFormatConstant.VERSION) {
            throw new IllegalArgumentException("不合法的version: " + version);
        }

        // 3、解析头部长度
        short headLength = byteBuf.readShort();

        // 4、解析总长度
        int fullLength = byteBuf.readInt();

        // 5、解析响应码
        byte responseCode = byteBuf.readByte();

        // 6、解析序列化类型
        byte serializeType = byteBuf.readByte();

        // 7、解析压缩类型
        byte compressType = byteBuf.readByte();

        // 8、解析请求id
        long requestId = byteBuf.readLong();

        RpcResponse rpcResponse = RpcResponse.builder()
                .requestId(requestId)
                .code(responseCode)
                .serializeType(serializeType)
                .compressType(compressType)
                .build();

        //todo 心跳检测没有负载，此处可以判断并直接返回
//        if (respCode == RequestType.HEART_BEAT.getCode()) {
//            return rpcRequest;
//        }

        // 9、解析请求体
        int bodyLenth = fullLength - headLength;
        byte[] payLoad = new byte[bodyLenth];
        byteBuf.readBytes(payLoad);

        // todo: 解压缩

        // todo: 反序列化
        Serializer serializer = SerializerFactory.getSerializerWrapper(serializeType).getSerializer();
        rpcResponse.setBody(serializer.deserialize(payLoad, Object.class));

        // Decoder中打印读取的magic、version、full_length等字段
        if (log.isDebugEnabled()) {
            log.debug("响应---->【{}】已完成解码", rpcResponse.getRequestId());
            log.debug("read_full_response_length: {}", fullLength);
        }

        return rpcResponse;
    }
}
