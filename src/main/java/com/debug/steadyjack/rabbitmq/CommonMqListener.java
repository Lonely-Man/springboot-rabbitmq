package com.debug.steadyjack.rabbitmq;

import com.debug.steadyjack.service.ConcurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Created by steadyjack on 2018/8/23.
 */
@Component
public class CommonMqListener {

    private static final Logger log= LoggerFactory.getLogger(CommonMqListener.class);

    @Autowired
    private ConcurrencyService concurrencyService;

    @Autowired
    private ObjectMapper objectMapper;


    /**
     * 基本消息测试
     * @param message
     */
    @RabbitListener(queues = "dd")
    public void mqDirectQueue(@Payload byte[] message){
        try {
            String result=new String(message,"utf-8");
            log.info("监听到消息： {} ",result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }






    /**
     * 处理抢单请求
     * @param message
     */
    @RabbitListener(queues = "${product.robbing.queue.name}",containerFactory = "multiListenerContainer")
    public void mqRobbingProductQueue(@Payload byte[] message){
        try {
            String result=new String(message,"UTF-8");
            concurrencyService.manageProduct(result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



}































