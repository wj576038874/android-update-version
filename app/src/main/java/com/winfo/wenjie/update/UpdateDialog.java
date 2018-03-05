package com.winfo.wenjie.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

class UpdateDialog {


    static void show(final Context context, String content, final String downloadUrl) {
        if (isContextValid(context)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.android_auto_update_dialog_title);
            builder.setMessage(Html.fromHtml(content))
                    .setPositiveButton(R.string.android_auto_update_dialog_btn_download, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            goToDownload(context, downloadUrl);
                        }
                    })
                    .setNegativeButton(R.string.android_auto_update_dialog_btn_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });

            AlertDialog dialog = builder.create();
            //点击对话框外面,对话框不消失
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    static void show2(final Context context, String versionDesc, String versionName, String versionSize, final String downloadUrl) {
        final Dialog dialog = new AlertDialog.Builder(context).create();
        dialog.setCancelable(false);// 可以用“返回键”取消
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        View view = LayoutInflater.from(context).inflate(R.layout.version_update_dialog, null);
        dialog.setContentView(view);
        TextView tvContent = view.findViewById(R.id.tv_update_content);
        TextView tvUpdateTile = view.findViewById(R.id.tv_update_title);
        TextView tvUpdateMsgSize = view.findViewById(R.id.tv_update_msg_size);
        Button btnOk = view.findViewById(R.id.btn_update_id_ok);
        Button btnCancel = view.findViewById(R.id.btn_update_id_cancel);

        tvContent.setText(versionDesc);
        tvUpdateTile.setText("最新版本：" + versionName);
        tvUpdateMsgSize.setText("新版本大小：" + versionSize);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                goToDownload(context, downloadUrl);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private static boolean isContextValid(Context context) {
        return context instanceof Activity && !((Activity) context).isFinishing();
    }


    private static void goToDownload(Context context, String downloadUrl) {
        Intent intent = new Intent(context.getApplicationContext(), DownloadService.class);
        intent.putExtra(Constants.APK_DOWNLOAD_URL, downloadUrl);
        context.startService(intent);
    }
}
