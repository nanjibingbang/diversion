package com.liou.diversion.element.cache;

/**
 * Content :
 *
 * @author liou 2018-01-31.
 */
public class TransientResult {

    private final Object result;
    private final long limit;

    public TransientResult(Object result, long timestamp) {
        this.limit = timestamp;
        this.result = result;
    }

    public Object getResult() {
        return result;
    }

    public long getLimit() {
        return limit;
    }

}
