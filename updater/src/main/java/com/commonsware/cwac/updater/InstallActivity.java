package com.commonsware.cwac.updater;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;

public class InstallActivity extends Activity {

    private static final int INSTALL_REQUEST = 99369922;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INSTALL_REQUEST) {
            sendBroadcast(new Intent(UpdateRequest.ACTION_COMPLETE));
            finish();
        }
    }
}
