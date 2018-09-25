package com.debug.steadyjack.service;

import com.debug.steadyjack.entity.Product;
import com.debug.steadyjack.entity.ProductRobbingRecord;
import com.debug.steadyjack.mapper.ProductMapper;
import com.debug.steadyjack.mapper.ProductRobbingRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by steadyjack on 2018/8/24.
 */
@Service
public class ConcurrencyService {

    private static final Logger log= LoggerFactory.getLogger(ConcurrencyService.class);

    private static final String ProductNo="a123";

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductRobbingRecordMapper productRobbingRecordMapper;


    /**
     * 处理抢单
     * @param mobile
     */
    public void manageProduct(String mobile){
            try {
                //log.info("当前手机号： {} ",mobile);

                //TODO：查询存库->发送短信给用户,更新库存
                /*Product product=productMapper.selectByProductNo(ProductNo);
                if (product!=null && product.getTotal()>0){
                    sendRobbingResult(mobile,true,product.getId());

                    productMapper.updateTotal(product);
                }else{
                    sendRobbingResult(mobile,false,product.getId());
                }*/ //--v1.0


                //+v2.0
                Product p=productMapper.selectByProductNo(ProductNo);
                if (p!=null && p.getTotal()>0){
                    int i=productMapper.updateTotal(p);
                    if (i>0){
                        ProductRobbingRecord record=new ProductRobbingRecord();
                        record.setMobile(mobile);
                        record.setProductId(p.getId());
                        productRobbingRecordMapper.insertSelective(record);
                    }
                }

            }catch (Exception e){
                log.error("处理抢单 发生异常；{} ",mobile,e.fillInStackTrace());
        }
    }


}



























