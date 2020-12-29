package cn.liuxiaoer.webview.lxewebview.proxy;

public class LXENativeCallHtmlJavascriptProxy {
    public static final String JAVASCRIPT = "javascript:";
    public static final String GO_BACK = "javascript:goBack()";

    public static final String QR_CODE_SCAN_RESULT_SCRIPT_NAME = "window.scanResult";

    public static final String qrCodeScanResult(String code) {
        return "javascript:window.scanResult('" + code + ")";
//        return script(QR_CODE_SCAN_RESULT_SCRIPT_NAME, code);
    }

    public static final String canGoBack() {
        return "javascript:window.canGoBack";
    }

    private static String script(String name, String... params) {
        if (name.indexOf(JAVASCRIPT) != -1) {
            name = JAVASCRIPT + name;
        }
        String paramsStr = "";
        if (params != null) {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < params.length; i++) {
                str.append("'").append(params[i]).append("',");
            }
            paramsStr = str.substring(0, str.length() - 1);
        }
        return String.format("%s(%s)", name, paramsStr);
    }
}
