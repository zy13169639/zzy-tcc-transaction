package com.zzy.sample.transaction.service;

import com.zzy.dt.annotation.DistributeTransaction;
import com.zzy.dt.annotation.InTransactional;
import com.zzy.dt.enums.InTransactionalWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    @Autowired
    ProductExecutor productExecutor;

    @DistributeTransaction(commitMethod = "confirm", rollbackMethod = "cancel")
    @InTransactional(InTransactionalWorker.BUSINESS_SUCCESS)
    public Boolean normal(long id) throws Exception {
        productExecutor.deduce(id);
        return true;
    }


    @InTransactional(InTransactionalWorker.BUSINESS_SUCCESS)
    @DistributeTransaction(commitMethod = "confirm", rollbackMethod = "cancel")
    public Object exception(long id) throws Exception {
        productExecutor.deduce(id);
        throw new RuntimeException("test");
    }

    public void confirm(long id) throws Exception {
        //nothing
    }


    public void cancel(long id) throws Exception {
        productExecutor.add(id);
    }

}
