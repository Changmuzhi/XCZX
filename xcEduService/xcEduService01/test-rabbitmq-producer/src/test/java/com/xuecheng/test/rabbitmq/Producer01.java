package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


//rabbitmq的入门程序
public class Producer01 {
    //队列名称
    private static final String QUEUE = "key";

    public static void main(String[] args) {
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
        Connection connection = null;
        Channel channel = null;
        try {
            //新建连接
            connection = connectionFactory.newConnection();
            //创建会话通道,生产者和mq服务所有通信都在channel通道中完成
            channel = connection.createChannel();
            /**
             * 声明队列，如果Rabbit中没有此队列将自动创建
             * param1:队列名称
             * param2:是否持久化
             * param3:队列是否独占此连接
             * param4:队列不再使用时是否自动删除此队列
             * param5:队列参数
             */
            channel.queueDeclare(QUEUE, true, false, false, null);
            /**
             * 消息发布方法
             * param1：Exchange的名称，如果没有指定，则使用Default Exchange
             * param2:routingKey,消息的路由Key，是用于Exchange（交换机）将消息转发到指定的消息队列
             * param3:消息包含的属性
             * param4：消息体
             */
            String message = "hello world";
            channel.basicPublish("", QUEUE, null, message.getBytes());
            //System.out.println(message);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
