package com.liou.diversion;

import com.liou.diversion.element.Element;

public interface FailoverHandler {

    Object failover(Element element, Exception e);

}
