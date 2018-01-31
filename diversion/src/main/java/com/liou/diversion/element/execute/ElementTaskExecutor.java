package com.liou.diversion.element.execute;

import com.liou.diversion.container.Container;
import com.liou.diversion.container.Destroyable;
import com.liou.diversion.container.spring.GetElementUpdaterException;
import com.liou.diversion.element.Element;
import com.liou.diversion.element.ElementUpdater;
import com.liou.diversion.element.ElementUpdaterRepository;
import com.liou.diversion.element.cache.TransientProvider;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ElementTask队列运行管理,结合暂存器解决重复缓存更新
 *
 * @author liou
 */
public class ElementTaskExecutor implements Destroyable {

    private ExecutorService executorService;
    private LinkedBlockingQueue<Runnable> blockingQueue;

    private TransientProvider transientProvider;
    private Container container;

    public ElementTaskExecutor() {
        blockingQueue = new LinkedBlockingQueue<>();
        executorService = new ThreadPoolExecutor(0, 32, 1, TimeUnit.MINUTES, blockingQueue);
    }

    public ElementUpdateTask execute(Element element, ExecuteContext executeContext) throws Exception {
        List<Runnable> collect = blockingQueue.stream().filter(o -> {
            ElementUpdateTask origin = (ElementUpdateTask) o;
            return origin.getElement().equals(element) && origin.addContext(executeContext);
        }).collect(Collectors.toList());
        if (collect != null && collect.size() > 0) {
            return (ElementUpdateTask) collect.get(0);
        }
        ElementUpdateTask elementUpdateTask = createElementUpdateTask(element);
        elementUpdateTask.addContext(executeContext);
        executorService.execute(elementUpdateTask);
        return elementUpdateTask;
    }

    /**
     * 实现指定执行，从容器获取
     *
     * @param element
     * @return
     * @throws GetElementUpdaterException
     */
    private ElementUpdateTask createElementUpdateTask(Element element) throws GetElementUpdaterException {
        ElementUpdater elementUpdater = ElementUpdaterRepository.getUpdaterByElement(element);
        if (elementUpdater == null && StringUtils.isNotBlank(element.getTagCla()) && StringUtils.isNotBlank(element.getTagMed())) {
            elementUpdater = container.getElementUpdater(element);
            ElementUpdaterRepository.registeElementUpdater(elementUpdater);
        }
        return new ElementUpdateTask(element, elementUpdater, transientProvider);
    }

    public void setTransientProvider(TransientProvider transientProvider) {
        this.transientProvider = transientProvider;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    @Override
    public void destroy() {
        executorService.shutdown();
    }

}
