package com.zy.annotationprocessor;

import android.net.Uri;
import android.os.Environment;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.FileInputStream;

public class MyWebviewClient extends WebViewClient {

  @Override
  public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
    android.util.Log.e("XXXXX", "load url request " + url);
    Uri uri = Uri.parse(url);
    if (uri.getScheme().equals("apt") && uri.getHost().equals("res")) {
      String type = uri.getPathSegments().get(0);
      String fileName = uri.getPathSegments().get(1);
      File file = new File(Environment.getExternalStorageDirectory(), fileName);
      try {
        return new WebResourceResponse(type + "/mpeg", "utf-8", new FileInputStream(file));
      } catch (Exception e) {
        e.printStackTrace();
        android.util.Log.e("XXXXXXX", "error load uri ", e);
      }
    }
    return super.shouldInterceptRequest(view, url);
  }
}
