package audio.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
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
  private List<Long> mFrags = new ArrayList<>();

  private Status mStatus;
  private AudioRecord mRecord;
  private AACEncoder mEncoder;
  private long mMaxTimeInMillis = -1;

  private int mBufferSize = 0;

  private static final int SAMPLE_RATE = 44100;
  private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
  private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;

  private Executor mExecutor;
  private Handler mHandler = new Handler(Looper.getMainLooper());
  private Listener mListener;

  public AudioRecorder() {
    mBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, FORMAT);
    mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, FORMAT, mBufferSize);
    mEncoder = new AACEncoder(SAMPLE_RATE, 1);
    int state = mRecord.getState();
    if (state == AudioRecord.STATE_INITIALIZED && mEncoder.getState() == 0) {
      mStatus = Status.INIT;
    } else {

    }
    mExecutor = Executors.newSingleThreadExecutor();
  }

  public void setListener(Listener listener) {
    mListener = listener;
    if (listener != null) {
      listener.onStatusChange(mStatus);
    }
  }

  public void setMaxTime(long time) {
    mMaxTimeInMillis = time;
  }

  public void setFile(String dir, String baseName) {
    mFolder = dir;
    mBaseName = baseName;
  }

  public void start() {
    if (mStatus == Status.START) {
      return;
    }
    mStatus = Status.START;
    if (mListener != null) {
      mListener.onStatusChange(mStatus);
    }
    mRecord.startRecording();
    mExecutor.execute(this::processRecord);
  }

  public void pause() {
    if (mStatus != Status.START) {
      return;
    }
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
    if (mListener != null) {
      mListener.onPieceDeleted(mFrags.size());
    }
  }

  public void abort() {
    if (mStatus == Status.STOP) {
      return;
    }
    mStatus = Status.STOP;
    if (mListener != null) {
      mListener.onStatusChange(mStatus);
    }
    mExecutor.execute(this::release);
  }

  public void finish() {
    if (mStatus == Status.STOP) {
      return;
    }
    mStatus = Status.STOP;
    if (mListener != null) {
      mListener.onStatusChange(mStatus);
    }
    mExecutor.execute(this::compose);
  }

  private void processRecord() {
    String fileName = mBaseName + "_" + (mFrags.size() + 1);
    File folder = new File(mFolder);
    File currentFile = new File(folder, fileName);
    byte[] buffer = new byte[mBufferSize];
    long timeRemain = mMaxTimeInMillis;
    long start = System.currentTimeMillis();
    long timeLength = 0;
    FileOutputStream fos = null;
    try {
      if (!folder.exists()) {
        boolean createDir = folder.mkdirs();
        android.util.Log.e("XXXX", "mkdir " + createDir + ", " + folder);
      }
      if (!currentFile.exists()) {
        currentFile.createNewFile();
      }
      fos = new FileOutputStream(currentFile);
      while (mStatus == Status.START
          && (timeRemain == -1 || System.currentTimeMillis() - start < timeRemain)) {
        int len = mRecord.read(buffer, 0, mBufferSize);
        mEncoder.encode(buffer, 0, len, fos);
        timeLength = System.currentTimeMillis() - start;
        if (mListener != null) {
          final long currentLength = timeLength;
          mHandler.post(() -> mListener.onPieceRecord(currentLength));
        }
      }
      if (mMaxTimeInMillis > 0) {
        mMaxTimeInMillis = Math.max(0, mMaxTimeInMillis - timeLength);
      }
      fos.flush();
      fos.close();
      mRecord.stop();
      mStatus = Status.PAUSE;
      if (timeLength != 0) {
        mFrags.add(timeLength);
      }
      if (mListener != null) {
        mHandler.post(() -> {
          if (mListener != null) {
            mListener.onStatusChange(mStatus);
            if (mFrags.size() > 0) {
              mListener.onPieceAdded(mFrags.size(), mFrags.get(mFrags.size() - 1));
            }
          }
        });
      }
    } catch (Exception e) {
      android.util.Log.e("XXXX", "error recording ", e);
      mRecord.stop();
      mStatus = Status.PAUSE;
      currentFile.delete();
      mHandler.post(() -> {
        if (mListener != null) {
          mListener.onStatusChange(mStatus);
        }
      });
    } finally {
      closeQuietly(fos);
    }
  }

  private void compose() {
    ArrayList<File> files = new ArrayList<>(mFrags.size());
//    WavHeader header = new WavHeader();
//    header.setChannels(1).setEncode(FORMAT);
    for (int i = 0; i < mFrags.size(); i++) {
      File file = new File(mFolder, mBaseName + "_" + (i + 1));
//      header.appendFileLength((int) file.length());
      files.add(file);
    }

    final File outputFile = new File(mFolder, mBaseName);
    DataOutputStream dos = null;
    try {
       dos = new DataOutputStream(new FileOutputStream(outputFile));
//       header.write(dos);
       byte[] buffer = new byte[4096];
       for (File file : files) {
         appendFile(file, dos, buffer);
       }
       dos.flush();
       dos.close();
       if (mListener != null) {
         mHandler.post(() -> mListener.onComposed(outputFile));
       }
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
      while ((len = fis.read(buffer)) != -1) {
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
    void onPieceRecord(long length);
    void onPieceAdded(int count, long length);
    void onPieceDeleted(int remain);
    void onComposed(File file);
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
