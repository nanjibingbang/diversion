package com.diversion.utils;

import com.diversion.container.Config;
import com.diversion.container.ConfigApplyException;
import com.diversion.container.DiversionConfig;

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
                if (value == null && !annotation.nullable()) {
                    throw new ConfigApplyException(annotation.value() + " can't be null");
                }
                field.setAccessible(true);
                field.set(instance, value);
            }
        }
        return instance;
    }
}
