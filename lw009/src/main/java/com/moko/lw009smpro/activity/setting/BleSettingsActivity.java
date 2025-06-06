package com.moko.lw009smpro.activity.setting;

import static com.moko.lw009smpro.AppConstants.SAVE_ERROR;
import static com.moko.lw009smpro.AppConstants.SAVE_SUCCESS;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputFilter;
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
import com.moko.lw009smpro.databinding.Lw009ActivityBleSettingsBinding;
import com.moko.lib.loraui.dialog.ChangePasswordDialog;
import com.moko.lw009smpro.entity.TxPowerEnum;
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
import java.util.Timer;
import java.util.TimerTask;

public class BleSettingsActivity extends Lw009BaseActivity implements SeekBar.OnSeekBarChangeListener {
    private final String FILTER_ASCII = "[ -~]*";
    private Lw009ActivityBleSettingsBinding mBind;
    private boolean savedParamsError;
    private boolean mPasswordVerifyEnable;
    private boolean mPasswordVerifyDisable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw009ActivityBleSettingsBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        InputFilter inputFilter = (source, start, end, dest, dStart, dEnd) -> {
            if (!(source + "").matches(FILTER_ASCII)) return "";
            return null;
        };
        mBind.etAdvName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16), inputFilter});
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(6);
        orderTasks.add(OrderTaskAssembler.getAdvName());
        orderTasks.add(OrderTaskAssembler.getAdvInterval());
        orderTasks.add(OrderTaskAssembler.getAdvTxPower());
        orderTasks.add(OrderTaskAssembler.getAdvTimeout());
        orderTasks.add(OrderTaskAssembler.getPasswordVerifyEnable());
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

    @SuppressLint("DefaultLocale")
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
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (header != 0xED || configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_ADV_NAME:
                                case KEY_ADV_INTERVAL:
                                case KEY_ADV_TIMEOUT:
                                case KEY_ADV_TX_POWER:
                                    if (result != 1) savedParamsError = true;
                                    break;
                                case KEY_PASSWORD_VERIFY_ENABLE:
                                    if (result != 1) savedParamsError = true;
                                    ToastUtils.showToast(this, savedParamsError ? SAVE_ERROR : SAVE_SUCCESS);
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_ADV_NAME:
                                    if (length > 0) {
                                        mBind.etAdvName.setText(new String(Arrays.copyOfRange(value, 4, 4 + length)));
                                        mBind.etAdvName.setSelection(mBind.etAdvName.getText().length());
                                    }
                                    break;
                                case KEY_ADV_TIMEOUT:
                                    if (length > 0) {
                                        mBind.etAdvTimeout.setText(String.valueOf(value[4] & 0xFF));
                                        mBind.etAdvTimeout.setSelection(mBind.etAdvTimeout.getText().length());
                                    }
                                    break;
                                case KEY_PASSWORD_VERIFY_ENABLE:
                                    if (length > 0) {
                                        int enable = value[4] & 0xFF;
                                        mPasswordVerifyEnable = enable == 1;
                                        mPasswordVerifyDisable = enable == 0;
                                        mBind.ivLoginMode.setImageResource(mPasswordVerifyEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                                        mBind.tvChangePassword.setVisibility(mPasswordVerifyEnable ? View.VISIBLE : View.GONE);
                                    }
                                    break;
                                case KEY_ADV_TX_POWER:
                                    if (length > 0) {
                                        int txPower = value[4];
                                        TxPowerEnum txPowerEnum = TxPowerEnum.fromTxPower(txPower);
                                        if (null != txPowerEnum) {
                                            int progress = txPowerEnum.ordinal();
                                            mBind.sbTxPower.setProgress(progress);
                                            mBind.tvTxPowerValue.setText(String.format("%ddBm", txPower));
                                        }
                                    }
                                    break;

                                case KEY_ADV_INTERVAL:
                                    if (length > 0) {
                                        mBind.etAdInterval.setText(String.valueOf(value[4] & 0xff));
                                        mBind.etAdInterval.setSelection(mBind.etAdInterval.getText().length());
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void onBack(View view) {
        onBackPressed();
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
        if (TextUtils.isEmpty(mBind.etAdvTimeout.getText())) return false;
        final String advTimeoutStr = mBind.etAdvTimeout.getText().toString();
        final int timeout = Integer.parseInt(advTimeoutStr);
        if (timeout < 1 || timeout > 60) return false;

        if (TextUtils.isEmpty(mBind.etAdInterval.getText())) return false;
        int interval = Integer.parseInt(mBind.etAdInterval.getText().toString().trim());
        return interval >= 1 && interval <= 100;
    }

    private void saveParams() {
        final String advName = mBind.etAdvName.getText().toString();
        final String timeoutStr = mBind.etAdvTimeout.getText().toString();
        final int timeout = Integer.parseInt(timeoutStr);
        final int progress = mBind.sbTxPower.getProgress();
        int interval = Integer.parseInt(mBind.etAdInterval.getText().toString().trim());
        TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>(6);
        orderTasks.add(OrderTaskAssembler.setAdvName(advName));
        orderTasks.add(OrderTaskAssembler.setAdvInterval(interval));
        orderTasks.add(OrderTaskAssembler.setAdvTimeout(timeout));
        if (txPowerEnum != null) {
            orderTasks.add(OrderTaskAssembler.setAdvTxPower(txPowerEnum.getTxPower()));
        }
        orderTasks.add(OrderTaskAssembler.setPasswordVerifyEnable(mPasswordVerifyEnable ? 1 : 0));
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onChangePassword(View view) {
        if (isWindowLocked()) return;
        if (mPasswordVerifyDisable) return;
        final ChangePasswordDialog dialog = new ChangePasswordDialog(this);
        dialog.setOnPasswordClicked(password -> {
            showSyncingProgressDialog();
            MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.changePassword(password));
        });
        dialog.show();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(dialog::showKeyboard);
            }
        }, 200);
    }

    public void onChangeLoginMode(View view) {
        if (isWindowLocked()) return;
        mPasswordVerifyEnable = !mPasswordVerifyEnable;
        mBind.ivLoginMode.setImageResource(mPasswordVerifyEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        mBind.tvChangePassword.setVisibility(mPasswordVerifyEnable ? View.VISIBLE : View.GONE);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
        if (txPowerEnum == null) return;
        int txPower = txPowerEnum.getTxPower();
        mBind.tvTxPowerValue.setText(String.format("%ddBm", txPower));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
