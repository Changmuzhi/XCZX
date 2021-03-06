package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.*;


import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer01 {
    //队列名称
    private static final String QUEUE = "key";
    public static void main(String[] args) throws IOException, TimeoutException {
        //和mq建立链接
        //通过链接工厂创建新的mq连接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        //设置服务器地址
        connectionFactory.setHost("127.0.0.1");
        //设置服务器端口
        connectionFactory.setPort(5672);
        //设置用户名与密码
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        //设置虚拟机，一个mq服务可以设置多个虚拟机，每一个虚拟机相当于一个独立的mq
        connectionFactory.setVirtualHost("/");
        //新建连接
        Connection connection = connectionFactory.newConnection();
        //创建会话通道
        Channel channel = connection.createChannel();
        //声明队列
        channel.queueDeclare(QUEUE,true,false,false,null);

        //实现消费方法
        DefaultConsumer defaultConsumer = new DefaultConsumer(channel){
            /**
             * 1 消费者标签
             * 2 信封
             * 3 消息属性
             * 4 消息内容
             */

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "utf-8");
                System.out.println(message);
            }
        };

        //监听队列
        /**
         * 1、队列名称
         * 2、自动回复
         * 3、消费方法
         */
        channel.basicConsume(QUEUE,true,defaultConsumer);

    }

}
