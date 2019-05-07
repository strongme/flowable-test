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

    String DEFAULT_CN_NAME_START = "开始";
    String DEFAULT_CN_NAME_END = "结束";
    String DEFAULT_CN_NAME_APPROVE = "审批";
    String DEFAULT_CN_NAME_AUDIT = "审核";

    /**
     * 唯一的工作流定义key
     */
    String WORKFLOW_ID = "business_workflow";

    String WORKFLOW_START_ID = "start";
    String WORKFLOW_END_ID = "end";

    String VAR_NAME_APPROVER = "approver";//审批人员办理人变量名
    String VAR_NAME_AUDITOR = "auditor";//审核人员办理人变量名
    String VAR_NAME_AUDITORS = "auditors";//审批人员列表
    String VAR_NAME_APPROVE_CODE = "approveCode"; //审批通过不通过变量名称local
    String VAR_NAME_PASS = "pass";//流程通过网关的变量
    String VAR_NAME_MULTI_AUDITOR = "multiAuditor";//多实例审核中的办理人变量名称
    String VAR_NAME_MULTI_AUDIT_FINISH = "multiAuditFinish";//多实例是否完成的变量标识

    String EXPRESSION_ASSIGNEE_APPROVER = "${" + VAR_NAME_APPROVER + "}";//审批办理人员
    String EXPRESSION_ASSIGNEE_AUDITOR = "${" + VAR_NAME_AUDITOR + "}";//审核办理人员
    String EXPRESSION_VAR_MULTI_AUDITOR = "${"+VAR_NAME_MULTI_AUDITOR+"}";//多实例审核办理人
    String EXPRESSION_COLLECTION_AUDITORS = "${" + VAR_NAME_AUDITORS + "}";//多实例所需要的办理人集合

    String CONDITION_HAVE_AUDITORS = "${vars:notEmpty(" + VAR_NAME_AUDITORS + ")}";//有审核办理人集合变量条件
    String CONDITION_HAVE_APPROVER = "${vars:exists(" + VAR_NAME_APPROVER + ")}";//有审批办理人变量条件
    String CONDITION_DOES_NOT_HAVE_APPROVER = "${!vars:exists(" + VAR_NAME_APPROVER + ")}";//没有审批办理人条件
    String CONDITION_PASS_YES = "${vars:eq(" + VAR_NAME_PASS + ",true)}";//流程网关通过
    String CONDITION_PASS_NO = "${vars:eq(" + VAR_NAME_PASS + ",false)}";//流程网关未通过
    String CONDITION_PASS_YES_OR_DOES_NOT_HAVE_ARRPOVER = "${vars:eq(" + VAR_NAME_PASS + ",true) || !vars:exists(" + VAR_NAME_APPROVER + ")}";//流程网关通过或者不存在审批人员条件
    String CONDITION_MULTI_AUDIT_FINISH = "${vars:exists(" + VAR_NAME_MULTI_AUDIT_FINISH + ")}";//判断多实例审核任务完成条件

    String DEFAULT_APPROVE_TASK_LISTENER_CLASS_NAME = DefaultApproveTaskListener.class.getCanonicalName();
    String DEFAULT_APPROVE_WITH_AUDIT_TASK_LISTENER_CLASS_NAME = DefaultApproveWithAuditTaskListener.class.getCanonicalName();

}
