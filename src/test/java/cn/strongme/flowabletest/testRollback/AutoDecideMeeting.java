package cn.strongme.flowabletest.testRollback;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * description:
 * email: <a href="strongwalter2014@gmail.com">阿水</a>
 *
 * @author 阿水
 * @date 2019-03-26 22:47.
 */

@Slf4j
public class AutoDecideMeeting implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Inside auto decide service...");
        Integer finalSum = execution.getVariable("finalSum", Integer.class);
        if (finalSum == 3) {
            execution.setVariable("approveCode", "00");
        }else {
            execution.setVariable("approveCode", "01");
        }
    }
}
