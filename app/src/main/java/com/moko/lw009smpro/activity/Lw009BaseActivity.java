package com.moko.lw009smpro.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.moko.lw009smpro.dialog.LoadingMessageDialog;

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
}
