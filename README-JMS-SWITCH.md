# 在Spring JMS和原始JMS API之间切换

本项目提供了两种JMS实现方式：
1. 基于Spring JMS的高级抽象实现
2. 基于底层JMS API的原始实现

你可以通过简单的配置来选择使用哪种实现方式。

## 配置方法

在`application-local.properties`文件中，有一个关键配置项：

```properties
# JMS实现选择
# 可选值: spring, raw
jms.implementation=spring
```

- 当设置为`spring`时（默认值），系统使用Spring JMS的高级抽象
- 当设置为`raw`时，系统使用底层JMS API的实现

## 系统行为

### Spring JMS实现（默认）

当`jms.implementation=spring`时：

1. `@JmsListener`注解的组件会被激活
2. 使用`JmsTemplate`的服务实现会被激活
3. 底层JMS API的组件不会被实例化

这是最简单、最直观的实现方式，推荐在大多数场景下使用。

### 原始JMS API实现

当`jms.implementation=raw`时：

1. `RawJmsService`和相关的底层JMS组件会被激活
2. 使用原始JMS API的监听器和发送器会被激活
3. Spring JMS的组件不会被实例化

这种实现方式提供了更多的灵活性和控制力，但同时也需要更多的代码和更复杂的错误处理。

## 修改配置的位置

配置文件位置：
- `order-system/src/main/resources/application-local.properties`（订单系统配置）
- `logistics-system/src/main/resources/application-local.properties`（物流系统配置）

由于项目使用了Spring profiles，实际上是`application-local.properties`文件被加载（而不是通用的`application.properties`），因为`spring.profiles.active=local`。

**注意**：为了确保系统行为一致，建议在所有配置文件中使用相同的`jms.implementation`值。

## 重启应用

修改配置后，需要重启应用才能生效：

```bash
# 使用Spring JMS
# 1. 修改配置文件中的jms.implementation=spring
# 2. 重启应用
mvn spring-boot:run

# 使用原始JMS API
# 1. 修改配置文件中的jms.implementation=raw
# 2. 重启应用
mvn spring-boot:run
```

## 注意事项

1. 由于ActiveMQ队列是点对点模式，每个消息只能被一个消费者处理，所以不能同时激活两种实现方式
2. 系统中使用条件注解`@ConditionalOnProperty`确保只有一种实现方式被激活
3. 当切换实现方式时，业务逻辑保持不变，只是消息传递的底层机制不同
4. 对于学习和调试目的，可以查看日志了解不同实现的行为差异 