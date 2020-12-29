package cn.liuxiaoer.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.liuxiaoer.LxeApplication;
import cn.liuxiaoer.net.Api;
import cn.liuxiaoer.net.BaseModel;
import cn.liuxiaoer.net.VersionModel;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ApiManager {
    private static final String TAG = ApiManager.class.getSimpleName();

    private static final int DEFAULT_TIMEOUT = 10;
    private Retrofit retrofit;
    private Api api;
    private static ApiManager manager;

    private static final String SERVER_HOST = "https://www.eplus.org.cn";


    public static ApiManager getInstance() {
        if (manager == null) {
            synchronized (ApiManager.class) {
                if (manager == null) {
                    manager = new ApiManager();
                }
            }
        }
        return manager;
    }

    private ApiManager() {
        //手动创建一个okhttpclient并设置超时时间
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        httpClientBuilder.addInterceptor(netCacheInterceptor);
        try {
            retrofit = new Retrofit.Builder()
                    .client(httpClientBuilder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .baseUrl(SERVER_HOST)
                    .build();
        } catch (Exception e) {
            ToastUtil.showToast("您的网络不稳定，请稍后再试");
        }
        api = retrofit.create(Api.class);
    }

    private Interceptor netCacheInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            HttpUrl httpUrl = request.url();
            LogUtil.d(TAG,"url:",httpUrl.toString());
            request = request.newBuilder()
                    .addHeader("deviceVersion", android.os.Build.MODEL)
                    .addHeader("systemVersion", android.os.Build.VERSION.RELEASE)
                    .addHeader("resolution", LxeApplication.getInstance().getPhoneInfo().getResolution())
                    .addHeader("version", LxeApplication.getInstance().getPhoneInfo().getVersionName())
                    .addHeader("systemName", "android")
                    .addHeader("density", LxeApplication.getInstance().getPhoneInfo().getDpi())
                    .addHeader("networkType", LxeApplication.getInstance().getPhoneInfo().getNetworkType())
                    .addHeader("deviceId", LxeApplication.getInstance().getPhoneInfo().getDeviceId())
                    .url(httpUrl)
                    .build();
            return chain.proceed(request);
        }
    };

    //添加线程管理并订阅
    private void toSubscribe(Observable o, Subscriber s) {
        o.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s);
    }


    private class HttpResultFunc<V extends BaseModel, R extends BaseModel> implements Func1<V, R> {
        @Override
        public BaseModel call(BaseModel result) {
            return result;
        }
    }

    public void checkupVersion(Subscriber<VersionModel> subscriber) {
        Observable o = api.checkupVersion()
                .map(new HttpResultFunc<VersionModel,VersionModel>());
        toSubscribe(o, subscriber);
    }

}
