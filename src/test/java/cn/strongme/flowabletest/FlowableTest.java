package cn.strongme.flowabletest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableTestCase;
import org.flowable.task.api.Task;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Created by 阿水 on 2018/5/25 23:31.
 */
@Slf4j
public class FlowableTest extends FlowableTestCase {

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

    @Deployment(resources = {"processes/测试撤回功能.bpmn20.xml"})
    public void testProcessRollback() {
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("test-rollback");
        log.info("ProcessInstance Id : {}, Id: {}", instance.getProcessInstanceId(), instance.getProcessInstanceId());
        String processInstanceId = instance.getProcessInstanceId();

        //发起会议
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).taskDefinitionKey("startMeeting").active().singleResult();
        List<String> userList = Lists.newArrayList("userA", "userB", "userC");
        Map<String, Object> params = Maps.newHashMap();
        params.put("userList", userList);
        taskService.complete(task.getId(), params);

        //查找发表意见的三个任务
        List<Task> tasksForSubmitOpinion = taskService.createTaskQuery().processInstanceId(processInstanceId).taskDefinitionKey("submitOpinion").active().list();
        assertThat(tasksForSubmitOpinion.size()).isEqualTo(3);


        //回退到发起会议
        Execution taskExecution = runtimeService.createExecutionQuery().executionId(tasksForSubmitOpinion.get(0).getExecutionId()).singleResult();

        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstanceId)
                .moveExecutionToActivityId(taskExecution.getParentId(), "startMeeting")
                .changeState();

        task = taskService.createTaskQuery().processInstanceId(processInstanceId).taskDefinitionKey("startMeeting").active().singleResult();
        assertThat(task).isNotNull();
        //再次发起会议
        taskService.complete(task.getId(), params);

        //再次查询发表会议意见任务
        tasksForSubmitOpinion = taskService.createTaskQuery().processInstanceId(processInstanceId).taskDefinitionKey("submitOpinion").active().list();
        assertThat(tasksForSubmitOpinion.size()).isEqualTo(3);

        for (int i = 0; i < tasksForSubmitOpinion.size(); i++) {
            Task taskCurrent = tasksForSubmitOpinion.get(i);
            Map<String, Object> paramsInner = Maps.newHashMap();
            paramsInner.put("code", i);
            taskService.complete(taskCurrent.getId(), paramsInner, true);
        }

        instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).active().singleResult();

        assertThat(instance).isNull();


    }


}
