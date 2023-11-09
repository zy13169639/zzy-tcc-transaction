package com.zzy.jdbc.handler.impl;

import com.zzy.dt.common.enums.ExceptionEnum;
import com.zzy.dt.common.enums.IEnum;
import com.zzy.dt.common.exception.DtCommonException;
import com.zzy.jdbc.handler.BaseClassTypeHandler;
import com.zzy.jdbc.holder.JdbcTypeHandlerHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EnumTypeHandler extends BaseClassTypeHandler {

    @Override
    public IEnum doGet(ResultSet resultSet, String columnName) throws Exception {
        Object object = resultSet.getObject(columnName);
        Class<IEnum> javaType = this.getJavaType();
        IEnum iEnum = javaType.getEnumConstants()[0];
        return iEnum.of(object);
    }

    @Override
    public void doSet(PreparedStatement statement, Object o, Integer index) throws Exception {
        if (o!=null && !(o instanceof IEnum)) {
            throw DtCommonException.throwException(ExceptionEnum.SYSTEM_ERROR);
        }
        IEnum param = (IEnum) o;
        Object code = param.getCode();
        JdbcTypeHandlerHolder.getHandler(code.getClass()).doSet(statement, code, index);
    }
}
