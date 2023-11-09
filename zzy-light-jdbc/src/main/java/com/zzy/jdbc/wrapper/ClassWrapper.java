package com.zzy.jdbc.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassWrapper {

    private Class targetClass;

    public static ClassWrapper of(Class targetType){
        return new ClassWrapper(targetType);
    }

}
