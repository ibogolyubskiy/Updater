package com.commonsware.cwac.updater;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

public class DialogConfirmationStrategy implements ConfirmationStrategy {

    private String title;
    private String message;

    /**
     * Public constructor
     * @param title String - title for dialog
     * @param message String - message for dialog
     */
    public DialogConfirmationStrategy(String title, String message) {
        this.title = title;
        this.message = message;
    }

    /**
     * Private constructor for use in Parcelable implementation
     * @param in Parcel to restore instance from
     */
    private DialogConfirmationStrategy(Parcel in) {
        title = in.readString();
        message = in.readString();
    }

    /* (non-Javadoc)
     * @see com.commonsware.cwac.updater.ConfirmationStrategy#confirm(android.content.Context, android.app.PendingIntent)
     */
    @Override
    public boolean confirm(Context context, PendingIntent contentIntent) {
        Intent intent = new Intent(context, ServiceDialogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ServiceDialogActivity.TITLE, title);
        intent.putExtra(ServiceDialogActivity.MESSAGE, message);
        intent.putExtra(ServiceDialogActivity.INTENT, contentIntent);
        context.startActivity(intent);
        return false;
    }

    /* (non-Javadoc)
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /* (non-Javadoc)
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(message);
    }

    /**
     * Required to complete Parcelable interface. Creates
     * an NotificationConfirmationStrategy instance or array
     * upon demand.
     */
    public static final Parcelable.Creator<DialogConfirmationStrategy> CREATOR =
            new Parcelable.Creator<DialogConfirmationStrategy>() {
                public DialogConfirmationStrategy createFromParcel(Parcel in) {
                    return new DialogConfirmationStrategy(in);
                }

                public DialogConfirmationStrategy[] newArray(int size) {
                    return new DialogConfirmationStrategy[size];
                }
            };

}
