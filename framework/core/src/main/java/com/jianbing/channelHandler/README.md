1、服务调用方

发送报文 writeAndFlush(Object) 请求
pipeline就生效了,报文开始出站
---> 第一个处理器(in/out) log 打印日志
---> 第二个处理器(out) 编码器 (转化rpcRequest -> msg(请求报文)、序列化、压缩)

2、服务提供方

通过netty接受请求报文
---> 第一个处理器(in/out) log 打印日志
---> 第二个处理器(in) 解码器 (解压缩、反序列化、msg->rpcRequest)
---> 想办法处理(in) rpcRequest 执行方法调用，得到结果


3、执行方法调用，得到结果


4、服务提供方

发送报文 writeAndFlush(Object) 响应

pipeline就生效了,报文开始出站

---> 第一个处理器(out) (转化Object -> msg(响应报文))
---> 第二个处理器(out) (序列化)
---> 第三个处理器(out) (压缩)

5、服务调用方

通过netty接受响应报文
---> 第一个处理器(in) (解压缩)
---> 第二个处理器(in) (反序列化)
---> 第三个处理器(in) (解析报文)

6、得到结果