package audio.record;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.SparseIntArray;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class AACEncoder {

  MediaCodec mEncoder;
  ByteBuffer[] mInputBuffers;
  ByteBuffer[] mOutputBuffers;
  MediaCodec.BufferInfo mBufferInfo;

  private int mSampleRate, mChannels;

  private static final String MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AAC;
  private static final int AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
  private static final SparseIntArray SAMPLE_RATES = new SparseIntArray();
  private static final int[] BIT_RATES = {64000, 96000, 128000};

  int mState;

  public AACEncoder(int sampleRate, int channels) {
    mSampleRate = sampleRate;
    mChannels = channels;
    try {
      mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
      MediaFormat format = MediaFormat.createAudioFormat(MIME_TYPE, mSampleRate, 1);
      format.setString(MediaFormat.KEY_MIME, MIME_TYPE);
      format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channels);
      format.setInteger(MediaFormat.KEY_AAC_PROFILE, AAC_PROFILE);
      format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATES[2]);
      format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 8192);
      mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
      mEncoder.start();
      mInputBuffers = mEncoder.getInputBuffers();
      mOutputBuffers = mEncoder.getOutputBuffers();
      mBufferInfo = new MediaCodec.BufferInfo();
      mState = 0;
    } catch (Exception e) {
      mState = 1;
    }
  }

  public int getState() {
    return mState;
  }

  private void checkState() {
    if (mState != 0) {
      throw new IllegalStateException("failed to init encoder");
    }
  }

  public void encode(byte[] data, int offset, int len, OutputStream os) throws IOException {
    checkState();

    int index = mEncoder.dequeueInputBuffer(-1);
    ByteBuffer input = mInputBuffers[index];
    input.clear();
    input.put(data, offset, len);
    input.limit(len);
    mEncoder.queueInputBuffer(index, 0, len, 0, 0);

    int outerIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, 0);
    while (outerIndex >= 0) {
      int size = mBufferInfo.size;
      int adtsSize = size + 7;
      ByteBuffer output = mOutputBuffers[outerIndex];
      output.position(mBufferInfo.offset);
      output.limit(size + mBufferInfo.offset);

      byte[] outData = new byte[adtsSize];
      writeADTSHeader(outData, adtsSize);

      output.get(outData, 7, size);
      output.position(mBufferInfo.offset);

      os.write(outData);

      mEncoder.releaseOutputBuffer(outerIndex, false);

      outerIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, 0);
    }
  }

  private void writeADTSHeader(byte[] data, int size) {
    int si = SAMPLE_RATES.get(mSampleRate);
    int ch = mChannels;
    int pr = AAC_PROFILE;
    data[0] = (byte) 0xFF;
    data[1] = (byte) 0xF9;
    data[2] = (byte) (((pr - 1) << 6) + (si << 2) + (ch >> 2));
    data[3] = (byte) (((ch & 3) << 6) + (size >> 11));
    data[4] = (byte) ((size & 0x7FF) >> 3);
    data[5] = (byte) (((size & 7) << 5) + 0x1F);
    data[6] = (byte) 0xFC;
  }

  static {
    SAMPLE_RATES.put(96000, 0);
    SAMPLE_RATES.put(88200, 1);
    SAMPLE_RATES.put(64000, 2);
    SAMPLE_RATES.put(48000, 3);
    SAMPLE_RATES.put(44100, 4);
    SAMPLE_RATES.put(32000, 5);
    SAMPLE_RATES.put(24000, 6);
    SAMPLE_RATES.put(22050, 7);
    SAMPLE_RATES.put(16000, 8);
    SAMPLE_RATES.put(12000, 9);
    SAMPLE_RATES.put(11025, 10);
    SAMPLE_RATES.put(8000, 11);
    SAMPLE_RATES.put(7350, 12);
  }
}
