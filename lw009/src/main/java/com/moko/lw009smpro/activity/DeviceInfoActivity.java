package com.moko.lw009smpro.activity;


import static com.moko.lw009smpro.AppConstants.SAVE_ERROR;
import static com.moko.lw009smpro.AppConstants.SAVE_SUCCESS;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw009smpro.R;
import com.moko.lw009smpro.activity.lora.LoRaAppSettingActivity;
import com.moko.lw009smpro.activity.lora.LoRaConnSettingActivity;
import com.moko.lw009smpro.activity.setting.AdvancedSettingActivity;
import com.moko.lw009smpro.databinding.ActivityDeviceInfoBinding;
import com.moko.lw009smpro.dialog.AlertMessageDialog;
import com.moko.lw009smpro.fragment.DeviceFragment;
import com.moko.lw009smpro.fragment.GeneralFragment;
import com.moko.lw009smpro.fragment.LoRaFragment;
import com.moko.lw009smpro.fragment.ParkingFragment;
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

public class DeviceInfoActivity extends Lw009BaseActivity implements RadioGroup.OnCheckedChangeListener {
    private ActivityDeviceInfoBinding mBind;
    private FragmentManager fragmentManager;
    private LoRaFragment loraFragment;
    private ParkingFragment parkingFragment;
    private GeneralFragment generalFragment;
    private DeviceFragment deviceFragment;
    private final String[] mUploadMode = {"ABP", "OTAA"};
    private final String[] mRegions = {"AS923", "AU915", "EU868", "KR920", "IN865", "US915", "RU864", "AS923-1", "AS923-2", "AS923-3", "AS923-4"};
    private int mSelectedRegion;
    private int mSelectUploadMode;
    private int disConnectType;
    private boolean savedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityDeviceInfoBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        fragmentManager = getSupportFragmentManager();
        initFragment();
        mBind.radioBtnLora.setChecked(true);
        mBind.tvTitle.setText(R.string.title_lora);
        mBind.rgOptions.setOnCheckedChangeListener(this);
        if (!MoKoSupport.getInstance().isBluetoothOpen()) {
            MoKoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            mBind.frameContainer.postDelayed(() -> {
                List<OrderTask> orderTasks = new ArrayList<>();
                // sync time after connect success;
                orderTasks.add(OrderTaskAssembler.setTime());
                orderTasks.add(OrderTaskAssembler.getLoraRegion());
                orderTasks.add(OrderTaskAssembler.getLoraUploadMode());
                orderTasks.add(OrderTaskAssembler.getLoraNetworkStatus());
                MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
            }, 300);
        }
        mBind.layoutTop.setOnClickListener(v -> {
            if (mBind.radioBtnParking.isChecked()) {
                if (isTriggerValid()) showPwdDialog();
            }
        });
    }

    private void showPwdDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_advance, null);
        final EditText etPwd = v.findViewById(R.id.et_order_number);
        new AlertDialog.Builder(this)
                .setView(v)
                .setPositiveButton("Sure", (dialog, which) -> {
                    if (TextUtils.isEmpty(etPwd.getText())) {
                        ToastUtils.showToast(this, "password can not be empty！");
                        return;
                    }
                    String pwd = etPwd.getText().toString();
                    if (!"MOKO4567".equals(pwd)) {
                        ToastUtils.showToast(this, "password error");
                        return;
                    }
                    startActivity(new Intent(this, AdvancedSettingActivity.class));
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void initFragment() {
        loraFragment = LoRaFragment.newInstance();
        parkingFragment = ParkingFragment.newInstance();
        generalFragment = GeneralFragment.newInstance();
        deviceFragment = DeviceFragment.newInstance();
        fragmentManager.beginTransaction()
                .add(R.id.frame_container, loraFragment)
                .add(R.id.frame_container, parkingFragment)
                .add(R.id.frame_container, generalFragment)
                .add(R.id.frame_container, deviceFragment)
                .show(loraFragment)
                .hide(parkingFragment)
                .hide(generalFragment)
                .hide(deviceFragment)
                .commit();
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                if (MoKoSupport.getInstance().exportDatas != null) {
                    MoKoSupport.getInstance().exportDatas.clear();
                    MoKoSupport.getInstance().storeString = null;
                    MoKoSupport.getInstance().startTime = 0;
                    MoKoSupport.getInstance().sum = 0;
                }
                showDisconnectDialog();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                int header = value[0] & 0xFF;
                int flag = value[1] & 0xFF;
                int cmd = value[2] & 0xFF;
                int len = value[3] & 0xFF;
                if (orderCHAR == OrderCHAR.CHAR_DISCONNECTED_NOTIFY) {
                    if (len != 1) return;
                    if (header == 0xED && flag == 0x02 && cmd == 0x01) {
                        disConnectType = value[4] & 0xFF;
                    }
                } else if (orderCHAR == OrderCHAR.CHAR_SLAVE_NOTIFY) {
                    //从机通知
                    if (header == 0xED && flag == 0x02 && cmd == ParamsKeyEnum.KEY_NO_PARKING_CALIBRATION_RESULT.getParamsKey()) {
                        if (null != parkingFragment) {
                            parkingFragment.setNoParkingCalibration(value[4] & 0xff);
                        }
                    }
                }
            }
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
                                case KEY_TIME_UTC:
                                    if (result == 1)
                                        ToastUtils.showToast(this, "Time sync completed!");
                                    break;
                                case KEY_HEARTBEAT_INTERVAL:
                                case KEY_TIME_ZONE:
                                case KEY_LOW_POWER_PAYLOAD_ENABLE:
                                    ToastUtils.showToast(this, result != 1 ? SAVE_ERROR : SAVE_SUCCESS);
                                    break;
                                case KEY_PARKING_DETECTION_MODE:
                                case KEY_PARKING_DETECTION_SENSITIVITY:
                                case KEY_PARKING_DETECTION_DURATION:
                                case KEY_PARKING_DETECTION_CONFIRM_DURATION:
                                    if (result != 1) savedParamsError = true;
                                    break;
                                case KEY_PARKING_DETECTION_PAYLOAD_TYPE:
                                    if (result != 1) savedParamsError = true;
                                    ToastUtils.showToast(this, savedParamsError ? SAVE_ERROR : SAVE_SUCCESS);
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_LORA_REGION:
                                    if (length > 0) {
                                        mSelectedRegion = value[4] & 0xFF;
                                        if (mSelectedRegion > 1) mSelectedRegion -= 3;
                                    }
                                    break;
                                case KEY_LORA_MODE:
                                    if (length > 0) {
                                        mSelectUploadMode = value[4];
                                        String loraInfo = String.format("%s/%s/ClassA",
                                                mUploadMode[mSelectUploadMode - 1],
                                                mRegions[mSelectedRegion]);
                                        loraFragment.setLoRaInfo(loraInfo);
                                    }
                                    break;
                                case KEY_LORA_NETWORK_STATUS:
                                    if (length > 0) {
                                        loraFragment.setLoraStatus(value[4] & 0xFF);
                                    }
                                    break;
                                case KEY_HEARTBEAT_INTERVAL:
                                    if (length > 0) {
                                        byte[] intervalBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        generalFragment.setHeartbeatInterval(MokoUtils.toInt(intervalBytes));
                                    }
                                    break;
                                case KEY_TIME_ZONE:
                                    if (length > 0) {
                                        deviceFragment.setTimeZone(value[4]);
                                    }
                                    break;
                                case KEY_LOW_POWER_PAYLOAD_ENABLE:
                                    if (length > 0) {
                                        deviceFragment.setLowPowerPayload(value[4] & 0xFF);
                                    }
                                    break;

                                case KEY_PARKING_DETECTION_MODE:
                                    if (length == 1) {
                                        parkingFragment.setParkingDetectionMode(value[4] & 0xff);
                                    }
                                    break;

                                case KEY_PARKING_DETECTION_SENSITIVITY:
                                    if (length == 1) {
                                        parkingFragment.setParkingDetectionSensitivity(value[4] & 0xff);
                                    }
                                    break;

                                case KEY_PARKING_DETECTION_DURATION:
                                    if (length == 1) {
                                        parkingFragment.setParkingDetectionDuration(value[4] & 0xff);
                                    }
                                    break;

                                case KEY_PARKING_DETECTION_CONFIRM_DURATION:
                                    if (length == 1) {
                                        parkingFragment.setParkingDetectionConfirmDuration(value[4] & 0xff);
                                    }
                                    break;

                                case KEY_PARKING_DETECTION_PAYLOAD_TYPE:
                                    if (length == 2) {
                                        parkingFragment.setReportSwitch(value[4] & 0xff, value[5] & 0xff);
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void showDisconnectDialog() {
        if (disConnectType == 2) {
            showDisconnectType("Change Password", "Password changed successfully!Please reconnect the device.", "OK");
        } else if (disConnectType == 3) {
            showDisconnectType(null, "No data communication for 3 minutes, the device is disconnected.", "OK");
        } else if (disConnectType == 5) {
            showDisconnectType("Factory Reset", "Factory reset successfully!\nPlease reconnect the device.", "OK");
        } else if (disConnectType == 4) {
            showDisconnectType("Dismiss", "Reboot successfully!\nPlease reconnect the device", "OK");
        } else if (disConnectType == 1) {
            showDisconnectType(null, "The device is disconnected!", "OK");
        } else {
            if (MoKoSupport.getInstance().isBluetoothOpen()) {
                showDisconnectType("Dismiss", "The device disconnected!", "Exit");
            }
        }
    }

    private void showDisconnectType(String title, String message, String confirm) {
        AlertMessageDialog dialog = new AlertMessageDialog();
        if (!TextUtils.isEmpty(title)) dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setConfirm(confirm);
        dialog.setCancelGone();
        dialog.setOnAlertConfirmListener(() -> {
            setResult(RESULT_OK);
            finish();
        });
        dialog.show(getSupportFragmentManager());
    }

    @Override
    protected void onSystemBleTurnOff() {
        dismissSyncProgressDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceInfoActivity.this);
        builder.setTitle("Dismiss");
        builder.setCancelable(false);
        builder.setMessage("The current system of bluetooth is not available!");
        builder.setPositiveButton("OK", (dialog, which) -> {
            setResult(RESULT_OK);
            finish();
        });
        builder.show();
    }

    public void onBack(View view) {
        back();
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        if (mBind.radioBtnGeneral.isChecked()) {
            generalFragment.saveParams();
        } else if (mBind.radioBtnParking.isChecked()) {
            savedParamsError = false;
            parkingFragment.saveParkingParams();
        }
    }

    private void back() {
        MoKoSupport.getInstance().disConnectBle();
    }

    @Override
    public void onBackPressed() {
        if (isWindowLocked()) return;
        back();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (checkedId == R.id.radioBtn_lora) {
            showLoRaAndGetData();
        } else if (checkedId == R.id.radioBtn_parking) {
            showParkingAndGetData();
        } else if (checkedId == R.id.radioBtn_general) {
            showGeneralAndGetData();
        } else if (checkedId == R.id.radioBtn_device) {
            showDeviceAndGetData();
        }
    }

    private void showDeviceAndGetData() {
        mBind.tvTitle.setText("Device Settings");
        mBind.ivSave.setVisibility(View.GONE);
        showFragment(deviceFragment);
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(2);
        orderTasks.add(OrderTaskAssembler.getTimeZone());
        orderTasks.add(OrderTaskAssembler.getLowPowerPayloadEnable());
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private void showFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                .hide(loraFragment)
                .hide(parkingFragment)
                .hide(generalFragment)
                .hide(deviceFragment)
                .show(fragment)
                .commit();
    }

    private void showGeneralAndGetData() {
        mBind.tvTitle.setText("General");
        mBind.ivSave.setVisibility(View.VISIBLE);
        showFragment(generalFragment);
        showSyncingProgressDialog();
        MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.getHeartBeatInterval());
    }

    private void showParkingAndGetData() {
        mBind.tvTitle.setText("Parking");
        mBind.ivSave.setVisibility(View.VISIBLE);
        showFragment(parkingFragment);
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(6);
        orderTasks.add(OrderTaskAssembler.getParkingDetectionMode());
        orderTasks.add(OrderTaskAssembler.getParkingDetectionSensitivity());
        orderTasks.add(OrderTaskAssembler.getParkingDetectionDuration());
        orderTasks.add(OrderTaskAssembler.getParkingDetectionConfirmDuration());
        orderTasks.add(OrderTaskAssembler.getParkingDetectionPayloadType());
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
    }

    private void showLoRaAndGetData() {
        mBind.tvTitle.setText(R.string.title_lora);
        mBind.ivSave.setVisibility(View.GONE);
        showFragment(loraFragment);
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.getLoraRegion());
        orderTasks.add(OrderTaskAssembler.getLoraUploadMode());
        orderTasks.add(OrderTaskAssembler.getLoraNetworkStatus());
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onLoRaConnSetting(View view) {
        if (isWindowLocked()) return;
        launcher.launch(new Intent(this, LoRaConnSettingActivity.class));
    }

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (null != result && result.getResultCode() == RESULT_OK) {
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(4);
            orderTasks.add(OrderTaskAssembler.getLoraRegion());
            orderTasks.add(OrderTaskAssembler.getLoraUploadMode());
            orderTasks.add(OrderTaskAssembler.getLoraNetworkStatus());
            MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
    });

    public void onLoRaAppSetting(View view) {
        if (isWindowLocked()) return;
        startActivity(new Intent(this, LoRaAppSettingActivity.class));
    }

    private long mLastOnClickTime = 0;
    private int mTriggerSum;

    private boolean isTriggerValid() {
        long current = SystemClock.elapsedRealtime();
        if (current - mLastOnClickTime > 500) {
            mTriggerSum = 0;
            mLastOnClickTime = current;
        } else {
            mTriggerSum++;
            if (mTriggerSum == 3) {
                mTriggerSum = 0;
                return true;
            }
        }
        return false;
    }
}
