package com.liou.diversion.container;

import org.springframework.beans.BeansException;

/**
 * Content :
 *
 * @author liou 2018-02-06.
 */
public class ConfigApplyException extends BeansException {

    public ConfigApplyException(String msg) {
        super(msg);
    }

    public ConfigApplyException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
