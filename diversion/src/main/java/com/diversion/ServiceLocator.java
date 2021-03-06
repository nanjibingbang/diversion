package com.diversion;

import java.util.*;
import java.util.Map.Entry;

public class ServiceLocator {

    private static Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();

    public synchronized static <T> void addService(Class<T> clazz, T instance) {
        map.put(clazz, instance);
    }

    public synchronized static <T> T instanceOf(Class<T> clazz) {
        try {
            T newInstance = getService(clazz);
            if (newInstance == null) {
                newInstance = clazz.newInstance();
                registerService(clazz, newInstance);
            }
            return newInstance;
        } catch (Exception e) {
        }
        return null;
    }

    public static <T> T getService(Class<T> clazz) {
        return clazz.cast(map.get(clazz));
    }

    public synchronized static <T> Enumeration<T> getServices(Class<T> clazz) {
        Vector<T> result = new Vector<T>();
        Iterator<Entry<Class<?>, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Class<?>, Object> next = it.next();
            Class<?>[] interfaces = next.getKey().getInterfaces();
            if (((next.getKey() == clazz) || (interfaces.length > 0 ? clazz == interfaces[0] : false))
                    && !result.contains(next.getValue())) {
                result.add(clazz.cast(next.getValue()));
            }
        }
        return result.elements();
    }

    private static void registerService(Class<? extends Object> clazz, Object object) {
        Class<?>[] interfaces = object.getClass().getInterfaces();
        if (interfaces.length > 0) {
            map.put(interfaces[0], object);
        }
        map.put(object.getClass(), object);
    }

}
