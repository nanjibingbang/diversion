package com.liou.diversion.element.execute;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.liou.diversion.container.Container;
import com.liou.diversion.container.Destroyable;
import com.liou.diversion.container.spring.GetElementUpdaterException;
import com.liou.diversion.element.Element;
import com.liou.diversion.element.ElementUpdater;
import com.liou.diversion.element.ElementUpdaterRepository;
import com.liou.diversion.element.cache.TransientProvider;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.*;


/**
 * ElementTask队列运行管理,结合暂存器解决重复缓存更新
 *
 * @author liou
 */
public class ElementTaskExecutor implements Destroyable {

    private ExecutorService executorService;
    private LinkedBlockingQueue<Runnable> blockingQueue;
    private ExecuteStatePool executeStatePool;

    private TransientProvider transientProvider;
    private Container container;

    public ElementTaskExecutor() {
        executeStatePool = new ExecuteStatePool();
        blockingQueue = new LinkedBlockingQueue<>();
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("element-update-%d").build();
        executorService = new ThreadPoolExecutor(4, 32, 1,
                TimeUnit.MINUTES, blockingQueue, threadFactory);
    }

    public ElementUpdateTask execute(Element element, ExecuteContext executeContext) throws Exception {
        ElementUpdateTask elementUpdateTask = executeStatePool.findAndAppendElement(element, executeContext);
        if (elementUpdateTask != null) {
            return elementUpdateTask;
        }
        elementUpdateTask = createElementUpdateTask(element);
        executeStatePool.stateChange(ExecuteStatePool.State.EXECUTE_STATE_NEW, elementUpdateTask);
        elementUpdateTask.registerExecuteStateListener(executeStatePool);
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
        if (elementUpdater == null) {
            throw new RuntimeException("没有找到适配的ElementUpdater");
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
