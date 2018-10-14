package com.commonsware.cwac.updater;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.IntentCompat;
import android.util.Log;

public class UpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            int prevAppVersion = prefs.getInt(UpdateRequest.APP_VERSION, 1);
            if (getAppVersion(context) > prevAppVersion)
                startLauncherActivity(context);
        }
    }

    private void startLauncherActivity(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            ComponentName componentName = intent.getComponent();
            Intent mainIntent;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                mainIntent = IntentCompat.makeRestartActivityTask(componentName);
            else
                mainIntent = Intent.makeRestartActivityTask(componentName);
            context.startActivity(mainIntent);
        }
    }

    private int getAppVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            String packageName = context.getPackageName();
            return manager.getPackageInfo(packageName, 0).versionCode;
        }
        catch (Exception e) {
            Log.e(UpdateReceiver.class.getName(), "getAppVersion: ", e);
            return 1;
        }
    }
}
