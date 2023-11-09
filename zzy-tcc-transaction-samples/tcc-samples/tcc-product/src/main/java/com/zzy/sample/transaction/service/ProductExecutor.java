package com.zzy.sample.transaction.service;

import com.zzy.jdbc.JdbcExecutor;
import com.zzy.jdbc.holder.ResourceManager;
import com.zzy.sample.transaction.entity.Product;
import org.springframework.stereotype.Service;

@Service
public class ProductExecutor extends JdbcExecutor<Product> {

    private String deduceSql = " update product set stock = stock - 1 where id = ?";

    private String addSql = " update product set stock = stock + 1 where id = ?";

    public void add(long id) throws Exception {
        this.executeUpdate(ResourceManager.getTransactionConnection(), addSql, id);
    }


    public void deduce(long id) throws Exception {
        this.executeUpdate(ResourceManager.getTransactionConnection(), deduceSql, id);
    }

}
