package com.moko.support.lw009.task;

import android.text.TextUtils;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.lw009.MoKoSupport;
import com.moko.support.lw009.entity.OrderCHAR;
import com.moko.support.lw009.entity.ParamsKeyEnum;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class ParamsWriteTask extends OrderTask {
    public byte[] data;

    public ParamsWriteTask() {
        super(OrderCHAR.CHAR_PARAMS, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(ParamsKeyEnum keyEnum) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) keyEnum.getParamsKey(),
                (byte) 0x00
        };
    }

    public void setParkingDetectionSensitivity(@IntRange(from = 1, to = 7) int sensitivity) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_PARKING_DETECTION_SENSITIVITY.getParamsKey(),
                (byte) 0x01,
                (byte) sensitivity
        };
    }

    public void setParkingDetectionMode(@IntRange(from = 0, to = 2) int mode) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_PARKING_DETECTION_MODE.getParamsKey(),
                (byte) 0x01,
                (byte) mode
        };
    }

    public void setParkingDetectionDuration(@IntRange(from = 1, to = 60) int duration) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_PARKING_DETECTION_DURATION.getParamsKey(),
                (byte) 0x01,
                (byte) duration
        };
    }

    public void setParkingDetectionConfirmDuration(@IntRange(from = 1, to = 60) int duration) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_PARKING_DETECTION_CONFIRM_DURATION.getParamsKey(),
                (byte) 0x01,
                (byte) duration
        };
    }

    public void setParkingDetectionThreshold(@IntRange(from = 5, to = 20000) int thresholdX,
                                             @IntRange(from = 5, to = 20000) int thresholdY,
                                             @IntRange(from = 5, to = 20000) int thresholdZ) {
        byte[] bytesX = MokoUtils.toByteArray(thresholdX, 2);
        byte[] bytesY = MokoUtils.toByteArray(thresholdY, 2);
        byte[] bytesZ = MokoUtils.toByteArray(thresholdZ, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_PARKING_DETECTION_THRESHOLD.getParamsKey(),
                (byte) 0x06,
                bytesX[0],
                bytesX[1],
                bytesY[0],
                bytesY[1],
                bytesZ[0],
                bytesZ[1]
        };
    }

    public void setParkingLotType(@IntRange(from = 0, to = 1) int type) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_PARKING_LOT_TYPE.getParamsKey(),
                (byte) 0x01,
                (byte) type
        };
    }

    public void setAutoCalibrationEnable(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_AUTO_CALIBRATION_SWITCH.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    public void setAutoCalibrationThreshold(@IntRange(from = 600, to = 6000) int threshold) {
        byte[] bytes = MokoUtils.toByteArray(threshold, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_AUTO_CALIBRATION_THRESHOLD.getParamsKey(),
                (byte) 0x02,
                bytes[0],
                bytes[1]
        };
    }

    public void setAutoCalibrationDelaySampleTime(@IntRange(from = 30, to = 240) int delaySampleTime) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_AUTO_CALIBRATION_DELAY_SAMPLE_TIME.getParamsKey(),
                (byte) 0x01,
                (byte) delaySampleTime
        };
    }

    public void setRadarDelayCloseEnable(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_RADAR_DELAY_CLOSE_SWITCH.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    public void setSlaveLogEnable(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLAVE_LOG_SWITCH.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    public void setParkingDetectionPayloadType(@IntRange(from = 0, to = 1) int parkingDetectionEnable,
                                               @IntRange(from = 0, to = 1) int parkingDetectionBeaconEnable) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_PARKING_DETECTION_PAYLOAD_TYPE.getParamsKey(),
                (byte) 0x02,
                (byte) parkingDetectionEnable,
                (byte) parkingDetectionBeaconEnable
        };
    }

    public void setBleScanTime(@IntRange(from = 1, to = 10) int scanTime) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_BLE_SCAN_TIME.getParamsKey(),
                (byte) 0x01,
                (byte) scanTime
        };
    }

    public void setBeaconPayloadCount(@IntRange(from = 1, to = 10) int count) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_PAYLOAD_BEACON_COUNT.getParamsKey(),
                (byte) 0x01,
                (byte) count
        };
    }

    public void setTriggerSlaveUpdate(int hardwareVersion, int softwareVersion, int deviceMode, int firmwareLength, byte[] checkCrc) {
        byte[] lenBytes = MokoUtils.toByteArray(firmwareLength, 4);
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_TRIGGER_SLAVE_UPDATE.getParamsKey(),
                (byte) 11,
                (byte) hardwareVersion,
                (byte) softwareVersion,
                (byte) deviceMode,
                lenBytes[0],
                lenBytes[1],
                lenBytes[2],
                lenBytes[3],
                checkCrc[0],
                checkCrc[1],
                checkCrc[2],
                checkCrc[3]
        };
    }

    public void setSlaveUpdate(@NonNull byte[] bytes) {
        data = new byte[bytes.length + 4];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_SLAVE_UPDATE.getParamsKey();
        data[3] = (byte) bytes.length;
        System.arraycopy(bytes, 0, data, 4, bytes.length);
        response.responseValue = data;
    }

    /**
     * 上行配置参数
     *
     * @param flag  0 1
     * @param times 1-4
     */
    public void setPayloadInfo(@IntRange(from = 0, to = 1) int flag, @IntRange(from = 1, to = 4) int times, int key) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) key,
                (byte) 0x02,
                (byte) flag,
                (byte) times
        };
    }

    public void close() {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_CLOSE.getParamsKey(),
                (byte) 0x00
        };
        response.responseValue = data;
    }

    public void reboot() {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_REBOOT.getParamsKey(),
                (byte) 0x00
        };
        response.responseValue = data;
    }

    public void reset() {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_RESET.getParamsKey(),
                (byte) 0x00
        };
        response.responseValue = data;
    }

    public void setTime() {
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        calendar.setTimeZone(timeZone);
        long time = calendar.getTimeInMillis() / 1000;
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; ++i) {
            bytes[i] = (byte) (time >> 8 * (3 - i) & 255);
        }
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_TIME_UTC.getParamsKey(),
                (byte) 0x04,
                bytes[0],
                bytes[1],
                bytes[2],
                bytes[3],
        };
        response.responseValue = data;
    }

    public void setTimeZone(@IntRange(from = -24, to = 28) int timeZone) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_TIME_ZONE.getParamsKey(),
                (byte) 0x01,
                (byte) timeZone
        };
        response.responseValue = data;
    }

    public void setHeartBeatInterval(@IntRange(from = 1, to = 14400) int interval) {
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_HEARTBEAT_INTERVAL.getParamsKey(),
                (byte) 0x02,
                intervalBytes[0],
                intervalBytes[1],
        };
        response.responseValue = data;
    }

    public void setManufacturer(String manufacturer) {
        byte[] manufacturerBytes = manufacturer.getBytes();
        int length = manufacturerBytes.length;
        data = new byte[length + 4];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_MANUFACTURER.getParamsKey();
        data[3] = (byte) length;
        System.arraycopy(manufacturerBytes, 0, data, 4, manufacturerBytes.length);
        response.responseValue = data;
    }

    public void setShutdownInfoReport(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SHUTDOWN_PAYLOAD_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setLowPowerReportEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_LOW_POWER_PAYLOAD_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setPasswordVerifyEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_PASSWORD_VERIFY_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void changePassword(String password) {
        byte[] passwordBytes = password.getBytes();
        int length = passwordBytes.length;
        data = new byte[length + 4];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_PASSWORD.getParamsKey();
        data[3] = (byte) length;
        System.arraycopy(passwordBytes, 0, data, 4, passwordBytes.length);
        response.responseValue = data;
    }

    public void setAdvTimeout(@IntRange(from = 1, to = 60) int timeout) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ADV_TIMEOUT.getParamsKey(),
                (byte) 0x01,
                (byte) timeout
        };
        response.responseValue = data;
    }

    public void setAdvTxPower(@IntRange(from = -40, to = 8) int txPower) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ADV_TX_POWER.getParamsKey(),
                (byte) 0x01,
                (byte) txPower
        };
        response.responseValue = data;
    }

    public void setAdvName(String advName) {
        byte[] advNameBytes = advName.getBytes();
        int length = advNameBytes.length;
        data = new byte[length + 4];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_ADV_NAME.getParamsKey();
        data[3] = (byte) length;
        for (int i = 0; i < advNameBytes.length; i++) {
            data[i + 4] = advNameBytes[i];
        }
        response.responseValue = data;
    }

    public void setAdvInterval(@IntRange(from = 1, to = 100) int interval) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ADV_INTERVAL.getParamsKey(),
                (byte) 0x01,
                (byte) interval
        };
    }

    public void setFilterRSSI(@IntRange(from = -127, to = 0) int rssi) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_RSSI.getParamsKey(),
                (byte) 0x01,
                (byte) rssi
        };
        response.responseValue = data;
    }

    public void setFilterRelationship(@IntRange(from = 0, to = 6) int relationship) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_RELATIONSHIP.getParamsKey(),
                (byte) 0x01,
                (byte) relationship
        };
        response.responseValue = data;
    }

    public void setFilterMacPrecise(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MAC_PRECISE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterMacReverse(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MAC_REVERSE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterMacRules(List<String> filterMacRules) {
        if (filterMacRules == null || filterMacRules.size() == 0) {
            data = new byte[]{
                    (byte) 0xED,
                    (byte) 0x01,
                    (byte) ParamsKeyEnum.KEY_FILTER_MAC_RULES.getParamsKey(),
                    (byte) 0x00
            };
        } else {
            int length = 0;
            for (String mac : filterMacRules) {
                length += 1;
                length += mac.length() / 2;
            }
            data = new byte[4 + length];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_MAC_RULES.getParamsKey();
            data[3] = (byte) length;
            int index = 0;
            for (int i = 0, size = filterMacRules.size(); i < size; i++) {
                String mac = filterMacRules.get(i);
                byte[] macBytes = MokoUtils.hex2bytes(mac);
                int l = macBytes.length;
                data[4 + index] = (byte) l;
                index++;
                for (int j = 0; j < l; j++, index++) {
                    data[4 + index] = macBytes[j];
                }
            }
        }
        response.responseValue = data;
    }

    public void setFilterNamePrecise(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_NAME_PRECISE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterNameReverse(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_NAME_REVERSE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterRawData(int unknown, int ibeacon,
                                 int eddystone_uid, int eddystone_url, int eddystone_tlm,
                                 int bxp_acc, int bxp_th,
                                 int mkibeacon, int mkibeacon_acc) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_RAW_DATA.getParamsKey(),
                (byte) 0x09,
                (byte) unknown,
                (byte) ibeacon,
                (byte) eddystone_uid,
                (byte) eddystone_url,
                (byte) eddystone_tlm,
                (byte) bxp_acc,
                (byte) bxp_th,
                (byte) mkibeacon,
                (byte) mkibeacon_acc
        };
        response.responseValue = data;
    }

    public void setFilterIBeaconEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_I_BEACON_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterIBeaconMajorRange(@IntRange(from = 0, to = 65535) int min,
                                           @IntRange(from = 0, to = 65535) int max) {
        byte[] minBytes = MokoUtils.toByteArray(min, 2);
        byte[] maxBytes = MokoUtils.toByteArray(max, 2);
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_I_BEACON_MAJOR_RANGE.getParamsKey(),
                (byte) 0x04,
                minBytes[0],
                minBytes[1],
                maxBytes[0],
                maxBytes[1]
        };
        response.responseValue = data;
    }

    public void setFilterIBeaconMinorRange(@IntRange(from = 0, to = 65535) int min,
                                           @IntRange(from = 0, to = 65535) int max) {
        byte[] minBytes = MokoUtils.toByteArray(min, 2);
        byte[] maxBytes = MokoUtils.toByteArray(max, 2);
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_I_BEACON_MINOR_RANGE.getParamsKey(),
                (byte) 0x04,
                minBytes[0],
                minBytes[1],
                maxBytes[0],
                maxBytes[1]
        };
        response.responseValue = data;
    }

    public void setFilterIBeaconUUID(String uuid) {
        if (TextUtils.isEmpty(uuid)) {
            data = new byte[4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_I_BEACON_UUID.getParamsKey();
            data[3] = (byte) 0x00;
        } else {
            byte[] uuidBytes = MokoUtils.hex2bytes(uuid);
            int length = uuidBytes.length;
            data = new byte[length + 4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_I_BEACON_UUID.getParamsKey();
            data[3] = (byte) length;
            System.arraycopy(uuidBytes, 0, data, 4, uuidBytes.length);
        }
        response.responseValue = data;
    }

    public void setFilterBXPIBeaconEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_I_BEACON_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterBXPIBeaconMajorRange(@IntRange(from = 0, to = 65535) int min,
                                              @IntRange(from = 0, to = 65535) int max) {
        byte[] minBytes = MokoUtils.toByteArray(min, 2);
        byte[] maxBytes = MokoUtils.toByteArray(max, 2);
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_I_BEACON_MAJOR_RANGE.getParamsKey(),
                (byte) 0x04,
                minBytes[0],
                minBytes[1],
                maxBytes[0],
                maxBytes[1]
        };
        response.responseValue = data;
    }

    public void setFilterBXPIBeaconMinorRange(@IntRange(from = 0, to = 65535) int min,
                                              @IntRange(from = 0, to = 65535) int max) {
        byte[] minBytes = MokoUtils.toByteArray(min, 2);
        byte[] maxBytes = MokoUtils.toByteArray(max, 2);
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_I_BEACON_MINOR_RANGE.getParamsKey(),
                (byte) 0x04,
                minBytes[0],
                minBytes[1],
                maxBytes[0],
                maxBytes[1]
        };
        response.responseValue = data;
    }

    public void setFilterBXPIBeaconUUID(String uuid) {
        if (TextUtils.isEmpty(uuid)) {
            data = new byte[4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_BXP_I_BEACON_UUID.getParamsKey();
            data[3] = (byte) 0x00;
        } else {
            byte[] uuidBytes = MokoUtils.hex2bytes(uuid);
            int length = uuidBytes.length;
            data = new byte[length + 4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_BXP_I_BEACON_UUID.getParamsKey();
            data[3] = (byte) length;
            for (int i = 0; i < uuidBytes.length; i++) {
                data[i + 4] = uuidBytes[i];
            }
        }
        response.responseValue = data;
    }

    public void setFilterBXPTagEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_TAG_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterBXPTagPrecise(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_TAG_PRECISE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterBXPTagReverse(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_TAG_REVERSE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterBXPTagRules(List<String> filterBXPTagRules) {
        if (filterBXPTagRules == null || filterBXPTagRules.size() == 0) {
            data = new byte[]{
                    (byte) 0xED,
                    (byte) 0x01,
                    (byte) ParamsKeyEnum.KEY_FILTER_BXP_TAG_RULES.getParamsKey(),
                    (byte) 0x00
            };
        } else {
            int length = 0;
            for (String mac : filterBXPTagRules) {
                length += 1;
                length += mac.length() / 2;
            }
            data = new byte[4 + length];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_BXP_TAG_RULES.getParamsKey();
            data[3] = (byte) length;
            int index = 0;
            for (int i = 0, size = filterBXPTagRules.size(); i < size; i++) {
                String mac = filterBXPTagRules.get(i);
                byte[] macBytes = MokoUtils.hex2bytes(mac);
                int l = macBytes.length;
                data[4 + index] = (byte) l;
                index++;
                for (int j = 0; j < l; j++, index++) {
                    data[4 + index] = macBytes[j];
                }
            }
        }
        response.responseValue = data;
    }

    public void setFilterMkPirEnable(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MK_PIR_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    public void setFilterMkPirSensorDetectionStatus(@IntRange(from = 0, to = 2) int type) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MK_PIR_DETECTION_STATUS.getParamsKey(),
                (byte) 0x01,
                (byte) type
        };
    }

    public void setFilterMkPirSensorSensitivity(@IntRange(from = 0, to = 3) int type) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MK_PIR_SENSOR_SENSITIVITY.getParamsKey(),
                (byte) 0x01,
                (byte) type
        };
    }

    public void setFilterMkPirDoorStatus(@IntRange(from = 0, to = 2) int type) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MK_PIR_DOOR_STATUS.getParamsKey(),
                (byte) 0x01,
                (byte) type
        };
    }

    public void setFilterMkPirDelayResStatus(@IntRange(from = 0, to = 3) int type) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MK_PIR_DELAY_RES_STATUS.getParamsKey(),
                (byte) 0x01,
                (byte) type
        };
    }

    public void setFilterMkPirMajorRange(@IntRange(from = 0, to = 65535) int min,
                                         @IntRange(from = 0, to = 65535) int max) {
        byte[] minBytes = MokoUtils.toByteArray(min, 2);
        byte[] maxBytes = MokoUtils.toByteArray(max, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MK_PIR_MAJOR.getParamsKey(),
                (byte) 0x04,
                minBytes[0],
                minBytes[1],
                maxBytes[0],
                maxBytes[1]
        };
    }

    public void setFilterMkPirMinorRange(@IntRange(from = 0, to = 65535) int min,
                                         @IntRange(from = 0, to = 65535) int max) {
        byte[] minBytes = MokoUtils.toByteArray(min, 2);
        byte[] maxBytes = MokoUtils.toByteArray(max, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MK_PIR_MINOR.getParamsKey(),
                (byte) 0x04,
                minBytes[0],
                minBytes[1],
                maxBytes[0],
                maxBytes[1]
        };
    }

    public void setFilterBXPButtonEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_BUTTON_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterBXPButtonRules(@IntRange(from = 0, to = 1) int singleEnable,
                                        @IntRange(from = 0, to = 1) int doubleEnable,
                                        @IntRange(from = 0, to = 1) int longEnable,
                                        @IntRange(from = 0, to = 1) int abnormalEnable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_BUTTON_RULES.getParamsKey(),
                (byte) 0x04,
                (byte) singleEnable,
                (byte) doubleEnable,
                (byte) longEnable,
                (byte) abnormalEnable,
        };
        response.responseValue = data;
    }

    public void setFilterEddystoneUIDEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_UID_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterEddystoneUIDNamespace(String namespace) {
        if (TextUtils.isEmpty(namespace)) {
            data = new byte[4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_UID_NAMESPACE.getParamsKey();
            data[3] = (byte) 0x00;
        } else {
            byte[] dataBytes = MokoUtils.hex2bytes(namespace);
            int length = dataBytes.length;
            data = new byte[length + 4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_UID_NAMESPACE.getParamsKey();
            data[3] = (byte) length;
            System.arraycopy(dataBytes, 0, data, 4, dataBytes.length);
        }
        response.responseValue = data;
    }

    public void setFilterEddystoneUIDInstance(String instance) {
        if (TextUtils.isEmpty(instance)) {
            data = new byte[4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_UID_INSTANCE.getParamsKey();
            data[3] = (byte) 0x00;
        } else {
            byte[] dataBytes = MokoUtils.hex2bytes(instance);
            int length = dataBytes.length;
            data = new byte[length + 4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_UID_INSTANCE.getParamsKey();
            data[3] = (byte) length;
            System.arraycopy(dataBytes, 0, data, 4, dataBytes.length);
        }
        response.responseValue = data;
    }

    public void setFilterEddystoneUrlEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_URL_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterEddystoneUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            data = new byte[4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_URL.getParamsKey();
            data[3] = (byte) 0x00;
        } else {
            byte[] dataBytes = url.getBytes();
            int length = dataBytes.length;
            data = new byte[length + 4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_URL.getParamsKey();
            data[3] = (byte) length;
            for (int i = 0; i < dataBytes.length; i++) {
                data[i + 4] = dataBytes[i];
            }
        }
        response.responseValue = data;
    }

    public void setFilterEddystoneTlmEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_TLM_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterEddystoneTlmVersion(@IntRange(from = 0, to = 2) int version) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_TLM_VERSION.getParamsKey(),
                (byte) 0x01,
                (byte) version
        };
        response.responseValue = data;
    }

    public void setFilterBXPAccEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_ACC.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterBXPTHEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_TH.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterBXPDeviceEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_DEVICE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterOtherEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_OTHER_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterOtherRelationship(@IntRange(from = 0, to = 5) int relationship) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_OTHER_RELATIONSHIP.getParamsKey(),
                (byte) 0x01,
                (byte) relationship
        };
        response.responseValue = data;
    }

    public void setFilterOtherRules(ArrayList<String> filterOtherRules) {
        if (filterOtherRules == null || filterOtherRules.isEmpty()) {
            data = new byte[]{
                    (byte) 0xED,
                    (byte) 0x01,
                    (byte) ParamsKeyEnum.KEY_FILTER_OTHER_RULES.getParamsKey(),
                    (byte) 0x00
            };
        } else {
            int length = 0;
            for (String other : filterOtherRules) {
                length += 1;
                length += other.length() / 2;
            }
            data = new byte[4 + length];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_OTHER_RULES.getParamsKey();
            data[3] = (byte) length;
            int index = 0;
            for (int i = 0, size = filterOtherRules.size(); i < size; i++) {
                String rule = filterOtherRules.get(i);
                byte[] ruleBytes = MokoUtils.hex2bytes(rule);
                int l = ruleBytes.length;
                data[4 + index] = (byte) l;
                index++;
                for (int j = 0; j < l; j++, index++) {
                    data[4 + index] = ruleBytes[j];
                }
            }
        }
        response.responseValue = data;
    }

    public void setLoraRegion(@IntRange(from = 0, to = 13) int region) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_LORA_REGION.getParamsKey(),
                (byte) 0x01,
                (byte) region
        };
    }

    public void setLoraUploadMode(@IntRange(from = 1, to = 2) int mode) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_LORA_MODE.getParamsKey(),
                (byte) 0x01,
                (byte) mode
        };
    }

    public void setLoraDevEUI(String devEui) {
        byte[] rawDataBytes = MokoUtils.hex2bytes(devEui);
        int length = rawDataBytes.length;
        data = new byte[4 + length];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_LORA_DEV_EUI.getParamsKey();
        data[3] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[i + 4] = rawDataBytes[i];
        }
    }

    public void setLoraAppEUI(String appEui) {
        byte[] rawDataBytes = MokoUtils.hex2bytes(appEui);
        int length = rawDataBytes.length;
        data = new byte[4 + length];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_LORA_APP_EUI.getParamsKey();
        data[3] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[i + 4] = rawDataBytes[i];
        }
    }

    public void setLoraAppKey(String appKey) {
        byte[] rawDataBytes = MokoUtils.hex2bytes(appKey);
        int length = rawDataBytes.length;
        data = new byte[4 + length];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_LORA_APP_KEY.getParamsKey();
        data[3] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[i + 4] = rawDataBytes[i];
        }
    }

    public void setLoraDevAddr(String devAddr) {
        byte[] rawDataBytes = MokoUtils.hex2bytes(devAddr);
        int length = rawDataBytes.length;
        data = new byte[4 + length];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_LORA_DEV_ADDR.getParamsKey();
        data[3] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[i + 4] = rawDataBytes[i];
        }
    }

    public void setLoraAppSKey(String appSkey) {
        byte[] rawDataBytes = MokoUtils.hex2bytes(appSkey);
        int length = rawDataBytes.length;
        data = new byte[4 + length];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_LORA_APP_SKEY.getParamsKey();
        data[3] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[i + 4] = rawDataBytes[i];
        }
    }

    public void setLoraNwkSKey(String nwkSkey) {
        byte[] rawDataBytes = MokoUtils.hex2bytes(nwkSkey);
        int length = rawDataBytes.length;
        data = new byte[4 + length];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_LORA_NWK_SKEY.getParamsKey();
        data[3] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[i + 4] = rawDataBytes[i];
        }
    }

    public void setLoraCH(int ch1, int ch2) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_LORA_CH.getParamsKey(),
                (byte) 0x02,
                (byte) ch1,
                (byte) ch2
        };
    }

    public void setLoraDR(int dr1) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_LORA_DR.getParamsKey(),
                (byte) 0x01,
                (byte) dr1
        };
    }

    public void setLoraUplinkStrategy(int adr, int number, int dr1, int dr2) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_LORA_UPLINK_STRATEGY.getParamsKey(),
                (byte) 0x04,
                (byte) adr,
                (byte) number,
                (byte) dr1,
                (byte) dr2
        };
    }


    public void setLoraDutyCycleEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_LORA_DUTY_CYCLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    public void setLoraTimeSyncInterval(@IntRange(from = 0, to = 255) int interval) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_LORA_TIME_SYNC_INTERVAL.getParamsKey(),
                (byte) 0x01,
                (byte) interval
        };
    }

    public void setLoraNetworkInterval(@IntRange(from = 0, to = 255) int interval) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_LORA_NETWORK_CHECK_INTERVAL.getParamsKey(),
                (byte) 0x01,
                (byte) interval
        };
    }

    public void setLoraAdrAckLimit(@IntRange(from = 1, to = 100) int interval) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_LORA_ADR_ACK_LIMIT.getParamsKey(),
                (byte) 0x01,
                (byte) interval
        };
    }

    public void setLoraAdrAckDelay(@IntRange(from = 1, to = 100) int interval) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_LORA_ADR_ACK_DELAY.getParamsKey(),
                (byte) 0x01,
                (byte) interval
        };
    }

    public void setFilterNameRules(List<String> filterNameRules) {
        if (null == filterNameRules || filterNameRules.isEmpty()) {
            data = new byte[]{
                    (byte) 0xEE,
                    (byte) 0x01,
                    (byte) ParamsKeyEnum.KEY_FILTER_NAME_RULES.getParamsKey(),
                    (byte) 0x01,
                    (byte) 0x00,
                    (byte) 0x00
            };
        } else {
            int length = 0;
            for (String name : filterNameRules) {
                length += 1;
                length += name.length();
            }
            dataBytes = new byte[length];
            int index = 0;
            for (int i = 0, size = filterNameRules.size(); i < size; i++) {
                String name = filterNameRules.get(i);
                byte[] nameBytes = name.getBytes();
                int l = nameBytes.length;
                dataBytes[index] = (byte) l;
                index++;
                for (int j = 0; j < l; j++, index++) {
                    dataBytes[index] = nameBytes[j];
                }
            }
            dataLength = dataBytes.length;
            if (dataLength != 0) {
                if (dataLength % DATA_LENGTH_MAX > 0) {
                    packetCount = dataLength / DATA_LENGTH_MAX + 1;
                } else {
                    packetCount = dataLength / DATA_LENGTH_MAX;
                }
            } else {
                packetCount = 1;
            }
            remainPack = packetCount - 1;
            packetIndex = 0;
            delayTime = DEFAULT_DELAY_TIME + 500L * packetCount;
            if (packetCount > 1) {
                data = new byte[DATA_LENGTH_MAX + 6];
                data[0] = (byte) 0xEE;
                data[1] = (byte) 0x01;
                data[2] = (byte) ParamsKeyEnum.KEY_FILTER_NAME_RULES.getParamsKey();
                data[3] = (byte) packetCount;
                data[4] = (byte) packetIndex;
                data[5] = (byte) DATA_LENGTH_MAX;
                for (int i = 0; i < DATA_LENGTH_MAX; i++, dataOrigin++) {
                    data[i + 6] = dataBytes[dataOrigin];
                }
            } else {
                data = new byte[dataLength + 6];
                data[0] = (byte) 0xEE;
                data[1] = (byte) 0x01;
                data[2] = (byte) ParamsKeyEnum.KEY_FILTER_NAME_RULES.getParamsKey();
                data[3] = (byte) packetCount;
                data[4] = (byte) packetIndex;
                data[5] = (byte) dataLength;
                for (int i = 0; i < dataLength; i++) {
                    data[i + 6] = dataBytes[i];
                }
            }
        }
    }

    private int packetCount;
    private int packetIndex;
    private int remainPack;
    private int dataLength;
    private int dataOrigin;
    private byte[] dataBytes;
    private static final int DATA_LENGTH_MAX = 176;

    @Override
    public boolean parseValue(byte[] value) {
        final int header = value[0] & 0xFF;
        if (header == 0xED)
            return true;
        final int cmd = value[2] & 0xFF;
        final int result = value[4] & 0xFF;
        if (result == 1) {
            remainPack--;
            packetIndex++;
            if (remainPack >= 0) {
                assembleRemainData(cmd);
                return false;
            }
            return true;
        }
        return false;
    }

    private void assembleRemainData(int cmd) {
        int length = dataLength - dataOrigin;
        if (length > DATA_LENGTH_MAX) {
            data = new byte[DATA_LENGTH_MAX + 6];
            data[0] = (byte) 0xEE;
            data[1] = (byte) 0x01;
            data[2] = (byte) cmd;
            data[3] = (byte) packetCount;
            data[4] = (byte) packetIndex;
            data[5] = (byte) DATA_LENGTH_MAX;
            for (int i = 0; i < DATA_LENGTH_MAX; i++, dataOrigin++) {
                data[i + 6] = dataBytes[dataOrigin];
            }
        } else {
            data = new byte[length + 6];
            data[0] = (byte) 0xEE;
            data[1] = (byte) 0x01;
            data[2] = (byte) cmd;
            data[3] = (byte) packetCount;
            data[4] = (byte) packetIndex;
            data[5] = (byte) length;
            for (int i = 0; i < length; i++, dataOrigin++) {
                data[i + 6] = dataBytes[dataOrigin];
            }
        }
        MoKoSupport.getInstance().sendDirectOrder(this);
    }
}
