package cn.liuxiaoer.webview.lxewebview;

/**
 * 自定义WebView标题栏按钮点击事件处理
 */
interface LXEWebViewHeaderButtonHandler {
    //后退按钮点击事件处理
    void onPreButtonClick();

    //前进按钮击事件处理
    void onNextButtonClick();
}
