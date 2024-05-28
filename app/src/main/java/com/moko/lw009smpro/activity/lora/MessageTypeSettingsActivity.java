package com.moko.lw009smpro.activity.lora;

import static com.moko.lw009smpro.AppConstants.SAVE_ERROR;
import static com.moko.lw009smpro.AppConstants.SAVE_SUCCESS;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.lw009smpro.activity.Lw009BaseActivity;
import com.moko.lw009smpro.databinding.ActivityMsgTypeSettingsBinding;
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
 * @date: 2024/4/18 16:25
 * @des:
 */
public class MessageTypeSettingsActivity extends Lw009BaseActivity {
    private ActivityMsgTypeSettingsBinding mBind;
    private boolean mReceiverTag = false;
    private static final String unconfirmed = "Unconfirmed";
    private static final String confirmed = "Confirmed";
    private final String[] payloadTypes = {unconfirmed, confirmed};
    private final String[] retransmissionTimes = {"0", "1", "2", "3"};
    private boolean savedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityMsgTypeSettingsBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(8);
        orderTasks.add(OrderTaskAssembler.getHeartbeatPayload());
        orderTasks.add(OrderTaskAssembler.getParkingInfoPayload());
        orderTasks.add(OrderTaskAssembler.getBeaconPayload());
        orderTasks.add(OrderTaskAssembler.getLowPowerPayload());
        orderTasks.add(OrderTaskAssembler.getShutdownPayload());
        orderTasks.add(OrderTaskAssembler.getEventPayload());
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        setListener();
    }

    private void setListener() {
        mBind.tvParkingInfoPayloadType.setOnClickListener(v -> {
            int index = unconfirmed.equals(mBind.tvParkingInfoPayloadType.getText().toString().trim()) ? 0 : 1;
            showBottomDialog(payloadTypes, index, mBind.tvParkingInfoPayloadType, 2);
        });
        mBind.tvHeartbeatPayloadType.setOnClickListener(v -> {
            int index = unconfirmed.equals(mBind.tvHeartbeatPayloadType.getText().toString().trim()) ? 0 : 1;
            showBottomDialog(payloadTypes, index, mBind.tvHeartbeatPayloadType, 1);
        });
        mBind.tvBeaconPayloadType.setOnClickListener(v -> {
            int index = unconfirmed.equals(mBind.tvBeaconPayloadType.getText().toString().trim()) ? 0 : 1;
            showBottomDialog(payloadTypes, index, mBind.tvBeaconPayloadType, 3);
        });
        mBind.tvLowPowerPayloadType.setOnClickListener(v -> {
            int index = unconfirmed.equals(mBind.tvLowPowerPayloadType.getText().toString().trim()) ? 0 : 1;
            showBottomDialog(payloadTypes, index, mBind.tvLowPowerPayloadType, 4);
        });
        mBind.tvShutdownPayloadType.setOnClickListener(v -> {
            int index = unconfirmed.equals(mBind.tvShutdownPayloadType.getText().toString().trim()) ? 0 : 1;
            showBottomDialog(payloadTypes, index, mBind.tvShutdownPayloadType, 5);
        });
        mBind.tvEventPayloadType.setOnClickListener(v -> {
            int index = unconfirmed.equals(mBind.tvEventPayloadType.getText().toString().trim()) ? 0 : 1;
            showBottomDialog(payloadTypes, index, mBind.tvEventPayloadType, 6);
        });

        mBind.tvHeartbeatTimes.setOnClickListener(v -> {
            int index = Integer.parseInt(mBind.tvHeartbeatTimes.getText().toString().trim());
            showBottomDialog(retransmissionTimes, index, mBind.tvHeartbeatTimes, 0);
        });
        mBind.tvParkingInfoTimes.setOnClickListener(v -> {
            int index = Integer.parseInt(mBind.tvParkingInfoTimes.getText().toString().trim());
            showBottomDialog(retransmissionTimes, index, mBind.tvParkingInfoTimes, 0);
        });
        mBind.tvBeaconTimes.setOnClickListener(v -> {
            int index = Integer.parseInt(mBind.tvBeaconTimes.getText().toString().trim());
            showBottomDialog(retransmissionTimes, index, mBind.tvBeaconTimes, 0);
        });
        mBind.tvLowPowerTimes.setOnClickListener(v -> {
            int index = Integer.parseInt(mBind.tvLowPowerTimes.getText().toString().trim());
            showBottomDialog(retransmissionTimes, index, mBind.tvLowPowerTimes, 0);
        });
        mBind.tvShutdownTimes.setOnClickListener(v -> {
            int index = Integer.parseInt(mBind.tvShutdownTimes.getText().toString().trim());
            showBottomDialog(retransmissionTimes, index, mBind.tvShutdownTimes, 0);
        });
        mBind.tvEventTimes.setOnClickListener(v -> {
            int index = Integer.parseInt(mBind.tvEventTimes.getText().toString().trim());
            showBottomDialog(retransmissionTimes, index, mBind.tvEventTimes, 0);
        });
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
                        if (flag == 0) {
                            switch (configKeyEnum) {
                                case KEY_PARKING_INFO_PAYLOAD:
                                    if (length == 2) {
                                        int enable = value[4] & 0xff;
                                        int times = (value[5] & 0xff) - 1;
                                        mBind.tvParkingInfoPayloadType.setText(enable == 1 ? confirmed : unconfirmed);
                                        mBind.tvParkingInfoTimes.setText(String.valueOf(times));
                                        setMaxTimes(enable, mBind.lineParkingInfoTimes, mBind.layoutParkingInfoTimes);
                                    }
                                    break;

                                case KEY_HEARTBEAT_PAYLOAD:
                                    if (length == 2) {
                                        int enable = value[4] & 0xff;
                                        int times = (value[5] & 0xff) - 1;
                                        mBind.tvHeartbeatPayloadType.setText(enable == 1 ? confirmed : unconfirmed);
                                        mBind.tvHeartbeatTimes.setText(String.valueOf(times));
                                        setMaxTimes(enable, mBind.lineHeartbeatTime, mBind.layoutHeartbeatTime);
                                    }
                                    break;

                                case KEY_BEACON_PAYLOAD:
                                    if (length == 2) {
                                        int enable = value[4] & 0xff;
                                        int times = (value[5] & 0xff) - 1;
                                        mBind.tvBeaconPayloadType.setText(enable == 1 ? confirmed : unconfirmed);
                                        mBind.tvBeaconTimes.setText(String.valueOf(times));
                                        setMaxTimes(enable, mBind.lineBeaconTimes, mBind.layoutBeaconTimes);
                                    }
                                    break;

                                case KEY_EVENT_PAYLOAD:
                                    if (length == 2) {
                                        int enable = value[4] & 0xff;
                                        int times = (value[5] & 0xff) - 1;
                                        mBind.tvEventPayloadType.setText(enable == 1 ? confirmed : unconfirmed);
                                        mBind.tvEventTimes.setText(String.valueOf(times));
                                        setMaxTimes(enable, mBind.lineEventTimes, mBind.layoutEventTimes);
                                    }
                                    break;

                                case KEY_LOW_POWER_PAYLOAD:
                                    if (length == 2) {
                                        int enable = value[4] & 0xff;
                                        int times = (value[5] & 0xff) - 1;
                                        mBind.tvLowPowerPayloadType.setText(enable == 1 ? confirmed : unconfirmed);
                                        mBind.tvLowPowerTimes.setText(String.valueOf(times));
                                        setMaxTimes(enable, mBind.lineLowPowerTime, mBind.layoutLowPowerTime);
                                    }
                                    break;

                                case KEY_SHUTDOWN_PAYLOAD:
                                    if (length == 2) {
                                        int enable = value[4] & 0xff;
                                        int times = (value[5] & 0xff) - 1;
                                        mBind.tvShutdownPayloadType.setText(enable == 1 ? confirmed : unconfirmed);
                                        mBind.tvShutdownTimes.setText(String.valueOf(times));
                                        setMaxTimes(enable, mBind.lineShutdownTimes, mBind.layoutShutdownTimes);
                                    }
                                    break;
                            }
                        } else if (flag == 1) {
                            switch (configKeyEnum) {
                                case KEY_HEARTBEAT_PAYLOAD:
                                case KEY_PARKING_INFO_PAYLOAD:
                                case KEY_BEACON_PAYLOAD:
                                case KEY_LOW_POWER_PAYLOAD:
                                case KEY_SHUTDOWN_PAYLOAD:
                                    if ((value[4] & 0xff) != 1) savedParamsError = true;
                                    break;

                                case KEY_EVENT_PAYLOAD:
                                    if ((value[4] & 0xff) != 1) savedParamsError = true;
                                    ToastUtils.showToast(this, savedParamsError ? SAVE_ERROR : SAVE_SUCCESS);
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void setMaxTimes(int enable, View view, LinearLayout linearLayout) {
        view.setVisibility(enable == 1 ? View.VISIBLE : View.GONE);
        linearLayout.setVisibility(enable == 1 ? View.VISIBLE : View.GONE);
    }

    private void showBottomDialog(String[] mValues, int mSelected, TextView textView, int type) {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(mValues)), mSelected);
        dialog.setListener(value -> {
            textView.setText(mValues[value]);
            if (type == 1) {
                setMaxTimes(value, mBind.lineHeartbeatTime, mBind.layoutHeartbeatTime);
            } else if (type == 2) {
                setMaxTimes(value, mBind.lineParkingInfoTimes, mBind.layoutParkingInfoTimes);
            } else if (type == 3) {
                setMaxTimes(value, mBind.lineBeaconTimes, mBind.layoutBeaconTimes);
            } else if (type == 4) {
                setMaxTimes(value, mBind.lineLowPowerTime, mBind.layoutLowPowerTime);
            } else if (type == 5) {
                setMaxTimes(value, mBind.lineShutdownTimes, mBind.layoutShutdownTimes);
            } else if (type == 6) {
                setMaxTimes(value, mBind.lineEventTimes, mBind.layoutEventTimes);
            }
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onBack(View view) {
        finish();
    }

    public void onSave(View view) {
        showSyncingProgressDialog();
        savedParamsError = false;
        int parkingInfoPayloadType = confirmed.equals(mBind.tvParkingInfoPayloadType.getText().toString().trim()) ? 1 : 0;
        int parkingInfoTime = Integer.parseInt(mBind.tvParkingInfoTimes.getText().toString().trim()) + 1;
        int heartbeatPayloadType = confirmed.equals(mBind.tvHeartbeatPayloadType.getText().toString().trim()) ? 1 : 0;
        int heartbeatTime = Integer.parseInt(mBind.tvHeartbeatTimes.getText().toString().trim()) + 1;
        int lowPowerPayloadType = confirmed.equals(mBind.tvLowPowerPayloadType.getText().toString().trim()) ? 1 : 0;
        int lowPowerTime = Integer.parseInt(mBind.tvLowPowerTimes.getText().toString().trim()) + 1;
        int eventPayloadType = confirmed.equals(mBind.tvEventPayloadType.getText().toString().trim()) ? 1 : 0;
        int eventTime = Integer.parseInt(mBind.tvEventTimes.getText().toString().trim()) + 1;
        int beaconPayloadType = confirmed.equals(mBind.tvBeaconPayloadType.getText().toString().trim()) ? 1 : 0;
        int beaconTime = Integer.parseInt(mBind.tvBeaconTimes.getText().toString().trim()) + 1;
        int shutdownPayloadType = confirmed.equals(mBind.tvShutdownPayloadType.getText().toString().trim()) ? 1 : 0;
        int shutdownTime = Integer.parseInt(mBind.tvShutdownTimes.getText().toString().trim()) + 1;
        List<OrderTask> orderTasks = new ArrayList<>(8);
        orderTasks.add(OrderTaskAssembler.setHeartbeatPayload(heartbeatPayloadType, heartbeatTime));
        orderTasks.add(OrderTaskAssembler.setParkingInfoPayload(parkingInfoPayloadType, parkingInfoTime));
        orderTasks.add(OrderTaskAssembler.setBeaconPayload(beaconPayloadType, beaconTime));
        orderTasks.add(OrderTaskAssembler.setLowPowerPayload(lowPowerPayloadType, lowPowerTime));
        orderTasks.add(OrderTaskAssembler.setShutdownPayload(shutdownPayloadType, shutdownTime));
        orderTasks.add(OrderTaskAssembler.setEventPayload(eventPayloadType, eventTime));
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    if (blueState == BluetoothAdapter.STATE_TURNING_OFF) {
                        dismissSyncProgressDialog();
                        finish();
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
    }
}
