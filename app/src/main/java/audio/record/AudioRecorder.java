package audio.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.widget.LinearLayout;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AudioRecorder {

  private Status mStatus;
  private AudioRecord mRecord;

  private int mBufferSize = 0;

  public static final int SAMPLE_RATE = 44100;
  private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
  private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;

  private Executor mExecutor;

  private FileBuilder mBuilder;

  private Listener mListener;

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

  public void setListener(Listener listener) {
    mListener = listener;
  }

  public void start(FileBuilder builder) {
    if (mStatus == Status.START) {
      return;
    }
    mBuilder = builder;
    mStatus = Status.START;
    mRecord.startRecording();
    mExecutor.execute(this::processRecord);
  }

  public void pause() {
    mStatus = Status.PAUSE;
    mBuilder = null;
  }

  public void stop() {
    if (mStatus == Status.STOP) {
      return;
    }
    mStatus = Status.STOP;
    release();
  }

  private void processRecord() {
    byte[] buffer = new byte[mBufferSize];
    try {
      FileBuilder builder;
      while ((builder = mBuilder) != null && mStatus == Status.START) {
        int len = mRecord.read(buffer, 0, mBufferSize);
        builder.addAudio(buffer, len, 0, 0, 0);
      }
      mRecord.stop();
      mStatus = Status.PAUSE;
      if (mListener != null) {
        mListener.onStatusChange(mStatus);
      }
    } catch (Exception e) {
      android.util.Log.e("XXXX", "error recording ", e);
      mRecord.stop();
      mStatus = Status.PAUSE;
    }
  }

  private void release() {
    mRecord.release();
  }

  public interface Listener {
    void onStatusChange(Status status);
  }

  public enum Status {
    INIT,
    START,
    PAUSE,
    STOP
  }


}
