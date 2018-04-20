package com.zy.annotationprocessor;

import android.content.Context;
import android.webkit.WebView;

public class MyWebview extends WebView {
  
  public MyWebview(Context context) {
    super(context);
    init(context);
  }
  
  private void init(Context context) {
    getSettings().setJavaScriptEnabled(true);
    getSettings().setBuiltInZoomControls(true);
    getSettings().setSupportZoom(true);
    getSettings().setUseWideViewPort(true);
    getSettings().setLoadWithOverviewMode(true);
    getSettings().setDomStorageEnabled(true);

    // 关闭文件协议，解决 webview 漏洞导致的问题
    getSettings().setAllowFileAccessFromFileURLs(false);
    getSettings().setAllowUniversalAccessFromFileURLs(false);
    getSettings().setAllowFileAccess(false);

    setWebViewClient(new MyWebviewClient());
    setWebChromeClient(new MyWebChromeClient());

    addJavascriptInterface(new MyJsBridge(), "APT");


  }
}
