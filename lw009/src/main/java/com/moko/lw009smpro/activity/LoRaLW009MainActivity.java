package com.moko.lw009smpro.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.lw009smpro.AppConstants;
import com.moko.lw009smpro.BuildConfig;
import com.moko.lw009smpro.R;
import com.moko.lw009smpro.activity.device.LogDataActivity;
import com.moko.lw009smpro.adapter.DeviceListAdapter;
import com.moko.lw009smpro.databinding.Lw009ActivityMainBinding;
import com.moko.lib.loraui.dialog.AlertMessageDialog;
import com.moko.lib.loraui.dialog.LoadingDialog;
import com.moko.lib.loraui.dialog.LoadingMessageDialog;
import com.moko.lib.loraui.dialog.PasswordDialog;
import com.moko.lib.loraui.dialog.ScanFilterDialog;
import com.moko.lw009smpro.entity.AdvInfo;
import com.moko.lw009smpro.utils.AdvInfoAnalysisImpl;
import com.moko.lw009smpro.utils.SPUtiles;
import com.moko.lw009smpro.utils.ToastUtils;
import com.moko.support.lw009.MoKoSupport;
import com.moko.support.lw009.MokoBleScanner;
import com.moko.support.lw009.OrderTaskAssembler;
import com.moko.support.lw009.callback.MokoScanDeviceCallback;
import com.moko.support.lw009.entity.DeviceInfo;
import com.moko.support.lw009.entity.OrderCHAR;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class LoRaLW009MainActivity extends Lw009BaseActivity implements MokoScanDeviceCallback, BaseQuickAdapter.OnItemChildClickListener {
    private Lw009ActivityMainBinding mBind;
    private ConcurrentHashMap<String, AdvInfo> beaconInfoHashMap;
    private ArrayList<AdvInfo> beaconInfo;
    private DeviceListAdapter adapter;
    private Animation animation = null;
    private MokoBleScanner mokoBleScanner;
    public Handler mHandler;
    private boolean isPasswordError;
    private boolean isVerifyEnable;
    public static String PATH_LOGCAT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw009ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        // 初始化Xlog
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 优先保存到SD卡中
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PATH_LOGCAT = getExternalFilesDir(null).getAbsolutePath() + File.separator + (BuildConfig.IS_LIBRARY ? "MKLoRa" : "LW009");
            } else {
                PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + (BuildConfig.IS_LIBRARY ? "MKLoRa" : "LW009");
            }
        } else {
            // 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = getFilesDir().getAbsolutePath() + File.separator + (BuildConfig.IS_LIBRARY ? "MKLoRa" : "LW009");
        }
        MoKoSupport.getInstance().init(getApplicationContext());
        mSavedPassword = SPUtiles.getStringValue(this, AppConstants.SP_KEY_SAVED_PASSWORD, "");
        beaconInfoHashMap = new ConcurrentHashMap<>();
        beaconInfo = new ArrayList<>();
        adapter = new DeviceListAdapter();
        adapter.replaceData(beaconInfo);
        adapter.setOnItemChildClickListener(this);
        adapter.openLoadAnimation();
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.shape_recycleview_divider)));
        mBind.rvDevices.addItemDecoration(itemDecoration);
        mBind.rvDevices.setAdapter(adapter);
        mHandler = new Handler(Looper.getMainLooper());
        mokoBleScanner = new MokoBleScanner();
        if (!MoKoSupport.getInstance().isBluetoothOpen()) {
            MoKoSupport.getInstance().enableBluetooth();
        } else {
            if (animation == null) {
                startScan();
            }
        }
    }

    private void startScan() {
        if (!MoKoSupport.getInstance().isBluetoothOpen()) {
            MoKoSupport.getInstance().enableBluetooth();
            return;
        }
        animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
        mBind.ivRefresh.startAnimation(animation);
        beaconInfoParseable = new AdvInfoAnalysisImpl();
        mokoBleScanner.startScanDevice(this);
        mHandler.postDelayed(() -> mokoBleScanner.stopScanDevice(), 1000 * 60);
    }

    private AdvInfoAnalysisImpl beaconInfoParseable;
    public String filterName;
    public int filterRssi = -127;
    private Timer timer;

    @SuppressLint("DefaultLocale")
    @Override
    public void onStartScan() {
        beaconInfoHashMap.clear();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateDevices();
                runOnUiThread(() -> {
                    adapter.replaceData(beaconInfo);
                    mBind.tvDeviceNum.setText(String.format("DEVICE(%d)", beaconInfo.size()));
                });
            }
        }, 100, 600);
    }

    @Override
    public void onScanDevice(DeviceInfo deviceInfo) {
        AdvInfo beaconInfo = beaconInfoParseable.parseDeviceInfo(deviceInfo);
        if (beaconInfo == null) return;
        beaconInfoHashMap.put(beaconInfo.mac, beaconInfo);
    }

    @Override
    public void onStopScan() {
        mBind.ivRefresh.clearAnimation();
        animation = null;
        if (null != timer) timer.cancel();
    }

    private void updateDevices() {
        beaconInfo.clear();
        if (!TextUtils.isEmpty(filterName) || filterRssi != -127) {
            ArrayList<AdvInfo> beaconInfosFilter = new ArrayList<>(beaconInfoHashMap.values());
            Iterator<AdvInfo> iterator = beaconInfosFilter.iterator();
            while (iterator.hasNext()) {
                AdvInfo beaconInfo = iterator.next();
                if (beaconInfo.rssi > filterRssi) {
                    if (TextUtils.isEmpty(filterName)) {
                        continue;
                    } else {
                        if (TextUtils.isEmpty(beaconInfo.name) && TextUtils.isEmpty(beaconInfo.mac)) {
                            iterator.remove();
                        } else if (TextUtils.isEmpty(beaconInfo.name) && beaconInfo.mac.toLowerCase().replaceAll(":", "").contains(filterName.toLowerCase())) {
                            continue;
                        } else if (TextUtils.isEmpty(beaconInfo.mac) && beaconInfo.name.toLowerCase().contains(filterName.toLowerCase())) {
                            continue;
                        } else if (!TextUtils.isEmpty(beaconInfo.name) && !TextUtils.isEmpty(beaconInfo.mac) && (beaconInfo.name.toLowerCase().contains(filterName.toLowerCase()) || beaconInfo.mac.toLowerCase().replaceAll(":", "").contains(filterName.toLowerCase()))) {
                            continue;
                        } else {
                            iterator.remove();
                        }
                    }
                } else {
                    iterator.remove();
                }
            }
            beaconInfo.addAll(beaconInfosFilter);
        } else {
            beaconInfo.addAll(beaconInfoHashMap.values());
        }
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(beaconInfo, (lhs, rhs) -> {
            if (lhs.rssi > rhs.rssi) {
                return -1;
            } else if (lhs.rssi < rhs.rssi) {
                return 1;
            }
            return 0;
        });
    }

    public void onRefresh(View view) {
        if (isWindowLocked()) return;
        if (!MoKoSupport.getInstance().isBluetoothOpen()) {
            MoKoSupport.getInstance().enableBluetooth();
            return;
        }
        if (animation == null) {
            startScan();
        } else {
            mHandler.removeMessages(0);
            mokoBleScanner.stopScanDevice();
        }
    }

    public void onBack(View view) {
        back();
    }

    private void back() {
        if (animation != null) {
            mHandler.removeMessages(0);
            mokoBleScanner.stopScanDevice();
        }
        if (BuildConfig.IS_LIBRARY) {
            finish();
        } else {
            AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setMessage(R.string.main_exit_tips);
            dialog.setOnAlertConfirmListener(this::finish);
            dialog.show(getSupportFragmentManager());
        }
    }

    public void onAbout(View view) {
        if (isWindowLocked()) return;
        startActivity(new Intent(this, AboutActivity.class));
    }

    public void onFilter(View view) {
        if (isWindowLocked()) return;
        if (animation != null) {
            mHandler.removeMessages(0);
            mokoBleScanner.stopScanDevice();
        }
        ScanFilterDialog scanFilterDialog = new ScanFilterDialog(this);
        scanFilterDialog.setFilterName(filterName);
        scanFilterDialog.setFilterRssi(filterRssi);
        scanFilterDialog.setOnScanFilterListener((filterName, filterRssi) -> {
            LoRaLW009MainActivity.this.filterName = filterName;
            LoRaLW009MainActivity.this.filterRssi = filterRssi;
            if (!TextUtils.isEmpty(filterName) || filterRssi != -127) {
                mBind.rlFilter.setVisibility(View.VISIBLE);
                mBind.rlEditFilter.setVisibility(View.GONE);
                StringBuilder stringBuilder = new StringBuilder();
                if (!TextUtils.isEmpty(filterName)) {
                    stringBuilder.append(filterName);
                    stringBuilder.append(";");
                }
                if (filterRssi != -127) {
                    stringBuilder.append(String.format("%sdBm", filterRssi + ""));
                    stringBuilder.append(";");
                }
                mBind.tvFilter.setText(stringBuilder.toString());
            } else {
                mBind.rlFilter.setVisibility(View.GONE);
                mBind.rlEditFilter.setVisibility(View.VISIBLE);
            }
            if (animation == null) startScan();
        });
        scanFilterDialog.setOnDismissListener(dialog -> {
            if (animation == null) startScan();
        });
        scanFilterDialog.show();
    }

    public void onFilterDelete(View view) {
        if (isWindowLocked()) return;
        if (animation != null) {
            mHandler.removeMessages(0);
            mokoBleScanner.stopScanDevice();
        }
        mBind.rlFilter.setVisibility(View.GONE);
        mBind.rlEditFilter.setVisibility(View.VISIBLE);
        filterName = "";
        filterRssi = -127;
        if (animation == null) startScan();
    }

    private String mPassword;
    private String mSavedPassword;

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        if (!MoKoSupport.getInstance().isBluetoothOpen()) {
            MoKoSupport.getInstance().enableBluetooth();
            return;
        }
        AdvInfo advInfo = (AdvInfo) adapter.getItem(position);
        if (advInfo != null && advInfo.connectable && !isFinishing()) {
            if (animation != null) {
                mHandler.removeMessages(0);
                mokoBleScanner.stopScanDevice();
            }
            isVerifyEnable = advInfo.verifyEnable;
            if (!isVerifyEnable) {
                showLoadingProgressDialog();
                MoKoSupport.getInstance().connDevice(advInfo.mac);
                return;
            }
            // show password
            final PasswordDialog dialog = new PasswordDialog(this);
            dialog.setData(mSavedPassword);
            dialog.setOnPasswordClicked(new PasswordDialog.PasswordClickListener() {
                @Override
                public void onEnsureClicked(String password) {
                    if (!MoKoSupport.getInstance().isBluetoothOpen()) {
                        MoKoSupport.getInstance().enableBluetooth();
                        return;
                    }
                    XLog.i(password);
                    mPassword = password;
                    if (animation != null) {
                        mHandler.removeMessages(0);
                        mokoBleScanner.stopScanDevice();
                    }
                    showLoadingProgressDialog();
                    MoKoSupport.getInstance().connDevice(advInfo.mac);
                }

                @Override
                public void onDismiss() {

                }
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
    }

    @Override
    protected void onSystemBleTurnOff() {
        if (animation != null) {
            mHandler.removeMessages(0);
            mokoBleScanner.stopScanDevice();
            onStopScan();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        String action = event.getAction();
        if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
            mPassword = "";
            dismissLoadingProgressDialog();
            dismissLoadingMessageDialog();
            if (isPasswordError) {
                isPasswordError = false;
            } else {
                ToastUtils.showToast(this, "Connection Failed");
            }
            if (animation == null) startScan();
        }
        if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
            dismissLoadingProgressDialog();
            if (!isVerifyEnable) {
                XLog.i("Success");
                launcher.launch(new Intent(this, DeviceInfoActivity.class));
                return;
            }
            showLoadingMessageDialog();
            MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.setPassword(mPassword));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
        if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            dismissLoadingMessageDialog();
            MoKoSupport.getInstance().disConnectBle();
        }
        if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
            OrderTaskResponse response = event.getResponse();
            OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
            byte[] value = response.responseValue;
            if (orderCHAR == OrderCHAR.CHAR_PASSWORD) {
                dismissLoadingMessageDialog();
                if (value.length == 5) {
                    int header = value[0] & 0xFF;// 0xED
                    int flag = value[1] & 0xFF;// read or write
                    int cmd = value[2] & 0xFF;
                    if (header != 0xED) return;
                    int length = value[3] & 0xFF;
                    if (flag == 0x01 && cmd == 0x01 && length == 0x01) {
                        int result = value[4] & 0xFF;
                        if (1 == result) {
                            mSavedPassword = mPassword;
                            SPUtiles.setStringValue(this, AppConstants.SP_KEY_SAVED_PASSWORD, mSavedPassword);
                            XLog.i("Success");
                            launcher.launch(new Intent(this, DeviceInfoActivity.class));
                        } else if (0 == result) {
                            isPasswordError = true;
                            ToastUtils.showToast(this, "Password Error");
                            MoKoSupport.getInstance().disConnectBle();
                        }
                    }
                }
            }
        }
    }

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (null != result && result.getResultCode() == RESULT_OK) {
            if (animation == null) startScan();
        }
    });

    private LoadingDialog mLoadingDialog;

    private void showLoadingProgressDialog() {
        mLoadingDialog = new LoadingDialog();
        mLoadingDialog.show(getSupportFragmentManager());
    }

    private void dismissLoadingProgressDialog() {
        if (mLoadingDialog != null)
            mLoadingDialog.dismissAllowingStateLoss();
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    private void showLoadingMessageDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Verifying..");
        mLoadingMessageDialog.show(getSupportFragmentManager());
    }

    private void dismissLoadingMessageDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        XLog.i("onNewIntent...");
        setIntent(intent);
        if (getIntent().getExtras() != null) {
            String from = getIntent().getStringExtra(AppConstants.EXTRA_KEY_FROM_ACTIVITY);
            if (LogDataActivity.TAG.equals(from)) {
                if (animation == null) startScan();
            }
        }
    }
}
