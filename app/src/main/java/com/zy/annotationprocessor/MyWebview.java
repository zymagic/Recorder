package com.zy.annotationprocessor;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import java.util.jar.Attributes;

public class MyWebview extends WebView {

  public MyWebview(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public MyWebview(Context context) {
    this(context, null);
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

    addJavascriptInterface(new MyJsBridge(getContext(), this), "kwai");


  }
}
