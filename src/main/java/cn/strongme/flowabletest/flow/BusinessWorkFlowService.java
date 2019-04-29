package cn.strongme.flowabletest.flow;

import org.flowable.engine.repository.Deployment;

/**
 * description: 业务工作流程绘制工具
 * email: <a href="strongwalter2014@gmail.com">阿水</a>
 *
 * @author 阿水
 * @date 2019-04-25 17:15.
 */


public interface BusinessWorkFlowService {

    /**
     * 创建空白工作流，并将此Process进行持久化（存储到redis）
     *
     * @return 存储到redis所使用的key值
     */
    String createWorkFlow();

    /**
     * 为指定的当前在编辑的流程添加开始事件
     *
     * @param currentEditWorkFlowStoredKey 当前在编辑的存储在redis中的Process的key值
     * @return
     */
    String createStart(String currentEditWorkFlowStoredKey, String name);

    /**
     * 为指定的当前在编辑的流程添加结束事件
     *
     * @param currentEditWorkFlowStoredKey 当前在编辑的存储在redis中的Process的key值
     * @param name
     * @return
     */
    String createEnd(String currentEditWorkFlowStoredKey, String name);

    /**
     * 将指定的seqFlow的from设置为fromId
     * task <-> (from)<---seqFlow--->(to)
     *
     * @param currentEditWorkFlowStoredKey
     * @param fromId
     * @param toId
     * @param condition
     * @return
     */
    String connect(String currentEditWorkFlowStoredKey, String fromId, String toId, String condition);

    /**
     * 为指定的当前在编辑的流程添加审批事件
     * 由一个userTask+一个exclusiveGateway组成
     *
     * @param currentEditWorkFlowStoredKey
     * @param name
     * @param assigneeGroup                审批人角色分组
     * @return
     */
    CreateOperationResult createApprove(String currentEditWorkFlowStoredKey, String name, String assigneeGroup);


    /**
     * 为指定的当前在编辑的流程添加审核事件（1审批+n个或者0个审核人）
     *
     * @param currentEditWorkFlowStoredKey * @param name
     *                                     * @param assigneeGroup                审核角色分组
     */
    CreateOperationAuditResult createAudit(String currentEditWorkFlowStoredKey, String name, String assigneeGroup);

    /**
     * 为指定的当前在编辑的流程添加审核事件（1审批+n个或者0个审核人）
     *
     * @param currentEditWorkFlowStoredKey
     * @param approveName
     * @param auditName
     * @param approveAssigneeGroup         审批人角色分组
     * @param auditAssigneeGroup           审核人角色分组
     */
    CreateOperationResult createApproveAndAudit(String currentEditWorkFlowStoredKey, String approveName, String auditName, String approveAssigneeGroup, String auditAssigneeGroup);

    /**
     * 验证指定的流程是否为合法的流程
     *
     * @param currentEditWorkFlowStoredKey
     * @return
     */
    boolean validate(String currentEditWorkFlowStoredKey);

    /**
     * 发布当前在编辑的流程到工作流引擎中
     *
     * @param currentEditWorkFlowStoredKey
     * @return 返回部署id
     */
    Deployment deploy(String currentEditWorkFlowStoredKey);

}
