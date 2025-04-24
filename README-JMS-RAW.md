# 底层JMS API使用指南

本项目提供了两种不同的消息队列实现：
1. 基于Spring JMS的高级抽象实现
2. 基于底层JMS API的原始实现

本文档将介绍基于底层JMS API的实现，帮助你理解JMS的底层工作原理。

## 核心组件

### 公共模块

- **RawJmsService**: 底层JMS服务，提供了与ActiveMQ交互的核心功能
  - 使用ConnectionFactory、Connection、Session等底层API
  - 提供了发送对象消息和文本消息的方法
  - 提供了同步和异步接收消息的功能
  - 支持消息监听器注册

### 物流系统

- **RawJmsLogisticsListener**: 使用底层JMS API的物流系统消息监听器
  - 监听订单创建队列
  - 处理新订单消息

- **RawJmsLogisticsSender**: 使用底层JMS API的物流系统消息发送服务
  - 发送物流状态更新到订单系统

- **RawJmsLogisticsServiceImpl**: 使用底层JMS API的物流业务服务实现
  - 处理订单发货逻辑
  - 通过RawJmsLogisticsSender发送消息

### 订单系统

- **RawJmsOrderListener**: 使用底层JMS API的订单系统消息监听器
  - 监听物流状态更新队列
  - 处理状态更新消息

- **RawJmsOrderSender**: 使用底层JMS API的订单系统消息发送服务
  - 发送新订单通知到物流系统

- **RawJmsOrderServiceImpl**: 使用底层JMS API的订单业务服务实现
  - 处理订单创建逻辑
  - 通过RawJmsOrderSender发送消息

## 底层JMS API工作原理

### 连接建立

```java
// 创建连接工厂
ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(username, password, brokerUrl);

// 创建连接
Connection connection = connectionFactory.createConnection();
connection.start();

// 创建会话（不使用事务，使用自动确认模式）
Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
```

### 发送消息

```java
// 创建目标队列
Queue queue = session.createQueue(queueName);

// 创建生产者
MessageProducer producer = session.createProducer(queue);
producer.setDeliveryMode(DeliveryMode.PERSISTENT); // 设置持久化模式

// 创建消息
ObjectMessage objectMessage = session.createObjectMessage(message);
// 或
TextMessage textMessage = session.createTextMessage(message);

// 发送消息
producer.send(objectMessage);
```

### 接收消息

#### 同步接收

```java
// 创建消费者
MessageConsumer consumer = session.createConsumer(queue);

// 同步接收消息（带超时）
Message message = consumer.receive(timeout);

// 处理消息
if (message instanceof ObjectMessage) {
    ObjectMessage objectMessage = (ObjectMessage) message;
    Object object = objectMessage.getObject();
    // 处理对象...
}
```

#### 异步接收（使用监听器）

```java
// 创建消费者
MessageConsumer consumer = session.createConsumer(queue);

// 设置消息监听器
consumer.setMessageListener(message -> {
    // 处理消息
    if (message instanceof ObjectMessage) {
        try {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Object object = objectMessage.getObject();
            // 处理对象...
        } catch (JMSException e) {
            // 处理异常
        }
    }
});
```

### 资源清理

```java
// 关闭生产者
producer.close();

// 关闭消费者
consumer.close();

// 关闭会话
session.close();

// 关闭连接
connection.close();
```

## 与Spring JMS对比

| 特性 | 底层JMS API | Spring JMS |
|------|-------------|------------|
| 复杂度 | 高，需要手动管理连接和会话 | 低，Spring提供了高级抽象 |
| 灵活性 | 高，可以控制所有JMS特性 | 中，使用Spring的约定配置 |
| 代码量 | 多，需要处理所有细节 | 少，使用注解和模板简化 |
| 异常处理 | 复杂，需要处理所有JMS异常 | 简单，Spring提供了统一的异常转换 |
| 资源管理 | 手动，需要小心关闭所有资源 | 自动，Spring管理资源生命周期 |
| 事务支持 | 手动配置和管理 | 与Spring事务管理集成 |

## 使用场景

底层JMS API适用于以下场景：
1. 需要对JMS连接和会话进行精细控制
2. 需要使用Spring JMS未提供的特殊JMS功能
3. 学习和理解JMS的底层工作原理
4. 在不使用Spring框架的环境中使用JMS 

## ActiveMQ序列化安全限制

使用原始JMS API时，需要特别注意ActiveMQ的序列化安全限制。默认情况下，ActiveMQ不允许序列化任何类，除非它们被明确添加到信任包列表中。

### 问题表现

如果遇到类似以下的错误：

```
javax.jms.JMSException: Failed to build body from content. Serializable class not available to broker. 
Reason: java.lang.ClassNotFoundException: Forbidden class java.time.Ser! 
This class is not trusted to be serialized as ObjectMessage payload.
```

这表示某个类（在这个例子中是`java.time.Ser`）不在ActiveMQ的信任列表中。

### 解决方案

1. **配置信任包**
   
   ```java
   ((ActiveMQConnectionFactory) connectionFactory).setTrustedPackages(
       Arrays.asList(
           "com.example.common.dto",
           "com.example.common.model",
           "java.util",
           "java.lang",
           "java.math",
           "java.time"  // 添加所需包
       )
   );
   ```

2. **完全禁用安全检查（不推荐用于生产环境）**

   ```java
   ((ActiveMQConnectionFactory) connectionFactory).setTrustAllPackages(true);
   ```

3. **使用替代序列化方式**
   
   可以考虑使用JSON或其他序列化格式代替Java原生序列化，这通常也能提高性能和减小消息大小。

在本项目中，我们已经在`RawJmsService`中添加了适当的包到信任列表。如果添加新的域对象，可能需要更新这个列表。 