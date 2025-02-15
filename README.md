# JianRPC - 轻量级分布式RPC框架

📢 一个基于Netty + ZooKeeper实现的Java RPC框架，具备服务注册发现、负载均衡、心跳检测等核心功能，适用于分布式系统服务化调用

## 🌟 核心特性
1. **服务治理**  
   - 基于ZooKeeper实现服务自动注册与发现
   - 支持多节点服务注册与健康监测
   - 服务元数据管理（版本/分组/权重）

2. **通信协议**  
   - 自定义二进制协议（魔数校验+版本控制）
   - 支持请求类型区分（普通请求/心跳检测）
   - 报文压缩与校验机制

3. **核心机制**  
   - JDK动态代理实现透明化调用
   - CompletableFuture实现异步回调
   - 基于Netty的NIO长连接通信
   - LengthFieldBasedFrameDecoder解决粘包问题

4. **扩展设计**  
   - SPI机制支持序列化扩展
   - 策略模式实现负载均衡
   - 工厂模式管理连接池

## 🛠️ 技术矩阵
| 领域            | 技术选型                          |
|----------------|---------------------------------|
| 网络通信        | Netty4.x + NIO长连接             | 
| 注册中心        | ZooKeeper 3.7                   |
| 动态代理        | JDK Proxy                       |
| 序列化          | JDK原生序列化                    |
| 异步机制        | CompletableFuture + Callback    |
| 开发工具        | Lombok + SLF4J                  |

## 🚀 快速体验
### 服务提供方
```java
public class ProviderBootstrap {
    public static void main(String[] args) {
        ServiceConfig<HelloService> service = new ServiceConfig<>();
        service.setInterface(HelloService.class) 
               .setRef(new HelloServiceImpl());
        
        RpcBootstrap.getInstance() 
            .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
            .protocol(new ProtocolConfig("jdk", 8080))
            .publish(service)
            .start();
    }
}
