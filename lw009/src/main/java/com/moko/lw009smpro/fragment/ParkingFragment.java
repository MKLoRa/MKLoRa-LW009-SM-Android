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

import com.moko.ble.lib.task.OrderTask;
import com.moko.lw009smpro.activity.setting.BleFixActivity;
import com.moko.lw009smpro.activity.DeviceInfoActivity;
import com.moko.lib.loraui.dialog.AlertMessageDialog;
import com.moko.lib.loraui.dialog.BottomDialog;
import com.moko.lw009smpro.databinding.Lw009FragmentParkingBinding;
import com.moko.lw009smpro.utils.ToastUtils;
import com.moko.support.lw009.MoKoSupport;
import com.moko.support.lw009.OrderTaskAssembler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParkingFragment extends Fragment {
    private static final String TAG = ParkingFragment.class.getSimpleName();
    private Lw009FragmentParkingBinding mBind;
    private DeviceInfoActivity activity;
    private final String[] parkingDetectionModeArray = {"Magnetic Sensor Only", "Radar Only", "Joint Detection(Magnetic&Radar)"};
    private int parkingDetectionModeSelect;
    private final String[] parkingDetectionSensitivityArray = {"1", "2", "3", "4", "5", "6", "7"};
    private int parkingDetectionSensitivitySelect;
    private final String[] switchArray = {"OFF", "ON"};
    private int beaconReportSwitchSelect;
    private int parkingDataReportSelect;

    public static ParkingFragment newInstance() {
        return new ParkingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = Lw009FragmentParkingBinding.inflate(inflater, container, false);
        activity = (DeviceInfoActivity) getActivity();
        setListener();
        return mBind.getRoot();
    }

    private void setListener() {
        mBind.tvDetectionAlogorithms.setOnClickListener(v -> {
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(new ArrayList<>(Arrays.asList(parkingDetectionModeArray)), parkingDetectionModeSelect);
            dialog.setListener(value -> {
                this.parkingDetectionModeSelect = value;
                mBind.tvDetectionAlogorithms.setText(parkingDetectionModeArray[value]);
            });
            dialog.show(getChildFragmentManager());
        });

        mBind.tvDetectionSensitivity.setOnClickListener(v -> {
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(new ArrayList<>(Arrays.asList(parkingDetectionSensitivityArray)), parkingDetectionSensitivitySelect - 1);
            dialog.setListener(value -> {
                this.parkingDetectionSensitivitySelect = value + 1;
                mBind.tvDetectionSensitivity.setText(parkingDetectionSensitivityArray[value]);
            });
            dialog.show(getChildFragmentManager());
        });

        mBind.tvCalibra.setOnClickListener(v -> {
            activity.showSyncingProgressDialog();
            MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.setNoParkingCalibration());
        });

        mBind.tvParkingReportSwitch.setOnClickListener(v -> {
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(new ArrayList<>(Arrays.asList(switchArray)), parkingDataReportSelect);
            dialog.setListener(value -> {
                this.parkingDataReportSelect = value;
                mBind.tvParkingReportSwitch.setText(switchArray[value]);
            });
            dialog.show(getChildFragmentManager());
        });

        mBind.tvBeaconReportSwitch.setOnClickListener(v -> {
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(new ArrayList<>(Arrays.asList(switchArray)), beaconReportSwitchSelect);
            dialog.setListener(value -> {
                this.beaconReportSwitchSelect = value;
                mBind.tvBeaconReportSwitch.setText(switchArray[value]);
            });
            dialog.show(getChildFragmentManager());
        });

        mBind.tvBleFilterSetting.setOnClickListener(v -> startActivity(new Intent(requireActivity(), BleFixActivity.class)));
    }

    public void setParkingDetectionMode(int parkingDetectionModeSelect) {
        this.parkingDetectionModeSelect = parkingDetectionModeSelect;
        mBind.tvDetectionAlogorithms.setText(parkingDetectionModeArray[parkingDetectionModeSelect]);
    }

    public void setParkingDetectionSensitivity(int parkingDetectionSensitivitySelect) {
        this.parkingDetectionSensitivitySelect = parkingDetectionSensitivitySelect;
        mBind.tvDetectionSensitivity.setText(parkingDetectionSensitivityArray[parkingDetectionSensitivitySelect - 1]);
    }

    public void setParkingDetectionDuration(int duration) {
        mBind.etParkingDetection.setText(String.valueOf(duration));
        mBind.etParkingDetection.setSelection(mBind.etParkingDetection.getText().length());
    }

    public void setParkingDetectionConfirmDuration(int duration) {
        mBind.etDetectionConfirmation.setText(String.valueOf(duration));
        mBind.etDetectionConfirmation.setSelection(mBind.etDetectionConfirmation.getText().length());
    }

    public void setReportSwitch(int parkingDataReportSelect, int beaconReportSwitchSelect) {
        this.parkingDataReportSelect = parkingDataReportSelect;
        this.beaconReportSwitchSelect = beaconReportSwitchSelect;
        mBind.tvParkingReportSwitch.setText(switchArray[parkingDataReportSelect]);
        mBind.tvBeaconReportSwitch.setText(switchArray[beaconReportSwitchSelect]);
    }

    public void setNoParkingCalibration(int result) {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setMessage(result == 1 ? "Calibration successful!" : "Calibration failed!");
        dialog.setConfirm("OK");
        if (result == 1) dialog.setCancelGone();
        if (result != 1) {
            dialog.setCancel("Try again");
            dialog.setOnAlertCancelListener(() -> mBind.tvCalibra.callOnClick());
        }
        dialog.show(getChildFragmentManager());
    }

    public void saveParkingParams() {
        if (isValid()) {
            activity.showSyncingProgressDialog();
            int duration = Integer.parseInt(mBind.etParkingDetection.getText().toString());
            int confirmDuration = Integer.parseInt(mBind.etDetectionConfirmation.getText().toString());
            List<OrderTask> orderTasks = new ArrayList<>(6);
            orderTasks.add(OrderTaskAssembler.setParkingDetectionMode(parkingDetectionModeSelect));
            orderTasks.add(OrderTaskAssembler.setParkingDetectionSensitivity(parkingDetectionSensitivitySelect));
            orderTasks.add(OrderTaskAssembler.setParkingDetectionDuration(duration));
            orderTasks.add(OrderTaskAssembler.setParkingDetectionConfirmDuration(confirmDuration));
            orderTasks.add(OrderTaskAssembler.setParkingDetectionPayloadType(parkingDataReportSelect, beaconReportSwitchSelect));
            MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        } else {
            ToastUtils.showToast(requireContext(), "Para error!");
        }
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(mBind.etParkingDetection.getText())) return false;
        int duration = Integer.parseInt(mBind.etParkingDetection.getText().toString());
        if (duration < 1 || duration > 60) return false;
        if (TextUtils.isEmpty(mBind.etDetectionConfirmation.getText())) return false;
        int confirmDuration = Integer.parseInt(mBind.etDetectionConfirmation.getText().toString());
        return confirmDuration >= 1 && confirmDuration <= 60;
    }
}
