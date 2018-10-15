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
package com.commonsware.cwac.updater;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.commonsware.cwac.updater.check.VersionCheckStrategy;
import com.commonsware.cwac.updater.confirmation.ConfirmationStrategy;
import com.commonsware.cwac.updater.download.DownloadStrategy;
import com.commonsware.cwac.wakeful.WakefulIntentService;

public class UpdateService extends WakefulIntentService {

    public UpdateService() {
        super("UpdateService");
    }

    @Override
    protected void doWakefulWork(Intent cmd) {
        UpdateRequest req = new UpdateRequest(cmd);
        VersionCheckStrategy vcs = req.getVersionCheckStrategy();

        try {
            if (req.getPhase() == UpdateRequest.PHASE_DOWNLOAD) {
                downloadAndInstall(cmd, req, req.getUpdateURL());
            } else if (req.getPhase() == UpdateRequest.PHASE_INSTALL) {
                install(req.getInstallUri());
            } else {
                int updateVersionCode = vcs.getVersionCode();
                int currentVersionCode =
                        getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;

                if (updateVersionCode > currentVersionCode) {
                    ConfirmationStrategy strategy = req.getPreDownloadConfirmationStrategy();

                    if (strategy == null || strategy.confirm(this, buildDownloadPhase(cmd, vcs.getUpdateURL()))) {
                        downloadAndInstall(cmd, req, vcs.getUpdateURL());
                    }
                } else {
                    sendBroadcast(new Intent(UpdateRequest.ACTION_COMPLETE));
                }
            }
        } catch (Exception e) {
            Intent intent = new Intent(UpdateRequest.ACTION_COMPLETE);
            intent.putExtra(UpdateRequest.EXTRA_ERROR, true);
            sendBroadcast(intent);
            Log.e("CWAC-Updater", "Exception in applying update", e);
        }
    }

    private void downloadAndInstall(Intent cmd, UpdateRequest req, String updateURL) throws Exception {
        DownloadStrategy ds = req.getDownloadStrategy();
        Uri apk = ds.downloadAPK(this, updateURL);

        if (apk != null) {
            confirmAndInstall(cmd, req, apk);
        }
    }

    private void confirmAndInstall(Intent cmd, UpdateRequest req, Uri apk) {

        ConfirmationStrategy strategy = req.getPreInstallConfirmationStrategy();

        if (strategy == null || strategy.confirm(this, buildInstallPhase(cmd, apk))) {
            install(apk);
        }
    }

    private void install(Uri apk) {
        Intent intent = new Intent(this, InstallActivity.class);
        intent.setData(apk);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private PendingIntent buildDownloadPhase(Intent cmd, String updateURL) {
        UpdateRequest.Builder builder = new UpdateRequest.Builder(this, cmd);

        builder.setPhase(UpdateRequest.PHASE_DOWNLOAD);
        builder.setUpdateURL(updateURL);

        return builder.buildPendingIntent();
    }

    private PendingIntent buildInstallPhase(Intent cmd, Uri apk) {
        UpdateRequest.Builder builder = new UpdateRequest.Builder(this, cmd);

        builder.setPhase(UpdateRequest.PHASE_INSTALL);
        builder.setInstallUri(apk);

        return builder.buildPendingIntent();
    }
}
