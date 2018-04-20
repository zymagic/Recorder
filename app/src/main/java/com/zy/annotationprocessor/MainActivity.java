package com.zy.annotationprocessor;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import com.zy.processor.ViewBind;
import com.zy.processor.http.FieldName;
import com.zy.processor.http.GET;
import com.zy.processor.http.POST;

public class MainActivity extends Activity {

  @ViewBind(R.id.txt)
  TextView myTextView;
  @ViewBind(R.id.txt2)
  TextView second;

  @Override
  @GET
  @POST
  protected void onCreate(@FieldName("bundle") Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
//    MainActivity_Bind.bind(this, this);
//    myTextView.setText("success");
//    second.setText("second");

    WebView webView = new MyWebview(this);
    setContentView(webView);
    webView.loadUrl("http://192.168.176.155/test.html");
  }
}
