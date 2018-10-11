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

package com.commonsware.cwac.updater.check;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

/**
 * VersionCheckStrategy implementation that downloads
 * a public-visible JSON document via HTTP and extracts
 * information about the available version from it.
 *
 * The JSON document needs to be a JSON object containing
 * a versionCode and an updateURL value. The versionCode
 * should be the android:versionCode value of the latest
 * APK available for download. The updateURL can provide
 * information to your chosen DownloadStrategy of where
 * to download the APK. For example, the updateURL could
 * be a URL to a publicly-visible APK for download. The
 * JSON document can have other contents if desired, but
 * they will be ignored.
 *
 * This implementation is fairly simplistic, just blindly
 * downloading the document. In particular, it will not
 * handle a failover (e.g., drop off WiFi and fail over
 * to 3G).
 *
 */
public class HttpVersionCheckStrategy implements VersionCheckStrategy {

    private String versionCodeField = "versionCode";
    private String updateUrlField = "updateURL";
    private String url;
    private String updateURL = null;

    /**
     * Basic constructor
     * @param url Location of the JSON document to download
     */
    @SuppressWarnings("unused")
    public HttpVersionCheckStrategy(String url) {
        this.url = url;
    }

    public HttpVersionCheckStrategy (String url, String versionCodeField, String updateUrlField) {
        this.url = url;
        this.versionCodeField = versionCodeField;
        this.updateUrlField = updateUrlField;
    }

    /**
     * Constructor for use with Parcelable
     * @param in Parcel from which to reconstitute this object
     */
    private HttpVersionCheckStrategy(Parcel in) {
        url = in.readString();
        versionCodeField = in.readString();
        updateUrlField = in.readString();
    }

    /* (non-Javadoc)
     * @see com.commonsware.cwac.updater.interfaces.VersionCheckStrategy#getVersionCode()
     */
    @Override
    public int getVersionCode() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setInstanceFollowRedirects(true);
        HttpURLConnection.setFollowRedirects(true);
        conn.addRequestProperty("User-Agent", "Wget/1.18 (linux-gnu)");
        int result;
        try {
            conn.connect();

            int status = conn.getResponseCode();

            if (status == HttpsURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                StringBuilder buf = new StringBuilder();
                String str;

                while ((str = in.readLine()) != null) {
                    buf.append(str);
                    buf.append('\n');
                }

                in.close();

                result = parseVersionCode(buf.toString());

            } else {
                throw new RuntimeException(String.format(Locale.getDefault(), "Received %d from server", status));
            }
        } finally {
            conn.disconnect();
        }

        return result;
    }

    private int parseVersionCode(String buffer) throws JSONException {
        JSONObject json = new JSONObject(buffer);
        updateURL = json.getString(updateUrlField);
        return json.getInt(versionCodeField);
    }

    /* (non-Javadoc)
     * @see com.commonsware.cwac.updater.interfaces.VersionCheckStrategy#getUpdateURL()
     */
    @Override
    public String getUpdateURL() {
        return (updateURL);
    }

    /* (non-Javadoc)
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return (0);
    }

    /* (non-Javadoc)
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(versionCodeField);
        dest.writeString(updateUrlField);
    }

    /**
     * Required to complete Parcelable interface. Creates
     * an HttpVersionCheckStrategy instance or array
     * upon demand.
     */
    public static final Parcelable.Creator<HttpVersionCheckStrategy> CREATOR =
            new Parcelable.Creator<HttpVersionCheckStrategy>() {
                public HttpVersionCheckStrategy createFromParcel(Parcel in) {
                    return new HttpVersionCheckStrategy(in);
                }

                public HttpVersionCheckStrategy[] newArray(int size) {
                    return new HttpVersionCheckStrategy[size];
                }
            };
}
