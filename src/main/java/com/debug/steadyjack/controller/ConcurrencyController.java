package com.debug.steadyjack.controller;

import com.debug.steadyjack.response.BaseResponse;
import com.debug.steadyjack.response.StatusCode;
import com.debug.steadyjack.service.ConcurrencyService;
import com.debug.steadyjack.service.InitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by steadyjack on 2018/8/24.
 */
@RestController
public class ConcurrencyController {

    private static final Logger log= LoggerFactory.getLogger(HelloWorldController.class);

    private static final String Prefix="concurrency";

    @Autowired
    private ConcurrencyService concurrencyService;

    @Autowired
    private InitService initService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;
    private static int mobile=0;


    @RequestMapping(value = Prefix+"/product",method = RequestMethod.GET)
    public BaseResponse productController(){
        BaseResponse response=new BaseResponse(StatusCode.Success);
        try {
                //TODO：流程->查商品对应的库存量->有->手机短信或者其他方式提示客户成功,同时更新库存量->无则提示

                //TODO：问题1：并发导致数据库数据不一致->更新的时候，前几秒查到的数据已经在用了,导致更新完库存变为负数
                //TODO：问题2：并发导致提示客户信息不对：有->但是更新时变为负数了!(由问题1产生的)

                //TODO：优化1：限流，明面上是是高并发，但是处理时排队
                //TODO：优化2：数据库表的更新限定逻辑-并根据更新结果再来提示客户抢单成功( a.total>0 或者 a.total=total)

            response.setMsg("成功");
               response.setMsg(String.format("您已完成抢单，稍后请留意手机消息，我们会将抢单结果发送到您绑定的手机: %s !",mobile));
                concurrencyService.manageProduct(mobile+"");

                initService.generateMultiThread();

        }catch (Exception e){
            log.error("商品抢单发生异常：",e.fillInStackTrace());
        }
        return response;
    }

    /**
     *
     */
    @RequestMapping(value = Prefix+"/mq/send",method = RequestMethod.GET)
    public BaseResponse mqSendInfo(){
        BaseResponse response=new BaseResponse(StatusCode.Success);
        try {
            rabbitTemplate.setExchange(env.getProperty("basic.info.exchange.name"));
            rabbitTemplate.setRoutingKey(env.getProperty("basic.info.routing.key.name"));

            String str="这是消息";
            Message message=MessageBuilder.withBody(str.getBytes("UTF-8")).build();
            rabbitTemplate.send(message);
        }catch (Exception e){
            log.error("发送mq：",e.fillInStackTrace());
        }
        return response;
    }


}
































































