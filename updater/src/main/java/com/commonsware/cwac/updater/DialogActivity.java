package com.commonsware.cwac.updater;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.content.DialogInterface.OnClickListener;
import static android.content.DialogInterface.OnKeyListener;
import static android.view.WindowManager.LayoutParams;

public class DialogActivity extends Activity implements OnClickListener, OnKeyListener {

    public static final String INTENT = "INTENT";
    public static final String TITLE = "TITLE";
    public static final String MESSAGE = "MESSAGE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            setFinishOnTouchOutside(false);
        else
            getWindow().clearFlags(LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setOnKeyListener(this);
        builder.setPositiveButton(android.R.string.ok, this);
        builder.setNegativeButton(android.R.string.cancel, this);
        String title = getIntent().getStringExtra(TITLE);
        if (!TextUtils.isEmpty(title))
            builder.setTitle(title);
        String message = getIntent().getStringExtra(MESSAGE);
        if (!TextUtils.isEmpty(message))
            builder.setMessage(message);

        alert = builder.show();
        registerReceiver(mProgressReceiver, new IntentFilter(UpdateRequest.ACTION_PROGRESS));
        registerReceiver(mCompleteReceiver, new IntentFilter(UpdateRequest.ACTION_COMPLETE));
    }

    private AlertDialog alert;

    @Override
    protected void onDestroy() {
        if (alert != null) alert.dismiss();
        unregisterReceiver(mProgressReceiver);
        unregisterReceiver(mCompleteReceiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() { }

    private BroadcastReceiver mCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    private AlertDialog dialog;

    private BroadcastReceiver mProgressReceiver = new BroadcastReceiver() {

        TextView current;
        TextView size;
        TextView percent;
        ProgressBar progress;

        @Override
        public void onReceive(Context context, Intent intent) {
            int totalSize = intent.getIntExtra(UpdateRequest.EXTRA_SIZE, 0);
            int currentSize = intent.getIntExtra(UpdateRequest.EXTRA_CURRENT, 0);
            if (dialog == null) {
                ContextThemeWrapper wrapper = new ContextThemeWrapper(context, R.style.Theme_Transparent);
                View view = View.inflate(wrapper, R.layout.view_download_progress, null);
                current = (TextView) view.findViewById(R.id.current);
                size = (TextView) view.findViewById(R.id.size);
                percent = (TextView) view.findViewById(R.id.percent);
                progress = (ProgressBar) view.findViewById(R.id.progress);

                dialog = new AlertDialog.Builder(wrapper)
                        .setTitle(R.string.downloading_update)
                        .setCancelable(false)
                        .setView(view)
                        .create();

                progress.setMax(totalSize);
                size.setText(getString(R.string.size_format, totalSize / 1024f / 1024f));
            }

            if (dialog != null) dialog.show();

            progress.setProgress(currentSize);
            current.setText(getString(R.string.size_format, currentSize / 1024f / 1024f));
            float value = currentSize * 100f / totalSize;
            percent.setText(getString(R.string.percent_format, (int)value));

            if (Float.compare(value, 100f) >= 0) {
                if (dialog != null) dialog.dismiss();
                finish();
            }
        }
    };

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                PendingIntent intent = getIntent().getParcelableExtra(INTENT);
                if (intent != null) {
                    try {
                        intent.send();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case BUTTON_NEGATIVE:
                sendBroadcast(new Intent(UpdateRequest.ACTION_COMPLETE));
                finish();
                break;
        }
    }

    @Override
    public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
            sendBroadcast(new Intent(UpdateRequest.ACTION_COMPLETE));
            finish();
        }
        return keyCode == KeyEvent.KEYCODE_BACK;
    }
}