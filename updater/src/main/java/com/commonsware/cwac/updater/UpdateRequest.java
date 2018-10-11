/*
 Copyright (c) 2012 CommonsWare, LLC
 <p>
 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.commonsware.cwac.updater;

import java.security.InvalidParameterException;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.commonsware.cwac.updater.confirmation.ConfirmationStrategy;
import com.commonsware.cwac.updater.download.DownloadStrategy;
import com.commonsware.cwac.updater.check.VersionCheckStrategy;
import com.commonsware.cwac.wakeful.WakefulIntentService;

@SuppressWarnings({"WeakerAccess", "unused"})
public class UpdateRequest {

    public static final int PHASE_VERSION_CHECK = 1;
    public static final int PHASE_PRE_DOWNLOAD = 2;
    public static final int PHASE_DOWNLOAD = 3;
    public static final int PHASE_PRE_INSTALL = 4;
    public static final int PHASE_INSTALL = 5;

    private static final String PACKAGE_NAME = "com.commonsware.cwac.updater.";
    private static final String EXTRA_VCS = PACKAGE_NAME + "EXTRA_VCS";
    private static final String EXTRA_CONFIRM_DOWNLOAD = PACKAGE_NAME + "EXTRA_CONFIRM_DOWNLOAD";
    private static final String EXTRA_PHASE = PACKAGE_NAME + "EXTRA_PHASE";
    private static final String EXTRA_DS = PACKAGE_NAME + "EXTRA_DS";
    private static final String EXTRA_CONFIRM_INSTALL = PACKAGE_NAME + "EXTRA_CONFIRM_INSTALL";
    private static final String EXTRA_UPDATE_URL = PACKAGE_NAME + "EXTRA_UPDATE_URL";
    private static final String EXTRA_INSTALL_URI = PACKAGE_NAME + "EXTRA_INSTALL_URI";
    static final String EXTRA_COMMAND = PACKAGE_NAME + "EXTRA_COMMAND";

    public static final String EXTRA_ERROR = PACKAGE_NAME + "EXTRA_ERROR";
    public static final String EXTRA_SIZE = PACKAGE_NAME + "SIZE";
    public static final String EXTRA_CURRENT = PACKAGE_NAME + "CURRENT";
    public static final String ACTION_PROGRESS = PACKAGE_NAME + "ACTION_PROGRESS";
    public static final String ACTION_COMPLETE = PACKAGE_NAME + "ACTION_COMPLETE";

    private Intent cmd;

    UpdateRequest(Intent cmd) {
        this.cmd = cmd;
    }

    VersionCheckStrategy getVersionCheckStrategy() {
        return ((VersionCheckStrategy) cmd.getParcelableExtra(EXTRA_VCS));
    }

    ConfirmationStrategy getPreDownloadConfirmationStrategy() {
        return ((ConfirmationStrategy) cmd.getParcelableExtra(EXTRA_CONFIRM_DOWNLOAD));
    }

    ConfirmationStrategy getPreInstallConfirmationStrategy() {
        return ((ConfirmationStrategy) cmd.getParcelableExtra(EXTRA_CONFIRM_INSTALL));
    }

    DownloadStrategy getDownloadStrategy() {
        return ((DownloadStrategy) cmd.getParcelableExtra(EXTRA_DS));
    }

    int getPhase() {
        return (cmd.getIntExtra(EXTRA_PHASE, PHASE_VERSION_CHECK));
    }

    String getUpdateURL() {
        return (cmd.getStringExtra(EXTRA_UPDATE_URL));
    }

    Uri getInstallUri() {
        return (Uri.parse(cmd.getStringExtra(EXTRA_INSTALL_URI)));
    }

    public static class Builder {
        protected Context context;
        protected Intent cmd;

        public Builder(Context context) {
            this.context = context;
            cmd = new Intent(context, UpdateService.class);
        }

        Builder(Context context, Intent cmd) {
            this.context = context;
            this.cmd = new Intent(cmd);
        }

        public Builder setVersionCheckStrategy(VersionCheckStrategy strategy) {
            cmd.putExtra(EXTRA_VCS, strategy);

            return (this);
        }

        public Builder setPreDownloadStrategy(ConfirmationStrategy strategy) {
            cmd.putExtra(EXTRA_CONFIRM_DOWNLOAD, strategy);

            return (this);
        }

        public Builder setPreInstallStrategy(ConfirmationStrategy strategy) {
            cmd.putExtra(EXTRA_CONFIRM_INSTALL, strategy);

            return (this);
        }

        public Builder setDownloadStrategy(DownloadStrategy strategy) {
            cmd.putExtra(EXTRA_DS, strategy);

            return (this);
        }

        public void execute() {
            Bundle bundle = cmd.getExtras();
            if (bundle == null) return;
            if (!bundle.containsKey(EXTRA_VCS) || !bundle.containsKey(EXTRA_DS)) {
                throw new InvalidParameterException("Missing a strategy!");
            }

            WakefulIntentService.sendWakefulWork(context, cmd);
        }

        void setPhase(int phase) {
            cmd.putExtra(EXTRA_PHASE, phase);
        }

        void setUpdateURL(String updateURL) {
            cmd.putExtra(EXTRA_UPDATE_URL, updateURL);
        }

        void setInstallUri(Uri apk) {
            cmd.putExtra(EXTRA_INSTALL_URI, apk.toString());
        }

        PendingIntent buildPendingIntent() {
            Intent i = new Intent(context, WakefulReceiver.class);

            i.putExtra(EXTRA_COMMAND, cmd);

            return (PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }
}
