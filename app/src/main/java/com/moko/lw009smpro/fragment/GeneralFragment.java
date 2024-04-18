package com.moko.lw009smpro.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.lw009smpro.databinding.Lw006FragmentGeneralBinding;
import com.moko.support.lw009.MoKoSupport;
import com.moko.support.lw009.OrderTaskAssembler;

public class GeneralFragment extends Fragment {
    private static final String TAG = GeneralFragment.class.getSimpleName();
    private Lw006FragmentGeneralBinding mBind;

    public GeneralFragment() {
    }


    public static GeneralFragment newInstance() {
        return new GeneralFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = Lw006FragmentGeneralBinding.inflate(inflater, container, false);
        return mBind.getRoot();
    }

    public void setHeartbeatInterval(int interval) {
        mBind.etHeartbeatInterval.setText(String.valueOf(interval));
        mBind.etHeartbeatInterval.setSelection(mBind.etHeartbeatInterval.getText().length());
    }

    public boolean isValid() {
        if (TextUtils.isEmpty(mBind.etHeartbeatInterval.getText())) return false;
        final String intervalStr = mBind.etHeartbeatInterval.getText().toString();
        final int interval = Integer.parseInt(intervalStr);
        return interval >= 1 && interval <= 14400;
    }

    public void saveParams() {
        final String intervalStr = mBind.etHeartbeatInterval.getText().toString();
        final int interval = Integer.parseInt(intervalStr);
        MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.setHeartBeatInterval(interval));
    }
}
