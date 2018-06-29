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

package com.commonsware.cwac.updater.download;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.FileProvider;

import com.commonsware.cwac.updater.UpdateRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class HttpDownloadStrategy implements DownloadStrategy {

    static final String FOLDER_NAME = ".update";
    static final String FILENAME = "update.apk";
    private static final int BUFFER_SIZE = 4096;

    @Override
    public Uri downloadAPK(Context context, String url) throws Exception {
        File apk = getDownloadFile(context);

        if (apk.exists()) {
            //noinspection ResultOfMethodCallIgnored
            apk.delete();
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setInstanceFollowRedirects(true);
        HttpURLConnection.setFollowRedirects(true);
        conn.addRequestProperty("User-Agent", "Wget/1.18 (linux-gnu)");
        try {
            conn.connect();

            int status = conn.getResponseCode();

            if (status == HttpsURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                OutputStream f = openDownloadFile(context, apk);
                byte[] buffer = new byte[BUFFER_SIZE];
                int length;
                int currentSize = 0;
                Intent intent = new Intent(UpdateRequest.ACTION_PROGRESS);
                while ((length = is.read(buffer)) > 0) {
                    f.write(buffer, 0, length);
                    intent.putExtra(UpdateRequest.EXTRA_SIZE, conn.getContentLength());
                    currentSize += length;
                    intent.putExtra(UpdateRequest.EXTRA_CURRENT, currentSize);
                    context.sendBroadcast(intent);
                }

                f.close();
                is.close();
            } else {
                throw new RuntimeException(String.format(Locale.getDefault(), "Received %d from server", status));
            }
        } finally {
            conn.disconnect();
        }

        return getDownloadUri(context, apk);
    }

    @Override
    public int describeContents() {
        return (0);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // no-op
    }

    protected File getDownloadFile(Context context) {
        File updateDir = new File(context.getExternalFilesDir(null), FOLDER_NAME);
        //noinspection ResultOfMethodCallIgnored
        updateDir.mkdirs();
        return new File(updateDir, FILENAME);
    }

    protected OutputStream openDownloadFile(Context context, File apk) throws FileNotFoundException {
        return new FileOutputStream(apk);
    }

    private Uri getDownloadUri(Context context, File apk) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String packageName = context.getPackageName();
            return FileProvider.getUriForFile(context, packageName + ".fileProvider", apk);
        }
        else {
            return Uri.fromFile(apk);
        }
    }

    public static final Parcelable.Creator<HttpDownloadStrategy> CREATOR =
            new Parcelable.Creator<HttpDownloadStrategy>() {
                public HttpDownloadStrategy createFromParcel(Parcel in) {
                    return (new HttpDownloadStrategy());
                }

                public HttpDownloadStrategy[] newArray(int size) {
                    return (new HttpDownloadStrategy[size]);
                }
            };
}
