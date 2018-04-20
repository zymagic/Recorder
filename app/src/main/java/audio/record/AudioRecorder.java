package audio.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AudioRecorder {

  private String mBaseName;
  private String mFolder;
  private List<Long> mFrags;

  private Status mStatus;
  private AudioRecord mRecord;
  private long mMaxTimeInMillis = -1;

  private int mBufferSize = 0;

  private static final int SAMPLE_RATE = 44100;
  private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
  private static final int FORMAT = AudioFormat.ENCODING_PCM_8BIT;

  private Executor mExecutor;
  private Handler mHandler = new Handler(Looper.getMainLooper());

  public AudioRecorder() {
    mBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, FORMAT);
    mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, FORMAT, mBufferSize);
    int state = mRecord.getState();
    if (state == AudioRecord.STATE_INITIALIZED) {
      mStatus = Status.INIT;
    } else {

    }
    mExecutor = Executors.newSingleThreadExecutor();
  }

  public void start() {
    if (mStatus == Status.START) {
      return;
    }
    mRecord.startRecording();
    mExecutor.execute(this::processRecord);
  }

  public void pause() {
    mStatus = Status.PAUSE;
  }

  public void deleteLatest() {
    if (mStatus != Status.PAUSE) {
      return;
    }
    File lastFile = new File(mFolder, mBaseName + "_" + mFrags.size());
    lastFile.delete();
    long len = mFrags.remove(mFrags.size() - 1);
    if (mMaxTimeInMillis >= 0) {
      mMaxTimeInMillis += len;
    }
  }

  public void abort() {
    mStatus = Status.STOP;
    mExecutor.execute(this::release);
  }

  public void finish() {
    mStatus = Status.STOP;
    mExecutor.execute(this::compose);
  }

  private void processRecord() {
    String fileName = mBaseName + "_" + (mFrags.size() + 1);
    File currentFile = new File(mFolder, fileName);
    byte[] buffer = new byte[mBufferSize];
    long timeRemain = mMaxTimeInMillis;
    long start = System.currentTimeMillis();
    long timeLength = 0;
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(currentFile);
      while (mStatus == Status.START
          && (timeRemain == -1 || System.currentTimeMillis() - start < timeRemain)) {
        int len = mRecord.read(buffer, 0, mBufferSize);
        fos.write(buffer, 0, len);
        timeLength = System.currentTimeMillis() - start;
      }
      if (mMaxTimeInMillis > 0) {
        mMaxTimeInMillis = Math.max(0, mMaxTimeInMillis - timeLength);
      }
      fos.flush();
      if (timeLength != 0) {
        mFrags.add(timeLength);
      }
      mRecord.stop();
      mStatus = Status.PAUSE;
    } catch (Exception e) {
      //
    } finally {
      closeQuietly(fos);
    }
  }

  private void compose() {
    ArrayList<File> files = new ArrayList<>(mFrags.size());
    WavHeader header = new WavHeader();
    header.setChannels(1).setEncode(AudioFormat.ENCODING_PCM_8BIT);
    for (int i = 0; i < mFrags.size(); i++) {
      File file = new File(mFolder, mBaseName + "_" + (i + 1));
      header.appendFileLength((int) file.length());
      files.add(file);
    }

    File outputFile = new File(mFolder, mBaseName);
    DataOutputStream dos = null;
    try {
       dos = new DataOutputStream(new FileOutputStream(outputFile));
       header.write(dos);
       byte[] buffer = new byte[4096];
       for (File file : files) {
         appendFile(file, dos, buffer);
       }
       dos.flush();
    } catch (IOException e) {
      // ignore
    } finally {
      closeQuietly(dos);
    }
    release();
  }

  private void appendFile(File file, OutputStream fos, byte[] buffer) {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      int len = 0;
      while ((len = fis.read(buffer)) != 0) {
        fos.write(buffer, 0, len);
      }
    } catch (Exception e) {
      //
    } finally {
      closeQuietly(fis);
    }
  }

  private void release() {
    mRecord.release();
    for (int i = 0; i < mFrags.size(); i++) {
      File f = new File(mFolder, mBaseName + "_" + (i + 1));
      f.delete();
    }
  }

  public interface Listener {
    void onStatusChange(Status status);
  }

  public interface StreamListener {
    void onAudioStream();
  }

  public enum Status {
    INIT,
    START,
    PAUSE,
    STOP
  }

  private void closeQuietly(Closeable c) {
    try {
      c.close();
    } catch (Exception e) {
      // ignore
    }

  }
}
