package com.liou.diversion.zk;

/**
 * Content :
 *
 * @author liou 2017-12-29.
 */
public class SimpleChildrenChangeHandler implements ChildrenChangeHandler {

    private String path;

    public SimpleChildrenChangeHandler(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public void handleChildAdded(String child) {
        System.out.println("add child:" + child);
    }

    @Override
    public void handleChildRemoved(String child) {
        System.out.println("remove child:" + child);
    }
}
