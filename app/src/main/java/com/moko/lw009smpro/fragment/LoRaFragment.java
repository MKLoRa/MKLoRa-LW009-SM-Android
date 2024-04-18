package com.moko.lw009smpro.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.lw009smpro.databinding.FragmentLoraBinding;

public class LoRaFragment extends Fragment {
    private static final String TAG = LoRaFragment.class.getSimpleName();
    private FragmentLoraBinding mBind;
    public LoRaFragment() {
    }

    public static LoRaFragment newInstance() {
        return new LoRaFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentLoraBinding.inflate(inflater, container, false);
        return mBind.getRoot();
    }

    public void setLoRaInfo(String loraInfo) {
        mBind.tvLoraInfo.setText(loraInfo);
    }

    public void setLoraStatus(int networkCheck) {
        String networkCheckDisPlay = "";
        switch (networkCheck) {
            case 0:
                networkCheckDisPlay = "Connecting";
                break;
            case 1:
                networkCheckDisPlay = "Connected";
                break;
        }
        mBind.tvLoraStatus.setText(networkCheckDisPlay);
    }
}
