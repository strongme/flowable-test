package cn.strongme.flowabletest.modelerTest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.FlowableTestCase;
import org.flowable.task.api.Task;
import org.flowable.validation.ValidationError;
import org.flowable.validation.validator.impl.BpmnModelValidator;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by 阿水 on 2018/5/25 23:31.
 */
@Slf4j
public class ModelerTest extends FlowableTestCase {

    private DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private CountDownLatch countDownLatch = new CountDownLatch(1);

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
        // 添加的边界事件也需要add到process，应该也是所有的FlowElement子类的使用，都需要单独调用process.addFlowElement
        BoundaryEvent boundaryEvent = createBoundaryEvent("timerWatcher", "超时监控", userTask1, "triggerDate1");
        BoundaryEvent boundaryEvent2 = createBoundaryEvent("timerWatcher2", "超时监控2", userTask1, "triggerDate2");
        process.addFlowElement(boundaryEvent);
        process.addFlowElement(boundaryEvent2);
        userTask1.setBoundaryEvents(Lists.newArrayList(boundaryEvent, boundaryEvent2));
        process.addFlowElement(userTask1);
        process.addFlowElement(createServiceTask("timerReminder", "超时提醒"));
        process.addFlowElement(createExclusieGateway("aOrb", "A还是B"));
        process.addFlowElement(createUserTask("userTask2", "用户任务2", "mary"));
        process.addFlowElement(createUserTask("userTask3", "用户任务3", "jason"));
        process.addFlowElement(createEndEvent());

        process.addFlowElement(createSequenceFlow("start", "userTask1"));
        process.addFlowElement(createSequenceFlow("timerWatcher", "timerReminder"));
        process.addFlowElement(createSequenceFlow("timerWatcher2", "timerReminder"));
        process.addFlowElement(createSequenceFlow("userTask1", "aOrb"));
        process.addFlowElement(createSequenceFlow("aOrb", "userTask2", "${v=='a'}"));
        process.addFlowElement(createSequenceFlow("aOrb", "userTask3", "${v=='b'}"));
        process.addFlowElement(createSequenceFlow("userTask2", "end"));
        process.addFlowElement(createSequenceFlow("userTask3", "end"));

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

        Calendar calendar = Calendar.getInstance();
        log.debug("time now : {}", sdf.format(calendar.getTime()));
        calendar.add(Calendar.SECOND, 2);
        log.debug("time after 2s : {}", sdf.format(calendar.getTime()));
        Map<String, Object> vars = Maps.newHashMap();
        vars.put("triggerDate1", sdf.format(calendar.getTime()));
        calendar.add(Calendar.SECOND, 2);
        log.debug("time after 4s : {}", sdf.format(calendar.getTime()));
        vars.put("triggerDate2", sdf.format(calendar.getTime()));
        vars.put("v", "b");

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

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                countDownLatch.countDown();
            }
        }, 5000);

        countDownLatch.await();

        taskService.complete(task.getId());

        task = taskService
                .createTaskQuery()
                .processInstanceId(processInstance.getId())
                .active()
                .singleResult();
        assertThat(task).isNotNull();

        log.info("当前激活的任务Id：{}，任务名称：{}", task.getId(), task.getName());

        taskService.complete(task.getId());

        processInstance = runtimeService
                .createProcessInstanceQuery()
                .processInstanceId(processInstance.getId())
                .active()
                .singleResult();
        assertThat(processInstance).isNull();

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
        loopCharacteristics.setCollectionString("${userList}");
        loopCharacteristics.setLoopCardinality("2");
        loopCharacteristics.setElementVariable("user");

        userTask.setLoopCharacteristics(loopCharacteristics);


        FlowableListener flowableAssigneeListener = new FlowableListener();
        flowableAssigneeListener.setId("onAssignee");
        flowableAssigneeListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
        flowableAssigneeListener.setImplementation("cn.strongme.flowabletest.modelerTest.ModelerListener");
        flowableAssigneeListener.setEvent(TaskListener.EVENTNAME_ASSIGNMENT);

        FlowableListener flowableCreateListener = new FlowableListener();
        flowableCreateListener.setId("onCreate");
        flowableCreateListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
        flowableCreateListener.setImplementation("cn.strongme.flowabletest.modelerTest.ModelerListener");
        flowableCreateListener.setEvent(TaskListener.EVENTNAME_CREATE);

        userTask.setTaskListeners(Lists.newArrayList(flowableAssigneeListener, flowableCreateListener));

        return userTask;
    }

    BoundaryEvent createBoundaryEvent(String id, String name, Activity activity, String triggerDateVarName) {
        BoundaryEvent boundaryEvent = new BoundaryEvent();
        boundaryEvent.setId(id);
        boundaryEvent.setName(name);
        boundaryEvent.setCancelActivity(false);
        boundaryEvent.setAttachedToRef(activity);
        boundaryEvent.setEventDefinitions(Lists.newArrayList(createTimerEventDefinition(triggerDateVarName)));
        return boundaryEvent;
    }

    ServiceTask createServiceTask(String id, String name) {
        ServiceTask serviceTask = new ServiceTask();
        serviceTask.setId(id);
        serviceTask.setName(name);
        serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
        serviceTask.setImplementation("cn.strongme.flowabletest.testTimer.TimerWork");
        return serviceTask;
    }

    TimerEventDefinition createTimerEventDefinition(String triggerDateVarName) {
        TimerEventDefinition timerEventDefinition = new TimerEventDefinition();
        timerEventDefinition.setTimeDate("${" + triggerDateVarName + "}");
        return timerEventDefinition;
    }

    SequenceFlow createSequenceFlow(FlowElement from, FlowElement to) {
        return createSequenceFlow(from.getId(), to.getId(), null);
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

    ExclusiveGateway createExclusieGateway(String id, String name) {
        ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
        exclusiveGateway.setId(id);
        exclusiveGateway.setName(name);
        return exclusiveGateway;
    }


}
