package cn.liuxiaoer.net.subscriber;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import cn.liuxiaoer.view.LoadingDialog;
import rx.Subscriber;

public abstract class BaseSubscriber<T> extends Subscriber<T> {
    protected Context mContext;
    private static LoadingDialog loadingDialog;

    public BaseSubscriber(Context mContext) {
        this.mContext = mContext;
    }


    private void showProgressDialog() {
        if(loadingDialog != null && loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
        LoadingDialog.Builder builder = new LoadingDialog.Builder(mContext)
                .setMessage("加载中...")
                .setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void dismissProgressDialog() {
        loadingDialog.dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        showProgressDialog();
    }

    @Override
    public void onCompleted() {
        dismissProgressDialog();
    }

    @Override
    public void onNext(T t) {
        dismissProgressDialog();
        //获取json字符串获取缓存   获取json字符串获取缓存
        JSONObject obj;
        try {
            obj = new JSONObject(new Gson().toJson(t));
            Log.e("network response" + t.getClass().getSimpleName(), "parse json data succeed.");
            Log.e("network response data: ", obj.toString());

        } catch (JSONException e) {
            dismissProgressDialog();
            Log.e("network response" + t.getClass().getSimpleName(), "parse json data error.", e);
        }

    }

    @Override
    public void onError(Throwable e) {
        dismissProgressDialog();
        Log.e("network response", "onError be called.", e);
    }
}
