package com.diversion.element.execute;

/**
 * Content : 执行状态监听
 *
 * @author liou 2018-03-01.
 */
public interface ExecuteStateListener {

    void stateChange(ExecuteStatePool.State state, ElementUpdateTask task);
}
