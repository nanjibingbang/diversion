package com.liou.diversion.zk;

import org.apache.curator.framework.api.UnhandledErrorListener;

/**
 * Content :
 *
 * @author liou 2017-12-29.
 */
public class ExceptionListener implements UnhandledErrorListener {

    @Override
    public void unhandledError(String message, Throwable e) {
        e.printStackTrace();
    }

}
