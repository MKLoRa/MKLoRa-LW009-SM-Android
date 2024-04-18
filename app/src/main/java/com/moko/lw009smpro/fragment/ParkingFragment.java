package com.moko.lw009smpro.fragment;

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
import com.moko.lw009smpro.databinding.Lw006FragmentPosBinding;
import com.moko.support.lw009.MoKoSupport;
import com.moko.support.lw009.OrderTaskAssembler;

import java.util.ArrayList;

public class ParkingFragment extends Fragment {
    private static final String TAG = ParkingFragment.class.getSimpleName();
    private Lw006FragmentPosBinding mBind;
    private boolean mOfflineLocationEnable;
    private DeviceInfoActivity activity;

    public ParkingFragment() {
    }

    public static ParkingFragment newInstance() {
        return new ParkingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = Lw006FragmentPosBinding.inflate(inflater, container, false);
        activity = (DeviceInfoActivity) getActivity();
        return mBind.getRoot();
    }

    public void setOfflineLocationEnable(int enable) {
        mOfflineLocationEnable = enable == 1;
        mBind.ivOfflineFix.setImageResource(mOfflineLocationEnable ? R.drawable.lw006_ic_checked : R.drawable.lw006_ic_unchecked);
    }

    public void changeOfflineFix() {
        mOfflineLocationEnable = !mOfflineLocationEnable;
        activity.showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setOfflineLocationEnable(mOfflineLocationEnable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getOfflineLocationEnable());
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }
}
