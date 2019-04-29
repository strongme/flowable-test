package cn.strongme.flowabletest.flow;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * 继承重写相应的要加入业务逻辑的handle方法即可
 */
@Slf4j
public class SeprateEventTaskListener implements TaskListener {

    private static final long serialVersionUID = 8337308478750382502L;

    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("Event: {} 任务ID：{} 任务名称：{} 任务办理人：{}", delegateTask.getEventName(), delegateTask.getId(), delegateTask.getName(), delegateTask.getAssignee());
        String eventName = delegateTask.getEventName();
        switch (eventName) {
            case TaskListener.EVENTNAME_ASSIGNMENT:
                handleAssignment(delegateTask);
                break;
            case TaskListener.EVENTNAME_CREATE:
                handleCreate(delegateTask);
                break;
            case TaskListener.EVENTNAME_COMPLETE:
                handleComplete(delegateTask);
                break;
            case TaskListener.EVENTNAME_DELETE:
                handleDelete(delegateTask);
                break;
            default:
                handleAll(delegateTask);
                break;
        }
    }

    protected void handleCreate(DelegateTask delegateTask) {
        log.debug("Create Task");
    }

    protected void handleAssignment(DelegateTask delegateTask) {
        log.debug("Assign Task");
    }

    protected void handleComplete(DelegateTask delegateTask) {
        log.debug("Complete Task");
    }

    protected void handleDelete(DelegateTask delegateTask) {
        log.debug("Delete Task");
    }

    protected void handleAll(DelegateTask delegateTask) {
        log.debug("Default Handle Task");
    }

}
