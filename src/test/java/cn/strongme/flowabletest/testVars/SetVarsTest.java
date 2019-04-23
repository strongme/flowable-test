package cn.strongme.flowabletest.testVars;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableTestCase;
import org.flowable.task.api.Task;

import java.util.Map;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Created by 阿水 on 2018/5/25 23:31.
 */
@Slf4j
public class SetVarsTest extends FlowableTestCase {

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

    @Deployment(resources = {"processes/最后一步设置的变量是否有用.bpmn20.xml"})
    public void testProcessRollback() {
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("last-step-vars-works-or-not");
        log.info("ProcessInstance Id : {}, Id: {}", instance.getProcessInstanceId(), instance.getProcessInstanceId());
        String processInstanceId = instance.getProcessInstanceId();

        //发起会议
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKey("setvars")
                .active()
                .singleResult();

        assertThat(task).isNotNull();

        taskService.complete(task.getId(), null);

        instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).active().singleResult();

        assertThat(instance).isNull();

        log.debug("当前已经流程已经结束，查询历史流程变量是否包含设置的变量");

        HistoricProcessInstance historicProcessInstance = historicDataService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .includeProcessVariables()
                .singleResult();

        assertThat(historicProcessInstance).isNotNull();

        Map<String, Object> hisProVars = historicProcessInstance.getProcessVariables();

        log.info(hisProVars.toString());

        assertThat(hisProVars)
                .isNotNull()
                .isNotEmpty()
                .containsEntry("testVars", "我是最后一步设置的变量")
        ;

    }


}
