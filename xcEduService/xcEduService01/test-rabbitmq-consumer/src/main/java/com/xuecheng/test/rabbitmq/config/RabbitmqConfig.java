package com.xuecheng.test.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    //队列名称
    public static final String QUEUE_INFORM_EMAIL = "queue_email";
    public static final String QUEUE_INFORM_SMS = "queue_msg";
    //路由名称
    public static final String TOPICS = "topics";
    //routingkey名称
    public static final String ROUTINGK_MSG = "com.xuecheng.test.#.msg.#";
    public static final String ROUTINGKEY_EMAIL = "com.xuecheng.test.#.email.#";

    //声明交换机
    @Bean(TOPICS)
    public Exchange TOPICS() {
        //交换机持久化
        return ExchangeBuilder.topicExchange(TOPICS).durable(true).build();
    }

    //声明队列
    @Bean(QUEUE_INFORM_EMAIL)
    public Queue QUEUE_INFORM_EMAIL() {
        return new Queue(QUEUE_INFORM_EMAIL);
    }

    @Bean(QUEUE_INFORM_SMS)
    public Queue QUEUE_INFORM_SMS() {
        return new Queue(QUEUE_INFORM_SMS);
    }

    //绑定交换机和队列
    @Bean
    public Binding BINGDING_MSG(@Qualifier(QUEUE_INFORM_SMS) Queue queue
            , @Qualifier(TOPICS) Exchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTINGK_MSG).noargs();
    }

    @Bean
    public Binding BINGDING_EMAIL(@Qualifier(QUEUE_INFORM_EMAIL) Queue queue
            , @Qualifier(TOPICS) Exchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_EMAIL).noargs();
    }
}
