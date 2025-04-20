# 订单物流系统

基于ActiveMQ消息队列的订单物流系统，包含订单系统和物流系统两个子系统。

## 功能特点

### 订单系统
- 添加订单：可添加多个商品，包含商品名称、价格和数量
- 查看订单列表：显示所有订单信息，包括订单状态（已发货/未发货）
- 当创建订单时，自动发送消息到物流系统
- 当物流系统发货后，会收到消息更新订单状态

### 物流系统
- 显示所有待发货订单
- 执行发货操作
- 发货后自动通知订单系统更新状态

## 技术栈

- Java 11
- Spring Boot 2.7.3
- Spring JMS
- ActiveMQ 5.17.3
- H2数据库（内存模式）
- Thymeleaf
- CSS（苹果风格）

## 系统架构

系统分为三个模块：
- `common`: 公共模块，包含共享的DTO、配置和常量
- `order-system`: 订单系统模块
- `logistics-system`: 物流系统模块

两个系统通过ActiveMQ消息队列进行通信。

## 运行说明

### 前提条件

- JDK 11+
- Maven 3.6+
- ActiveMQ 5.17.3+ （需要单独安装）

### 安装ActiveMQ

1. 下载ActiveMQ 5.17.3：https://activemq.apache.org/components/classic/download/
2. 解压后进入bin目录
3. 运行：
   - Windows: `activemq.bat start`
   - Linux/Mac: `./activemq start`
4. 访问控制台：http://localhost:8161/admin (用户名/密码: admin/admin)

### 构建和运行

1. 构建项目：
   ```
   mvn clean install
   ```

2. 启动订单系统（端口8080）：
   ```
   cd order-system
   mvn spring-boot:run
   ```

3. 在另一个终端启动物流系统（端口8081）：
   ```
   cd logistics-system
   mvn spring-boot:run
   ```

4. 访问系统：
   - 订单系统：http://localhost:8080/orders
   - 物流系统：http://localhost:8081/logistics

## 消息队列配置

消息队列配置在application.properties中，可以根据需要修改：

```properties
# ActiveMQ配置
activemq.broker-url=tcp://localhost:61616
activemq.user=admin
activemq.password=admin
```

## 示例流程

1. 在订单系统中创建新订单
2. 订单会自动出现在物流系统的待发货列表中
3. 在物流系统中点击"发货"按钮
4. 物流系统会发送状态更新消息到订单系统
5. 刷新订单系统，可以看到订单状态已更新为"已发货"