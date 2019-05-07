package cn.strongme.flowabletest.flow;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.repository.Deployment;
import org.flowable.validation.ValidationError;
import org.flowable.validation.validator.impl.BpmnModelValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static cn.strongme.flowabletest.flow.WorkFlowConstant.*;

/**
 * description: TODO 记得补全非空判断
 * email: <a href="strongwalter2014@gmail.com">阿水</a>
 *
 * @author 阿水
 * @date 2019-04-26 09:09.
 */

@Component
public class BusinessWorkFlowServiceImpl implements BusinessWorkFlowService {

    private static final String EDITING_WORKFLOW_KEY = "editing-workflow-key";

    @Autowired
    private RepositoryService repositoryService;

    private Map<String, Process> EDITING_PROCESS = Maps.newConcurrentMap();

    private BpmnModelValidator validator = new BpmnModelValidator();

    private String generateSid() {
        return "sid-" + UUID.randomUUID().toString().toUpperCase();
    }

    @Override
    public String createWorkFlow() {
        Process process = new Process();
        process.setId(WORKFLOW_ID);
        String uuid = generateSid();
        EDITING_PROCESS.put(uuid, process);
        return uuid;
    }

    private void addToProcess(String currentEditWorkFlowStoredKey, FlowElement... flowElements) {
        if (flowElements.length == 0) return;
        Process process = EDITING_PROCESS.get(currentEditWorkFlowStoredKey);
        for (FlowElement flowElement : flowElements) {
            process.removeFlowElement(flowElement.getId());
            process.addFlowElement(flowElement);
        }
        EDITING_PROCESS.put(currentEditWorkFlowStoredKey, process);
    }

    private void removeFromProcess(String currentEditWorkFlowStoredKey, FlowElement... flowElements) {
        if (flowElements.length == 0) return;
        Process process = EDITING_PROCESS.get(currentEditWorkFlowStoredKey);
        for (FlowElement flowElement : flowElements) {
            process.removeFlowElement(flowElement.getId());
        }
        EDITING_PROCESS.put(currentEditWorkFlowStoredKey, process);
    }


    @Override
    public String createStart(String currentEditWorkFlowStoredKey, String name) {
        StartEvent startEvent = new StartEvent();
        startEvent.setId(WORKFLOW_START_ID);
        startEvent.setName(StringUtils.isBlank(name) ? DEFAULT_CN_NAME_START : name);
        addToProcess(currentEditWorkFlowStoredKey, startEvent);
        return WORKFLOW_START_ID;
    }

    @Override
    public String createEnd(String currentEditWorkFlowStoredKey, String name) {
        EndEvent endEvent = new EndEvent();
        endEvent.setId(WORKFLOW_END_ID);
        endEvent.setName(StringUtils.isBlank(name) ? DEFAULT_CN_NAME_END : name);
        addToProcess(currentEditWorkFlowStoredKey, endEvent);
        return WORKFLOW_END_ID;
    }

    @Override
    public String connect(String currentEditWorkFlowStoredKey, String fromId, String toId, String condition) {
        SequenceFlow sequenceFlow = createSequenceFlow(fromId, toId, condition);
        addToProcess(currentEditWorkFlowStoredKey, sequenceFlow);
        return sequenceFlow.getId();
    }

    @Override
    public CreateOperationResult createApprove(String currentEditWorkFlowStoredKey, String name, String assigneeGroup) {

        //创建跳过进入网关
        ExclusiveGateway ignoreGatewayIn = new ExclusiveGateway();
        ignoreGatewayIn.setId(generateSid());

        //创建判断审批通过与否网关
        ExclusiveGateway judgeGatewayOut = new ExclusiveGateway();
        judgeGatewayOut.setId(generateSid());

        //创建审批人员任务
        UserTask userTask = createApproveUserTask(name, assigneeGroup, false);

        //创建跳过该审批的连线，没有审批人
        SequenceFlow ignoreYesSeqFlow = createSequenceFlow(ignoreGatewayIn.getId(), judgeGatewayOut.getId(), CONDITION_DOES_NOT_HAVE_APPROVER);

        //创建不跳过审批的flow，有审批人
        SequenceFlow ignoreNoSeqFlow = createSequenceFlow(ignoreGatewayIn.getId(), userTask.getId(), CONDITION_HAVE_APPROVER);

        //创建提交审批意见到排他网关的连线的连线
        SequenceFlow submitApproveFlow = createSequenceFlow(userTask.getId(), judgeGatewayOut.getId());

        //将元素添加到Process
        addToProcess(currentEditWorkFlowStoredKey,
                ignoreGatewayIn, judgeGatewayOut,
                userTask,
                ignoreYesSeqFlow, ignoreNoSeqFlow, submitApproveFlow
        );

        //构建返回的Id信息
        CreateOperationResult result = new CreateOperationResult();
        result.setIncome(new OperationPortIncome(ignoreGatewayIn.getId()));
        result.setOutput(new OperationPortOutputYesAndNo(judgeGatewayOut.getId(), CONDITION_PASS_YES_OR_DOES_NOT_HAVE_ARRPOVER, CONDITION_PASS_NO));

        return result;
    }

    @Override
    public CreateOperationAuditResult createAudit(String currentEditWorkFlowStoredKey, String name, String assigneeGroup) {
        //创建跳过进入网关
        ExclusiveGateway ignoreGatewayIn = new ExclusiveGateway();
        ignoreGatewayIn.setId(generateSid());

        //创建判断审批通过与否网关
        ExclusiveGateway judgeGatewayOut = new ExclusiveGateway();
        judgeGatewayOut.setId(generateSid());

        //创建审核人任务(1)
        UserTask userTaskAudit = createAuditUserTask(name, assigneeGroup, false);

        //创建进入网关与审核任务连线
        SequenceFlow ignore2AuditSeqFlow = createSequenceFlow(ignoreGatewayIn.getId(), userTaskAudit.getId());

        //创建审核任务与判断网关的连线
        SequenceFlow audit2JudgeSeqFlow = createSequenceFlow(userTaskAudit.getId(), judgeGatewayOut.getId());

        addToProcess(currentEditWorkFlowStoredKey,
                ignoreGatewayIn, judgeGatewayOut,
                userTaskAudit,
                ignore2AuditSeqFlow, audit2JudgeSeqFlow
        );

        CreateOperationAuditResult result = new CreateOperationAuditResult();
        result.setIncome(new OperationPortIncome(ignoreGatewayIn.getId()));
        result.setOutput(new OperationPortOutput(judgeGatewayOut.getId()));

        return result;
    }

    @Override
    public CreateOperationResult createApproveAndAudit(String currentEditWorkFlowStoredKey, String approveName, String auditName, String approveAssigneeGroup, String auditAssigneeGroup) {

        //创建跳过进入网关
        ExclusiveGateway ignoreGatewayIn = new ExclusiveGateway();
        ignoreGatewayIn.setId(generateSid());

        //创建判断审批通过与否网关
        ExclusiveGateway judgeGatewayOut = new ExclusiveGateway();
        judgeGatewayOut.setId(generateSid());

        //创建fork inclusive gateway
        InclusiveGateway forkGateway = new InclusiveGateway();
        forkGateway.setId(generateSid());

        //创建join inclusive gateway
        InclusiveGateway joinGateway = new InclusiveGateway();
        joinGateway.setId(generateSid());

        //创建审批人任务(1)
        UserTask userTaskApprove = createApproveUserTask(approveName, approveAssigneeGroup, true);

        //创建审核人任务(n)
        UserTask userTaskAudit = createAuditUserTask(auditName, auditAssigneeGroup, true);

        //创建进入网关与fork连线
        SequenceFlow ignoreIn2forkSeqFlow = createSequenceFlow(ignoreGatewayIn.getId(), forkGateway.getId(), CONDITION_HAVE_APPROVER);

        //创建进入网关与审批判断网关连线
        SequenceFlow ignoreIn2JudgeSeqFlow = createSequenceFlow(ignoreGatewayIn.getId(), judgeGatewayOut.getId(), CONDITION_DOES_NOT_HAVE_APPROVER);

        //创建fork与审批任务连线
        SequenceFlow fork2ApproveSeqFlow = createSequenceFlow(forkGateway.getId(), userTaskApprove.getId(), CONDITION_HAVE_APPROVER);

        //创建审批任务与join连线
        SequenceFlow approve2Join = createSequenceFlow(userTaskApprove.getId(), joinGateway.getId());

        //创建fork与审核任务连线
        SequenceFlow fork2AuditSeqFlow = createSequenceFlow(forkGateway.getId(), userTaskAudit.getId(), CONDITION_HAVE_AUDITORS);

        //创建审核任务与join连线
        SequenceFlow audit2Join = createSequenceFlow(userTaskAudit.getId(), joinGateway.getId());

        //创建join与judge网关连线
        SequenceFlow join2Judge = createSequenceFlow(joinGateway.getId(), judgeGatewayOut.getId());

        addToProcess(currentEditWorkFlowStoredKey,
                ignoreGatewayIn, judgeGatewayOut, forkGateway, joinGateway,
                userTaskApprove, userTaskAudit,
                ignoreIn2forkSeqFlow, ignoreIn2JudgeSeqFlow, fork2ApproveSeqFlow, approve2Join, fork2AuditSeqFlow,
                audit2Join, join2Judge
        );

        CreateOperationResult result = new CreateOperationResult();
        result.setIncome(new OperationPortIncome(ignoreGatewayIn.getId()));
        result.setOutput(new OperationPortOutputYesAndNo(judgeGatewayOut.getId(), CONDITION_PASS_YES_OR_DOES_NOT_HAVE_ARRPOVER, CONDITION_PASS_NO));

        return result;
    }

    @Override
    public boolean validate(String currentEditWorkFlowStoredKey) {
        Process process = EDITING_PROCESS.get(currentEditWorkFlowStoredKey);
        BpmnModel model = new BpmnModel();
        model.addProcess(process);
        List<ValidationError> errors = com.google.common.collect.Lists.newArrayList();
        validator.validate(model, errors);
        return errors.isEmpty();
    }

    @Override
    public Deployment deploy(String currentEditWorkFlowStoredKey) {
        Process process = EDITING_PROCESS.get(currentEditWorkFlowStoredKey);
        BpmnModel model = new BpmnModel();
        model.addProcess(process);
        List<ValidationError> errors = com.google.common.collect.Lists.newArrayList();
        validator.validate(model, errors);
        if (!errors.isEmpty()) {
            throw new EditWorkFlowException("当前编辑的流程有(" + errors.size() + ")条错误");
        }
        new BpmnAutoLayout(model).execute();
        Deployment deployment = repositoryService
                .createDeployment()
                .addBpmnModel(EDITING_WORKFLOW_KEY + "_" + DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN) + ".bpmn", model)
                .name("动态流程定义部署")
                .deploy();
        return deployment;
    }


    SequenceFlow createSequenceFlow(String from, String to) {
        return createSequenceFlow(from, to, null);
    }

    SequenceFlow createSequenceFlow(String from, String to, String conditionExpression) {
        SequenceFlow flow = new SequenceFlow();
        flow.setId(generateSid());
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        flow.setConditionExpression(conditionExpression);
        return flow;
    }

    FlowableListener createTaskListener(String eventName, String listenerFullClassName) {
        FlowableListener taskListener = new FlowableListener();
        taskListener.setId(generateSid());
        taskListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
        taskListener.setImplementation(listenerFullClassName);
        taskListener.setEvent(eventName);
        return taskListener;
    }

    /**
     * 创建审批人UserTask
     *
     * @param approveName
     * @param approveAssigneeGroup
     * @return
     */
    private UserTask createApproveUserTask(String approveName, String approveAssigneeGroup, boolean withAudit) {
        UserTask userTaskApprove = new UserTask();
        userTaskApprove.setId(generateSid());
        userTaskApprove.setName(StringUtils.isBlank(approveName) ? DEFAULT_CN_NAME_APPROVE : approveName);
        userTaskApprove.setAssignee(EXPRESSION_ASSIGNEE_APPROVER);
        userTaskApprove.setCandidateGroups(Lists.newArrayList(approveAssigneeGroup));

        FlowableListener onComplete = createTaskListener(TaskListener.EVENTNAME_COMPLETE,
                withAudit ? DEFAULT_APPROVE_WITH_AUDIT_TASK_LISTENER_CLASS_NAME : DEFAULT_APPROVE_TASK_LISTENER_CLASS_NAME
        );
        userTaskApprove.setTaskListeners(Lists.newArrayList(onComplete));
        return userTaskApprove;
    }

    /**
     * 创建审核人任务
     *
     * @param auditName
     * @param auditAssigneeGroup
     * @param multi              是否开启多人审核任务
     * @return
     */
    private UserTask createAuditUserTask(String auditName, String auditAssigneeGroup, boolean multi) {
        UserTask userTaskAudit = new UserTask();
        userTaskAudit.setId(generateSid());
        userTaskAudit.setName(StringUtils.isBlank(auditName) ? DEFAULT_CN_NAME_AUDIT : auditName);
        if (multi) {
            userTaskAudit.setAssignee(EXPRESSION_VAR_MULTI_AUDITOR);

            MultiInstanceLoopCharacteristics loopCharacteristics = new MultiInstanceLoopCharacteristics();
            loopCharacteristics.setSequential(false);
            loopCharacteristics.setInputDataItem(EXPRESSION_COLLECTION_AUDITORS);
            loopCharacteristics.setElementVariable(VAR_NAME_MULTI_AUDITOR);
            loopCharacteristics.setCompletionCondition(CONDITION_MULTI_AUDIT_FINISH);

            userTaskAudit.setLoopCharacteristics(loopCharacteristics);
        } else {
            userTaskAudit.setAssignee(EXPRESSION_ASSIGNEE_AUDITOR);
            userTaskAudit.setCandidateGroups(Lists.newArrayList(auditAssigneeGroup));
        }
        return userTaskAudit;
    }

}
