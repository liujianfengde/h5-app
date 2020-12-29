package cn.liuxiaoer.net;

import retrofit2.http.POST;
import rx.Observable;

public interface Api {

    //检测版本更新
    @POST("/appdownload/ioz/apkfiles/apkversion.json")
    Observable<VersionModel> checkupVersion();

}
