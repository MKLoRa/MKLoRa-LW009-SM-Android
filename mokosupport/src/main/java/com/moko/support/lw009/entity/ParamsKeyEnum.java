package com.moko.support.lw009.entity;


import java.io.Serializable;

public enum ParamsKeyEnum implements Serializable {
    //// 系统相关参数
    KEY_CLOSE(0x10),
    KEY_REBOOT(0x11),
    KEY_RESET(0x12),
    // 时间同步
    KEY_TIME_UTC(0x13),
    // 时区
    KEY_TIME_ZONE(0x14),
    // 芯片MAC
    KEY_CHIP_MAC(0x15),
    // 自检状态
    KEY_SELF_TEST_STATUS(0x16),
    // 产测状态
    KEY_PCBA_STATUS(0x17),
    // 读取当前需求版本
    KEY_DEMAND_VERSION(0x18),
    // 电池电量
    KEY_BATTERY_POWER(0x19),
    // 厂家信息
    KEY_MANUFACTURER(0x1A),
    // 关机信息上报
    KEY_SHUTDOWN_PAYLOAD_ENABLE(0x1B),
    // 低电触发心跳开关
    KEY_LOW_POWER_PAYLOAD_ENABLE(0x1C),
    // 设备心跳间隔
    KEY_HEARTBEAT_INTERVAL(0x1D),
    KEY_PARKING_DETECTION_PAYLOAD_TYPE(0x1E),
    KEY_BLE_SCAN_TIME(0x1F),
    KEY_PAYLOAD_BEACON_COUNT(0x20),
    KEY_NO_PARKING_CALIBRATION_STATUS(0x21),
    KEY_SLAVE_VERSION(0x22),


    //// 蓝牙相关参数
    // 登录是否需要密码
    KEY_PASSWORD_VERIFY_ENABLE(0x30),
    //连接密码
    KEY_PASSWORD(0x31),
    //蓝牙广播超时时间
    KEY_ADV_TIMEOUT(0x32),
    //蓝牙 TX power
    KEY_ADV_TX_POWER(0x33),
    //广播名称
    KEY_ADV_NAME(0x34),
    //广播间隔
    KEY_ADV_INTERVAL(0x35),



    //车位检测功能参数
    KEY_NO_PARKING_CALIBRATION(0x3A),
    KEY_TRIGGER_PARKING_DETECTION(0x3B),
    KEY_TRIGGER_SLAVE_RESET(0x3C),
    KEY_TRIGGER_PARKING_DETECTION_TIMES(0x3D),
    KEY_SLAVE_WORK_MODE(0x3E),
    KEY_PARKING_DETECTION_SENSITIVITY(0x3F),
    KEY_PARKING_DETECTION_MODE(0x40),
    KEY_PARKING_DETECTION_DURATION(0x41),
    KEY_PARKING_DETECTION_CONFIRM_DURATION(0x42),
    KEY_PARKING_DETECTION_THRESHOLD(0x43),
    KEY_PARKING_LOT_TYPE(0x44),
    KEY_AUTO_CALIBRATION_SWITCH(0x45),
    KEY_AUTO_CALIBRATION_THRESHOLD(0x46),
    KEY_AUTO_CALIBRATION_DELAY_SAMPLE_TIME(0x47),
    KEY_RADAR_DELAY_CLOSE_SWITCH(0x48),
    KEY_SLAVE_LOG_SWITCH(0x49),



    ////蓝牙扫描过滤参数
    // RSSI过滤规则
    KEY_FILTER_RSSI(0x51),
    // 广播内容过滤逻辑
    KEY_FILTER_RELATIONSHIP(0x52),
    // 精准过滤MAC开关
    KEY_FILTER_MAC_PRECISE(0x53),
    // 反向过滤MAC开关
    KEY_FILTER_MAC_REVERSE(0x54),
    // MAC过滤规则
    KEY_FILTER_MAC_RULES(0x55),
    // 精准过滤ADV Name开关
    KEY_FILTER_NAME_PRECISE(0x56),
    // 反向过滤ADV Name开关
    KEY_FILTER_NAME_REVERSE(0x57),
    // NAME过滤规则
    KEY_FILTER_NAME_RULES(0x58),
    // 过滤设备类型开关
    KEY_FILTER_RAW_DATA(0x59),
    // iBeacon类型过滤开关
    KEY_FILTER_I_BEACON_ENABLE(0x5A),
    // iBeacon类型Major范围
    KEY_FILTER_I_BEACON_MAJOR_RANGE(0x5B),
    // iBeacon类型Minor范围
    KEY_FILTER_I_BEACON_MINOR_RANGE(0x5C),
    // iBeacon类型UUID
    KEY_FILTER_I_BEACON_UUID(0x5D),
    // eddystone-UID类型过滤开关
    KEY_FILTER_EDDYSTONE_UID_ENABLE(0x5E),
    // eddystone-UID类型Namespace
    KEY_FILTER_EDDYSTONE_UID_NAMESPACE(0x5F),
    // eddystone-UID类型Instance
    KEY_FILTER_EDDYSTONE_UID_INSTANCE(0x60),
    // eddystone-URL类型过滤开关
    KEY_FILTER_EDDYSTONE_URL_ENABLE(0x61),
    // eddystone-URL类型URL
    KEY_FILTER_EDDYSTONE_URL(0x62),
    // eddystone-TLM类型过滤开关
    KEY_FILTER_EDDYSTONE_TLM_ENABLE(0x63),
    // eddystone- TLM类型TLMVersion
    KEY_FILTER_EDDYSTONE_TLM_VERSION(0x64),
    // BXP-iBeacon类型过滤开关
    KEY_FILTER_BXP_I_BEACON_ENABLE(0x65),
    // BXP-iBeacon类型Major范围
    KEY_FILTER_BXP_I_BEACON_MAJOR_RANGE(0x66),
    // BXP-iBeacon类型Minor范围
    KEY_FILTER_BXP_I_BEACON_MINOR_RANGE(0x67),
    // BXP-iBeacon类型UUID
    KEY_FILTER_BXP_I_BEACON_UUID(0x68),
    // BXP-Device类型过滤开关
    KEY_FILTER_BXP_DEVICE(0x69),
    // BeaconX Pro-ACC设备过滤开关
    KEY_FILTER_BXP_ACC(0x6A),
    // BeaconX Pro-T&H设备过滤开关
    KEY_FILTER_BXP_TH(0x6B),
    // BXP-Button类型过滤开关
    KEY_FILTER_BXP_BUTTON_ENABLE(0x6C),
    // BXP-Button类型过滤规则
    KEY_FILTER_BXP_BUTTON_RULES(0x6D),
    // BXP-Tag开关类型过滤开关
    KEY_FILTER_BXP_TAG_ENABLE(0x6E),
    // 精准过滤BXP-Tag开关
    KEY_FILTER_BXP_TAG_PRECISE(0x6F),
    // 反向过滤BXP-Tag开关
    KEY_FILTER_BXP_TAG_REVERSE(0x70),
    // BXP-Tag过滤规则
    KEY_FILTER_BXP_TAG_RULES(0x71),
    //MK-PIR 设备过滤开关
    KEY_FILTER_MK_PIR_ENABLE(0x72),
    //MK-PIR 设备过滤
    //sensor_detection_status
    KEY_FILTER_MK_PIR_DETECTION_STATUS(0x73),
    //MK-PIR 设备过滤
    //sensor_sensitivity
    KEY_FILTER_MK_PIR_SENSOR_SENSITIVITY(0x74),
    //MK-PIR 设备过滤
    //door_status
    KEY_FILTER_MK_PIR_DOOR_STATUS(0x75),
    //MK-PIR 设备过滤
    //delay_response_status
    KEY_FILTER_MK_PIR_DELAY_RES_STATUS(0x76),
    //MK-PIR 设备
    //Major 过滤范围
    KEY_FILTER_MK_PIR_MAJOR(0x77),
    //MK-PIR 设备
    //Minor 过滤范围
    KEY_FILTER_MK_PIR_MINOR(0x78),
    // Unknown设备过滤开关
    KEY_FILTER_OTHER_ENABLE(0x79),
    // 3组unknown过滤规则逻辑
    KEY_FILTER_OTHER_RELATIONSHIP(0x7A),
    // unknown类型过滤规则
    KEY_FILTER_OTHER_RULES(0x7B),



    //// LoRaWAN参数
    // LoRaWAN网络状态
    KEY_LORA_NETWORK_STATUS(0x90),
    // 频段
    KEY_LORA_REGION(0x91),
    // 入网类型
    KEY_LORA_MODE(0x92),
    KEY_LORA_DEV_EUI(0x93),
    KEY_LORA_APP_EUI(0x94),
    KEY_LORA_APP_KEY(0x95),
    KEY_LORA_DEV_ADDR(0x96),
    KEY_LORA_APP_SKEY(0x97),
    KEY_LORA_NWK_SKEY(0x98),
    // CH
    KEY_LORA_CH(0x99),
    // 入网DR
    KEY_LORA_DR(0x9A),
    // 数据发送策略
    KEY_LORA_UPLINK_STRATEGY(0x9B),
    // DUTYCYCLE
    KEY_LORA_DUTY_CYCLE(0x9C),
    //ADR_ACK_LIMIT
    KEY_LORA_ADR_ACK_LIMIT(0x9D),
    //ADR_ACK_DELAY
    KEY_LORA_ADR_ACK_DELAY(0x9E),
    //devtime同步间隔
    KEY_LORA_TIME_SYNC_INTERVAL(0x9F),
    // 网络检查间隔
    KEY_LORA_NETWORK_CHECK_INTERVAL(0xA0),
    //心跳数据包上行配置
    KEY_HEARTBEAT_PAYLOAD(0xA1),
    //车位信息包上行配置
    KEY_PARKING_INFO_PAYLOAD(0xA2),
    //车位Beacon信息包上行配置
    KEY_BEACON_PAYLOAD(0xA3),
    //低电信息包上行配置
    KEY_LOW_POWER_PAYLOAD(0xA4),
    //关机信息包上行配置
    KEY_SHUTDOWN_PAYLOAD(0xA5),
    //事件信息包上行配置
    KEY_EVENT_PAYLOAD(0xA6),



    //// 从机固件升级指令
    //触发从机固件升级
    KEY_TRIGGER_SLAVE_UPDATE(0xB0),
    // 从机固件升级
    KEY_SLAVE_UPDATE(0xB1),



    //从机升级
    KEY_SLAVE_UPDATE_RESULT(0xD1),
    KEY_NO_PARKING_CALIBRATION_RESULT(0xD2),
    KEY_PARKING_DETECTION_TIMES(0xD3),
    ;

    private final int paramsKey;

    ParamsKeyEnum(int paramsKey) {
        this.paramsKey = paramsKey;
    }

    public int getParamsKey() {
        return paramsKey;
    }

    public static ParamsKeyEnum fromParamKey(int paramsKey) {
        for (ParamsKeyEnum paramsKeyEnum : ParamsKeyEnum.values()) {
            if (paramsKeyEnum.getParamsKey() == paramsKey) {
                return paramsKeyEnum;
            }
        }
        return null;
    }
}
