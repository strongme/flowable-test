package cn.strongme.flowabletest.flow;

import cn.strongme.flowabletest.FlowableTestApplication;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by 阿水 on 2018/5/25 23:31.
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {FlowableTestApplication.class}
)
public class EditModelerTest {


    @Autowired
    private BusinessWorkFlowService businessWorkFlowService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Test
    public void testSingleApprove() throws IOException {

        assertThat(businessWorkFlowService).isNotNull();

        String cachedKey = businessWorkFlowService.createWorkFlow();

        String startId = businessWorkFlowService.createStart(cachedKey, "开始节点");

        String endId = businessWorkFlowService.createEnd(cachedKey, "结束节点");

        CreateOperationResult createApproveResult = businessWorkFlowService.createApprove(cachedKey, "预算审批", "admin");

        businessWorkFlowService.connect(cachedKey, startId, createApproveResult.getIncome().getPortId(), null);

        CreateOperationResult createApproveAndAuditResult = businessWorkFlowService.createApproveAndAudit(cachedKey, "成本审批", "审核节点", "approveGroup", "auditGroup");

        businessWorkFlowService.connect(cachedKey, createApproveResult.getOutput().getPortId(), createApproveAndAuditResult.getIncome().getPortId(), createApproveResult.getOutput().getConditionYes());

        businessWorkFlowService.connect(cachedKey, createApproveResult.getOutput().getPortId(), endId, createApproveResult.getOutput().getConditionNo());

        CreateOperationAuditResult createSingleAudit = businessWorkFlowService.createAudit(cachedKey, "最终审核", "finalGroup");

        businessWorkFlowService.connect(cachedKey, createApproveAndAuditResult.getOutput().getPortId(), createSingleAudit.getIncome().getPortId(), createApproveAndAuditResult.getOutput().getConditionYes());

        businessWorkFlowService.connect(cachedKey, createApproveAndAuditResult.getOutput().getPortId(), endId, createApproveAndAuditResult.getOutput().getConditionNo());

        businessWorkFlowService.connect(cachedKey, createSingleAudit.getOutput().getPortId(), endId, null);


        log.info("验证工作流合法:{}", businessWorkFlowService.validate(cachedKey));

        Deployment deployment = businessWorkFlowService.deploy(cachedKey);

        log.info("发布Id:{}", deployment.getId());

        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey(BusinessWorkFlowService.WORKFLOW_ID)
                .active()
                .singleResult();

        assertThat(processDefinition).isNotNull();

        String resourceName = processDefinition.getResourceName();
        InputStream processDiagram = repositoryService.getProcessDiagram(processDefinition.getId());

        //to png
        FileUtils.copyInputStreamToFile(processDiagram, new File("target/process/diagram.png"));
        //to xml
        InputStream processBpmn = repositoryService.getResourceAsStream(deployment.getId(), resourceName);
        FileUtils.copyInputStreamToFile(processBpmn, new File("target/process/process.bpmn20.xml"));

        Map<String, Object> vars = Maps.newHashMap();
        vars.put("approver", "jack");
        vars.put("auditor", "mary");

        ProcessInstance instance = runtimeService.startProcessInstanceByKey(BusinessWorkFlowService.WORKFLOW_ID, vars);

        log.info("InstanceId: {}", instance.getId());

        String instanceId = instance.getId();

        TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(instanceId);
        assertThat(taskQuery.count()).isEqualTo(1);
        log.info("有{}个待办任务", taskQuery.count());

        Task task = taskQuery.singleResult();
        log.info("TaskId: {}, TaskName: {}", task.getId(), task.getName());

        Map<String, Object> vars2CompleteTask = Maps.newHashMap();
        vars2CompleteTask.put(WorkFlowConstant.VAR_NAME_APPROVE_CODE, WorkFlowConstant.APPROVE_CODE_YES);

        //设置流程级别变量或者可以在这一步的listener中设置auditors变量

        runtimeService.setVariable(task.getExecutionId(), "auditors", Lists.newArrayList("a", "b", "c"));

        taskService.complete(task.getId(), vars2CompleteTask, true);

        taskQuery = taskQuery.processInstanceId(instanceId).active();

        log.info("当前活跃的任务数:{}", taskQuery.count());

        for (Task t : taskQuery.list()) {
            TaskQuery tqInner = taskService.createTaskQuery().taskId(t.getId()).active();
            if (tqInner.count() != 1) {
                continue;
            }
            Map<String, Object> varsLocal = Maps.newHashMap();
            varsLocal.put(WorkFlowConstant.VAR_NAME_APPROVE_CODE, WorkFlowConstant.APPROVE_CODE_YES);
            taskService.complete(t.getId(), varsLocal, true);
            log.info("当前办理任务Id:{}, 任务名称:{}, 待办人员:{}", t.getId(), t.getName(), t.getAssignee());
        }

        taskQuery = taskQuery.processInstanceId(instanceId).active();

        log.info("当前活跃的任务数:{}", taskQuery.count());


    }


}
