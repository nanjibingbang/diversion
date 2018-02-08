package com.liou.diversion.utils;

import com.liou.diversion.container.Config;
import com.liou.diversion.container.DiversionConfig;

import java.lang.reflect.Field;

/**
 * Content :
 *
 * @author liou 2018-02-02.
 */
public class ConfigUtils {

    public static Object applyConfig(Object instance, DiversionConfig diversionConfig) throws IllegalAccessException {
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            Config annotation = field.getAnnotation(Config.class);
            if (annotation != null) {
                String config = annotation.value();
                Object value = diversionConfig.getConfig(config);
                field.setAccessible(true);
                field.set(instance, value);
            }
        }
        return instance;
    }
}
