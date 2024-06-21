package com.moko.lw009smpro.utils;

import android.os.ParcelUuid;
import android.os.SystemClock;

import com.moko.lw009smpro.entity.AdvInfo;
import com.moko.support.lw009.entity.DeviceInfo;
import com.moko.support.lw009.entity.OrderServices;
import com.moko.support.lw009.service.DeviceInfoParseable;

import java.util.HashMap;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class AdvInfoAnalysisImpl implements DeviceInfoParseable<AdvInfo> {
    private final HashMap<String, AdvInfo> advInfoHashMap;

    public AdvInfoAnalysisImpl() {
        this.advInfoHashMap = new HashMap<>();
    }

    @Override
    public AdvInfo parseDeviceInfo(DeviceInfo deviceInfo) {
        ScanResult result = deviceInfo.scanResult;
        ScanRecord record = result.getScanRecord();
        if (null == record) return null;
        byte[] bytes = record.getServiceData(new ParcelUuid(OrderServices.SERVICE_ADV.getUuid()));
        if (null == bytes || bytes.length != 24) return null;
        boolean verifyEnable = ((bytes[9] & 0xff) >> 6 & 0x01) == 1;
        boolean lowPower = ((bytes[9] & 0xff) >> 7 & 0x01) == 1;
        AdvInfo advInfo;
        if (advInfoHashMap.containsKey(deviceInfo.mac)) {
            advInfo = advInfoHashMap.get(deviceInfo.mac);
            if (null == advInfo) return null;
            advInfo.name = deviceInfo.name;
            advInfo.rssi = deviceInfo.rssi;
            long currentTime = SystemClock.elapsedRealtime();
            advInfo.intervalTime = currentTime - advInfo.scanTime;
            advInfo.scanTime = currentTime;
            advInfo.verifyEnable = verifyEnable;
            advInfo.lowPower = lowPower;
            advInfo.txPower = record.getTxPowerLevel();
            advInfo.connectable = result.isConnectable();
        } else {
            advInfo = new AdvInfo();
            advInfo.name = deviceInfo.name;
            advInfo.mac = deviceInfo.mac;
            advInfo.rssi = deviceInfo.rssi;
            advInfo.scanTime = SystemClock.elapsedRealtime();
            advInfo.verifyEnable = verifyEnable;
            advInfo.lowPower = lowPower;
            advInfo.txPower = record.getTxPowerLevel();
            advInfo.connectable = result.isConnectable();
            advInfoHashMap.put(deviceInfo.mac, advInfo);
        }
        return advInfo;
    }
}
