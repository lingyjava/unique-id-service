package com.lingyuan.uniqueid.service;

import com.lingyuan.uniqueid.utils.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author LingYuan
 */
@Service
public class SnowflakeIdGeneratorService {

    @Value("${id-generator.machine-id}")
    private long machineId;

    private final Logger logger = LoggerFactory.getLogger(SnowflakeIdGeneratorService.class);

    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(machineId);

    public long generateSnowflakeId() {
        return idGenerator.generateId();
    }
}

