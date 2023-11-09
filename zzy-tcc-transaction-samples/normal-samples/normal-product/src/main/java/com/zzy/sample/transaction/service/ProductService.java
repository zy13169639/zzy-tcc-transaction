package com.zzy.sample.transaction.service;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import com.zzy.dt.annotation.DistributeTransaction;
import com.zzy.dt.annotation.InTransactional;
import com.zzy.dt.annotation.RejectRepeatable;
import com.zzy.dt.enums.InTransactionalWorker;
import com.zzy.jdbc.holder.ResourceManager;
import com.zzy.sample.transaction.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Service
@Transactional
public class ProductService {

    @Autowired
    ProductExecutor productExecutor;

    @Autowired
    RestTemplate restTemplate;

    public Boolean normal(long id) throws Exception {
        productExecutor.deduce(id);
        int i = new Random().nextInt(10) + 1;
        return restTemplate.getForObject("http://localhost:8083/point/add?uid=" + i, Boolean.class);
    }


    public Object exception(long id) throws Exception {
        productExecutor.deduce(id);
        throw new RuntimeException("test");
    }

    public void confirm(long id) throws Exception {
        //nothing
        System.out.println("product confirm... ");
    }


    public void cancel(long id) throws Exception {
        productExecutor.add(id);
    }

}
