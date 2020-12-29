package vpn;

import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import cn.liuxiaoer.util.LogUtil;

import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * 登录声学所webvpn,存储cookie
 */
public class IOZWebVpnManager {
    private static final String TAG = IOZWebVpnManager.class.getSimpleName();
    private static final String SIGN_IN_URL = "http://webvpn.ioz.ac.cn/api/signin";
    private static final String SIGN_IN_PARAMS = "{\"username\":\"iozwebtest\",\"password\":\"IOZ@web1928\",\"token\":\"rNHhqGecsUGPxswgquVjck6HHPpcioz\"}";
    private static final String KEY_UPDATE_URL = "http://webvpn.ioz.ac.cn/vpn_key/update";
    private static final String BIZ_URL_GET_URL = "http://webvpn.ioz.ac.cn/quick?url=";
    private static final String HOST_REGISTED_IN_VPN = "http://co.ioz.ac.cn";
    private static final String HOST_REGISTED_WITH_VPN = "co.webvpn.ioz.ac.cn";
    private static final String HOST_DOC_WIEWER_WITH_VPN = "co-8014.webvpn.ioz.ac.cn";

    private void refreshCookie(Context context) {
        bizUrl(context, HOST_REGISTED_IN_VPN);
    }

    public static void initCookie(Context context) {
        new IOZWebVpnManager().refreshCookie(context);
    }


    /**
     * 第一步，登录声学所vpn
     *
     * @param context
     * @return
     */
    private String signin(Context context) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(SIGN_IN_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Host", "webvpn.ioz.ac.cn");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Connection", "keep-alive");


            OutputStream outputStream = connection.getOutputStream();
            DataOutputStream dos = new DataOutputStream(outputStream);
            dos.write(SIGN_IN_PARAMS.getBytes());
            dos.flush();
            dos.close();

            int responseCode = connection.getResponseCode();
            System.err.println(responseCode);
            if (responseCode == HTTP_MOVED_TEMP) {
                String location = connection.getHeaderField("Location");
                connection.disconnect();

                connection = (HttpURLConnection) new URL(location).openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.connect();
            }
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {

                InputStream inputStream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                Map<String, List<String>> cookieMap = connection.getHeaderFields();
                List<String> cookies = cookieMap.get("Set-Cookie");
                String s = "";
                if (null != cookies && cookies.size() > 0) {
                    for (String cookie : cookies) {
                        if (s.isEmpty()) {
                            s = cookie;
                        } else {
                            s += ";" + cookie;
                        }
                    }

                    LogUtil.d(TAG, "signin ", "Cookie:", s);
                    if (s.length() > 0) {
//                        CookieUtil.setParam(context, s);
                    }
                }

                LogUtil.d(TAG, "signin ", "result:", sb.toString());
                return s;
            }
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {//关闭连接
                connection.disconnect();
            }
        }
        return null;
    }

    /**
     * 第二步，更新cookie。
     * 与webvpn技术人员沟通说是为了webvpn服务做记录，具体什么功能不了解
     *
     * @param context
     * @return
     */
    private String update(Context context) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(KEY_UPDATE_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Host", "webvpn.ioz.ac.cn");

            String cookie = signin(context);
            String[] cs = null;
            if (cookie != null) {
                cs = cookie.split(";");
            }
            if (cs != null && cs.length > 0) {
                for (int i = 0; i < cs.length; i++) {
                    if (cs[i].contains("_webvpn_key")) {
                        cookie = cs[i];
                        syncCookie(context, cookie);
                        break;
                    }
                }
            }
            connection.setRequestProperty("Cookie", cookie);
            connection.setInstanceFollowRedirects(false);

            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HTTP_MOVED_TEMP) {
                String location = connection.getHeaderField("Location");
                connection.disconnect();

                connection = (HttpURLConnection) new URL(location).openConnection();
                connection.setRequestProperty("Cookie", cookie);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.connect();
            }
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                InputStream inputStream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                Map<String, List<String>> cookieMap = connection.getHeaderFields();
                List<String> cookies = cookieMap.get("Set-Cookie");
                String s = "";
                if (null != cookies && cookies.size() > 0) {
                    for (String c : cookies) {
                        if (s.isEmpty()) {
                            s = c;
                        } else {
                            s += ";" + c;
                        }
                    }

                    LogUtil.d(TAG, "update ", "Cookie:", s);
                    if (s.length() > 0) {
//                        CookieUtil.setParam(context, s);
                    }
                }

                LogUtil.d(TAG, "update ", "result:", sb.toString());
                return s;
            }
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {//关闭连接
                connection.disconnect();
            }
        }
        return null;
    }

    /**
     * 根据在webvpn中配置的内网地址拉取webvpn对外的地址
     *
     * @param host webvpn中配置的内网地址
     */
    private String bizUrl(Context context, String host) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(BIZ_URL_GET_URL + host);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            String cookie = update(context);
            String[] cs = null;
            if (cookie != null) {
                cs = cookie.split(";");
            }
            if (cs != null && cs.length > 0) {
                for (int i = 0; i < cs.length; i++) {
                    if (cs[i].contains("_astraeus_session")) {
                        cookie = cs[i];
                        break;
                    }
                }
            }
            connection.setRequestProperty("Cookie", cookie);
            connection.setInstanceFollowRedirects(false);

            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HTTP_MOVED_TEMP) {
                String location = connection.getHeaderField("Location");
                connection.disconnect();

                connection = (HttpURLConnection) new URL(location).openConnection();
                LogUtil.d(TAG, "cookie:", cookie);
                connection.setRequestProperty("Cookie", cookie);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.connect();
            }
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                InputStream inputStream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                Map<String, List<String>> cookieMap = connection.getHeaderFields();
                List<String> cookies = cookieMap.get("Set-Cookie");
                if (cookies == null) return sb.toString();
                for (int i = 0; i < cookies.size(); i++) {
                    String c = cookies.get(i);
                    if (c.contains("_astraeus_session")) {

                    }
                }


                LogUtil.d(TAG, "bizUrl ", "result:", sb.toString());
                return sb.toString();
            }
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {//关闭连接
                connection.disconnect();
            }
        }
        return null;
    }


    /**
     * 设置cookie.因为业务固定原因，此处host的使用了硬编码
     *
     * @param context
     * @param cookie
     */
    private void syncCookie(Context context, String cookie) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();//移除
        cookieManager.setCookie(HOST_REGISTED_WITH_VPN, cookie);
        cookieManager.setCookie(HOST_DOC_WIEWER_WITH_VPN, cookie);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            cookieManager.flush();
        }else {
            CookieSyncManager.getInstance().sync();
        }

        LogUtil.d(TAG, "保存co.webvpn.ioz.ac.cn地址cookie", cookie);
    }
}
