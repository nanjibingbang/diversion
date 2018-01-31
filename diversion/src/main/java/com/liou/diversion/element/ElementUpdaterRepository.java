package com.liou.diversion.element;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ElementUpdaterRepository {

    private static Set<ElementUpdater> elementUpdaters = new HashSet<>();

    private ElementUpdaterRepository() {
    }

    public synchronized static void registeElementUpdater(ElementUpdater elementUpdater) {
        elementUpdaters.add(elementUpdater);
    }

    public synchronized static ElementUpdater getUpdaterByElement(Element element) {
        Iterator<ElementUpdater> it = elementUpdaters.iterator();
        while (it.hasNext()) {
            ElementUpdater elementUpdater = it.next();
            if (elementUpdater.adapter(element)) {
                return elementUpdater;
            }
        }
        return null;
    }

}
