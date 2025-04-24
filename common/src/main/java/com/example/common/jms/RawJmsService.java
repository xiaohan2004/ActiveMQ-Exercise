package com.example.common.jms;

import com.example.common.constants.JmsConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 使用底层JMS API进行消息收发的服务
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "jms.implementation", havingValue = "raw")
public class RawJmsService {

    @Value("${activemq.broker-url:tcp://localhost:61616}")
    private String brokerUrl;

    @Value("${activemq.user:admin}")
    private String username;

    @Value("${activemq.password:admin}")
    private String password;

    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Map<String, MessageProducer> producers = new HashMap<>();
    private Map<String, MessageConsumer> consumers = new ConcurrentHashMap<>();
    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        try {
            // 创建连接工厂
            connectionFactory = new org.apache.activemq.ActiveMQConnectionFactory(username, password, brokerUrl);
            
            // 设置信任的包
            if (connectionFactory instanceof org.apache.activemq.ActiveMQConnectionFactory) {
                // 配置ActiveMQ允许序列化的类包列表
                // 这是一个安全限制，只有这些包中的类才能被序列化为JMS消息
                ((org.apache.activemq.ActiveMQConnectionFactory) connectionFactory).setTrustedPackages(
                    java.util.Arrays.asList(
                        "com.example.common.dto",  // 项目DTO类
                        "com.example.common.model", // 项目模型类
                        "java.util",  // Java工具类
                        "java.lang",  // Java基本类
                        "java.math",  // Java数学类
                        "java.time"   // Java时间类（LocalDate, LocalDateTime等）
                    )
                );
                
                // 另一种方案：完全禁用信任包检查（在开发环境中可以使用，生产环境不推荐）
                // ((org.apache.activemq.ActiveMQConnectionFactory) connectionFactory).setTrustAllPackages(true);
            }
            
            // 创建连接
            connection = connectionFactory.createConnection();
            connection.start();
            
            // 创建会话（不使用事务，使用自动确认模式）
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            // 创建线程池用于处理消息
            executorService = Executors.newFixedThreadPool(5);
            
            log.info("RawJmsService 初始化完成，已连接到 {}", brokerUrl);
        } catch (JMSException e) {
            log.error("初始化JMS连接失败", e);
            throw new RuntimeException("无法初始化JMS连接", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info("关闭JMS连接和资源");
        try {
            // 关闭所有生产者
            for (MessageProducer producer : producers.values()) {
                try {
                    producer.close();
                } catch (JMSException e) {
                    log.warn("关闭消息生产者时出错", e);
                }
            }
            
            // 关闭所有消费者
            for (MessageConsumer consumer : consumers.values()) {
                try {
                    consumer.close();
                } catch (JMSException e) {
                    log.warn("关闭消息消费者时出错", e);
                }
            }
            
            // 关闭会话和连接
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
            
            // 关闭线程池
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
        } catch (JMSException e) {
            log.error("关闭JMS资源时出错", e);
        }
    }

    /**
     * 发送对象消息到指定队列
     * 
     * @param queueName 队列名称
     * @param message 消息对象（必须实现Serializable接口）
     */
    public void sendObjectMessage(String queueName, Serializable message) {
        try {
            // 获取或创建生产者
            MessageProducer producer = getOrCreateProducer(queueName);
            
            // 创建对象消息
            ObjectMessage objectMessage = session.createObjectMessage(message);
            
            // 发送消息
            producer.send(objectMessage);
            log.info("已发送消息到队列 {}: {}", queueName, message);
        } catch (JMSException e) {
            log.error("发送消息到队列 {} 失败", queueName, e);
            throw new RuntimeException("发送消息失败", e);
        }
    }

    /**
     * 发送文本消息到指定队列
     * 
     * @param queueName 队列名称
     * @param message 文本消息
     */
    public void sendTextMessage(String queueName, String message) {
        try {
            // 获取或创建生产者
            MessageProducer producer = getOrCreateProducer(queueName);
            
            // 创建文本消息
            TextMessage textMessage = session.createTextMessage(message);
            
            // 发送消息
            producer.send(textMessage);
            log.info("已发送文本消息到队列 {}: {}", queueName, message);
        } catch (JMSException e) {
            log.error("发送文本消息到队列 {} 失败", queueName, e);
            throw new RuntimeException("发送文本消息失败", e);
        }
    }

    /**
     * 注册一个消息监听器，使用异步方式处理消息
     * 
     * @param queueName 队列名称
     * @param messageHandler 消息处理函数
     */
    public <T extends Serializable> void registerListener(String queueName, Consumer<T> messageHandler) {
        try {
            // 创建目标队列
            Queue queue = session.createQueue(queueName);
            
            // 创建消费者
            MessageConsumer consumer = session.createConsumer(queue);
            consumers.put(queueName, consumer);
            
            // 设置消息监听器
            consumer.setMessageListener(message -> {
                executorService.submit(() -> {
                    try {
                        if (message instanceof ObjectMessage) {
                            ObjectMessage objectMessage = (ObjectMessage) message;
                            @SuppressWarnings("unchecked")
                            T object = (T) objectMessage.getObject();
                            log.info("从队列 {} 接收到消息: {}", queueName, object);
                            messageHandler.accept(object);
                        } else {
                            log.warn("从队列 {} 接收到不支持的消息类型: {}", queueName, message.getClass().getName());
                        }
                    } catch (JMSException e) {
                        log.error("处理来自队列 {} 的消息时出错", queueName, e);
                    } catch (ClassCastException e) {
                        log.error("消息类型转换错误: {}", e.getMessage());
                    }
                });
            });
            
            log.info("已为队列 {} 注册消息监听器", queueName);
        } catch (JMSException e) {
            log.error("为队列 {} 注册消息监听器失败", queueName, e);
            throw new RuntimeException("注册消息监听器失败", e);
        }
    }

    /**
     * 接收来自指定队列的单个消息，同步方式
     * 
     * @param queueName 队列名称
     * @param timeout 超时时间（毫秒）
     * @return 接收到的消息对象，如果超时则返回null
     */
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T receiveMessage(String queueName, long timeout) {
        try {
            // 创建目标队列
            Queue queue = session.createQueue(queueName);
            
            // 创建临时消费者
            MessageConsumer consumer = session.createConsumer(queue);
            try {
                // 等待接收消息，带超时时间
                Message message = consumer.receive(timeout);
                
                if (message != null && message instanceof ObjectMessage) {
                    ObjectMessage objectMessage = (ObjectMessage) message;
                    T object = (T) objectMessage.getObject();
                    log.info("从队列 {} 同步接收到消息: {}", queueName, object);
                    return object;
                } else if (message != null) {
                    log.warn("从队列 {} 接收到不支持的消息类型: {}", queueName, message.getClass().getName());
                } else {
                    log.info("从队列 {} 接收消息超时", queueName);
                }
                return null;
            } finally {
                // 手动关闭消费者
                consumer.close();
            }
        } catch (JMSException e) {
            log.error("从队列 {} 接收消息失败", queueName, e);
            throw new RuntimeException("接收消息失败", e);
        }
    }

    /**
     * 获取或创建消息生产者
     */
    private MessageProducer getOrCreateProducer(String queueName) throws JMSException {
        if (producers.containsKey(queueName)) {
            return producers.get(queueName);
        }
        
        // 创建目标队列
        Queue queue = session.createQueue(queueName);
        
        // 创建生产者
        MessageProducer producer = session.createProducer(queue);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT); // 设置持久化模式
        
        producers.put(queueName, producer);
        return producer;
    }
} 