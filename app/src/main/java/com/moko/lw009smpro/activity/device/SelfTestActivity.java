package com.moko.lw009smpro.activity.device;

import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.lw009smpro.activity.Lw009BaseActivity;
import com.moko.lw009smpro.databinding.ActivitySelftestBinding;
import com.moko.support.lw009.MoKoSupport;
import com.moko.support.lw009.OrderTaskAssembler;
import com.moko.support.lw009.entity.OrderCHAR;
import com.moko.support.lw009.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class SelfTestActivity extends Lw009BaseActivity {
    private ActivitySelftestBinding mBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivitySelftestBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.getSelfTestStatus());
        orderTasks.add(OrderTaskAssembler.getPCBAStatus());
        orderTasks.add(OrderTaskAssembler.getNoParkingCalibrationStatus());
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
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
                    if (null != value && value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (header != 0xED || configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
//                        if (flag == 0x01) {
//                            // write
//                            int result = value[4] & 0xFF;
//                            if (configKeyEnum == ParamsKeyEnum.KEY_BATTERY_RESET || configKeyEnum == ParamsKeyEnum.KEY_RESET_MOTOR_STATE) {
//                                if (result == 1) {
//                                    AlertMessageDialog dialog = new AlertMessageDialog();
//                                    dialog.setMessage("Reset Successfully！");
//                                    dialog.setConfirm("OK");
//                                    dialog.setCancelGone();
//                                    dialog.show(getSupportFragmentManager());
//                                }
//                            }
//                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_SELF_TEST_STATUS:
                                    if (length > 0) {
                                        int status = value[4] & 0xFF;
                                        mBind.tvSelftestStatus.setVisibility(status == 0 ? View.VISIBLE : View.GONE);
                                        if ((status & 0x01) == 0x01)
                                            mBind.tvThStatus.setVisibility(View.VISIBLE);
                                        if ((status & 0x02) == 0x02)
                                            mBind.tvSlaveTestStatus.setVisibility(View.VISIBLE);
                                    }
                                    break;
                                case KEY_PCBA_STATUS:
                                    if (length > 0) {
                                        mBind.tvPcbaStatus.setText(String.valueOf(value[4] & 0xFF));
                                    }
                                    break;
                                case KEY_NO_PARKING_CALIBRATION_STATUS:
                                    if (length == 1) {
                                        //无车校准状态
                                        mBind.tvCalibrationStatus.setText(value[4] & 0xff);
                                    }
                                    break;
//                                case KEY_BATTERY_INFO:
//                                    if (length == 36) {
//                                        int runtime = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 8));
//                                        mBind.tvRuntime.setText(String.format("%d s", runtime));
//                                        int advTimes = MokoUtils.toInt(Arrays.copyOfRange(value, 8, 12));
//                                        mBind.tvAdvTimes.setText(String.format("%d times", advTimes));
//                                        int flashTimes = MokoUtils.toInt(Arrays.copyOfRange(value, 12, 16));
//                                        mBind.tvFlashTimes.setText(String.format("%d times", flashTimes));
//                                        int axisDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 16, 20));
//                                        mBind.tvAxisDuration.setText(String.format("%d ms", axisDuration));
//                                        int bleFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 20, 24));
//                                        mBind.tvBleFixDuration.setText(String.format("%d ms", bleFixDuration));
//                                        int wifiFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 24, 28));
//                                        mBind.tvWifiFixDuration.setText(String.format("%d ms", wifiFixDuration));
//                                        int gpsFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 28, 32));
//                                        mBind.tvGpsFixDuration.setText(String.format("%d s", gpsFixDuration));
//                                        int loraTransmissionTimes = MokoUtils.toInt(Arrays.copyOfRange(value, 32, 36));
//                                        mBind.tvLoraTransmissionTimes.setText(String.format("%d times", loraTransmissionTimes));
//                                        int loraPower = MokoUtils.toInt(Arrays.copyOfRange(value, 36, 40));
//                                        mBind.tvLoraPower.setText(String.format("%d mAS", loraPower));
//                                    }
//                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

//    public void onBatteryReset(View view) {
//        if (isWindowLocked()) return;
//        AlertMessageDialog dialog = new AlertMessageDialog();
//        dialog.setTitle("Warning！");
//        dialog.setMessage("Are you sure to reset battery?");
//        dialog.setConfirm("OK");
//        dialog.setOnAlertConfirmListener(() -> {
//            showSyncingProgressDialog();
//            List<OrderTask> orderTasks = new ArrayList<>();
//            orderTasks.add(OrderTaskAssembler.setBatteryReset());
//            orderTasks.add(OrderTaskAssembler.getBatteryInfo());
//            MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
//        });
//        dialog.show(getSupportFragmentManager());
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onBack(View view) {
        finish();
    }
}
