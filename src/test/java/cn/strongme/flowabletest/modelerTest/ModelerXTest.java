package cn.strongme.flowabletest.modelerTest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.FlowableTestCase;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.validation.ValidationError;
import org.flowable.validation.validator.impl.BpmnModelValidator;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by 阿水 on 2018/5/25 23:31.
 */
@Slf4j
public class ModelerXTest extends FlowableTestCase {

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

    @Test
    public void testCustomWriteModel() throws IOException, InterruptedException {

        Process process = new Process();

        process.setId("code-write-process");

        process.addFlowElement(createStartEvent());
        UserTask userTask1 = createUserTask("userTask1", "用户任务1", "jack");
        process.addFlowElement(userTask1);
        process.addFlowElement(createEndEvent());

        process.addFlowElement(createSequenceFlow("start", "userTask1"));
        process.addFlowElement(createSequenceFlow("userTask1", "end"));

        BpmnModel model = new BpmnModel();
        model.addProcess(process);

        BpmnModelValidator validator = new BpmnModelValidator();
        List<ValidationError> errors = Lists.newArrayList();
        validator.validate(model, errors);
        log.info("错误数：{}", errors.size());

        new BpmnAutoLayout(model).execute();


        Deployment deployment = repositoryService
                .createDeployment()
                .addBpmnModel("dynamic-model.bpmn", model)
                .name("动态流程定义部署")
                .deploy();

        assertThat(deployment).isNotNull();

        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey("code-write-process")
                .active()
                .singleResult();

        assertThat(processDefinition).isNotNull();

        InputStream processDiagram = repositoryService.getProcessDiagram(processDefinition.getId());

        //to png
        FileUtils.copyInputStreamToFile(processDiagram, new File("target/process/diagram.png"));
        //to xml
        InputStream processBpmn = repositoryService.getResourceAsStream(deployment.getId(), "dynamic-model.bpmn");
        FileUtils.copyInputStreamToFile(processBpmn, new File("target/process/process.bpmn20.xml"));

        Map<String, Object> vars = Maps.newHashMap();
        vars.put("userList", Lists.newArrayList("a", "b", "c"));

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("code-write-process", vars);

        assertThat(processInstance).isNotNull();

        log.info("启动的流程Id：{}", processInstance.getId());

        Task task = taskService
                .createTaskQuery()
                .processInstanceId(processInstance.getId())
                .active()
                .singleResult();
        assertThat(task).isNotNull();

        log.info("当前激活的任务Id：{}，任务名称：{}", task.getId(), task.getName());

        taskService.complete(task.getId());

        TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active();

        assertThat(taskQuery.count()).isEqualTo(3);

    }

    StartEvent createStartEvent() {
        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        startEvent.setName("开始节点");
        return startEvent;
    }

    EndEvent createEndEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId("end");
        endEvent.setName("结束节点");
        return endEvent;
    }

    UserTask createUserTask(String id, String name, String assignee) {
        UserTask userTask = new UserTask();
        userTask.setId(id);
        userTask.setName(name);
        userTask.setAssignee(assignee);

        MultiInstanceLoopCharacteristics loopCharacteristics = new MultiInstanceLoopCharacteristics();
        loopCharacteristics.setSequential(false);
        loopCharacteristics.setInputDataItem("${userList}");
//        loopCharacteristics.setLoopCardinality("3");
        loopCharacteristics.setElementVariable("user");

        CollectionHandler collectionHandler = new CollectionHandler();
        collectionHandler.setImplementation(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION);
        collectionHandler.setImplementation("${userList}");


        userTask.setLoopCharacteristics(loopCharacteristics);
        return userTask;
    }

    SequenceFlow createSequenceFlow(String from, String to, String conditionExpression) {
        SequenceFlow flow = new SequenceFlow();
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        flow.setConditionExpression(conditionExpression);
        return flow;
    }

    SequenceFlow createSequenceFlow(String from, String to) {
        return createSequenceFlow(from, to, null);
    }

}
