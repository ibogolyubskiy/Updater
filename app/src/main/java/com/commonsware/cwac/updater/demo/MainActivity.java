/*
 * Copyright (c) 2012 CommonsWare, LLC
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commonsware.cwac.updater.demo;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.updater.ConfirmationStrategy;
import com.commonsware.cwac.updater.DialogConfirmationStrategy;
import com.commonsware.cwac.updater.DownloadStrategy;
import com.commonsware.cwac.updater.HttpDownloadStrategy;
import com.commonsware.cwac.updater.HttpVersionCheckStrategy;
import com.commonsware.cwac.updater.ImmediateConfirmationStrategy;
import com.commonsware.cwac.updater.InternalHttpDownloadStrategy;
import com.commonsware.cwac.updater.NotificationConfirmationStrategy;
import com.commonsware.cwac.updater.UpdateRequest;
import com.commonsware.cwac.updater.UpdateService;
import com.commonsware.cwac.updater.VersionCheckStrategy;

public class MainActivity extends Activity {

    public static final String UPDATE_CONFIG = "https://www.dropbox.com/s/08v9vsooa4i3x22/update.json?dl=0";
    public static final String VERSION_CODE = "versionCode";
    public static final String UPDATE_URL = "updateURL";

    private TextView result;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView versionCodeLabel = (TextView) findViewById(R.id.versionCode);
        result = (TextView) findViewById(R.id.result);

        try {
            int currentVersionCode =
                    getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;

            versionCodeLabel.setText(String.valueOf(currentVersionCode));
        } catch (Exception e) {
            Log.e(MainActivity.class.getName(), "An impossible exception", e);
        }
    }

    private VersionCheckStrategy buildVersionCheckStrategy() {
        return new HttpVersionCheckStrategy(UPDATE_CONFIG, VERSION_CODE, UPDATE_URL);
    }

    private ConfirmationStrategy buildPreDownloadStrategy() {
        return
            new DialogConfirmationStrategy(getString(R.string.dialog_title), getString(R.string.dialog_prompt));
    }

    private DownloadStrategy buildDownloadStrategy() {
        return Build.VERSION.SDK_INT >= 11 ?
            new InternalHttpDownloadStrategy() :
            new HttpDownloadStrategy();
    }

    private ConfirmationStrategy immediatePreInstallStrategy() {
        return new ImmediateConfirmationStrategy();
    }

    private ConfirmationStrategy notificationPreInstallStrategy() {
        Notification n = new NotificationCompat.Builder(this)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_prompt))
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .build();
        return new NotificationConfirmationStrategy(n);
    }

    public void checkForUpdate(View view) {
        result.setText(null);
        new UpdateRequest.Builder(this)
            .setVersionCheckStrategy(buildVersionCheckStrategy())
            .setPreDownloadStrategy(buildPreDownloadStrategy())
            .setDownloadStrategy(buildDownloadStrategy())
            .setPreInstallStrategy(immediatePreInstallStrategy())
            .execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mCompleteReceiver, new IntentFilter(UpdateRequest.ACTION_COMPLETE));
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mCompleteReceiver);
        super.onStop();
    }

    private BroadcastReceiver mCompleteReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean error = intent.getBooleanExtra(UpdateRequest.EXTRA_ERROR, false);
            result.setText(error ? R.string.update_error : R.string.no_update);
            Toast.makeText(context, result.getText(), Toast.LENGTH_SHORT).show();
        }
    };
}