package com.diversion.element.cache;

/**
 * Content :
 *
 * @author liou 2018-01-31.
 */
public class TransientResult {

    private Object result;
    private volatile long limit;

    public TransientResult(Object result, long timestamp) {
        this.limit = timestamp;
        this.result = result;
    }

    public Object getResult() {
        return result;
    }

    public boolean isExpire() {
        if (limit == 0L) {
            return true;
        }
        if (System.currentTimeMillis() > limit) {
            limit = 0L;
            return true;
        }
        return false;
    }

}
