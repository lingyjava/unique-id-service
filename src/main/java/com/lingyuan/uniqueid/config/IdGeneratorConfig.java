package com.lingyuan.uniqueid.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.util.List;

/**
 * 读取允许的 keyPrefix 配置
 *
 * @author LingYuan
 */
@Configuration
public class IdGeneratorConfig {

    // 通过配置项读取有效的 Key 前缀
    @Value("#{'${id-generator.valid-key-prefixes}'.split(',')}")
    private List<String> validKeyPrefixes;

    public List<String> getValidKeyPrefixes() {
        return validKeyPrefixes;
    }

    public boolean isValidKeyPrefix(String keyPrefix) {
        return validKeyPrefixes.contains(keyPrefix);
    }
}
