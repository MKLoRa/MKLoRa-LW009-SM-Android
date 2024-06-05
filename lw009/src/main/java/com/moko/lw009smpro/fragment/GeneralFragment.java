package com.moko.lw009smpro.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.lw009smpro.activity.DeviceInfoActivity;
import com.moko.lw009smpro.activity.setting.BleSettingsActivity;
import com.moko.lw009smpro.databinding.FragmentGeneralBinding;
import com.moko.lw009smpro.utils.ToastUtils;
import com.moko.support.lw009.MoKoSupport;
import com.moko.support.lw009.OrderTaskAssembler;

public class GeneralFragment extends Fragment {
    private static final String TAG = GeneralFragment.class.getSimpleName();
    private FragmentGeneralBinding mBind;
    private DeviceInfoActivity activity;

    public GeneralFragment() {
    }

    public static GeneralFragment newInstance() {
        return new GeneralFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentGeneralBinding.inflate(inflater, container, false);
        activity = (DeviceInfoActivity) getActivity();
        mBind.tvBleSettings.setOnClickListener(v -> startActivity(new Intent(activity, BleSettingsActivity.class)));
        return mBind.getRoot();
    }

    public void setHeartbeatInterval(int interval) {
        mBind.etHeartbeatInterval.setText(String.valueOf(interval));
        mBind.etHeartbeatInterval.setSelection(mBind.etHeartbeatInterval.getText().length());
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(mBind.etHeartbeatInterval.getText())) return false;
        final String intervalStr = mBind.etHeartbeatInterval.getText().toString();
        final int interval = Integer.parseInt(intervalStr);
        return interval >= 1 && interval <= 14400;
    }

    public void saveParams() {
        if (isValid()) {
            activity.showSyncingProgressDialog();
            final String intervalStr = mBind.etHeartbeatInterval.getText().toString();
            final int interval = Integer.parseInt(intervalStr);
            MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.setHeartBeatInterval(interval));
        } else {
            ToastUtils.showToast(requireContext(), "Para error!");
        }
    }
}
