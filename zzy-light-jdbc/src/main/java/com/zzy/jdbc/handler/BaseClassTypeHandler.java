package com.zzy.jdbc.handler;

import cn.hutool.core.util.ClassUtil;
import lombok.Setter;

public abstract class BaseClassTypeHandler<JAVA> implements TypeHandler<JAVA> {

    @Setter
    private Class<JAVA> javaType;

    public BaseClassTypeHandler() {
        javaType = (Class<JAVA>) ClassUtil.getTypeArgument(this.getClass(), 0);
    }

    public Class<JAVA> getJavaType(){
        return this.javaType;
    }

    public Integer getJdbcType(){
        return null;
    }

}
