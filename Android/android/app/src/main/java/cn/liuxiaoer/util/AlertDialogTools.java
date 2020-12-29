package cn.liuxiaoer.util;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.liuxiaoer.R;


/**
 * Created by liuxiaoer@live.cn on 2020/08/01.
 */

public class AlertDialogTools {


    public AlertDialog normalDialog(final Context context, CharSequence content, final View.OnClickListener okClickListener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.alert_dialog, null);
        final TextView tv = (TextView) layout.findViewById(R.id.dialog_title);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setCancelable(false);
        dialog.show();
        dialog.getWindow().setContentView(layout);
        TextView dialog_msg = (TextView) layout.findViewById(R.id.dialog_msg);
        dialog_msg.setText(content);
        Button btnOK = (Button) layout.findViewById(R.id.dialog_ok);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (okClickListener != null) okClickListener.onClick(v);
            }
        });

        Button btnCancel = (Button) layout.findViewById(R.id.dialog_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        return dialog;
    }


    public AlertDialog normalDialog(final Context context, CharSequence content, String okTitle, final View.OnClickListener okClickListener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.alert_dialog, null);
        final TextView tv = (TextView) layout.findViewById(R.id.dialog_title);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setCancelable(false);
        dialog.show();
        dialog.getWindow().setContentView(layout);
        TextView dialog_msg = (TextView) layout.findViewById(R.id.dialog_msg);
        dialog_msg.setText(content);
        Button btnOK = (Button) layout.findViewById(R.id.dialog_ok);
        if (okTitle != null && !"".equals(okTitle)) {
            btnOK.setText(okTitle);
        }
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (okClickListener != null) okClickListener.onClick(v);
            }
        });

        Button btnCancel = (Button) layout.findViewById(R.id.dialog_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        return dialog;
    }


}
