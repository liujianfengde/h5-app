package vpn;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class VpnCookieService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        new Thread(new Runnable() {
            @Override
            public void run() {
                IOZWebVpnManager.initCookie(VpnCookieService.this);
            }
        }).start();
    }
}
