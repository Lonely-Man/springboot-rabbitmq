package com.debug.steadyjack.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Created by steadyjack on 2018/8/24.
 */
@Service
public class CommonMqService {

    private static final Logger log= LoggerFactory.getLogger(CommonMqService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    @Autowired
    private ObjectMapper objectMapper;


    /**
     * mq队列处理请求
     * @param mobile
     */
    public void mqProductRobbing(String mobile){
        try {
            rabbitTemplate.setExchange(env.getProperty("product.robbing.exchange.name"));
            rabbitTemplate.setRoutingKey(env.getProperty("product.robbing.routing.key.name"));

            Message message=MessageBuilder.withBody(mobile.getBytes())
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .build();
            rabbitTemplate.convertAndSend(message);
        }catch (Exception e){
            log.error("mq队列处理请求 发生异常：mobile={} ",mobile,e.fillInStackTrace());
        }
    }


}


























