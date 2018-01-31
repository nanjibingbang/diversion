package com.liou.diversion.element;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;

/**
 * Content : ElementUpdater代理类, 实现Diversion指定执行
 *
 * @author liou 2018-01-12.
 */
public class ElementUpdaterProxy implements ElementUpdater {

    private Object proxyInstance;
    private Method exeMed;
    private String tagCla;
    private String tagMed;
    private Class<?>[] medTypes;

    public ElementUpdaterProxy(Object proxyInstance, String tagCla, String tagMed, Class<?>... medTypes)
            throws NoSuchMethodException {
        this.tagCla = tagCla;
        this.tagMed = tagMed;
        this.medTypes = medTypes;
        this.proxyInstance = proxyInstance;
        exeMed = proxyInstance.getClass().getDeclaredMethod(tagMed, medTypes);
    }

    @Override
    public Object update(Element element) throws Exception {
        return exeMed.invoke(proxyInstance, element.getParams());
    }

    @Override
    public boolean adapter(Element element) {
        boolean nameEquals = tagCla.equals(element.getTagCla()) && tagMed.equals(element.getTagMed());
        if (medTypes.length != element.getParams().length) {
            return false;
        }
        for (int i = 0; i < medTypes.length; i++) {
            if(medTypes[i] != element.getParams()[i].getClass()) {
                return false;
            }
        }
        return nameEquals;
    }

}
