package com.zzy.dt;

public interface SqlConstants {

    String SELECT_TRANSACTION_LOG = "select * from transaction_log where application = ? and type = ? and status = ? and  global_status = ? and ( create_time < ? - 10000) limit 100 ";

    String SELECT_TIMEOUT_ROOT_TRANSACTION_LOG = "select * from transaction_log where application = ? and type = ? and global_status = ? and ( create_time < ? - 10000) limit 100 ";

    String SELECT_ROOT_TRANSACTION_LOG = "select * from transaction_log where root_id = ? and type = ? and application = ? and global_status <>  ? ";

    String SELECT_COMPLETE_TRANSACTION_LOG = "select * from transaction_log where root_id = ? and application = ? and status = ? and global_status = ? and type = ? ";

    String UPDATE_TRANSACTION_META = "update transaction_log set meta = ? where id = ? and application = ?";

    String SELECT_IDEMPOTENT_LOG = "select count(*) from idempotent_log where module = ? and request_key = ? ";

    String UPDATE_GLOBAL_TRANSACTION_STATUS = " update transaction_log set global_status = ? where root_id = ? and application = ? and type = ? and global_status = ? and status = ? ";

    String DELETE_IDEMPOTENT_BY_TRANSACTION = " delete from idempotent_log where transaction_id = ? ";

    String UPDATE_TRANSACTION_LOG_BUS_TIME_AND_STATUS = " update transaction_log set status = ?, bus_complete_time = ? where id = ? and type = ? and application = ? ";

    String UPDATE_ROOT_TRANSACTION_LOG_BUS_TIME_AND_STATUS = " update transaction_log set status = ?, bus_complete_time = ?, global_status = ? where id = ? and type = ? and application = ?";

}
