package com.zzy.sample.transaction.service;

import com.zzy.jdbc.JdbcExecutor;
import com.zzy.sample.transaction.entity.OrderTab;
import org.springframework.stereotype.Service;

@Service
public class OrderExecutor extends JdbcExecutor<OrderTab> {
}
