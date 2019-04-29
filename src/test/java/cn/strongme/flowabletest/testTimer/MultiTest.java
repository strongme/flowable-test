package cn.strongme.flowabletest.testTimer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableTestCase;

import java.util.Map;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Created by 阿水 on 2018/5/25 23:31.
 */
@Slf4j
public class MultiTest extends FlowableTestCase {

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

    @Deployment(resources = {"processes/多实例.bpmn20.xml"})
    public void testProcessRollback() {

        Map<String, Object> vars = Maps.newHashMap();
        vars.put("userList", Lists.newArrayList("a", "b", "c"));

        ProcessInstance instance = runtimeService.startProcessInstanceByKey("one-multi", vars);

        BpmnModel bpmnModel = repositoryService.getBpmnModel(instance.getProcessDefinitionId());

        assertThat(bpmnModel).isNotNull();

    }


}
