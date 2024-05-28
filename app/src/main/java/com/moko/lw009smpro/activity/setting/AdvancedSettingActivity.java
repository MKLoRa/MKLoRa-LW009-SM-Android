package com.moko.lw009smpro.activity.setting;

import static com.moko.lw009smpro.AppConstants.SAVE_ERROR;
import static com.moko.lw009smpro.AppConstants.SAVE_SUCCESS;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw009smpro.activity.Lw009BaseActivity;
import com.moko.lw009smpro.databinding.ActivityAdvancedSettingBinding;
import com.moko.lw009smpro.dialog.BottomDialog;
import com.moko.lw009smpro.utils.ToastUtils;
import com.moko.support.lw009.MoKoSupport;
import com.moko.support.lw009.OrderTaskAssembler;
import com.moko.support.lw009.entity.OrderCHAR;
import com.moko.support.lw009.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: jun.liu
 * @date: 2024/4/24 18:12
 * @des:
 */
public class AdvancedSettingActivity extends Lw009BaseActivity {
    private ActivityAdvancedSettingBinding mBind;
    private boolean savedParamsError;
    private final String[] slaveWorkStatusArray = {"Sleep", "Normal", "Upgrading"};
    private final String[] parkingSlotTypeArray = {"0", "1"};
    private int parkingSlotType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityAdvancedSettingBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());

        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(8);
        orderTasks.add(OrderTaskAssembler.getAutoCalibrationEnable());
        orderTasks.add(OrderTaskAssembler.getAutoCalibrationThreshold());
        orderTasks.add(OrderTaskAssembler.getAutoCalibrationDelaySampleTime());
        orderTasks.add(OrderTaskAssembler.setTriggerParkingDetectionTimes());
        orderTasks.add(OrderTaskAssembler.getSlaveWorkMode());
        orderTasks.add(OrderTaskAssembler.getParkingLotType());
        orderTasks.add(OrderTaskAssembler.getParkingDetectionThreshold());
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        mBind.tvParkingSlotType.setOnClickListener(v -> {
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(new ArrayList<>(Arrays.asList(parkingSlotTypeArray)), parkingSlotType);
            dialog.setListener(value -> {
                parkingSlotType = value;
                mBind.tvParkingSlotType.setText(parkingSlotTypeArray[value]);
            });
            dialog.show(getSupportFragmentManager());
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
        if (!MokoConstants.ACTION_CURRENT_DATA.equals(action))
            EventBus.getDefault().cancelEventDelivery(event);
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        if (header != 0xED) return;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_AUTO_CALIBRATION_SWITCH:
                                case KEY_AUTO_CALIBRATION_THRESHOLD:
                                case KEY_AUTO_CALIBRATION_DELAY_SAMPLE_TIME:
                                case KEY_PARKING_LOT_TYPE:
                                    if (result != 1) savedParamsError = true;
                                    break;
                                case KEY_PARKING_DETECTION_THRESHOLD:
                                    if (result != 1) savedParamsError = true;
                                    ToastUtils.showToast(this, savedParamsError ? SAVE_ERROR : SAVE_SUCCESS);
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_AUTO_CALIBRATION_SWITCH:
                                    if (length == 1) {
                                        mBind.cbCalibrationSwitch.setChecked((value[4] & 0xff) == 1);
                                    }
                                    break;
                                case KEY_AUTO_CALIBRATION_THRESHOLD:
                                    if (length == 2) {
                                        mBind.etCalibrationTriggerValue.setText(String.valueOf(MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length))));
                                        mBind.etCalibrationTriggerValue.setSelection(mBind.etCalibrationTriggerValue.getText().length());
                                    }
                                    break;
                                case KEY_AUTO_CALIBRATION_DELAY_SAMPLE_TIME:
                                    if (length == 1) {
                                        mBind.etDelayTime.setText(String.valueOf(value[4] & 0xff));
                                        mBind.etDelayTime.setSelection(mBind.etDelayTime.getText().length());
                                    }
                                    break;
                                case KEY_SLAVE_WORK_MODE:
                                    if (length == 1) {
                                        mBind.tvParkingPartStatus.setText(slaveWorkStatusArray[value[4]]);
                                    }
                                    break;
                                case KEY_PARKING_LOT_TYPE:
                                    if (length == 1) {
                                        parkingSlotType = value[4];
                                        mBind.tvParkingSlotType.setText(parkingSlotTypeArray[parkingSlotType]);
                                    }
                                    break;
                                case KEY_PARKING_DETECTION_THRESHOLD:
                                    if (length == 6) {
                                        mBind.etThresholdX.setText(String.valueOf(MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6))));
                                        mBind.etThresholdX.setSelection(mBind.etThresholdX.getText().length());
                                        mBind.etThresholdY.setText(String.valueOf(MokoUtils.toInt(Arrays.copyOfRange(value, 6, 8))));
                                        mBind.etThresholdY.setSelection(mBind.etThresholdY.getText().length());
                                        mBind.etThresholdZ.setText(String.valueOf(MokoUtils.toInt(Arrays.copyOfRange(value, 8, 10))));
                                        mBind.etThresholdZ.setSelection(mBind.etThresholdZ.getText().length());
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                int header = value[0] & 0xFF;
                int flag = value[1] & 0xFF;
                int cmd = value[2] & 0xFF;
                int len = value[3] & 0xFF;
                if (orderCHAR == OrderCHAR.CHAR_SLAVE_NOTIFY) {
                    if (header == 0xED && flag == 0x02 && cmd == ParamsKeyEnum.KEY_PARKING_DETECTION_TIMES.getParamsKey() && len == 4) {
                        int parkingTimes = MokoUtils.toInt(Arrays.copyOfRange(value, 4, len + 4));
                        mBind.tvParkingDetectionTimes.setText(String.valueOf(parkingTimes));
                    }
                }
            }
        });
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        if (isValid()) {
            showSyncingProgressDialog();
            saveParams();
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private void saveParams() {
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>(6);
        orderTasks.add(OrderTaskAssembler.setAutoCalibrationEnable(mBind.cbCalibrationSwitch.isChecked() ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.setAutoCalibrationThreshold(Integer.parseInt(mBind.etCalibrationTriggerValue.getText().toString())));
        orderTasks.add(OrderTaskAssembler.setAutoCalibrationDelaySampleTime(Integer.parseInt(mBind.etDelayTime.getText().toString())));
        orderTasks.add(OrderTaskAssembler.setParkingLotType(parkingSlotType));
        int thresholdX = Integer.parseInt(mBind.etThresholdX.getText().toString());
        int thresholdY = Integer.parseInt(mBind.etThresholdY.getText().toString());
        int thresholdZ = Integer.parseInt(mBind.etThresholdZ.getText().toString());
        orderTasks.add(OrderTaskAssembler.setParkingDetectionThreshold(thresholdX, thresholdY, thresholdZ));
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(mBind.etCalibrationTriggerValue.getText())) return false;
        int calibrationTriggerValue = Integer.parseInt(mBind.etCalibrationTriggerValue.getText().toString());
        if (calibrationTriggerValue < 600 || calibrationTriggerValue > 6000) return false;
        if (TextUtils.isEmpty(mBind.etDelayTime.getText())) return false;
        int delayTime = Integer.parseInt(mBind.etDelayTime.getText().toString());
        if (delayTime < 30 || delayTime > 240) return false;
        if (TextUtils.isEmpty(mBind.etThresholdX.getText())) return false;
        int thresholdX = Integer.parseInt(mBind.etThresholdX.getText().toString());
        if (thresholdX < 5 || thresholdX > 20000) return false;
        if (TextUtils.isEmpty(mBind.etThresholdY.getText())) return false;
        int thresholdY = Integer.parseInt(mBind.etThresholdY.getText().toString());
        if (thresholdY < 5 || thresholdY > 20000) return false;
        if (TextUtils.isEmpty(mBind.etThresholdZ.getText())) return false;
        int thresholdZ = Integer.parseInt(mBind.etThresholdZ.getText().toString());
        return thresholdZ >= 5 && thresholdZ <= 20000;
    }

    public void onBack(View view) {
        finish();
    }
}
