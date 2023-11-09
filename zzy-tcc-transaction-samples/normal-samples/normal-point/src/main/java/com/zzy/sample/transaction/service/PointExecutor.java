package com.zzy.sample.transaction.service;

import com.zzy.jdbc.JdbcExecutor;
import com.zzy.sample.transaction.entity.PointTab;
import org.springframework.stereotype.Service;

@Service
public class PointExecutor extends JdbcExecutor<PointTab> {
}
