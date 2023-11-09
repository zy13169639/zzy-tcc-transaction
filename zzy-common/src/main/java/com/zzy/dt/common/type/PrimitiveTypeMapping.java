package com.zzy.dt.common.type;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrimitiveTypeMapping {
    /**
     * 基本类型
     */
    private Class primitiveType;
    /**
     * 包装类型
     */
    private Class wrapperType;

    public static PrimitiveTypeMapping of(Class primitiveType, Class wrapperType) {
        return new PrimitiveTypeMapping(primitiveType, wrapperType);
    }

}
