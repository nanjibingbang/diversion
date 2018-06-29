package com.diversion.container.spring;

import com.diversion.element.Element;

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
