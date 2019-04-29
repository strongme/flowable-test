package cn.strongme.flowabletest.testTimer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableTestCase;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Created by 阿水 on 2018/5/25 23:31.
 */
@Slf4j
public class TimerTest extends FlowableTestCase {

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        log.info("Setup...");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        log.info("Teardown...");
    }

    @Deployment(resources = {"processes/timer.bpmn20.xml"})
    public void testProcessRollback() throws InterruptedException {
        Calendar calendar = Calendar.getInstance();
        log.info("time now : {}", sdf.format(calendar.getTime()));
        calendar.add(Calendar.SECOND, 5);
        log.info("time after 5s : {}", sdf.format(calendar.getTime()));
        Map<String, Object> vars = Maps.newHashMap();
        vars.put("triggerDate", sdf.format(calendar.getTime()));
        vars.put("list", Lists.newArrayList("a", "b", "c"));

        ProcessInstance instance = runtimeService.startProcessInstanceByKey("create-modeler-test", vars);

        BpmnModel bpmnModel = repositoryService.getBpmnModel(instance.getProcessDefinitionId());

        log.info("ProcessInstance Id : {}, Id: {}", instance.getProcessInstanceId(), instance.getProcessInstanceId());
        String processInstanceId = instance.getProcessInstanceId();

        //完成第一个任务
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .active()
                .singleResult();

        assertThat(task).isNotNull();

        log.info("当前激活的任务：{}", task.getName());

//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                countDownLatch.countDown();
//            }
//        }, 8000);
//
//        countDownLatch.await();

        taskService.complete(task.getId());

        TaskQuery taskQuery = taskService.createTaskQuery();
        List<Task> taskList = taskQuery
                .processInstanceId(processInstanceId)
                .active()
                .list();

        for (Task t : taskList) {
            log.info("TaskId: {}, TaskName: {}, Assignee: {}", t.getId(), t.getName(), t.getAssignee());
            taskService.complete(t.getId());
        }

        taskList = taskQuery
                .processInstanceId(processInstanceId)
                .active()
                .list();

        for (Task t : taskList) {
            log.info("TaskId: {}, TaskName: {}, Assignee: {}", t.getId(), t.getName(), t.getAssignee());
            taskService.complete(t.getId());
        }

        instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).active().singleResult();

        assertThat(instance).isNull();

    }


}
