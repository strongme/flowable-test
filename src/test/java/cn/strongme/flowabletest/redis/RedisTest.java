package cn.strongme.flowabletest.redis;

import cn.strongme.flowabletest.CacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * description:
 * email: <a href="strongwalter2014@gmail.com">阿水</a>
 *
 * @author 阿水
 * @date 2018-11-27 09:51.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {RedisAutoConfiguration.class, CacheConfig.class}
)
@Slf4j
public class RedisTest {

    @Autowired
    @Qualifier("customRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testRedisBoundList() {
        log.info(RedisTest.class.getCanonicalName());
    }

}
