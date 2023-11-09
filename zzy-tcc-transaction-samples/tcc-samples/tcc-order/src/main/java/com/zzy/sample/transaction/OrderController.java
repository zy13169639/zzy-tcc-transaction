package com.zzy.sample.transaction;

import com.zzy.dt.annotation.DistributeTransaction;
import com.zzy.sample.transaction.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("order")
@RestController
public class OrderController {


    @Autowired
    OrderService orderService;

    @RequestMapping("create")
    public Object normal() throws Exception {
        return orderService.normal();
    }


    @RequestMapping("e")
    public Object exception() throws Exception {
        return orderService.exception();
    }

}
