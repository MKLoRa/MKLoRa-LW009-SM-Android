package com.moko.lw009smpro.activity.device;

import static com.moko.lw009smpro.AppConstants.SAVE_ERROR;
import static com.moko.lw009smpro.AppConstants.SAVE_SUCCESS;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw009smpro.R;
import com.moko.lw009smpro.activity.Lw009BaseActivity;
import com.moko.lw009smpro.databinding.Lw009ActivitySelftestBinding;
import com.moko.lib.loraui.dialog.AlertMessageDialog;
import com.moko.lw009smpro.utils.FileUtils;
import com.moko.lw009smpro.utils.ToastUtils;
import com.moko.support.lw009.MoKoSupport;
import com.moko.support.lw009.OrderTaskAssembler;
import com.moko.support.lw009.entity.OrderCHAR;
import com.moko.support.lw009.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelfTestActivity extends Lw009BaseActivity {
    private Lw009ActivitySelftestBinding mBind;
    private boolean isLogSwitch;
    private int currentPackageIndex = 1;
    private int firmwareBytesLength;
    private boolean isUpdate;
    private int updateStatus;
    private byte[] firmwareBytes;
    private boolean isDialogShowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw009ActivitySelftestBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(6);
        orderTasks.add(OrderTaskAssembler.getSelfTestStatus());
        orderTasks.add(OrderTaskAssembler.getPCBAStatus());
        orderTasks.add(OrderTaskAssembler.getNoParkingCalibrationStatus());
        orderTasks.add(OrderTaskAssembler.getSlaveVersion());
        orderTasks.add(OrderTaskAssembler.getSlaveLogEnable());
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        mBind.ivParkLogSwitch.setOnClickListener(v -> {
            showSyncingProgressDialog();
            List<OrderTask> tasks = new ArrayList<>(2);
            tasks.add(OrderTaskAssembler.setSlaveLogEnable(isLogSwitch ? 0 : 1));
            tasks.add(OrderTaskAssembler.getSlaveLogEnable());
            MoKoSupport.getInstance().sendOrder(tasks.toArray(new OrderTask[0]));
        });
        mBind.tvDfu.setOnClickListener(v -> slaveLauncher.launch("*/*"));
    }

    private byte[] checkCrc(byte[] bytes) {
        int crc = 0x0000;
        for (byte aByte : bytes) {
            crc += aByte & 0xff;
        }
        return MokoUtils.toByteArray(crc, 4);
    }

    private final ActivityResultLauncher<String> slaveLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (null == result) return;
        String path = FileUtils.getPath(this, result);
        if (TextUtils.isEmpty(path) || !path.endsWith(".bin")) return;
        showDFUProgressDialog("Waiting...");
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
                    firmwareBytesLength = bytes.length;
                    byte[] firmwareCheckCrc = checkCrc(bytes);
                    firmwareBytes = bytes;
                    runOnUiThread(() -> MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.setTriggerSlaveUpdate(hardwareVersion, softwareVersion, deviceMode, firmwareBytesLength, firmwareCheckCrc)));
                } catch (Exception e) {
                    XLog.e(e);
                    runOnUiThread(() -> dismissSyncProgressDialog());
                }
            }
        }.start();
    });

    private byte[] getFirmwareBytes(int packageNum) {
        int currentIndex = packageNum * 128;
        if (currentIndex <= firmwareBytesLength) {
            return Arrays.copyOfRange(firmwareBytes, currentIndex - 128, currentIndex);
        } else {
            if (currentIndex - 128 >= firmwareBytesLength) return null;
            return Arrays.copyOfRange(firmwareBytes, currentIndex - 128, firmwareBytesLength);
        }
    }

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
        if (!isFinishing() && mDFUDialog != null && mDFUDialog.isShowing()) {
            mDFUDialog.dismiss();
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                if (!isUpdate) {
                    finish();
                } else {
                    if (!isDialogShowing) showDfuResult(updateStatus);
                }
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
                        if (flag == 0x01) {
                            switch (configKeyEnum) {
                                case KEY_SLAVE_LOG_SWITCH:
                                    ToastUtils.showToast(this, (value[4] & 0xff) == 1 ? SAVE_SUCCESS : SAVE_ERROR);
                                    break;

                                case KEY_TRIGGER_SLAVE_UPDATE:
                                    //触发从机固件升级
                                    if ((value[4] & 0xff) == 1) {
                                        //开始发送从机升级的固件包 发送第一包数据
                                        currentPackageIndex = 1;
                                        byte[] bytes = getFirmwareBytes(currentPackageIndex);
                                        assert null != bytes;
                                        XLog.i("当前是第" + currentPackageIndex + "包数据：" + MokoUtils.bytesToHexString(bytes));
                                        MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.setSlaveUpdate(bytes));
                                    }
                                    break;

                                case KEY_SLAVE_UPDATE:
                                    if ((value[4] & 0xff) == 1) {
                                        currentPackageIndex++;
                                        byte[] bytes = getFirmwareBytes(currentPackageIndex);
                                        if (null != bytes) {
                                            XLog.i("当前是第" + currentPackageIndex + "包数据：" + MokoUtils.bytesToHexString(bytes));
                                            MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.setSlaveUpdate(bytes));
                                        } else {
                                            mDFUDialog.setMessage("DFU is start,this may takes about 3 minutes");
                                            isUpdate = true;
                                            mBind.tvDfu.postDelayed(()-> MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.getSlaveWorkMode()),3000);
                                        }
                                    } else {
                                        ToastUtils.showToast(this, "Opps!DFU Failed. Please try again!");
                                        dismissDFUProgressDialog();
                                    }
                                    break;
                            }
                        }
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
                                        mBind.tvCalibrationStatus.setText(String.valueOf(value[4] & 0xff));
                                    }
                                    break;

                                case KEY_SLAVE_VERSION:
                                    if (length == 3) {
                                        mBind.tvParkingFWVersion.setText(String.valueOf(value[4] & 0xff));
                                        mBind.tvParkingHWVersion.setText(String.valueOf(value[5] & 0xff));
                                        mBind.tvParkingProtocolVersion.setText(String.valueOf(value[6] & 0xff));
                                    }
                                    break;

                                case KEY_SLAVE_LOG_SWITCH:
                                    if (length == 1) {
                                        isLogSwitch = (value[4] & 0xff) == 1;
                                        mBind.ivParkLogSwitch.setImageResource(isLogSwitch ? R.drawable.ic_checked : R.drawable.ic_unchecked);
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
                    if (header == 0xED && flag == 0x02 && cmd == ParamsKeyEnum.KEY_SLAVE_UPDATE_RESULT.getParamsKey() && len == 1) {
                        //从机升级结果通知
                        updateStatus = value[4] & 0xff;
                        dismissDFUProgressDialog();
                        showDfuResult(value[4] & 0xff);
                    }
                }
            }
        });
    }

    private void showDfuResult(int status) {
        AlertMessageDialog dialog = new AlertMessageDialog();
        isDialogShowing = true;
        dialog.setTitle("Update Firmware");
        dialog.setCancelGone();
        dialog.setConfirm("OK");
        String msg = status == 1 ? "Update Firmware successfully!\nPlease reconnect the device." : "Opps !DFU Failed. Please try again!";
        dialog.setMessage(msg);
        dialog.setOnAlertConfirmListener(this::finish);
        dialog.show(getSupportFragmentManager());
    }

    public void onBack(View view) {
        finish();
    }
}
