package com.xuecheng.manage_cms.config;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {
    //配置交换机的名称
    public static final  String EX_ROTING_CMS_POSTPAGE="ex_routing_cms_postpage";
    /**
     * 交换机工作模式
     */
    @Bean(EX_ROTING_CMS_POSTPAGE)
    public Exchange EXCHANGE_TOPICS_INFORM(){
        Exchange build = ExchangeBuilder.directExchange(EX_ROTING_CMS_POSTPAGE).durable(true).build();
        return build;
    }
}
