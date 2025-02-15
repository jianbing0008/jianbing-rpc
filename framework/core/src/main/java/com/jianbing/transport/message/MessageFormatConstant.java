package com.jianbing.transport.message;

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
 */
public class MessageFormatConstant {
    /**
     * 定义魔术字，用于标识Jianbing-RPC协议的开始
     * 使用UTF-8编码确保跨平台兼容性
     */
    public static final byte[] MAGIC = "Jianbing-RPC".getBytes();

    /**
     * 协议版本号，用于未来协议升级的向后兼容
     */
    public static final byte VERSION = 1;

    /**
     * 定义头部长度，包括魔术字、版本号、消息类型、请求ID、序列化类型等字段的总长度
     * 具体长度计算基于协议设计，确保所有字段长度之和
     */
    public static final short HEADER_LENGTH = (byte)(MAGIC.length + 1 + 2 + 4 + 1 + 1 + 1 + 8);

    /**
     * 头部的长度字段的长度，用于序列化和反序列化时确定头部长度的字节长度
     */
    public static final int HEADER_FIELD_LENGTH = 2;

    /**
     * 定义最大帧长度，防止过大的消息导致内存溢出
     * 这里设置为1MB，根据实际情况可以调整
     */
    public static final int MAX_FRAME_LENGTH = 1024 * 1024;

    /**
     * 版本号字段的长度，用于序列化和反序列化时确定版本号的字节长度
     */
    public static final int VERSION_LENGTH = 1;


    /**
     * 报文总长度字段的长度，用于序列化和反序列化时确定报文总长度的字节长度
     */
    public static final int FULL_FIELD_LENGTH = 4;
}
