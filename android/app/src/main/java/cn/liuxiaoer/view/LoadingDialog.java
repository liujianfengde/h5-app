package cn.liuxiaoer.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import cn.liuxiaoer.R;


//1,创建LoadingDialog继承Dialog并实现构造方法
public class LoadingDialog extends Dialog {

    private Window window = null;

    public LoadingDialog(Context context) {
        super(context);
    }

    public LoadingDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected LoadingDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setProperty(int x, int y, int w, int h) {
        window = getWindow();//得到对话框的窗口．
        WindowManager.LayoutParams wl = window.getAttributes();
        //默认居中
//        wl.x = x;//设置对话框的位置．0为中间
//        wl.y = y;
        wl.width = w;
        wl.height = h;
        wl.alpha = 1f;// 设置对话框的透明度,1f不透明
        wl.gravity = Gravity.CENTER;//设置显示在中间
        window.setAttributes(wl);
    }

    //2,创建静态内部类Builder，将dialog的部分属性封装进该类
    public static class Builder {

        private Context context;
        //提示信息
        private String message;
        //是否展示提示信息
        private boolean isShowMessage = true;
        //是否按返回键取消
        private boolean isCancelable = true;
        //是否取消
        private boolean isCancelOutside = false;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * 设置提示信息
         *
         * @param message
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * 设置是否显示提示信息
         *
         * @param isShowMessage
         * @return
         */
        public Builder setShowMessage(boolean isShowMessage) {
            this.isShowMessage = isShowMessage;
            return this;
        }

        /**
         * 设置是否可以按返回键取消
         *
         * @param isCancelable
         * @return
         */
        public Builder setCancelable(boolean isCancelable) {
            this.isCancelable = isCancelable;
            return this;
        }

        /**
         * 设置是否可以取消
         *
         * @param isCancelOutside
         * @return
         */
        public Builder setCancelOutside(boolean isCancelOutside) {
            this.isCancelOutside = isCancelOutside;
            return this;
        }

        //创建Dialog
        public LoadingDialog create() {

            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.dialog_loading, null);
            //设置带自定义主题的dialog
            LoadingDialog loadingDialog = new LoadingDialog(context, R.style.LoadingDialogStyle);
            TextView msgText = (TextView) view.findViewById(R.id.tipTextView);
            if (isShowMessage) {
                msgText.setText(message);
            } else {
                msgText.setVisibility(View.GONE);
            }
            loadingDialog.setContentView(view);
            loadingDialog.setCancelable(isCancelable);
            loadingDialog.setCanceledOnTouchOutside(isCancelOutside);
            loadingDialog.setProperty(0, 0, 300, 250);
            return loadingDialog;
        }
    }

}