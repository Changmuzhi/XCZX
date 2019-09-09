package com.xuecheng.test.rabbitmq;


import com.xuecheng.test.rabbitmq.config.RabbitmqConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class Producer05_topics_springboot {

    @Autowired
    RabbitTemplate rabbitTemplate;

    //使用rabbitTempalte发送消息
    @Test
    public void testSenEamil(){
        /**
         * 1    交换机名称
         * 2    routingKey
         * 3    消息
         */
        rabbitTemplate.convertAndSend(RabbitmqConfig.TOPICS,"com.xuecheng.test.email","牛逼奥");
    }

}
