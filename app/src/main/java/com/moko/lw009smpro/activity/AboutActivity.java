package com.moko.lw009smpro.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.moko.lw009smpro.BuildConfig;
import com.moko.lw009smpro.R;
import com.moko.lw009smpro.databinding.Lw009ActivityAboutBinding;
import com.moko.lw009smpro.utils.Utils;

public class AboutActivity extends Lw009BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Lw009ActivityAboutBinding mBind = Lw009ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        if (!BuildConfig.IS_LIBRARY) {
            mBind.appVersion.setText(String.format("APP Version:V%s", Utils.getVersionInfo(this)));
        }
    }

    @Override
    protected boolean registerEvent() {
        return false;
    }

    public void onBack(View view) {
        finish();
    }

    public void onCompanyWebsite(View view) {
        if (isWindowLocked()) return;
        Uri uri = Uri.parse("https://" + getString(R.string.company_website));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
