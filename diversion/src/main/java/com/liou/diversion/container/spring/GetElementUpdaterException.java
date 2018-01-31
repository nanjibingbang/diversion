package com.liou.diversion.container.spring;

import com.liou.diversion.element.Element;

import java.util.Arrays;

/**
 * Content :
 *
 * @author liou 2018-01-15.
 */
public class GetElementUpdaterException extends Exception {

    public GetElementUpdaterException(Throwable cause, Element element) {
        super(String.format("Element Updater for %s", element), cause);
    }

}
