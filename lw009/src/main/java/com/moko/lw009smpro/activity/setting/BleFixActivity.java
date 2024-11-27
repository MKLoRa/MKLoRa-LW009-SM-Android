package com.moko.lw009smpro.activity.setting;

import static com.moko.lw009smpro.AppConstants.SAVE_ERROR;
import static com.moko.lw009smpro.AppConstants.SAVE_SUCCESS;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.SeekBar;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.lw009smpro.R;
import com.moko.lw009smpro.activity.Lw009BaseActivity;
import com.moko.lw009smpro.activity.filter.FilterAdvNameActivity;
import com.moko.lw009smpro.activity.filter.FilterMacAddressActivity;
import com.moko.lw009smpro.activity.filter.FilterRawDataSwitchActivity;
import com.moko.lw009smpro.databinding.Lw009ActivityBleFixBinding;
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

public class BleFixActivity extends Lw009BaseActivity implements SeekBar.OnSeekBarChangeListener {
    private Lw009ActivityBleFixBinding mBind;
    private boolean savedParamsError;
    private final String[] mRelationshipValues = {"Null", "Only MAC", "Only ADV Name", "Only Raw Data", "ADV Name&Raw Data", "MAC&ADV Name&Raw Data", "ADV Name | Raw Data"};
    private int mRelationshipSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw009ActivityBleFixBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());

        mBind.sbRssiFilter.setOnSeekBarChangeListener(this);
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.getBleScanTime());
        orderTasks.add(OrderTaskAssembler.getPayloadBeaconCount());
        orderTasks.add(OrderTaskAssembler.getFilterRSSI());
        orderTasks.add(OrderTaskAssembler.getFilterRelationship());
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
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
                                case KEY_BLE_SCAN_TIME:
                                case KEY_PAYLOAD_BEACON_COUNT:
                                case KEY_FILTER_RSSI:
                                    if (result != 1) savedParamsError = true;
                                    break;
                                case KEY_FILTER_RELATIONSHIP:
                                    if (result != 1) savedParamsError = true;
                                    ToastUtils.showToast(this, savedParamsError ? SAVE_ERROR : SAVE_SUCCESS);
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_BLE_SCAN_TIME:
                                    if (length > 0) {
                                        mBind.etPosTimeout.setText(String.valueOf(value[4] & 0xFF));
                                        mBind.etPosTimeout.setSelection(mBind.etPosTimeout.getText().length());
                                    }
                                    break;
                                case KEY_PAYLOAD_BEACON_COUNT:
                                    if (length > 0) {
                                        mBind.etMacNumber.setText(String.valueOf(value[4] & 0xFF));
                                        mBind.etMacNumber.setSelection(mBind.etMacNumber.getText().length());
                                    }
                                    break;
                                case KEY_FILTER_RSSI:
                                    if (length > 0) {
                                        final int rssi = value[4];
                                        int progress = rssi + 127;
                                        mBind.sbRssiFilter.setProgress(progress);
                                        mBind.tvRssiFilterTips.setText(getString(R.string.rssi_filter, rssi));
                                    }
                                    break;
                                case KEY_FILTER_RELATIONSHIP:
                                    if (length > 0) {
                                        mRelationshipSelected = value[4] & 0xFF;
                                        mBind.tvFilterRelationship.setText(mRelationshipValues[mRelationshipSelected]);
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
        if (TextUtils.isEmpty(mBind.etPosTimeout.getText())) return false;
        final int posTimeout = Integer.parseInt(mBind.etPosTimeout.getText().toString());
        if (posTimeout < 1 || posTimeout > 10) return false;
        if (TextUtils.isEmpty(mBind.etMacNumber.getText())) return false;
        final int number = Integer.parseInt(mBind.etMacNumber.getText().toString());
        return number >= 1 && number <= 15;
    }


    private void saveParams() {
        final String posTimeoutStr = mBind.etPosTimeout.getText().toString();
        final String numberStr = mBind.etMacNumber.getText().toString();
        final int posTimeout = Integer.parseInt(posTimeoutStr);
        final int number = Integer.parseInt(numberStr);
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.setBleScanTime(posTimeout));
        orderTasks.add(OrderTaskAssembler.setBeaconPayloadCount(number));
        orderTasks.add(OrderTaskAssembler.setFilterRSSI(mBind.sbRssiFilter.getProgress() - 127));
        orderTasks.add(OrderTaskAssembler.setFilterRelationship(mRelationshipSelected));
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
        setResult(RESULT_OK);
        EventBus.getDefault().unregister(this);
        finish();
    }

    public void onFilterRelationship(View view) {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(mRelationshipValues)), mRelationshipSelected);
        dialog.setListener(value -> {
            mRelationshipSelected = value;
            mBind.tvFilterRelationship.setText(mRelationshipValues[value]);
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onFilterByMac(View view) {
        startActivity(FilterMacAddressActivity.class);
    }

    public void onFilterByName(View view) {
        startActivity(FilterAdvNameActivity.class);
    }

    public void onFilterByRawData(View view) {
        startActivity(FilterRawDataSwitchActivity.class);
    }

    private void startActivity(Class<?> clazz) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        int rssi = progress - 127;
        mBind.tvRssiFilterValue.setText(String.format("%ddBm", rssi));
        mBind.tvRssiFilterTips.setText(getString(R.string.rssi_filter, rssi));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
