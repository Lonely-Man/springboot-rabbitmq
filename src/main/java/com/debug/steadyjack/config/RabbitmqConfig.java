package com.debug.steadyjack.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by steadyjack on 2018/8/20.
 */
@Configuration
public class RabbitmqConfig {

    private static final Logger log= LoggerFactory.getLogger(RabbitmqConfig.class);

    @Autowired
    private Environment env;

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Autowired
    private SimpleRabbitListenerContainerFactoryConfigurer factoryConfigurer;

    /**
     * 单一消费者
     * @return
     */
    @Bean(name = "singleListenerContainer")
    public SimpleRabbitListenerContainerFactory listenerContainer(){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setPrefetchCount(1);
        factory.setTxSize(1);
        return factory;
    }

    /**
     * 多个消费者
     * @return
     */
    @Bean(name = "multiListenerContainer")
    public SimpleRabbitListenerContainerFactory multiListenerContainer(){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factoryConfigurer.configure(factory,connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.NONE);
        factory.setConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.concurrency",int.class));
        factory.setMaxConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.max-concurrency",int.class));
        factory.setPrefetchCount(env.getProperty("spring.rabbitmq.listener.prefetch",int.class));
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(){
        connectionFactory.setPublisherConfirms(true);
        connectionFactory.setPublisherReturns(true);
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("消息发送成功:correlationData({}),ack({}),cause({})",correlationData,ack,cause);
            }
        });
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                log.info("消息丢失:exchange({}),route({}),replyCode({}),replyText({}),message:{}",exchange,routingKey,replyCode,replyText,message);
            }
        });
        return rabbitTemplate;
    }


    //TODO：RabbitMQ基本消息模型配置实战
    @Bean
    public DirectExchange basicExchange(){
        return new DirectExchange(env.getProperty("basic.info.exchange.name"),true,false);
    }

    @Bean(name = "basicQueue")
    public Queue basicQueue(){
        return new Queue(env.getProperty("basic.info.queue.name"),true);
    }

    @Bean
    public Binding basicBinding(){
        return BindingBuilder.bind(basicQueue()).to(basicExchange()).with(env.getProperty("basic.info.routing.key.name"));
    }

















    //TODO：抢单队列配置
    @Bean
    public DirectExchange productRobbingExchange(){
        return new DirectExchange(env.getProperty("product.robbing.exchange.name"),true,false);
    }

    @Bean(name = "productRobbingQueue")
    public Queue productRobbingQueue(){
        return new Queue(env.getProperty("product.robbing.queue.name"),true);
    }

    @Bean
    public Binding productRobbingBinding(){
        return BindingBuilder.bind(productRobbingQueue()).to(productRobbingExchange()).with(env.getProperty("product.robbing.routing.key.name"));
    }
















    //TODO：抢单死信队列配置
    @Bean
    public Queue prodDeadQueue(){
        Map<String, Object> args=new HashMap();
        args.put("x-dead-letter-exchange", env.getProperty("prod.dead.letter.exchange.name"));
        args.put("x-dead-letter-routing-key", env.getProperty("prod.dead.letter.routing.key.name"));
        args.put("x-message-ttl", 2000);

        return new Queue(env.getProperty("prod.dead.letter.queue.name"),true, false, false, args);
    }



}


















