package com.zzy.sample.transaction.service;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import com.zzy.dt.annotation.DistributeTransaction;
import com.zzy.dt.annotation.InTransactional;
import com.zzy.dt.enums.InTransactionalWorker;
import com.zzy.jdbc.holder.ResourceManager;
import com.zzy.sample.transaction.entity.OrderTab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class OrderService {

    @Autowired
    OrderExecutor orderExecutor;

    @Autowired
    RestTemplate restTemplate;

    @DistributeTransaction(commitMethod = "confirm", rollbackMethod = "cancel")
    @InTransactional(InTransactionalWorker.BUSINESS_SUCCESS)
    public Object normal() throws Exception {
        OrderTab orderTbl = new OrderTab();
        orderTbl.setId(IdUtil.getSnowflakeNextId());
        orderTbl.setOrderKey(UUID.fastUUID().toString(true));
        orderExecutor.insert(ResourceManager.getTransactionConnection(), orderTbl);
        return restTemplate.getForObject("http://localhost:8082/product/deduce?id=1", Boolean.class);
    }


    @InTransactional(InTransactionalWorker.BUSINESS_SUCCESS)
    @DistributeTransaction(commitMethod = "confirm", rollbackMethod = "cancel")
    public Object exception() throws Exception {
        OrderTab orderTbl = new OrderTab();
        orderTbl.setId(IdUtil.getSnowflakeNextId());
        orderTbl.setOrderKey(UUID.fastUUID().toString(true));
        orderExecutor.insert(ResourceManager.getTransactionConnection(), orderTbl);
        throw new RuntimeException("test");
    }

    public void confirm() {
        System.out.println("order confirm...");
    }


    public void cancel() {
        restTemplate.getForObject("http://localhost:8082/product/add?id=1", Boolean.class);
    }

}
