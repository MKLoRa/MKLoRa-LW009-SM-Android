package com.moko.lw009smpro.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.lw009smpro.R;
import com.moko.support.lw009.entity.ExportData;

public class ExportDataListAdapter extends BaseQuickAdapter<ExportData, BaseViewHolder> {
    public ExportDataListAdapter() {
        super(R.layout.lw006_item_export_data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ExportData item) {
        helper.setText(R.id.tv_time, item.time);
        helper.setText(R.id.tv_raw_data, item.rawData);

    }
}
