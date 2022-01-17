/*
 * Copyright (c) 2021-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.amrjlg.id;

/**
 * the id composition and structure
 * <pre>{
 * 10                  20                  30                  40                  50                  60                64
 * 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 * 0|0 0 0 0 0 0 0 0 0 0 0 0|0 0 0|0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0|0 0 0 0 0 0 0
 *       dataCenter          machineId                               timestamp                                           sequence
 * 1        12                  3                                       41                                                  7
 * }</pre>
 * @author amrjlg
 */

public class Snowflake {
    /**
     * 机器ID
     */
    private final long machineId;
    /**
     * 数据ID
     * 此方案 为获取id时传递的学校id
     */
    private long datacenterId;
    /**
     * 计数从零开始 记数
     */
    private static long sequence = 0;

    /**
     * 机器码字位数 3位 8台机器
     */
    private static final long machineIdBits = 3L;
    /**
     * 数据位 12位 最多支持 4096个学校
     */
    private static final long datacenterIdBits = 12L;
    /**
     * 时间戳 位 41位
     */
    private static final long timestampIdBits = 41L;
    /**
     * 计数器字节数，7位 每秒最多产生 128个id
     */
    private static final long sequenceBits = 7L;

    /**
     * 最大机器ID
     * 用于验证机器码 此方案中为 7
     */
    public static long maxMachineId = ~(-1L << (int) machineIdBits);
    /**
     * 最大数据ID
     * 用于验证学校id
     */
    private static final long maxDatacenterId = ~(-1L << (int) datacenterIdBits);
    /**
     * 生成id时 时间位移位数 此例为 7 位
     */
    private static final long timestampLeftShift = sequenceBits;
    /**
     * 生成id时 机器码 位移位数 此方案 为 48位
     */
    private static final long machineIdShift = sequenceBits + timestampIdBits;
    /**
     * 生成id时 数据中心位移位数 此方案为 51位
     */
    private static final long datacenterIdShift = sequenceBits + timestampIdBits + machineIdBits;
    /**
     * 自增序列号 阈值 此方案 为 127
     */
    public static long sequenceMask = ~(-1L << (int) sequenceBits);
    /**
     * 上次生成id的时间戳
     */
    private static long lastTimestamp = -1L;

    /**
     * 生成id
     *
     * @param datacenterId 数据中心id  此方案中实际为 学校id
     * @return 对应学校范围的分布式 雪花id
     */
    public synchronized long nextId(long datacenterId) {
        if (datacenterId < 0 || datacenterId >= maxDatacenterId) {
            throw new IllegalArgumentException("schoolId must between 0 and " + maxDatacenterId + "now is " + datacenterId);
        }
        long timestamp = getTimestamp();
        //同一微妙中生成ID
        if (lastTimestamp == timestamp) {
            //用&运算计算该微秒内产生的计数是否已经到达上限
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                //一微妙内产生的ID计数已达上限，等待下一微妙
                lastTimestamp = getNextTimestamp();
            }
        } else {
            //不同微秒生成ID
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        /**
         * 开始时间
         */
        long start = 1567999114610L;
        return datacenterId << datacenterIdShift
                | machineId << machineIdShift
                | (timestamp - start) << (timestampLeftShift)
                | sequence;
    }

    /**
     * @return 当时时间戳
     */
    private static long getTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 获取下一个时间戳
     *
     * @return 时间戳
     */
    private static long getNextTimestamp() {
        long currentTimestamp = getTimestamp();
        while (currentTimestamp <= lastTimestamp) {
            currentTimestamp = getTimestamp();
        }
        return currentTimestamp;
    }

    /**
     * 构造函数 第几台生成id的机器
     *
     * @param machineId 机器id
     */
    public Snowflake(int machineId) {
        if (machineId < 0 || machineId >= maxMachineId) {
            throw new IllegalArgumentException(String.format("workerId can't be greater than %d or less than 0 or is null", maxMachineId));
        }
        this.machineId = machineId;
    }

    public Snowflake() {
        String property = System.getProperty("workerId");
        if (property == null) {
            throw new IllegalArgumentException("the system property named workerId has not existed !");
        }
        int machineId = Integer.parseInt(property);
        if (machineId < 0 || machineId >= maxMachineId) {
            throw new IllegalArgumentException(String.format("workerId can't be greater than %d or less than 0", maxMachineId));
        }
        this.machineId = machineId;
    }
}