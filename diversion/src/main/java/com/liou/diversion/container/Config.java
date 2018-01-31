package com.liou.diversion.container;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Content : 引用diversion配置
 *
 * @author liou 2018-01-08.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Config {

    /**
     * 配置名称
     * @see com.liou.diversion.container.DiversionConfig.Configs#sign()
     * @return
     */
    String value();

}
