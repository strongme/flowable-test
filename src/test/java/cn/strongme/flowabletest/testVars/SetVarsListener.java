package cn.strongme.flowabletest.testVars;

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
public class SetVarsListener implements TaskListener {

    private static final long serialVersionUID = -2190725724306581189L;

    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.setVariable("testVars", "我是最后一步设置的变量");
    }
}
