package com.jianbing;

import com.jianbing.utils.DateUtil;
import java.util.concurrent.atomic.LongAdder;

/**
 * 基于雪花算法的分布式唯一ID生成器
 *
 * 1. 64位ID结构（时间戳+机房号+机器号+序列号）
 * 2. 支持最大32个机房，每个机房32台机器
 * 3. 单机每秒可生成409.6万个ID（理论值）
 * 4. 内置时钟回拨处理机制
 * 5. 线程安全的高并发序列号生成
 */
public class IdGenerator {
    // ---------- 算法参数配置 ----------
    /** 起始时间戳（2025-01-01 00:00:00） */
    public static final long START_STAMP = DateUtil.get("2025-1-1").getTime();

    /** 机房号位数（5位，范围0-31） */
    public static final long DATA_CENTER_BIT = 5L;
    /** 机器号位数（5位，范围0-31） */
    public static final long MACHINE_BIT = 5L;
    /** 序列号位数（12位，范围0-4095） */
    public static final long SEQUENCE_BIT = 12L;

    /** 机房号最大值计算：~(-1L << 5) = 31 */
    public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT);
    /** 机器号最大值计算：~(-1L << 5) = 31 */
    public static final long MACHINE_MAX = ~(-1L << MACHINE_BIT);
    /** 序列号最大值计算：~(-1L << 12) = 4095 */
    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT);

    /** 时间戳左移位数（5+5+12=22位） */
    public static final long TIMESTAMP_LEFT = DATA_CENTER_BIT + MACHINE_BIT + SEQUENCE_BIT;
    /** 机房号左移位数（5+12=17位） */
    public static final long DATA_CENTER_LEFT = MACHINE_BIT + SEQUENCE_BIT;
    /** 机器号左移位数（12位） */
    public static final long MACHINE_LEFT = SEQUENCE_BIT;

    // ---------- 实例属性 ----------
    /** 机房ID（取值范围：0-31） */
    private final long dataCenterId;
    /** 机器ID（取值范围：0-31） */
    private final long machineId;
    /** 序列号生成器（线程安全） */
    private final LongAdder sequence = new LongAdder();
    /** 上一次生成ID的时间戳（初始值为-1表示未初始化） */
    private long lastTimeStamp = -1L;

    /**
     * 初始化ID生成器
     * @param dataCenterId 机房编号（0-31）
     * @param machineId    机器编号（0-31）
     * @throws IllegalArgumentException 参数越界时抛出异常
     */
    public IdGenerator(long dataCenterId, long machineId) {
        // 参数有效性校验
        if (dataCenterId > DATA_CENTER_MAX || dataCenterId < 0) {
            throw new IllegalArgumentException("机房号必须在0-" + DATA_CENTER_MAX + "之间");
        }
        if (machineId > MACHINE_MAX || machineId < 0) {
            throw new IllegalArgumentException("机器号必须在0-" + MACHINE_MAX + "之间");
        }

        this.dataCenterId  = dataCenterId;
        this.machineId  = machineId;
    }

    /**
     * 生成全局唯一ID（线程安全）
     * @return 64位唯一ID
     * @throws RuntimeException 当时钟回拨超过5ms时抛出异常
     */
    public synchronized long getId() {
        // 获取当前时间戳（相对于起始时间的偏移量）
        long currentTimeStamp = System.currentTimeMillis();
        long timeStamp = currentTimeStamp - START_STAMP;

        // 时钟回拨处理（分级策略）
        if (timeStamp < lastTimeStamp) {
            long offset = lastTimeStamp - timeStamp;
            if (offset <= 5) { // 允许5ms内的回拨
                try {
                    // 等待两倍回拨时间（避免短时间多次回拨）
                    Thread.sleep(offset  << 1);
                    timeStamp = System.currentTimeMillis()  - START_STAMP;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else { // 严重时钟回拨
                throw new RuntimeException("检测到时钟回拨 " + offset + "ms，拒绝生成ID");
            }
        }

        // 序列号管理
        if (timeStamp == lastTimeStamp) {
            sequence.increment();
            if (sequence.sum()  >= SEQUENCE_MAX) { // 序列号溢出
                timeStamp = waitNextTimeStamp(timeStamp);
                sequence.reset();
            }
        } else { // 新时间窗口
            sequence.reset();
        }

        lastTimeStamp = timeStamp;

        // 组合ID（位运算优化）
        return (timeStamp << TIMESTAMP_LEFT)
                | (dataCenterId << DATA_CENTER_LEFT)
                | (machineId << MACHINE_LEFT)
                | sequence.sum();
    }

    /**
     * 等待到下一个时间窗口
     * @param currentTimeStamp 当前时间戳
     * @return 新的有效时间戳
     */
    private long waitNextTimeStamp(long currentTimeStamp) {
        long newTimeStamp;
        do {
            newTimeStamp = System.currentTimeMillis()  - START_STAMP;
        } while (newTimeStamp == currentTimeStamp);
        return newTimeStamp;
    }

    /**
     * 测试用例（模拟2000并发场景）
     */
    public static void main(String[] args) {
        IdGenerator generator = new IdGenerator(1, 2);
        for (int i = 0; i < 2000; i++) {
            new Thread(() -> System.out.println(generator.getId())).start();
        }
    }
}