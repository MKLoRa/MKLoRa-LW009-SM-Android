package com.moko.lw009smpro.activity.lora;

import static com.moko.lw009smpro.AppConstants.SAVE_ERROR;
import static com.moko.lw009smpro.AppConstants.SAVE_SUCCESS;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.lw009smpro.activity.Lw009BaseActivity;
import com.moko.lw009smpro.databinding.Lw009ActivityAppSettingBinding;
import com.moko.lw009smpro.utils.ToastUtils;
import com.moko.support.lw009.MoKoSupport;
import com.moko.support.lw009.OrderTaskAssembler;
import com.moko.support.lw009.entity.OrderCHAR;
import com.moko.support.lw009.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class LoRaAppSettingActivity extends Lw009BaseActivity {
    private Lw009ActivityAppSettingBinding mBind;
    private boolean savedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw009ActivityAppSettingBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(2);
        orderTasks.add(OrderTaskAssembler.getLoraTimeSyncInterval());
        orderTasks.add(OrderTaskAssembler.getLoraNetworkCheckInterval());
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        mBind.layoutMsgTypeSetting.setOnClickListener(v -> startActivity(new Intent(this, MessageTypeSettingsActivity.class)));
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
                                case KEY_LORA_TIME_SYNC_INTERVAL:
                                    if (result != 1) savedParamsError = true;
                                    break;

                                case KEY_LORA_NETWORK_CHECK_INTERVAL:
                                    if (result != 1) savedParamsError = true;
                                    ToastUtils.showToast(this, savedParamsError ? SAVE_ERROR : SAVE_SUCCESS);
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_LORA_TIME_SYNC_INTERVAL:
                                    if (length > 0) {
                                        int interval = value[4] & 0xFF;
                                        mBind.etSyncInterval.setText(String.valueOf(interval));
                                        mBind.etSyncInterval.setSelection(mBind.etSyncInterval.getText().length());
                                    }
                                    break;

                                case KEY_LORA_NETWORK_CHECK_INTERVAL:
                                    if (length > 0) {
                                        int interval = value[4] & 0xFF;
                                        mBind.etNetworkCheckInterval.setText(String.valueOf(interval));
                                        mBind.etNetworkCheckInterval.setSelection(mBind.etNetworkCheckInterval.getText().length());
                                    }
                                    break;
                            }
                        }
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

    private boolean isValid() {
        if (TextUtils.isEmpty(mBind.etSyncInterval.getText())) return false;
        final String syncIntervalStr = mBind.etSyncInterval.getText().toString();
        final int syncInterval = Integer.parseInt(syncIntervalStr);
        if (syncInterval > 255) return false;

        if (TextUtils.isEmpty(mBind.etNetworkCheckInterval.getText())) return false;
        final String networkCheckIntervalStr = mBind.etNetworkCheckInterval.getText().toString();
        final int networkCheckInterval = Integer.parseInt(networkCheckIntervalStr);
        return networkCheckInterval <= 255;
    }

    private void saveParams() {
        final String syncIntervalStr = mBind.etSyncInterval.getText().toString();
        final String networkCheckIntervalStr = mBind.etNetworkCheckInterval.getText().toString();
        final int syncInterval = Integer.parseInt(syncIntervalStr);
        final int networkCheckInterval = Integer.parseInt(networkCheckIntervalStr);
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setLoraTimeSyncInterval(syncInterval));
        orderTasks.add(OrderTaskAssembler.setLoraNetworkInterval(networkCheckInterval));
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onBack(View view) {
        backHome();
    }

    @Override
    public void onBackPressed() {
        backHome();
    }

    private void backHome() {
        EventBus.getDefault().unregister(this);
        finish();
    }
}
