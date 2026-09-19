package com.moko.lw009smpro.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.moko.lib.loraui.dialog.LoadingDialog;
import com.moko.lib.loraui.dialog.LoadingMessageDialog;

import org.greenrobot.eventbus.EventBus;

public class Lw009BaseActivity extends FragmentActivity {
    private boolean mReceiverTag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (registerEvent()) {
            EventBus.getDefault().register(this);
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mReceiver, filter);
            mReceiverTag = true;
        }
        // targetSdk 35+ / Android 16：预测性返回不会再走 Activity.onBackPressed()，
        // 在此统一接管系统返回，并继续回调子类已有的 onBackPressed() 实现。
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Lw009BaseActivity.this.onBackPressed();
            }
        });
        if (savedInstanceState != null) {
            Intent intent = new Intent(this, GuideActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }
        // 设置全屏
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        // 透明导航栏
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        // Android P及以上支持刘海屏
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        getWindow().setAttributes(params);

        // 设置WindowInsets监听
        getWindow().getDecorView().setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            private int lastOrientation = -1;

            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                DisplayCutout cutout = insets.getDisplayCutout();
                if (cutout != null) {
                    // 获取当前方向
                    int currentOrientation = getResources().getConfiguration().orientation;

                    // 只有当方向改变时才重新设置padding
                    if (currentOrientation != lastOrientation) {
                        lastOrientation = currentOrientation;

                        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                            // 横屏：只考虑左右安全区域
                            v.setPadding(cutout.getSafeInsetLeft(), 0,
                                    cutout.getSafeInsetRight(), 0);
                        } else {
                            int bottomInset = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                            }
                            // 竖屏：使用全部安全区域
                            v.setPadding(cutout.getSafeInsetLeft(), cutout.getSafeInsetTop(),
                                    cutout.getSafeInsetRight(), cutout.getSafeInsetBottom() + bottomInset);
                        }

                        // 请求重新布局
                        v.requestLayout();
                    }
                } else {
                    int bottomInset = 0;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                    }
                    // 没有刘海屏时重置padding
                    v.setPadding(0, 0, 0, bottomInset);
                    lastOrientation = -1;
                }

                return insets;
            }
        });
    }

    /**
     * 覆盖 ComponentActivity 默认实现（会再次进 OnBackPressedDispatcher，可能死循环）。
     * 未重写的页面默认 finish；已重写 onBackPressed 的子类仍走各自逻辑。
     */
    @Override
    public void onBackPressed() {
        finish();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    if (blueState == BluetoothAdapter.STATE_TURNING_OFF) {
                        onSystemBleTurnOff();
                    }
                }
            }
        }
    };

    protected boolean registerEvent() {
        return true;
    }

    protected void onSystemBleTurnOff() {
        dismissSyncProgressDialog();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }


    // 记录上次页面控件点击时间,屏蔽无效点击事件
    protected long mLastOnClickTime = 0;

    public boolean isWindowLocked() {
        long current = SystemClock.elapsedRealtime();
        if (current - mLastOnClickTime > voidDuration) {
            mLastOnClickTime = current;
            return false;
        } else {
            return true;
        }
    }

    public int voidDuration = 500;

    public boolean isWriteStoragePermissionOpen() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isLocationPermissionOpen() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    public void showSyncingProgressDialog() {
        if (null != mLoadingMessageDialog && mLoadingMessageDialog.isAdded() && !mLoadingMessageDialog.isDetached()) {
            mLoadingMessageDialog.dismissAllowingStateLoss();
        }
        mLoadingMessageDialog = null;
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        mLoadingMessageDialog.show(getSupportFragmentManager());
    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null && mLoadingMessageDialog.isAdded() && !mLoadingMessageDialog.isDetached())
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }



    private LoadingDialog mLoadingDialog;

    protected void showLoadingProgressDialog() {
        if (null != mLoadingDialog && mLoadingDialog.isAdded() && !mLoadingDialog.isDetached()) {
            mLoadingDialog.dismissAllowingStateLoss();
        }
        mLoadingDialog = null;
        mLoadingDialog = new LoadingDialog();
        if (!mLoadingDialog.isAdded())
            mLoadingDialog.show(getSupportFragmentManager());
    }

    protected void dismissLoadingProgressDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isAdded() && !mLoadingDialog.isDetached()) {
            mLoadingDialog.dismissAllowingStateLoss();
            mLoadingDialog = null;
        }
    }
}
