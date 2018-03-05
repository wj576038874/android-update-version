package com.winfo.wenjie.update;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


class CheckUpdateTask extends AsyncTask<Void, Void, String> {

    private ProgressDialog dialog;
    private Context mContext;
    private int mType;
    private boolean mShowProgressDialog;
    private static final String url = Constants.UPDATE_URL;

    CheckUpdateTask(Context context, int type, boolean showProgressDialog) {

        this.mContext = context;
        this.mType = type;
        this.mShowProgressDialog = showProgressDialog;

    }


    protected void onPreExecute() {
        if (mShowProgressDialog) {
            dialog = new ProgressDialog(mContext);
            dialog.setMessage(mContext.getString(R.string.android_auto_update_dialog_checking));
            dialog.show();
        }
    }


    @Override
    protected void onPostExecute(String result) {

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        if (!TextUtils.isEmpty(result)) {
            parseJson(result);
        }
    }

    private void parseJson(String result) {
        try {
            JSONObject obj = new JSONObject(result);
            String versionDesc = obj.getString(Constants.APK_UPDATE_CONTENT);
            String downloadUrl = obj.getString(Constants.APK_DOWNLOAD_URL);
            int versionCode = obj.getInt(Constants.APK_VERSION_CODE);
            String versionSize = obj.getString(Constants.APK_VERSION_SIZE);
            String versionName = obj.getString(Constants.APK_VERSION_NAME);

            int localVersionCode = AppUtils.getVersionCode(mContext);

            if (versionCode > localVersionCode) {
                if (mType == Constants.TYPE_NOTIFICATION) {
                    showNotification(mContext, versionDesc, downloadUrl);
                } else if (mType == Constants.TYPE_DIALOG) {
                    showDialog(mContext, versionDesc, versionName, versionSize, downloadUrl);
                }
            } else if (mShowProgressDialog) {
                Toast.makeText(mContext, mContext.getString(R.string.android_auto_update_toast_no_new_update), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            Log.e(Constants.TAG, "parse json error");
        }
    }


    /**
     * Show dialog
     */
    private void showDialog(Context context, String versionDesc, String versionName, String versionSize, String apkUrl) {
        UpdateDialog.show2(context, versionDesc, versionName, versionSize, apkUrl);
//        UpdateDialog.show(context , versionDesc , apkUrl);
    }

    /**
     * Show Notification
     */
    private void showNotification(Context context, String content, String apkUrl) {
        Intent myIntent = new Intent(context, DownloadService.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myIntent.putExtra(Constants.APK_DOWNLOAD_URL, apkUrl);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int smallIcon = context.getApplicationInfo().icon;
        Notification notify = new NotificationCompat.Builder(context)
                .setTicker(context.getString(R.string.android_auto_update_notify_ticker))
                .setContentTitle(context.getString(R.string.android_auto_update_notify_content))
                .setContentText(content)
                .setSmallIcon(smallIcon)
                .setContentIntent(pendingIntent).build();

        notify.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(0, notify);
    }

    @Override
    protected String doInBackground(Void... args) {
        return HttpUtils.get(url);
    }
}
