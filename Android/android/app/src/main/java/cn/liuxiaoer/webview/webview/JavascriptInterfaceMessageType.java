package cn.liuxiaoer.webview.webview;

public enum JavascriptInterfaceMessageType {

    WEB_VIEW_RELOAD("重新加载页面", 1);


    private int code;
    private String desc;

    JavascriptInterfaceMessageType(String desc, int code) {
        this.desc = desc;
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
