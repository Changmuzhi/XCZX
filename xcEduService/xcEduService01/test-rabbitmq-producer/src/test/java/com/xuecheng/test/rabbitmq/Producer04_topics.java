package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


//rabbitmq的入门程序
public class Producer04_topics {
    //队列名称
    private static final String QUEUE_INFORM_EMAIL = "queue_email";
    private static final String QUEUE_INFORM_SMS = "queue_msg";
    //路由名称
    private static final String TOPICS = "topics";
    //routingkey名称
    private static final String ROUTINGK_MSG = "com.xuecheng.test.#.msg.#";
    private static final String ROUTINGKEY_EMAIL = "com.xuecheng.test.#.email.#";

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
            channel.queueDeclare(QUEUE_INFORM_EMAIL, true, false, false, null);
            channel.queueDeclare(QUEUE_INFORM_SMS, true, false, false, null);


            /**
             * 声明一个交换机
             * param1:交换机的名称
             * param2:交换机的类型
             * fanout: 对应的rabbitmq的工作模式是publish/subscribe
             * direct:对应的Routing工作模式
             * topic:对应的Topics工作模式
             * headers:对应的headers工作模式
             */
            channel.exchangeDeclare(TOPICS, BuiltinExchangeType.TOPIC);


            /**
             * 交换机和队列绑定
             * 参数明细:
             * param1: 队列名称
             * param2: 交换机的名称
             * param3: 路由Key，在发布订阅模式中为空串
             * */
            channel.queueBind(QUEUE_INFORM_EMAIL, TOPICS, ROUTINGKEY_EMAIL);
            channel.queueBind(QUEUE_INFORM_SMS, TOPICS, ROUTINGK_MSG);

            /**
             * 消息发布方法
             * param1：Exchange的名称，如果没有指定，则使用Default Exchange，指定发送消息的交换机
             * param2:routingKey,消息的路由Key，是用于Exchange（交换机）将消息转发到指定的消息队列
             * param3:消息包含的属性
             * param4：消息体
             */
            String message = "topic key yes!";
            //channel.basicPublish(TOPICS, "com.xuecheng.test.email", null, message.getBytes());
            channel.basicPublish(TOPICS, "com.xuecheng.test.email.msg", null, message.getBytes());


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭队列连接
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            try {
                //关闭mq连接
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
