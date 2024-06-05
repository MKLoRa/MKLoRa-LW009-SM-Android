package com.moko.lw009smpro.activity.filter;

import static com.moko.lw009smpro.AppConstants.SAVE_ERROR;
import static com.moko.lw009smpro.AppConstants.SAVE_SUCCESS;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw009smpro.R;
import com.moko.lw009smpro.activity.Lw009BaseActivity;
import com.moko.lw009smpro.databinding.ActivityFilterBxpTagIdBinding;
import com.moko.lw009smpro.utils.ToastUtils;
import com.moko.support.lw009.MoKoSupport;
import com.moko.support.lw009.OrderTaskAssembler;
import com.moko.support.lw009.entity.OrderCHAR;
import com.moko.support.lw009.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterBXPTagIdActivity extends Lw009BaseActivity {
    private ActivityFilterBxpTagIdBinding mBind;
    private boolean savedParamsError;
    private final List<String> filterTagId = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityFilterBxpTagIdBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.getFilterBXPTagEnable());
        orderTasks.add(OrderTaskAssembler.getFilterBXPTagPrecise());
        orderTasks.add(OrderTaskAssembler.getFilterBXPTagReverse());
        orderTasks.add(OrderTaskAssembler.getFilterBXPTagRules());
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @SuppressLint("DefaultLocale")
    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
        if (!MokoConstants.ACTION_CURRENT_DATA.equals(action))
            EventBus.getDefault().cancelEventDelivery(event);
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (header != 0xED || configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_FILTER_BXP_TAG_ENABLE:
                                case KEY_FILTER_BXP_TAG_PRECISE:
                                case KEY_FILTER_BXP_TAG_REVERSE:
                                    if (result != 1) savedParamsError = true;
                                    break;
                                case KEY_FILTER_BXP_TAG_RULES:
                                    if (result != 1) savedParamsError = true;
                                    ToastUtils.showToast(this, savedParamsError ? SAVE_ERROR : SAVE_SUCCESS);
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_FILTER_BXP_TAG_ENABLE:
                                    if (length > 0) {
                                        mBind.cbEnable.setChecked((value[4] & 0xFF) == 1);
                                    }
                                    break;
                                case KEY_FILTER_BXP_TAG_PRECISE:
                                    if (length > 0) {
                                        mBind.cbPreciseMatch.setChecked((value[4] & 0xFF) == 1);
                                    }
                                    break;
                                case KEY_FILTER_BXP_TAG_REVERSE:
                                    if (length > 0) {
                                        mBind.cbReverseFilter.setChecked((value[4] & 0xFF) == 1);
                                    }
                                    break;
                                case KEY_FILTER_BXP_TAG_RULES:
                                    if (length > 0) {
                                        filterTagId.clear();
                                        byte[] tagIdBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        for (int i = 0, l = tagIdBytes.length; i < l; ) {
                                            int idLength = tagIdBytes[i] & 0xFF;
                                            i++;
                                            filterTagId.add(MokoUtils.bytesToHexString(Arrays.copyOfRange(tagIdBytes, i, i + idLength)));
                                            i += idLength;
                                        }
                                        for (int i = 0, l = filterTagId.size(); i < l; i++) {
                                            String macAddress = filterTagId.get(i);
                                            View v = LayoutInflater.from(this).inflate(R.layout.item_tag_id_filter, mBind.llTagId, false);
                                            TextView title = v.findViewById(R.id.tv_tag_id_title);
                                            EditText etMacAddress = v.findViewById(R.id.et_tag_id);
                                            title.setText(String.format("Tag ID %d", i + 1));
                                            etMacAddress.setText(macAddress);
                                            etMacAddress.setSelection(etMacAddress.getText().length());
                                            mBind.llTagId.addView(v);
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        if (isValid()) {
            showSyncingProgressDialog();
            saveParams();
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    @SuppressLint("DefaultLocale")
    public void onAdd(View view) {
        if (isWindowLocked()) return;
        int count = mBind.llTagId.getChildCount();
        if (count > 9) {
            ToastUtils.showToast(this, "You can set up to 10 filters!");
            return;
        }
        View v = LayoutInflater.from(this).inflate(R.layout.item_tag_id_filter, mBind.llTagId, false);
        TextView title = v.findViewById(R.id.tv_tag_id_title);
        title.setText(String.format("Tag ID %d", count + 1));
        mBind.llTagId.addView(v);
    }

    public void onDel(View view) {
        if (isWindowLocked()) return;
        final int c = mBind.llTagId.getChildCount();
        if (c == 0) {
            ToastUtils.showToast(this, "There are currently no filters to delete");
            return;
        }
        int count = mBind.llTagId.getChildCount();
        if (count > 0) {
            mBind.llTagId.removeViewAt(count - 1);
        }
    }

    private void saveParams() {
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.setFilterBXPTagEnable(mBind.cbEnable.isChecked() ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.setFilterBXPTagPrecise(mBind.cbPreciseMatch.isChecked() ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.setFilterBXPTagReverse(mBind.cbReverseFilter.isChecked() ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.setFilterBXPTagRules(filterTagId));
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private boolean isValid() {
        final int c = mBind.llTagId.getChildCount();
        filterTagId.clear();
        if (c > 0) {
            for (int i = 0; i < c; i++) {
                View v = mBind.llTagId.getChildAt(i);
                EditText etTagId = v.findViewById(R.id.et_tag_id);
                final String tagId = etTagId.getText().toString();
                if (TextUtils.isEmpty(tagId)) return false;
                int length = tagId.length();
                if (length % 2 != 0 || length > 12) return false;
                filterTagId.add(tagId);
            }
        }
        return true;
    }

    public void onBack(View view) {
        backHome();
    }

    @Override
    public void onBackPressed() {
        backHome();
    }

    private void backHome() {
        EventBus.getDefault().unregister(this);
        setResult(RESULT_OK);
        finish();
    }
}
