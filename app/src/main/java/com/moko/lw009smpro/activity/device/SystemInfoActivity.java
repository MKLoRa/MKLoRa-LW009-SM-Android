package com.moko.lw009smpro.activity.device;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw009smpro.AppConstants;
import com.moko.lw009smpro.activity.Lw009BaseActivity;
import com.moko.lw009smpro.databinding.ActivitySystemInfoBinding;
import com.moko.lw009smpro.dialog.AlertMessageDialog;
import com.moko.lw009smpro.service.DfuService;
import com.moko.lw009smpro.utils.FileUtils;
import com.moko.lw009smpro.utils.ToastUtils;
import com.moko.support.lw009.MoKoSupport;
import com.moko.support.lw009.OrderTaskAssembler;
import com.moko.support.lw009.entity.OrderCHAR;
import com.moko.support.lw009.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

public class SystemInfoActivity extends Lw009BaseActivity {
    private ActivitySystemInfoBinding mBind;
    private String mDeviceMac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivitySystemInfoBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(8);
        orderTasks.add(OrderTaskAssembler.getMacAddress());
        orderTasks.add(OrderTaskAssembler.getBattery());
        orderTasks.add(OrderTaskAssembler.getDeviceModel());
        orderTasks.add(OrderTaskAssembler.getSoftwareVersion());
        orderTasks.add(OrderTaskAssembler.getFirmwareVersion());
        orderTasks.add(OrderTaskAssembler.getHardwareVersion());
        orderTasks.add(OrderTaskAssembler.getManufacturer());
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
        mBind.layoutTop.setOnClickListener(v -> {
            if (isTriggerValid()) {
                startActivity(new Intent(this, SelfTestActivity.class));
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                if (!isUpgrade) {
                    finish();
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
        if (!MokoConstants.ACTION_CURRENT_DATA.equals(action))
            EventBus.getDefault().cancelEventDelivery(event);
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
                ToastUtils.showToast(this, "time out");
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
//                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_MODEL_NUMBER:
                        String productModel = new String(value);
                        mBind.tvProductModel.setText(productModel);
                        break;
                    case CHAR_SOFTWARE_REVISION:
                        String softwareVersion = new String(value);
                        mBind.tvSoftwareVersion.setText(softwareVersion);
                        break;
                    case CHAR_FIRMWARE_REVISION:
                        String firmwareVersion = new String(value);
                        mBind.tvFirmwareVersion.setText(firmwareVersion);
                        break;
                    case CHAR_HARDWARE_REVISION:
                        String hardwareVersion = new String(value);
                        mBind.tvHardwareVersion.setText(hardwareVersion);
                        break;
                    case CHAR_MANUFACTURER_NAME:
                        dismissSyncProgressDialog();
                        String manufacture = new String(value);
                        mBind.tvManufacture.setText(manufacture);
                        break;
                    case CHAR_PARAMS:
                        if (value.length >= 4) {
                            int header = value[0] & 0xFF;// 0xED
                            int flag = value[1] & 0xFF;// read or write
                            int cmd = value[2] & 0xFF;
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                            if (header != 0xED || configKeyEnum == null) return;
                            int length = value[3] & 0xFF;
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_BATTERY_POWER:
                                        if (length > 0) {
                                            byte[] batteryBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            int battery = MokoUtils.toInt(batteryBytes);
                                            String batteryStr = MokoUtils.getDecimalFormat("0.000").format(battery * 0.001);
                                            mBind.tvBattery.setText(String.format("%sV", batteryStr));
                                        }
                                        break;
                                    case KEY_CHIP_MAC:
                                        if (length > 0) {
                                            byte[] macBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            String mac = MokoUtils.bytesToHexString(macBytes);
                                            StringBuilder builder = new StringBuilder(mac);
                                            builder.insert(2, ":");
                                            builder.insert(5, ":");
                                            builder.insert(8, ":");
                                            builder.insert(11, ":");
                                            builder.insert(14, ":");
                                            mDeviceMac = builder.toString().toUpperCase();
                                            mBind.tvMacAddress.setText(mDeviceMac);
                                        }
                                        break;
                                }
                            }
                        }
                        break;
                }
            } else if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                int header = value[0] & 0xFF;
                int flag = value[1] & 0xFF;
                int cmd = value[2] & 0xFF;
                int len = value[3] & 0xFF;
                if (orderCHAR == OrderCHAR.CHAR_SLAVE_NOTIFY) {
                    if (header == 0xED && flag == 0x02 && cmd == ParamsKeyEnum.KEY_SLAVE_FIRMWARE_REQUEST.getParamsKey() && len == 2) {
                        //从机升级固件请求
                        int packageNum = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                        XLog.i("当前包数：" + packageNum);
                        MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.setSlaveUpdate(getFirmwareBytes(packageNum)));
                    } else if (header == 0xED && flag == 0x02 && cmd == ParamsKeyEnum.KEY_SLAVE_UPDATE_RESULT.getParamsKey() && len == 1) {
                        //从机升级结果通知
                        dismissSyncProgressDialog();
                        ToastUtils.showToast(this, value[4] == 1 ? "update success" : "update fail");
                    }
                }
            }
        });
    }

    private byte[] getFirmwareBytes(int packageNum) {
        int length = firmwareBytes.length;
        int currentIndex = packageNum * 128;
        if (currentIndex <= length) {
            return Arrays.copyOfRange(firmwareBytes, currentIndex - 128, currentIndex);
        } else {
            return Arrays.copyOfRange(firmwareBytes, currentIndex - 128, length);
        }
    }

    public void onDebuggerMode(View view) {
        if (isWindowLocked()) return;
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Debugger Mode");
        dialog.setMessage("Do you want to let the device come into debugger mode?");
        dialog.setCancel("Cancel");
        dialog.setConfirm("OK");
        dialog.setOnAlertConfirmListener(() -> {
            Intent intent = new Intent(this, LogDataActivity.class);
            intent.putExtra(AppConstants.EXTRA_KEY_DEVICE_MAC, mDeviceMac);
            startActivity(intent);
        });
        dialog.show(getSupportFragmentManager());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
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

    public void onUpdateFirmware(View view) {
        if (isWindowLocked() || TextUtils.isEmpty(mDeviceMac)) return;
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setMessage("Please disconnect the load device before DFU, otherwise there may be security risks.");
        dialog.setCancel("Cancel");
        dialog.setConfirm("OK");
        dialog.setOnAlertConfirmListener(() -> {
//            launcher.launch("application/zip");
            slaveLauncher.launch("application/octet-stream");
        });
        dialog.show(getSupportFragmentManager());
    }

    private byte[] checkCrc(byte[] bytes) {
        int crc = 0x0000;
        for (byte aByte : bytes) {
            crc += aByte & 0xff;
        }
        crc ^= 0x1021;
        return MokoUtils.toByteArray(crc, 4);
    }

    private byte[] firmwareBytes;

    private final ActivityResultLauncher<String> slaveLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (null == result) return;
        String path = FileUtils.getPath(this, result);
        if (TextUtils.isEmpty(path)) return;
        showSyncingProgressDialog();
        new Thread() {
            @Override
            public void run() {
                List<Integer> list = new ArrayList<>();
                try {
                    FileInputStream inputStream = new FileInputStream(path);
                    int data;
                    while ((data = inputStream.read()) != -1) {
                        list.add(data);
                    }
                    inputStream.close();
                    Integer[] array = list.toArray(new Integer[0]);
                    byte[] bytes = new byte[array.length];
                    for (int i = 0; i < array.length; i++) {
                        bytes[i] = array[i].byteValue();
                    }
                    int hardwareVersion = bytes[82] & 0xff;
                    int softwareVersion = bytes[81] & 0xff;
                    int deviceMode = bytes[80] & 0xff;
                    int firmwareLength = bytes.length;
                    byte[] firmwareCheckCrc = checkCrc(bytes);
                    firmwareBytes = bytes;
                    runOnUiThread(() -> MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.setTriggerSlaveUpdate(hardwareVersion, softwareVersion, deviceMode, firmwareLength, firmwareCheckCrc)));
                } catch (Exception e) {
                    XLog.e(e);
                    runOnUiThread(() -> dismissSyncProgressDialog());
                }
            }
        }.start();
    });

    private final ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (null == result) return;
        String firmwareFilePath = FileUtils.getPath(this, result);
        if (TextUtils.isEmpty(firmwareFilePath)) return;
        final File firmwareFile = new File(firmwareFilePath);
        if (!firmwareFile.exists() || firmwareFile.length() == 0) {
            ToastUtils.showToast(this, "File error!");
            return;
        }
        final DfuServiceInitiator starter = new DfuServiceInitiator(mDeviceMac)
                .setKeepBond(false)
                .setForeground(false)
                .setDisableNotification(true);
        starter.setZip(null, firmwareFilePath);
        starter.start(this, DfuService.class);
        showDFUProgressDialog("Waiting...");
    });

    private ProgressDialog mDFUDialog;

    private void showDFUProgressDialog(String tips) {
        mDFUDialog = new ProgressDialog(this);
        mDFUDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDFUDialog.setCanceledOnTouchOutside(false);
        mDFUDialog.setCancelable(false);
        mDFUDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDFUDialog.setMessage(tips);
        if (!isFinishing() && mDFUDialog != null && !mDFUDialog.isShowing()) {
            mDFUDialog.show();
        }
    }

    private void dismissDFUProgressDialog() {
        mDeviceConnectCount = 0;
        if (!isFinishing() && mDFUDialog != null && mDFUDialog.isShowing()) {
            mDFUDialog.dismiss();
        }
    }

    private boolean isUpgrade;
    private int mDeviceConnectCount;

    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(@NonNull String deviceAddress) {
            XLog.w("onDeviceConnecting...");
            mDeviceConnectCount++;
            if (mDeviceConnectCount > 3) {
                ToastUtils.showToast(SystemInfoActivity.this, "Error:DFU Failed");
                dismissDFUProgressDialog();
                final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(SystemInfoActivity.this);
                final Intent abortAction = new Intent(DfuService.BROADCAST_ACTION);
                abortAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_ABORT);
                manager.sendBroadcast(abortAction);
            }
        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            XLog.w("onDeviceDisconnecting...");
        }

        @Override
        public void onDfuProcessStarting(@NonNull String deviceAddress) {
            isUpgrade = true;
            mDFUDialog.setMessage("DfuProcessStarting...");
        }

        @Override
        public void onEnablingDfuMode(@NonNull String deviceAddress) {
            mDFUDialog.setMessage("EnablingDfuMode...");
        }

        @Override
        public void onFirmwareValidating(@NonNull String deviceAddress) {
            mDFUDialog.setMessage("FirmwareValidating...");
        }

        @Override
        public void onDfuCompleted(@NonNull String deviceAddress) {
            mDeviceConnectCount = 0;
            if (!isFinishing() && mDFUDialog != null && mDFUDialog.isShowing()) {
                mDFUDialog.dismiss();
            }
            showDfuSuccess();
        }

        @Override
        public void onDfuAborted(@NonNull String deviceAddress) {
            mDFUDialog.setMessage("DfuAborted...");
        }

        @Override
        public void onProgressChanged(@NonNull String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            XLog.i("Progress:" + percent + "%");
            mDFUDialog.setMessage("Progress：" + percent + "%");
        }

        @Override
        public void onError(@NonNull String deviceAddress, int error, int errorType, String message) {
            ToastUtils.showToast(SystemInfoActivity.this, "Opps!DFU Failed. Please try again!");
            XLog.i("Error:" + message);
            dismissDFUProgressDialog();
        }
    };

    private void showDfuSuccess() {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Update Firmware");
        dialog.setMessage("Update firmware successfully!\nPlease reconnect the device.");
        dialog.setConfirm("OK");
        dialog.setCancelGone();
        dialog.setOnAlertConfirmListener(this::finish);
        dialog.show(getSupportFragmentManager());
    }

    // 记录上次页面控件点击时间,屏蔽无效点击事件
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
