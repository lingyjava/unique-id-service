package com.lingyuan.uniqueid;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class IdGeneratorConcurrencyTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void testSnowflakeIdConcurrency() throws InterruptedException {
        String SNOWFLAKE_URL = "http://localhost:8080/api/id/snowflake";
        // 模拟 1000 次请求
        testConcurrentRequests(SNOWFLAKE_URL, 1000);
    }

    @Test
    public void testRedisIdConcurrency() throws InterruptedException {
        String REDIS_URL = "http://localhost:8080/api/id/redis/product";
        testConcurrentRequests(REDIS_URL, 100);
    }

    private void testConcurrentRequests(String url, int numberOfRequests) throws InterruptedException {
        // 并发线程数
        ExecutorService executor = Executors.newFixedThreadPool(100);

        // 线程安全的 Set
        Set<String> ids = Collections.synchronizedSet(new HashSet<>());

        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                try {
                    String response = restTemplate.getForObject(url, String.class);
                    Map map = new ObjectMapper().readValue(response, Map.class);
                    if (HttpStatus.OK.value() != Integer.parseInt(map.get("code").toString())) {
                        System.err.println("Request failed: " + url + response);
                        return;
                    }
                    String data = map.get("data").toString();
                    System.out.println("Generate ID: " + data);
                    if (!ids.add(data)) {
                        System.err.println("Duplicate ID: " + data);
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            });
        }

        // 关闭线程池并等待完成
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println("Completed " + numberOfRequests + " requests to " + url);
        System.out.println("Unique IDs generated: " + ids.size());
        if (ids.size() != numberOfRequests) {
            System.err.println("Test failed: Duplicate IDs detected");
        } else {
            System.out.println("Test passed: All IDs are unique");
        }
    }
}
