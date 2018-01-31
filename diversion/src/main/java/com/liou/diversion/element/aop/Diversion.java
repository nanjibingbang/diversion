package com.liou.diversion.element.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Diversion {

    /**
     * 调用目标类
     *
     * @return
     */
    String targetClass();

    /**
     * 目标方法
     *
     * @return
     */
    String targetMethod();

}
