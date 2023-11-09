package com.zzy.sample.transaction;

import com.zzy.sample.transaction.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RequestMapping("test")
@RestController
public class TransactionController {


    @Autowired
    ProductService productService;

    @RequestMapping("deduce")
    public Object normal(@RequestParam("id") long id) throws Exception {
        int i = new Random().nextInt(10);
        return productService.normal(id);
    }


    @RequestMapping("e")
    public Object exception(@RequestParam("id") long id) throws Exception {
        return productService.exception(id);
    }

}
