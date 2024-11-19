# 分布式唯一 ID 生成器

一个基于 Spring Boot 的分布式唯一 ID 生成项目，提供 雪花算法 (Snowflake) 和 Redis 自增 ID 的两种生成方式。系统支持高并发场景，并通过 RESTful API 提供服务，满足不同业务的需求。

---

## 功能特性

- **雪花算法 (Snowflake):**
  - 生成全局唯一的 64 位 ID。
  - 高性能、低延迟，适合分布式场景。
  - 无需依赖中心化协调服务。
- **Redis 自增 ID:**
  - 基于 Redis 的原子递增操作生成顺序 ID。
  - 支持通过业务前缀区分不同业务场景。
  - 数据一致性强，适用于流水号生成。
- **灵活配置:**
  - 支持动态启用或禁用 Redis 功能。
  - 可通过配置文件管理不同 ID 生成模式。
- **高并发支持:**
  - 设计针对高并发场景，保证性能和唯一性。
  - 多线程环境下稳定可靠。

## 快速开始

### 1. 环境准备

- Java 17+
- Maven 3.8+
- Redis 服务器（仅在使用 Redis 模式时需要）

### 2. 安装步骤

#### 克隆代码仓库

```bash
git clone https://github.com/your-repo/id-generator.git
cd id-generator
```

#### 构建项目

```bash
mvn clean install
```

#### 启动项目

```bash
mvn spring-boot:run 
```

### 3. 配置说明

#### 主要配置项

项目中配置文件为 `application.properties`，以下为主要配置项：

```properties
# 服务端口
server.port=8080
spring.application.name=unique-id-service

# 雪花算法机器 ID，运行在不同机器上应不相同
id-generator.machine-id=1

# Redis 连接配置
id-generator.enable-redis=false
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.database=0
spring.data.redis.timeout=5000
spring.data.redis.password=

# 定义允许的 Redis Key 前缀，使用逗号分隔的字符串
id-generator.valid-key-prefixes=order,product
```

## 接口文档

### 1. 生成雪花算法 ID

- **请求方式**: `GET`
- **URL**: `/api/id/snowflake`
- **响应示例**:

  ```json
  {
    "code": 200,
    "message": "Success",
    "data": 249448456052015104
  }
  ```

### 2. 生成 Redis 自增 ID

- **请求方式**: `GET`
- **URL**: `/api/id/redis/{keyPrefix}`
- **请求参数**:
  - keyPrefix（路径参数）：业务前缀，例如 `order` 或 `product`.
- **响应示例**:

  ```json
  {
    "code": 200,
    "message": "Success",
    "data": 1007
  }
  ```

## 运行测试

### 高并发测试

使用 JUnit 进行高并发场景模拟，代码位于 `test` 目录：

```java
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

```

## 注意事项

如果启用了 Redis，确保 Redis 服务已正确启动，并且可以通过 `application.properties` 的配置文件访问。

项目默认端口为 `8080`，可通过配置文件修改。

## 贡献

欢迎提交 Issue 或 Pull Request 来改进项目。

## 许可证

本项目基于 MIT License 开源。
