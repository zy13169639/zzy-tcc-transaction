package com.zzy.sample.transaction.service;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import com.zzy.dt.annotation.DistributeTransaction;
import com.zzy.dt.annotation.InTransactional;
import com.zzy.dt.enums.InTransactionalWorker;
import com.zzy.jdbc.holder.ResourceManager;
import com.zzy.sample.transaction.entity.PointTab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class PointService {

    @Autowired
    PointExecutor pointExecutor;

    @Autowired
    RestTemplate restTemplate;

    private String select = "select * from point_tab where uid = ? ";


    private String update = "update  point_tab set point = point + ? where uid = ? ";

    @DistributeTransaction(commitMethod = "confirm", rollbackMethod = "cancel")
    @InTransactional(InTransactionalWorker.BUSINESS_SUCCESS)
    public Object normal(Long uid, Long point) throws Exception {
        PointTab pointTbl = new PointTab();
        pointTbl.setPoint(point);
        pointTbl.setUid(uid);
        PointTab pointTab = pointExecutor.queryOne(ResourceManager.getTransactionConnection(), select, uid);
        if (pointTab == null) {
            pointExecutor.insert(ResourceManager.getTransactionConnection(), pointTbl);
        } else {
            pointExecutor.executeUpdate(ResourceManager.getTransactionConnection(), update, point, uid);
        }
        return true;
    }

    public void confirm(Long uid, Long point) {

        System.out.println("point confirm...");

    }

    public void cancel(Long uid, Long point) throws Exception {
        pointExecutor.executeUpdate(ResourceManager.getTransactionConnection(), update, -point, uid);
    }

}
