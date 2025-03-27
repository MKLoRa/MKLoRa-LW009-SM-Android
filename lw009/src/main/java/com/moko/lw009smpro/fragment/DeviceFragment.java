package com.moko.lw009smpro.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.ble.lib.task.OrderTask;
import com.moko.lw009smpro.R;
import com.moko.lw009smpro.activity.DeviceInfoActivity;
import com.moko.lw009smpro.activity.device.OnOffSettingsActivity;
import com.moko.lw009smpro.activity.device.SystemInfoActivity;
import com.moko.lw009smpro.databinding.Lw009FragmentDeviceBinding;
import com.moko.lib.loraui.dialog.AlertMessageDialog;
import com.moko.lib.loraui.dialog.BottomDialog;
import com.moko.support.lw009.MoKoSupport;
import com.moko.support.lw009.OrderTaskAssembler;

import java.util.ArrayList;

public class DeviceFragment extends Fragment {
    private static final String TAG = DeviceFragment.class.getSimpleName();
    private Lw009FragmentDeviceBinding mBind;
    private final ArrayList<String> mTimeZones = new ArrayList<>();
    private int mSelectedTimeZone;
    private boolean mLowPowerPayloadEnable;
    private DeviceInfoActivity activity;

    public DeviceFragment() {
    }

    public static DeviceFragment newInstance() {
        return new DeviceFragment();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = Lw009FragmentDeviceBinding.inflate(inflater, container, false);
        activity = (DeviceInfoActivity) getActivity();
        for (int i = -24; i <= 28; i++) {
            if (i < 0) {
                if (i % 2 == 0) {
                    mTimeZones.add(String.format("UTC%d", i / 2));
                } else {
                    mTimeZones.add(i < -1 ? String.format("UTC%d:30", (i + 1) / 2) : "UTC-0:30");
                }
            } else if (i == 0) {
                mTimeZones.add("UTC");
            } else {
                if (i % 2 == 0) {
                    mTimeZones.add(String.format("UTC+%d", i / 2));
                } else {
                    mTimeZones.add(String.format("UTC+%d:30", (i - 1) / 2));
                }
            }
        }
        setListener();
        return mBind.getRoot();
    }

    private void setListener() {
        mBind.tvTimeZone.setOnClickListener(v -> onTimezoneSelect());
        mBind.ivLowPowerPayload.setOnClickListener(v -> {
            mLowPowerPayloadEnable = !mLowPowerPayloadEnable;
            activity.showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>(2);
            orderTasks.add(OrderTaskAssembler.setLowPowerReportEnable(mLowPowerPayloadEnable ? 1 : 0));
            orderTasks.add(OrderTaskAssembler.getLowPowerPayloadEnable());
            MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        mBind.tvOnOffSet.setOnClickListener(v -> {
            if (activity.isWindowLocked()) return;
            startActivity(new Intent(activity, OnOffSettingsActivity.class));
        });
        mBind.tvFactoryReset.setOnClickListener(v -> onFactoryReset());
        mBind.tvDeviceInfo.setOnClickListener(v -> {
            if (activity.isWindowLocked()) return;
            startActivity(new Intent(activity, SystemInfoActivity.class));
        });
    }

    private void onTimezoneSelect() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mTimeZones, mSelectedTimeZone);
        dialog.setListener(value -> {
            mSelectedTimeZone = value;
            mBind.tvTimeZone.setText(mTimeZones.get(value));
            activity.showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>(2);
            orderTasks.add(OrderTaskAssembler.setTimeZone(value - 24));
            orderTasks.add(OrderTaskAssembler.getTimeZone());
            MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(activity.getSupportFragmentManager());
    }

    private void onFactoryReset() {
        if (activity.isWindowLocked()) return;
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Factory Reset!");
        dialog.setMessage("After factory reset,all the data will be reseted to the factory values.");
        dialog.setConfirm("OK");
        dialog.setOnAlertConfirmListener(() -> {
            activity.showSyncingProgressDialog();
            MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.restore());
        });
        dialog.show(getChildFragmentManager());
    }

    public void setTimeZone(int timeZone) {
        mSelectedTimeZone = timeZone + 24;
        mBind.tvTimeZone.setText(mTimeZones.get(mSelectedTimeZone));
    }

    public void setLowPowerPayload(int enable) {
        mLowPowerPayloadEnable = enable == 1;
        mBind.ivLowPowerPayload.setImageResource(mLowPowerPayloadEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
    }
}
