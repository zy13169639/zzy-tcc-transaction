package com.zzy.jdbc;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.zzy.dt.common.enums.ExceptionEnum;
import com.zzy.dt.common.exception.DtCommonException;
import com.zzy.dt.common.holder.PrimitiveTypeMappingHolder;
import com.zzy.jdbc.handler.TypeHandler;
import com.zzy.jdbc.holder.JdbcTypeHandlerHolder;
import com.zzy.jdbc.holder.ResourceManager;
import lombok.SneakyThrows;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.swing.text.FieldView;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 表名为类名的下划线形式<br></br>
 * <b>不负责连接的关闭</b>
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/18 9:49
 */
public class JdbcExecutor<T> {

    private Class<T> entityClass;

    private String insertSql;

    private Field[] fields;

    protected JdbcExecutor() {
        ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.entityClass = (Class) parameterizedType.getActualTypeArguments()[0];
        parserInsertSql(this.entityClass);
    }

    /**
     * 解析插入sql
     *
     * @param entityClass
     * @return: void
     */
    private void parserInsertSql(Class entityClass) {
        String simpleName = entityClass.getSimpleName();
        String tableName = StrUtil.toUnderlineCase(simpleName);
        Field[] fields = ReflectUtil.getFields(entityClass);
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("insert into ").append(tableName).append(" ( ");
        String columns = Arrays.stream(fields).map(field -> StrUtil.toUnderlineCase(field.getName())).collect(Collectors.joining(","));
        sqlBuilder.append(columns).append(" ) values ( ");
        String values = Arrays.stream(fields).map(field -> " ? ").collect(Collectors.joining(","));
        sqlBuilder.append(values).append(")");
        this.insertSql = sqlBuilder.toString();
        this.fields = fields;
    }


    public List<T> queryList(Connection connection, String sql, Object... params) throws Exception {
        return queryList(connection, sql, entityClass, params);
    }


    public T queryOne(Connection connection, String sql, Object... params) throws Exception {
        return this.queryOne(connection, sql, entityClass, params);
    }

    /**
     * 单值查询
     *
     * @param connection
     * @param sql
     * @param resultType
     * @param params
     * @return: E
     */
    public <E> E queryOne(Connection connection, String sql, Class<E> resultType, Object... params) throws Exception {
        List<E> es = queryList(connection, sql, resultType, params);
        if (CollectionUtil.isEmpty(es)) {
            return null;
        }
        if (es.size() != 1) {
            throw DtCommonException.throwException(ExceptionEnum.DATA_ERROR);
        }
        return es.get(0);
    }

    public boolean executeUpdate(Connection connection, String sql, Object... args) throws Exception {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            this.parameterize(ps, args);
            int count = ps.executeUpdate();
            return count > 0;
        } finally {
            release(ps);
            releaseConnectionIfNecessary(connection);
        }

    }


    /**
     * 列表查询
     *
     * @param connection 指定连接
     * @param sql        待执行的sql
     * @param resultType 返回值类型
     * @param params     sql参数
     * @return: java.util.List<E>
     */
    public <E> List<E> queryList(Connection connection, String sql, Class<E> resultType, Object... params) throws Exception {
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            ps = connection.prepareStatement(sql);
            this.parameterize(ps, params);
            resultSet = ps.executeQuery();
            return assembleData(resultSet, resultType);
        } finally {
            release(resultSet);
            release(ps);
            releaseConnectionIfNecessary(connection);
        }
    }


    private void parameterize(PreparedStatement ps, Object... params) throws Exception {
        if (ArrayUtil.isNotEmpty(params)) {
            long count = Arrays.stream(params).filter(param -> param == null).count();
            if (count > 0) {
                throw DtCommonException.throwException(ExceptionEnum.NOT_NULL);
            }
            for (int i = 0; i < params.length; i++) {
                JdbcTypeHandlerHolder.getHandler(PrimitiveTypeMappingHolder.getWrapperClass(params[i].getClass())).set(ps, params[i], i + 1);
            }
        }
    }

    /**
     * 插入数据
     *
     * @param connection
     * @param t
     * @return: boolean
     */

    public boolean insert(Connection connection, T t) throws Exception {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(insertSql);
            for (int i = 0; i < this.fields.length; i++) {
                Field field = this.fields[i];
                Object fieldValue = ReflectUtil.getFieldValue(t, field);
                JdbcTypeHandlerHolder.getHandler(PrimitiveTypeMappingHolder.getWrapperClass(field.getType())).set(ps, fieldValue, i + 1);
            }
            return ps.executeUpdate() > 0;
        } finally {
            release(ps);
            releaseConnectionIfNecessary(connection);
        }
    }


    /**
     * 转换查询结构为指定类型
     *
     * @param resultSet
     * @param resultType
     * @return: java.util.List<R>
     */
    protected <R> List<R> assembleData(ResultSet resultSet, Class<R> resultType) throws Exception {
        List<R> result = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (resultSet.next()) {
            if (!PrimitiveTypeMappingHolder.isBasicType(resultType)) {
                R r = (R) ReflectUtil.newInstanceIfPossible(resultType);
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String fieldName = StrUtil.toCamelCase(columnName);
                    int columnType = metaData.getColumnType(i);
                    Field field = ReflectUtil.getField(resultType, fieldName);
                    if (field != null) {
                        Object fieldValue = getColumnValue(resultSet, field.getType(), columnType, columnName);
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        if (fieldValue != null) {
                            field.set(r, fieldValue);
                        }
                    }
                }
                result.add(r);
            } else {
                int columnType = metaData.getColumnType(1);
                String columnName = metaData.getColumnName(1);
                Object filedValue = getColumnValue(resultSet, resultType, columnType, columnName);
                if (filedValue != null) {
                    result.add((R) filedValue);
                }
            }
        }
        return result;
    }

    private Object getColumnValue(ResultSet rs, Class targetType, Integer columnType, String columnName) throws Exception {
        Class<?> type = PrimitiveTypeMappingHolder.getWrapperClass(targetType);
        TypeHandler typeHandler = Optional.ofNullable(JdbcTypeHandlerHolder.getHandler(type, columnType)).orElse(null);
        Object fieldValue;
        if (typeHandler == null && rs.getObject(columnName) != null) {
            fieldValue = Convert.convert(type, rs.getObject(columnName));
        } else {
            fieldValue = typeHandler.get(rs, columnName);
        }
        return fieldValue;
    }


    /**
     * 释放资源
     *
     * @param autoCloseable
     * @return: void
     */
    public void release(AutoCloseable autoCloseable) {
        if (autoCloseable != null) {
            try {
                autoCloseable.close();
            } catch (Exception throwables) {
                //
            }
        }
    }

    public void releaseConnectionIfNecessary(Connection connection) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            return;
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception throwables) {
                //
            }
        }
    }
}
