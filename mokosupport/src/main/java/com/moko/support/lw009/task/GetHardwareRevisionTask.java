package com.moko.support.lw009.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.lw009.entity.OrderCHAR;

public class GetHardwareRevisionTask extends OrderTask {

    public byte[] data;

    public GetHardwareRevisionTask() {
        super(OrderCHAR.CHAR_HARDWARE_REVISION, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
