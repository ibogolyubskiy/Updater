package com.commonsware.cwac.updater;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager.LayoutParams;

public class InstallActivity extends Activity {

    private static final int INSTALL_REQUEST = 99369922;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saveAppVersion();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            setFinishOnTouchOutside(false);
        else
            getWindow().clearFlags(LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            //noinspection deprecation
            intent.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(getIntent().getData(), "application/vnd.android.package-archive");
        startActivityForResult(intent, INSTALL_REQUEST);
    }

    private void saveAppVersion() {
        try {
            PackageManager manager = getPackageManager();
            String packageName = getPackageName();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            int versionCode = manager.getPackageInfo(packageName, 0).versionCode;
            prefs.edit().putInt(UpdateReceiver.APP_VERSION, versionCode).apply();
        }
        catch (Exception e) {
            Log.e("Updater", "saveAppVersion: ", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().remove(UpdateReceiver.APP_VERSION).apply();
        if (requestCode == INSTALL_REQUEST) {
            sendBroadcast(new Intent(UpdateRequest.ACTION_COMPLETE));
            finish();
        }
    }
}
