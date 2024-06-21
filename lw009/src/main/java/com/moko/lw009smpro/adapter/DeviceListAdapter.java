package com.moko.lw009smpro.adapter;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw009smpro.R;
import com.moko.lw009smpro.entity.AdvInfo;

public class DeviceListAdapter extends BaseQuickAdapter<AdvInfo, BaseViewHolder> {
    public DeviceListAdapter() {
        super(R.layout.list_item_device);
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void convert(BaseViewHolder helper, AdvInfo item) {
        final String rssi = String.format("%ddBm", item.rssi);
        helper.setText(R.id.tv_rssi, rssi);
        final String name = TextUtils.isEmpty(item.name) ? "N/A" : item.name;
        helper.setText(R.id.tv_name, name);
        helper.setText(R.id.tv_mac, String.format("MAC:%s", item.mac));
        ImageView ivBattery = helper.getView(R.id.iv_battery);
        ivBattery.setImageResource(item.lowPower ? R.drawable.low_battery : R.drawable.full_battery);
        final String intervalTime = item.intervalTime == 0 ? "<->N/A" : String.format("<->%dms", item.intervalTime);
        helper.setText(R.id.tv_track_interval, intervalTime);
        helper.setText(R.id.tv_battery_percent, item.lowPower ? "Low" : "Full");
        helper.setText(R.id.tv_tx_power, String.format("Tx Power:%ddBm", item.txPower));
        helper.setVisible(R.id.tv_connect, item.connectable);
        helper.addOnClickListener(R.id.tv_connect);
    }
}
