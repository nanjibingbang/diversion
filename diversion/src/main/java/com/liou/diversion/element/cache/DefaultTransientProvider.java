package com.liou.diversion.element.cache;

import com.liou.diversion.container.Config;
import com.liou.diversion.element.Element;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 默认暂存器
 *
 * @author liou
 */
public class DefaultTransientProvider implements TransientProvider {

    /**
     * 暂留时间
     */
    @Config("diversion.cache.transienttime")
    private int transientTime;

    private Map<Element, TransientResult> transientMap;

    private ReadWriteLock readWriteLock;

    public DefaultTransientProvider() {
        transientMap = new WeakHashMap<>();
        readWriteLock = new ReentrantReadWriteLock(true);
    }

    @Override
    public TransientResult get(Element element) {
        TransientResult transientResult = null;
        Lock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            transientResult = transientMap.get(element);
            if (transientResult == null || System.currentTimeMillis() > transientResult.getLimit()) {
                return null;
            }
            return transientResult;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void record(Object result, Element element) {
        Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            transientMap.put(element, new TransientResult(result, System.currentTimeMillis() + transientTime));
        } finally {
            writeLock.unlock();
        }
    }

}
