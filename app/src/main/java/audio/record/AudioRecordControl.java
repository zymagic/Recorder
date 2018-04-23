package audio.record;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AudioRecordControl {

  private String mBaseName;
  private String mFolder;
  private List<Frag> mFrags = new ArrayList<>();

  private Handler mHandler = new Handler(Looper.getMainLooper());
  private Executor mExecutor = Executors.newFixedThreadPool(2);
  private Listener mListener;

  private AudioRecorder mRecorder;
  private Processor mCurrent;

  private long mMaxTimeInMillis = -1;
  private long mMaxFileSize = 2 * 1024 * 1024; // 2MB

  private Status mStatus;

  public AudioRecordControl() {
    mRecorder = new AudioRecorder();
    mRecorder.setListener(status -> {
      switch (status) {
        case PAUSE:
          try {
            mCurrent.updater.end();
            mCurrent.builder.finish();
            mFrags.add(new Frag(new File(mFolder, mBaseName + "_" + (mFrags.size() + 1)), mCurrent.updater.mDuration));
            mMaxTimeInMillis -= mCurrent.updater.mDuration;
            if (mListener != null) {
              mHandler.post(() -> mListener.onPieceAdded(mFrags.size(), 0));
            }
          } catch (Exception e) {

          }
          mStatus = Status.PAUSE;
          if (mListener != null) {
            mHandler.post(() ->
              mListener.onStatusChanged(mStatus)
            );
          }
          break;
      }
    });
    mStatus = Status.INIT;
  }

  public AudioRecordControl setMaxTime(long maxTime) {
    mMaxTimeInMillis = maxTime;
    return this;
  }

  public AudioRecordControl setListener(Listener listener) {
    mListener = listener;
    if (listener != null) {
      listener.onStatusChanged(mStatus);
    }
    return this;
  }

  public AudioRecordControl setOutput(String dir, String baseName) {
    mFolder = dir;
    mBaseName = baseName;
    return this;
  }

  public AudioRecordControl setExists() {
    return this;
  }

  public void start() {
    if (mStatus == Status.START) {
      return;
    }
    File outFile = new File(mFolder, mBaseName + "_" + (mFrags.size() + 1));
    try {
      Processor processor = new Processor();
      processor.file = outFile;
      processor.builder = new SoundBuilder(outFile, AudioRecorder.SAMPLE_RATE, 1);
      mRecorder.start(processor.builder);
      processor.updater.start();
      mCurrent = processor;
      mStatus = Status.START;
    } catch (IOException e) {
      // ignore
      mStatus = Status.PAUSE;
    }
    if (mListener != null) {
      mListener.onStatusChanged(mStatus);
    }
  }

  public void pause() {
    if (mStatus != Status.START) {
      return;
    }
    mStatus = Status.PAUSE;
    try {
      mRecorder.pause();
    } catch (Exception e) {
      // ignore
    }
    if (mListener != null) {
      mListener.onStatusChanged(mStatus);
    }
  }

  public void backspace() {
    if (mStatus != Status.PAUSE) {
      return;
    }
    if (mFrags.size() == 0) {
      return;
    }
    Frag last = mFrags.remove(mFrags.size() - 1);
    last.file.delete();
    if (mMaxTimeInMillis >= 0) {
      mMaxTimeInMillis += last.timeLength;
    }
    if (mListener != null) {
      mListener.onPieceDeleted(mFrags.size());
    }
  }

  public void finish() {
    if (mStatus != Status.PAUSE) {
      return;
    }
    mStatus = Status.STOP;
    mRecorder.stop();
    mExecutor.execute(this::compose);
  }

  public void abort() {
    if (mStatus == Status.STOP) {
      return;
    }
    mStatus = Status.STOP;
    mRecorder.stop();
    mExecutor.execute(this::release);
  }

  private void compose() {
    final File outputFile = new File(mFolder, mBaseName);
    FileChannel channel = null;
    try {
      touch(outputFile);

      channel = new FileOutputStream(outputFile).getChannel();
      for (Frag f : mFrags) {
        appendFile(channel, f.file);
      }
      channel.close();
      if (mListener != null) {
        mHandler.post(() -> mListener.onComposed(outputFile));
      }
    } catch (IOException e) {
      android.util.Log.e("XXXXX", "error compose", e);
    } finally {
      closeQuietly(channel);
    }
    release();
  }

  private void touch(File file) throws IOException {
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    if (!file.exists()) {
      file.createNewFile();
    }
  }

  private void appendFile(FileChannel out, File file) {
    FileChannel in = null;
    try {
      in = new FileInputStream(file).getChannel();
      out.transferFrom(in, out.size(), in.size());
    } catch (Exception e) {
      android.util.Log.e("XXXXX", "error append file", e);
    } finally {
      closeQuietly(in);
    }
  }

  private void release() {
    for (Frag f : mFrags) {
      f.file.delete();
    }
  }

  private void closeQuietly(Closeable c) {
    try {
      c.close();
    } catch (Exception e) {
      // ignore
    }
  }

  private static class Frag {
    File file;
    long timeLength;

    Frag(File file, long timeLength) {
      this.file = file;
      this.timeLength = timeLength;
    }
  }

  public enum Status {
    INIT, START, PAUSE, STOP;
  }

  public interface Listener {
    void onStatusChanged(Status status);
    void onProgressUpdate(int index, long time);
    void onPieceAdded(int count, long length);
    void onPieceDeleted(int remain);
    void onComposed(File file);
  }

  private class Processor {
    FileBuilder builder;
    File file;
    TimeUpdater updater = new TimeUpdater();

  }

  private class TimeUpdater extends Thread {

    boolean mEnded = false;
    long startTime = -1;
    long mDuration = 0;

    void end() {
      mEnded = true;
      interrupt();
    }

    @Override
    public void run() {
      startTime = System.currentTimeMillis();
      while (!mEnded) {
        long now = System.currentTimeMillis();
        if (startTime == -1) {
          startTime = now;
        }
        mDuration = now - startTime;
        if (mDuration < mMaxTimeInMillis) {
          if (mListener != null) {
            mHandler.post(() -> mListener.onProgressUpdate(mFrags.size(), mDuration));
          }
          try {
            Thread.sleep(2000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          mDuration = System.currentTimeMillis() - startTime;
        } else {
          mDuration = mMaxTimeInMillis;
          mHandler.post(() -> pause());
          break;
        }
      }
    }
  }
}
