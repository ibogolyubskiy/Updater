package com.commonsware.cwac.updater;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.IntentCompat;

public class UpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction()))
            startLauncherActivity(context);
    }

    private void startLauncherActivity(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            ComponentName componentName = intent.getComponent();
            Intent mainIntent = IntentCompat.makeRestartActivityTask(componentName);
            context.startActivity(mainIntent);
        }
    }
}
