package com.zzy.jdbc.handler;

import com.zzy.jdbc.holder.ResourceManager;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public interface TypeHandler<T> {

    default T get(ResultSet resultSet, String columnName) throws Exception {
        Object object = resultSet.getObject(columnName);
        if (object == null) {
            return null;
        }
        return doGet(resultSet, columnName);
    }

    T doGet(ResultSet resultSet, String columnName) throws Exception;

    void doSet(PreparedStatement statement, T param, Integer index) throws Exception;

    default void set(PreparedStatement statement, T param, Integer index) throws Exception {
        if (param == null) {
            JDBCType nullType = ResourceManager.getJdbcProperties().getNullType();
            if (nullType == null) {
                statement.setNull(index, JDBCType.OTHER.getVendorTypeNumber());
            } else {
                statement.setNull(index, nullType.getVendorTypeNumber());
            }
            return;
        }
        doSet(statement, param, index);
    }
}
