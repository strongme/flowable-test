package cn.strongme.flowabletest;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * description:
 * email: <a href="strongwalter2014@gmail.com">阿水</a>
 *
 * @author 阿水
 * @date 2019-03-26 23:04.
 */

@Slf4j
public class SubmitOpinionListener implements TaskListener {

    private static final long serialVersionUID = -2190725724306581189L;

    @Override
    public void notify(DelegateTask delegateTask) {
        if (TaskListener.EVENTNAME_COMPLETE.equals(delegateTask.getEventName())) {
            Integer code = delegateTask.getVariableLocal("code", Integer.class);
            log.info("Code is : {}", code);
            Integer finalSum = delegateTask.getVariable("finalSum", Integer.class);
            if (finalSum == null) {
                finalSum = 0;
            }
            finalSum += code;
            delegateTask.setVariable("finalSum", finalSum);
        }
    }
}
