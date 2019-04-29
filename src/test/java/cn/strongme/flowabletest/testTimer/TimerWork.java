package cn.strongme.flowabletest.testTimer;

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
public class TimerWork implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Timer Reminding EMAIL/SMS");
    }
}
