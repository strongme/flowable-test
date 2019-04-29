package cn.strongme.flowabletest.flow;

import org.apache.commons.lang3.StringUtils;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * description:
 * email: <a href="strongwalter2014@gmail.com">阿水</a>
 *
 * @author 阿水
 * @date 2019-04-26 17:50.
 */


public class DefaultApproveWithAuditTaskListener extends DefaultApproveTaskListener {

    private static final long serialVersionUID = -8382992197913113566L;

    @Override
    protected void handleComplete(DelegateTask delegateTask) {
        super.handleComplete(delegateTask);
        //检查当前任务是否提交approveCode
        String approveCode = delegateTask.getVariableLocal(WorkFlowConstant.VAR_NAME_APPROVE_CODE, String.class);
        if (StringUtils.isBlank(approveCode)) {
            throw new RuntimeException("缺少意见参数");
        }
        delegateTask.setVariable(WorkFlowConstant.VAR_NAME_MULTI_AUDIT_FINISH, WorkFlowConstant.APPROVE_CODE_YES.equals(approveCode));
    }

}
