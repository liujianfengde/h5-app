package cn.liuxiaoer.webview.webview.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import java.util.HashSet;

public class CookieUtil {
    public static void setParam(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("WaterHoney", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("logincookie", key);
        editor.commit();
    }

    public static Object getParam(Context context) {
        SharedPreferences sp = context.getSharedPreferences("WaterHoney", Context.MODE_PRIVATE);
        return sp.getString("logincookie", "");
    }

    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences("WaterHoney", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * 删除指定域名下的cookie
     * @param context Context
     * @param domain domain，如: http://www.baidu.com
     */
    public static void deleteCookiesForDomain(Context context, String domain) {
        CookieManager cookieManager = CookieManager.getInstance();
        if (cookieManager == null) return;

        /* http://code.google.com/p/android/issues/detail?id=19294 */
        if (Build.VERSION.SDK_INT < 11) {
            /* Trim leading '.'s */
            if (domain.startsWith(".")) domain = domain.substring(1);
        }

        String cookieGlob = cookieManager.getCookie(domain);
        if (cookieGlob != null) {
            String[] cookies = cookieGlob.split(";");
            for (String cookieTuple : cookies) {
                String[] cookieParts = cookieTuple.split("=");
                HashSet<String> domainSet = getDomainSet(domain);
                for (String dm : domainSet) {
                    /* Set an expire time so that this field will be removed after calling sync() */
                    cookieManager.setCookie(dm, cookieParts[0] + "=; Expires=Wed, 31 Dec 2000 23:59:59 GMT");
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.flush();
            } else {
                CookieSyncManager.createInstance(context);
                CookieSyncManager.getInstance().sync();
            }
        }
    }

    private static HashSet<String> getDomainSet(String domain) {
        HashSet<String> domainSet = new HashSet<>();
        String host = Uri.parse(domain).getHost();

        domainSet.add(host);
        domainSet.add("." + host);
        // exclude domain like "baidu.com"
        if (host.indexOf(".") != host.lastIndexOf(".")) {
            domainSet.add(host.substring(host.indexOf('.')));
        }

        return domainSet;
    }




}