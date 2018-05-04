package com.liou.diversion.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ElementUpdaterRepository {

    private static List<ElementUpdater> elementUpdaters = new ArrayList<>();

    private ElementUpdaterRepository() {
    }

    public synchronized static void registeElementUpdater(ElementUpdater elementUpdater) {
        elementUpdaters.add(elementUpdater);
    }

    public synchronized static ElementUpdater getUpdaterByElement(Element element) {
        Optional<ElementUpdater> any = elementUpdaters.stream()
                .filter(elementUpdater -> elementUpdater.adapter(element)).findAny();
        if (any.isPresent()) {
            return any.get();
        }
        return null;
    }

}
