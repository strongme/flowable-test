package cn.strongme.flowabletest.flow;

/**
 * description:
 * email: <a href="strongwalter2014@gmail.com">阿水</a>
 *
 * @author 阿水
 * @date 2019-04-26 17:53.
 */


public interface WorkFlowConstant {

    String APPROVE_CODE_YES = "00";
    String APPROVE_CODE_NO = "01";

    /**
     * 唯一的工作流定义key
     */
    String WORKFLOW_ID = "business_workflow";

    String WORKFLOW_START_ID = "start";
    String WORKFLOW_END_ID = "end";

    String VAR_NAME_APPROVE_CODE = "approveCode";
    String VAR_NAME_PASS = "pass";
    String VAR_NAME_MULTI_AUDITOR = "multiAuditor";
    String VAR_NAME_MULTI_AUDIT_FINISH = "multiAuditFinish";

    String DEFAULT_START_NAME = "开始";
    String DEFAULT_END_NAME = "结束";
    String DEFAULT_APPROVE_NAME = "审批";
    String DEFAULT_AUDIT_NAME = "审核";


    String EXPRESSION_ASSIGNEE_APPROVER = "${approver}";
    String EXPRESSION_ASSIGNEE_AUDITOR = "${auditor}";
    String EXPRESSION_VAR_MULTI_AUDITOR = "${multiAuditor}";
    String EXPRESSION_COLLECTION_AUDITORS = "${auditors}";

    String CONDITION_HAVE_AUDITORS = "${vars:notEmpty(auditors)}";
    String CONDITION_HAVE_APPROVER = "${vars:exists(approver)}";
    String CONDITION_DOES_NOT_HAVE_APPROVER = "${!vars:exists(approver)}";
    String CONDITION_PASS_YES = "${vars:eq(pass,true)}";
    String CONDITION_PASS_NO = "${vars:eq(pass,false)}";
    String CONDITION_PASS_YES_OR_DOES_NOT_HAVE_ARRPOVER = "${vars:eq(pass,true) || !vars:exists(approver)}";
    String CONDITION_MULTI_AUDIT_FINISH = "${vars:exists("+ VAR_NAME_MULTI_AUDIT_FINISH +")}";

    String DEFAULT_APPROVE_TASK_LISTENER_CLASS_NAME = DefaultApproveTaskListener.class.getCanonicalName();
    String DEFAULT_APPROVE_WITH_AUDIT_TASK_LISTENER_CLASS_NAME = DefaultApproveWithAuditTaskListener.class.getCanonicalName();

}
