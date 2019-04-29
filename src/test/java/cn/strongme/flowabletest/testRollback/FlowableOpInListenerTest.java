package cn.strongme.flowabletest.testRollback;

import cn.strongme.flowabletest.CacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by 阿水 on 2018/5/25 23:31.
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {RedisAutoConfiguration.class, CacheConfig.class}
)
public class FlowableOpInListenerTest extends FlowableTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        log.info("Setup...");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        log.info("Teardown...");
    }

    @Deployment(resources = {"processes/测试撤回功能.bpmn20.xml"})
    @Test
    public void testProcessOpInAnotherProcessListener() {
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("test-rollback");
        log.info("ProcessInstance Id : {}, Id: {}", instance.getProcessInstanceId(), instance.getProcessInstanceId());
        String processInstanceId = instance.getProcessInstanceId();




    }


}
