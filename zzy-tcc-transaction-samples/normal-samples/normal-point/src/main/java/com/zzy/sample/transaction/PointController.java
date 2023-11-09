package com.zzy.sample.transaction;

import com.zzy.sample.transaction.service.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RequestMapping("point")
@RestController
public class PointController {


    @Autowired
    PointService pointService;

    @RequestMapping("add")
    public Object normal(@RequestParam("uid") Long uid) throws Exception {
//         int i = new Random().nextInt(5);
//         if (i >= 4) {
//             Thread.sleep(i * 1000);
//         }
        return pointService.normal(uid, uid * 100);
    }

}
