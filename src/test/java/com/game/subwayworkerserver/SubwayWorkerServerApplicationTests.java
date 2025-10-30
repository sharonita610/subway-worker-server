package com.game.subwayworkerserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SubwayWorkerServerApplicationTests {

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void contextLoads() {
    }

}
