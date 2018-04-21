package com.zy.annotationprocessor;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import com.zy.processor.ViewBind;
import com.zy.processor.http.FieldName;
import com.zy.processor.http.GET;
import com.zy.processor.http.POST;

public class MainActivity extends Activity {

  @Override
  @GET
  @POST
  protected void onCreate(@FieldName("bundle") Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
//    MainActivity_Bind.bind(this, this);
//    myTextView.setText("success");
//    second.setText("second");

    WebView webView = findViewById(R.id.webview);
    webView.loadUrl("http://192.168.176.155/test.html");

    if (Build.VERSION.SDK_INT >= 23 && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, android.os.Process.myPid(), android.os.Process.myUid()) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }
  }
}
