package com.moko.lw009smpro.activity.lora;

import static com.moko.lw009smpro.AppConstants.SAVE_ERROR;
import static com.moko.lw009smpro.AppConstants.SAVE_SUCCESS;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw009smpro.AppConstants;
import com.moko.lw009smpro.R;
import com.moko.lw009smpro.activity.Lw009BaseActivity;
import com.moko.lw009smpro.databinding.ActivityConnSettingBinding;
import com.moko.lw009smpro.dialog.BottomDialog;
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

public class LoRaConnSettingActivity extends Lw009BaseActivity implements CompoundButton.OnCheckedChangeListener {
    private ActivityConnSettingBinding mBind;
    private final String[] mModeList = {"ABP", "OTAA"};
    private final String[] mRegionsList = {"AS923", "AU915", "EU868", "KR920", "IN865", "US915", "RU864", "AS923-1", "AS923-2", "AS923-3", "AS923-4"};
    private final String[] mMaxRetransmissionTimesList = {"1", "2"};
    private int mSelectedMode;
    private int mSelectedRegion;
    private int mSelectedRegionIndex;
    private int mSelectedCh1;
    private int mSelectedCh2;
    private int mSelectedDr;
    private int mSelectedDr1;
    private int mSelectedDr2;
    private int mMaxCH;
    private int mMaxDR;
    private boolean savedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityConnSettingBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        mBind.cbAdvanceSetting.setOnCheckedChangeListener(this);
        mBind.cbAdr.setOnCheckedChangeListener(this);
        if (!MoKoSupport.getInstance().isBluetoothOpen()) {
            MoKoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getLoraUploadMode());
            orderTasks.add(OrderTaskAssembler.getLoraDevEUI());
            orderTasks.add(OrderTaskAssembler.getLoraAppEUI());
            orderTasks.add(OrderTaskAssembler.getLoraAppKey());
            orderTasks.add(OrderTaskAssembler.getLoraDevAddr());
            orderTasks.add(OrderTaskAssembler.getLoraAppSKey());
            orderTasks.add(OrderTaskAssembler.getLoraNwkSKey());
            orderTasks.add(OrderTaskAssembler.getLoraRegion());
            orderTasks.add(OrderTaskAssembler.getLoraCH());
            orderTasks.add(OrderTaskAssembler.getLoraDutyCycleEnable());
            orderTasks.add(OrderTaskAssembler.getLoraDR());
            orderTasks.add(OrderTaskAssembler.getLoraUplinkStrategy());
            orderTasks.add(OrderTaskAssembler.getLoraAdrAckLimit());
            orderTasks.add(OrderTaskAssembler.getLoraAdrAckDelay());
            MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
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
                        if (header != 0xED) return;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_LORA_MODE:
                                case KEY_LORA_DEV_EUI:
                                case KEY_LORA_APP_EUI:
                                case KEY_LORA_APP_KEY:
                                case KEY_LORA_DEV_ADDR:
                                case KEY_LORA_APP_SKEY:
                                case KEY_LORA_NWK_SKEY:
                                case KEY_LORA_REGION:
                                case KEY_LORA_CH:
                                case KEY_LORA_DR:
                                case KEY_LORA_DUTY_CYCLE:
                                case KEY_LORA_ADR_ACK_LIMIT:
                                case KEY_LORA_ADR_ACK_DELAY:
                                    if (result != 1) savedParamsError = true;
                                    break;
                                case KEY_LORA_UPLINK_STRATEGY:
                                    if (result != 1) savedParamsError = true;
                                    if (savedParamsError) {
                                        ToastUtils.showToast(this, SAVE_ERROR);
                                    } else {
                                        showSyncingProgressDialog();
                                        MoKoSupport.getInstance().sendOrder(OrderTaskAssembler.restart());
                                    }
                                    break;

                                case KEY_REBOOT:
                                    if (result != 1) savedParamsError = true;
                                    ToastUtils.showToast(this, savedParamsError ? SAVE_ERROR : SAVE_SUCCESS);
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_LORA_MODE:
                                    if (length > 0) {
                                        final int mode = value[4];
                                        mBind.tvUploadMode.setText(mModeList[mode - 1]);
                                        mSelectedMode = mode - 1;
                                        if (mode == 1) {
                                            mBind.llModemAbp.setVisibility(View.VISIBLE);
                                            mBind.llModemOtaa.setVisibility(View.GONE);
                                        } else {
                                            mBind.llModemAbp.setVisibility(View.GONE);
                                            mBind.llModemOtaa.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    break;
                                case KEY_LORA_DEV_EUI:
                                    if (length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        mBind.etDevEui.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        mBind.etDevEui.setSelection(mBind.etDevEui.getText().length());
                                    }
                                    break;
                                case KEY_LORA_APP_EUI:
                                    if (length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        mBind.etAppEui.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        mBind.etAppEui.setSelection(mBind.etAppEui.getText().length());
                                    }
                                    break;
                                case KEY_LORA_APP_KEY:
                                    if (length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        mBind.etAppKey.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        mBind.etAppKey.setSelection(mBind.etAppKey.getText().length());
                                    }
                                    break;
                                case KEY_LORA_DEV_ADDR:
                                    if (length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        mBind.etDevAddr.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        mBind.etDevAddr.setSelection(mBind.etDevAddr.getText().length());
                                    }
                                    break;
                                case KEY_LORA_APP_SKEY:
                                    if (length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        mBind.etAppSkey.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                    }
                                    break;
                                case KEY_LORA_NWK_SKEY:
                                    if (length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        mBind.etNwkSkey.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        mBind.etNwkSkey.setSelection(mBind.etNwkSkey.getText().length());
                                    }
                                    break;
                                case KEY_LORA_REGION:
                                    if (length > 0) {
                                        final int region = value[4] & 0xFF;
                                        mSelectedRegion = region;
                                        mSelectedRegionIndex = region > 1 ? region - 3 : region;
                                        mBind.tvRegion.setText(mRegionsList[mSelectedRegionIndex]);
                                        initCHDRRange();
                                        initDutyCycle();
                                    }
                                    break;

                                case KEY_LORA_CH:
                                    if (length > 1) {
                                        final int ch1 = value[4] & 0xFF;
                                        final int ch2 = value[5] & 0xFF;
                                        mSelectedCh1 = ch1;
                                        mSelectedCh2 = ch2;
                                        mBind.tvCh1.setText(String.valueOf(ch1));
                                        mBind.tvCh2.setText(String.valueOf(ch2));
                                    }
                                    break;
                                case KEY_LORA_DUTY_CYCLE:
                                    if (length > 0) {
                                        final int dutyCycleEnable = value[4] & 0xFF;
                                        mBind.cbDutyCycle.setChecked(dutyCycleEnable == 1);
                                    }
                                    break;
                                case KEY_LORA_DR:
                                    if (length > 0) {
                                        final int dr = value[4] & 0xFF;
                                        mSelectedDr = dr;
                                        mBind.tvDr.setText(String.valueOf(dr));
                                    }
                                    break;
                                case KEY_LORA_ADR_ACK_LIMIT:
                                    if (length > 0) {
                                        mBind.etAdrAckLimit.setText(String.valueOf(value[4] & 0xFF));
                                        mBind.etAdrAckLimit.setSelection(mBind.etAdrAckLimit.getText().length());
                                    }
                                    break;
                                case KEY_LORA_ADR_ACK_DELAY:
                                    if (length > 0) {
                                        mBind.etAdrAckDelay.setText(String.valueOf(value[4] & 0xFF));
                                        mBind.etAdrAckDelay.setSelection(mBind.etAdrAckDelay.getText().length());
                                    }
                                    break;
                                case KEY_LORA_UPLINK_STRATEGY:
                                    if (length == 4) {
                                        final int adr = value[4] & 0xFF;
                                        mBind.cbAdr.setChecked(adr == 1);
                                        mBind.llAdrOptions.setVisibility(mBind.cbAdr.isChecked() ? View.GONE : View.VISIBLE);
                                        mBind.layoutMaxTimes.setVisibility(mBind.cbAdr.isChecked() ? View.GONE : View.VISIBLE);
                                        final int times = value[5] & 0xFF;
                                        mBind.tvMaxRetransmissionTimes.setText(String.valueOf(times));
                                        final int dr1 = value[6] & 0xFF;
                                        mSelectedDr1 = dr1;
                                        final int dr2 = value[7] & 0xFF;
                                        mSelectedDr2 = dr2;
                                        mBind.tvDr1.setText(String.valueOf(dr1));
                                        mBind.tvDr2.setText(String.valueOf(dr2));
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
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

    public void selectMode(View view) {
        if (isWindowLocked()) return;
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(new ArrayList<>(Arrays.asList(mModeList)), mSelectedMode);
        bottomDialog.setListener(value -> {
            mBind.tvUploadMode.setText(mModeList[value]);
            mSelectedMode = value;
            if (value == 0) {
                mBind.llModemAbp.setVisibility(View.VISIBLE);
                mBind.llModemOtaa.setVisibility(View.GONE);
            } else {
                mBind.llModemAbp.setVisibility(View.GONE);
                mBind.llModemOtaa.setVisibility(View.VISIBLE);
            }

        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectRegion(View view) {
        if (isWindowLocked()) return;
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(new ArrayList<>(Arrays.asList(mRegionsList)), mSelectedRegionIndex);
        bottomDialog.setListener(value -> {
            int selectValue = value > 1 ? value + 3 : value;
            if (mSelectedRegion != selectValue) {
                mBind.cbAdr.setChecked(true);
                mSelectedRegionIndex = value;
                mSelectedRegion = selectValue;
                mBind.tvRegion.setText(mRegionsList[value]);
                initCHDRRange();
                updateCHDR();
                initDutyCycle();
            }
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    private void updateCHDR() {
        switch (mSelectedRegion) {
            case 1:
            case 8:
                // AU915、US915
                mSelectedCh1 = 8;
                mSelectedCh2 = 15;
                mSelectedDr = 0;
                break;
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                // CN779、EU443、EU868、KR920、IN865
                mSelectedCh1 = 0;
                mSelectedCh2 = 2;
                mSelectedDr = 0;
                break;
            case 2:
                // CN470
                mSelectedCh1 = 0;
                mSelectedCh2 = 7;
                mSelectedDr = 0;
                break;
            case 0:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
                // AS923、RU864
                mSelectedCh1 = 0;
                mSelectedCh2 = 1;
                mSelectedDr = 0;
                break;
        }
        if (mSelectedRegion == 0 || mSelectedRegion == 1 || mSelectedRegion == 10 ||
                mSelectedRegion == 11 || mSelectedRegion == 12 || mSelectedRegion == 13) {
            mSelectedDr1 = 2;
            mSelectedDr2 = 2;
        } else {
            mSelectedDr1 = 0;
            mSelectedDr2 = 0;
        }
        mBind.tvCh1.setText(String.valueOf(mSelectedCh1));
        mBind.tvCh2.setText(String.valueOf(mSelectedCh2));
        mBind.tvDr.setText(String.valueOf(mSelectedDr));
        mBind.tvDr1.setText(String.valueOf(mSelectedDr1));
        mBind.tvDr2.setText(String.valueOf(mSelectedDr2));
    }

    private ArrayList<String> mCHList;
    private ArrayList<String> mDRList;

    private void initCHDRRange() {
        mCHList = new ArrayList<>();
        mDRList = new ArrayList<>();
        switch (mSelectedRegion) {
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                // CN779、EU443、EU868、KR920、IN865
                mMaxCH = 2;
                mMaxDR = 5;
                break;
            case 1:
                // AU915
                mMaxCH = 63;
                mMaxDR = 6;
                break;
            case 2:
                // CN470
                mMaxCH = 95;
                mMaxDR = 5;
                break;
            case 8:
                // US915
                mMaxCH = 63;
                mMaxDR = 4;
                break;
            case 0:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
                // AS923、RU864
                mMaxCH = 1;
                mMaxDR = 5;
                break;
        }
        for (int i = 0; i <= mMaxCH; i++) {
            mCHList.add(String.valueOf(i));
        }
        int minDR = 0;
        if (mSelectedRegion == 0 || mSelectedRegion == 1 || mSelectedRegion == 10 ||
                mSelectedRegion == 11 || mSelectedRegion == 12 || mSelectedRegion == 13) {
            // AS923,AU915
            minDR = 2;
        }
        for (int i = minDR; i <= mMaxDR; i++) {
            mDRList.add(String.valueOf(i));
        }
        if (mSelectedRegion == 1 || mSelectedRegion == 2 || mSelectedRegion == 8) {
            // US915,AU915,CN470
            mBind.rlCh.setVisibility(View.VISIBLE);
        } else {
            mBind.rlCh.setVisibility(View.GONE);
        }
        if (mSelectedRegion == 0 || mSelectedRegion == 1 || mSelectedRegion == 8 ||
                mSelectedRegion == 10 || mSelectedRegion == 11 || mSelectedRegion == 12 || mSelectedRegion == 13) {
            // AS923,US915,AU915
            mBind.rlDr.setVisibility(View.GONE);
        } else {
            mBind.rlDr.setVisibility(View.VISIBLE);
        }
    }

    private void initDutyCycle() {
        if (mSelectedRegion == 3 || mSelectedRegion == 4
                || mSelectedRegion == 5 || mSelectedRegion == 9) {
            mBind.cbDutyCycle.setChecked(false);
            // CN779,EU433,EU868 and RU864
            mBind.llDutyCycle.setVisibility(View.VISIBLE);
        } else {
            mBind.llDutyCycle.setVisibility(View.GONE);
        }
    }


    public void selectCh1(View view) {
        if (isWindowLocked()) return;
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(mCHList, mSelectedCh1);
        bottomDialog.setListener(value -> {
            mSelectedCh1 = value;
            mBind.tvCh1.setText(mCHList.get(value));
            if (mSelectedCh1 > mSelectedCh2) {
                mSelectedCh2 = mSelectedCh1;
                mBind.tvCh2.setText(mCHList.get(value));
            }
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectCh2(View view) {
        if (isWindowLocked()) return;
        final ArrayList<String> ch2List = new ArrayList<>();
        for (int i = mSelectedCh1; i <= mMaxCH; i++) {
            ch2List.add(i + "");
        }
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(ch2List, mSelectedCh2 - mSelectedCh1);
        bottomDialog.setListener(value -> {
            mSelectedCh2 = value + mSelectedCh1;
            mBind.tvCh2.setText(ch2List.get(value));
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectDr1(View view) {
        if (isWindowLocked()) return;
        BottomDialog bottomDialog = new BottomDialog();
        if (mSelectedRegion == 0 || mSelectedRegion == 1 || mSelectedRegion == 10 ||
                mSelectedRegion == 11 || mSelectedRegion == 12 || mSelectedRegion == 13) {
            bottomDialog.setDatas(mDRList, mSelectedDr1 - 2);
            bottomDialog.setListener(value -> {
                mSelectedDr1 = value + 2;
                mBind.tvDr1.setText(mDRList.get(value));
                if (mSelectedDr1 > mSelectedDr2) {
                    mSelectedDr2 = mSelectedDr1;
                    mBind.tvDr2.setText(mDRList.get(value));
                }
            });
        } else {
            bottomDialog.setDatas(mDRList, mSelectedDr1);
            bottomDialog.setListener(value -> {
                mSelectedDr1 = value;
                mBind.tvDr1.setText(mDRList.get(value));
                if (mSelectedDr1 > mSelectedDr2) {
                    mSelectedDr2 = mSelectedDr1;
                    mBind.tvDr2.setText(mDRList.get(value));
                }
            });
        }
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectDr2(View view) {
        if (isWindowLocked()) return;
        final ArrayList<String> dr2List = new ArrayList<>();
        for (int i = mSelectedDr1; i <= mMaxDR; i++) {
            dr2List.add(i + "");
        }
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(dr2List, mSelectedDr2 - mSelectedDr1);
        bottomDialog.setListener(value -> {
            mSelectedDr2 = value + mSelectedDr1;
            mBind.tvDr2.setText(dr2List.get(value));
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectDrForJoin(View view) {
        if (isWindowLocked()) return;
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(mDRList, mSelectedDr);
        bottomDialog.setListener(value -> {
            mSelectedDr = value;
            mBind.tvDr.setText(mDRList.get(value));
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectMaxRetransmissionTimes(View view) {
        if (isWindowLocked()) return;
        int times = Integer.parseInt(mBind.tvMaxRetransmissionTimes.getText().toString()) - 1;
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(new ArrayList<>(Arrays.asList(mMaxRetransmissionTimesList)), times);
        bottomDialog.setListener(value -> mBind.tvMaxRetransmissionTimes.setText(mMaxRetransmissionTimesList[value]));
        bottomDialog.show(getSupportFragmentManager());
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        String adrAckLimitStr = mBind.etAdrAckLimit.getText().toString();
        String adrAckDelayStr = mBind.etAdrAckDelay.getText().toString();
        if (TextUtils.isEmpty(adrAckLimitStr)) {
            ToastUtils.showToast(this, "Para error!");
            return;
        }
        int adrAckLimit = Integer.parseInt(adrAckLimitStr);
        if (adrAckLimit < 1 || adrAckLimit > 100) {
            ToastUtils.showToast(this, "Para error!");
            return;
        }
        if (TextUtils.isEmpty(adrAckDelayStr)) {
            ToastUtils.showToast(this, "Para error!");
            return;
        }
        int adrAckDelay = Integer.parseInt(adrAckDelayStr);
        if (adrAckDelay < 1 || adrAckDelay > 100) {
            ToastUtils.showToast(this, "Para error!");
            return;
        }
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        if (mSelectedMode == 0) {
            String devEui = mBind.etDevEui.getText().toString();
            String appEui = mBind.etAppEui.getText().toString();
            String devAddr = mBind.etDevAddr.getText().toString();
            String appSkey = mBind.etAppSkey.getText().toString();
            String nwkSkey = mBind.etNwkSkey.getText().toString();
            if (devEui.length() != 16) {
                ToastUtils.showToast(this, "Para error!");
                return;
            }
            if (appEui.length() != 16) {
                ToastUtils.showToast(this, "Para error!");
                return;
            }
            if (devAddr.length() != 8) {
                ToastUtils.showToast(this, "Para error!");
                return;
            }
            if (appSkey.length() != 32) {
                ToastUtils.showToast(this, "Para error!");
                return;
            }
            if (nwkSkey.length() != 32) {
                ToastUtils.showToast(this, "Para error!");
                return;
            }
            orderTasks.add(OrderTaskAssembler.setLoraDevEUI(devEui));
            orderTasks.add(OrderTaskAssembler.setLoraAppEUI(appEui));
            orderTasks.add(OrderTaskAssembler.setLoraDevAddr(devAddr));
            orderTasks.add(OrderTaskAssembler.setLoraAppSKey(appSkey));
            orderTasks.add(OrderTaskAssembler.setLoraNwkSKey(nwkSkey));
        } else {
            String devEui = mBind.etDevEui.getText().toString();
            String appEui = mBind.etAppEui.getText().toString();
            String appKey = mBind.etAppKey.getText().toString();
            if (devEui.length() != 16) {
                ToastUtils.showToast(this, "Para error!");
                return;
            }
            if (appEui.length() != 16) {
                ToastUtils.showToast(this, "Para error!");
                return;
            }
            if (appKey.length() != 32) {
                ToastUtils.showToast(this, "Para error!");
                return;
            }
            orderTasks.add(OrderTaskAssembler.setLoraDevEUI(devEui));
            orderTasks.add(OrderTaskAssembler.setLoraAppEUI(appEui));
            orderTasks.add(OrderTaskAssembler.setLoraAppKey(appKey));
        }
        orderTasks.add(OrderTaskAssembler.setLoraUploadMode(mSelectedMode + 1));
        savedParamsError = false;
        // 保存并连接
        orderTasks.add(OrderTaskAssembler.setLoraRegion(mSelectedRegion));
        if (mSelectedRegion == 1 || mSelectedRegion == 2 || mSelectedRegion == 8) {
            // US915,AU915,CN470
            orderTasks.add(OrderTaskAssembler.setLoraCH(mSelectedCh1, mSelectedCh2));
        }
        if (mSelectedRegion == 3 || mSelectedRegion == 4
                || mSelectedRegion == 5 || mSelectedRegion == 9) {
            // CN779,EU433,EU868 and RU864
            orderTasks.add(OrderTaskAssembler.setLoraDutyCycleEnable(mBind.cbDutyCycle.isChecked() ? 1 : 0));
        }
        if (mSelectedRegion == 2 || mSelectedRegion == 3 || mSelectedRegion == 4 || mSelectedRegion == 5
                || mSelectedRegion == 6 || mSelectedRegion == 7 || mSelectedRegion == 9) {
            // AS923,US915,AU915
            orderTasks.add(OrderTaskAssembler.setLoraDR(mSelectedDr));
        }
        orderTasks.add(OrderTaskAssembler.setLoraAdrAckLimit(adrAckLimit));
        orderTasks.add(OrderTaskAssembler.setLoraAdrAckDelay(adrAckDelay));
        int time = Integer.parseInt(mBind.tvMaxRetransmissionTimes.getText().toString());
        orderTasks.add(OrderTaskAssembler.setLoraUplinkStrategy(mBind.cbAdr.isChecked() ? 1 : 0, time, mSelectedDr1, mSelectedDr2));
        showSyncingProgressDialog();
        MoKoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.cb_advance_setting) {
            mBind.llAdvancedSetting.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        } else if (buttonView.getId() == R.id.cb_adr) {
            mBind.llAdrOptions.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            mBind.layoutMaxTimes.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        }
    }
}
