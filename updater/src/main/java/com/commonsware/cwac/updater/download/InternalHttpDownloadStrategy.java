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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.content.Context;

public class InternalHttpDownloadStrategy extends HttpDownloadStrategy {

    @Override
    protected File getDownloadFile(Context context) {
        File updateDir = new File(context.getFilesDir(), FOLDER_NAME);
        //noinspection ResultOfMethodCallIgnored
        updateDir.mkdirs();
        return new File(updateDir, FILENAME);
    }

    @SuppressLint("WorldReadableFiles")
    @Override
    protected OutputStream openDownloadFile(Context context, File apk) throws FileNotFoundException {
        return context.openFileOutput(FILENAME, Context.MODE_WORLD_READABLE);
    }
}
