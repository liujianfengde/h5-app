package cn.liuxiaoer.net;

import cn.liuxiaoer.LxeApplication;

/**
 * {
 * "msg":"",
 * "url":"http://116.90.80.66:8012/ServiceAction/com.tap.document.file.FileDownload?attachid=ff8080817394d67a0173bd3761c319b2&isdownload=1&nIndex=1",
 * "versionCode":"2",
 * "updateMessage":"发布测试包",
 * "status":"true"
 * }
 */
public class VersionModel extends BaseModel {

    private String url;
    private Long versionCode;
    private String updateMessage;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Long versionCode) {
        this.versionCode = versionCode;
    }

    public String getUpdateMessage() {
        return updateMessage;
    }

    public void setUpdateMessage(String updateMessage) {
        this.updateMessage = updateMessage;
    }

    public boolean needUpdate() {
        return versionCode != null
                && LxeApplication.getInstance().getPhoneInfo() != null
                && versionCode > LxeApplication.getInstance().getPhoneInfo().getVersionCode();
    }
}
