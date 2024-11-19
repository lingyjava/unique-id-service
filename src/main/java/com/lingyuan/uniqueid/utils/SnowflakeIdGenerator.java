package com.lingyuan.uniqueid.utils;

/**
 * @author LingYuan
 *
 * SnowflakeIdGenerator 实现了雪花算法（Snowflake ID Generation），用于在分布式系统中生成唯一的 ID。
 * 生成的 ID 基于时间戳、机器 ID 和序列号，确保全局唯一性。
 * <p>
 * 该实现不依赖于业务类型，而是通过时间戳、机器 ID 和序列号来确保生成的 ID 唯一。
 * <p>
 * 雪花算法生成的 ID 结构为：
 * 符号位 (1 bit) | 时间戳 (41 bits) | 机器 ID (10 bits) | 序列号 (12 bits) |
 * 时间戳部分提供了时间信息，机器 ID 用于标识不同机器，序列号确保同一毫秒内生成唯一的 ID。
 */
public class SnowflakeIdGenerator {

    /**
     * 自定义纪元时间（起始时间），以毫秒为单位
     * 2023-01-01 00:00:00
     */
    private static final long EPOCH = 1672531200000L;

    // 各部分的位数

    /**
     * 序列号部分占用的位数
     */
    private static final long SEQUENCE_BITS = 12L;
    /**
     * 机器 ID 部分占用的位数
     */
    private static final long MACHINE_ID_BITS = 10L;
    /**
     * 序列号的最大值（4095）
     */
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;
    /**
     * 机器 ID 的最大值（1023）
     */
    private static final long MAX_MACHINE_ID = (1L << MACHINE_ID_BITS) - 1;

    // 位移量

    /**
     * 序列号位移量
     */
    private static final long SEQUENCE_SHIFT = 0L;
    /**
     * 机器 ID 位移量
     */
    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
    /**
     * 时间戳位移量
     */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;

    // 组件

    /**
     * 机器 ID
     */
    private final long machineId;
    /**
     * 序列号
     */
    private long sequence = 0L;
    /**
     * 上次生成 ID 的时间戳
     */
    private long lastTimestamp = -1L;

    /**
     * 构造器，初始化机器 ID。
     *
     * @param machineId 机器 ID，标识唯一机器，必须小于 1024（0-1023）
     */
    public SnowflakeIdGenerator(long machineId) {
        if (machineId > MAX_MACHINE_ID || machineId < 0) {
            throw new IllegalArgumentException("Machine ID must be between 0 and " + MAX_MACHINE_ID);
        }
        this.machineId = machineId;
    }

    /**
     * 生成唯一的 ID。
     *
     * @return 唯一的 ID
     */
    public synchronized long generateId() {
        long timestamp = System.currentTimeMillis();

        // 检查系统时钟是否回拨，如果回拨则抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards, refusing to generate ID for " + (lastTimestamp - timestamp) + " milliseconds");
        }

        // 同一毫秒内生成多个 ID
        if (timestamp == lastTimestamp) {
            // 序列号递增并限制在最大序列号范围内
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // 如果序列号溢出，等待下一毫秒
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置为 0
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 生成并返回唯一的 ID
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (machineId << MACHINE_ID_SHIFT)
                | sequence;
    }

    /**
     * 等待下一毫秒，直到时钟更新。
     *
     * @param lastTimestamp 上一次生成 ID 的时间戳
     * @return 当前时间戳
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        // 等待直到系统时钟变为下一毫秒
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
