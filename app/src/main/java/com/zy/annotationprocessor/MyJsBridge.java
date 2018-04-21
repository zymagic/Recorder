package com.zy.annotationprocessor;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import audio.record.AudioRecordPanel;

public class MyJsBridge {

  Activity mActivity;
  WebView view;

  public MyJsBridge(Context context, WebView webview) {
    mActivity = (Activity) context;
    view = webview;
  }

  @JavascriptInterface
  public void startRecord(String inputJson) {
    android.util.Log.e("XXXXX", "call start record");
    AudioRecordPanel panel = new AudioRecordPanel();
    panel.listener = () -> {
      view.loadUrl(view.getUrl());
    };
    mActivity.getFragmentManager().beginTransaction()
        .add(R.id.container, panel, "record")
        .commitAllowingStateLoss();
  }

  @JavascriptInterface
  public void getAAC(String inputJson) {
    android.util.Log.e("XXXXX", "call getAAC");
    try {
      JSONObject json = new JSONObject(inputJson);
      JSONObject out = new JSONObject();
      final String fileName = json.getString("file");
      final String callback = json.getString("callback");
      File file = new File(Environment.getExternalStorageDirectory(), fileName);
      new Thread(() -> {
        try {
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          FileInputStream fis = new FileInputStream(file);
          byte[] buffer = new byte[4096];
          int len = 0;
          while ((len = fis.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
          }
          fis.close();
          bos.flush();
          String str = android.util.Base64.encodeToString(bos.toByteArray(), 0);
          File outFile = new File(Environment.getExternalStorageDirectory(), "song.base64.txt");
          if (!outFile.exists()) {
            outFile.createNewFile();
          }
          BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
          writer.write(str);
          writer.close();
          out.put("result", 1);
          out.put("data", str);
        } catch (Exception e) {
          android.util.Log.e("XXXXX", "failed to load fil in thread ", e);
          // ignore
          try {
            out.put("result", 0);
            out.put("error_msg", e.getMessage());
          } catch (JSONException e1) {
            e1.printStackTrace();
          }
        }
        final String call = String.format("javascript:%s(%s)", callback, out.toString());
        view.post(() -> {
          android.util.Log.e("XXXXX", "call " + call);
          view.loadUrl(call);
        });
      }).start();
    } catch (Exception e) {
      android.util.Log.e("XXXXX", "failed to load file in main thread ", e);
    }
  }
}
