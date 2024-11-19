package com.lingyuan.uniqueid.controller;

import com.lingyuan.uniqueid.exception.BusinessException;
import com.lingyuan.uniqueid.response.ApiResponse;
import com.lingyuan.uniqueid.service.SnowflakeIdGeneratorService;
import com.lingyuan.uniqueid.service.RedisIdGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @author LingYuan
 */
@RestController
@RequestMapping("/api/id")
public class IdGeneratorController {

    private final SnowflakeIdGeneratorService snowflakeService;

    @Autowired(required = false)
    private RedisIdGeneratorService redisService;

    @Autowired
    public IdGeneratorController(SnowflakeIdGeneratorService snowflakeService) {
        this.snowflakeService = snowflakeService;
    }

    /**
     * 基于雪花算法生成全球唯一 ID
     * */
    @GetMapping("/snowflake")
    public ApiResponse<Long> generateSnowflakeId() {
        return ApiResponse.success(snowflakeService.generateSnowflakeId());
    }

    /**
     * 基于 Redis 生成 ID
     * Redis 生成的 ID 为流水号，需根据 keyPrefix 进行区分
     * */
    @GetMapping("/redis/{keyPrefix}")
    public ApiResponse<Long> generateRedisId(@PathVariable String keyPrefix) {
        if (redisService == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Redis ID generation is disabled");
        }
        if (keyPrefix == null) {
            throw new IllegalArgumentException("keyPrefix cannot be null");
        }
        return ApiResponse.success(redisService.generateRedisId(keyPrefix));
    }
}
