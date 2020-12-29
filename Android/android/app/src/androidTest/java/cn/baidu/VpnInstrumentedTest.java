package cn.baidu;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import cn.liuxiaoer.util.LogUtil;
import vpn.IOZWebVpnManager;

public class VpnInstrumentedTest {
    private static final String TAG = VpnInstrumentedTest.class.getSimpleName();

    @Test
    public void testVpn() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getContext();
        String result = new IOZWebVpnManager().bizUrl(appContext, "http://10.0.0.228\n");
        try {
            JSONObject jsonObject = new JSONObject(result);
            LogUtil.d(TAG, "url:", String.valueOf(jsonObject.get("url")));
        } catch (JSONException e) {
            LogUtil.d(TAG, e.getMessage());
        }

        Assert.assertNotNull(result);
    }
}
