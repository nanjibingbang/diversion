package com.diversion.element.execute;

import com.diversion.element.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Content : ElementUpdateTask执行状态池
 *
 * @author liou 2018-02-27.
 */
public class ExecuteStatePool implements ExecuteStateListener {

    private final Map<ElementUpdateTask, AtomicReference<State>> stateMap;

    public ExecuteStatePool() {
        stateMap = new HashMap<>();
    }

    /**
     * 从状态池获取task并添加context
     *
     * @param element
     * @param executeContext
     * @return
     */
    public ElementUpdateTask findAndAppendElement(Element element, ExecuteContext executeContext) {
        synchronized (stateMap) {
            Optional<ElementUpdateTask> any = stateMap.entrySet().stream()
                    .filter(entry -> entry.getKey().getElement().equals(element)
                            && (entry.getValue().get() == State.EXECUTE_STATE_NEW))
                    .map(Map.Entry::getKey).findAny();
            if (any.isPresent() && any.get().addContext(executeContext)) {
                return any.get();
            }
        }
        return null;
    }

    @Override
    public void stateChange(State state, ElementUpdateTask task) {
        if (state == State.EXECUTE_STATE_DONE) {
            synchronized (stateMap) {
                stateMap.remove(task);
            }
        } else {
            AtomicReference<State> stateRef = stateMap.get(task);
            if (stateRef == null) {
                synchronized (stateMap) {
                    stateRef = new AtomicReference<>(State.EXECUTE_STATE_NEW);
                    stateMap.put(task, stateRef);
                }
            }
            stateRef.set(state);
        }
    }

    public enum State {
        /**
         * 新建
         */
        EXECUTE_STATE_NEW,
        /**
         * 完成
         */
        EXECUTE_STATE_DONE
    }
}
