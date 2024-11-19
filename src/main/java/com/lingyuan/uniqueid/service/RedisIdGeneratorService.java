package com.lingyuan.uniqueid.service;

import com.lingyuan.uniqueid.config.IdGeneratorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author LingYuan
 */
@Service
@ConditionalOnProperty(name = "id-generator.enable-redis", havingValue = "true")
public class RedisIdGeneratorService {

    private final StringRedisTemplate redisTemplate;
    private final IdGeneratorConfig idGeneratorConfig;

    private final Logger logger = LoggerFactory.getLogger(RedisIdGeneratorService.class);

    @Autowired
    public RedisIdGeneratorService(StringRedisTemplate redisTemplate,
                                   IdGeneratorConfig idGeneratorConfig) {
        this.redisTemplate = redisTemplate;
        this.idGeneratorConfig = idGeneratorConfig;
    }

    /**
     * 根据 keyPrefix 生成唯一的 Redis ID。
     *
     * @param keyPrefix 业务前缀，必须是 KeyPrefix 定义的值
     * @return 唯一的 ID
     * @throws IllegalArgumentException 如果 keyPrefix 无效
     */
    public long generateRedisId(String keyPrefix) {
        // 校验 keyPrefix 是否有效
        if (!idGeneratorConfig.isValidKeyPrefix(keyPrefix)) {
            throw new IllegalArgumentException("Invalid keyPrefix: " + keyPrefix);
        }

        // Redis 的键格式为 "keyPrefix:id"
        Long result = redisTemplate.opsForValue().increment(keyPrefix.toUpperCase() + ":id");
        return Optional.ofNullable(result)
                .orElseThrow(() -> new IllegalStateException("ID generation failed for keyPrefix: " + keyPrefix));
    }
}
