package audio.record;

import android.media.AudioFormat;

import java.io.DataOutputStream;
import java.io.IOException;

public class WavHeader {

  private short channel = 1;
  private int sampleRate = 44100;
  private short bit;
  private int pcmSize;

  byte[] header = new byte[44];
  int index;

  public void write(DataOutputStream stream) throws IOException {
    android.util.Log.e("XXXX", "write " + pcmSize + ", " + bit + ", " + channel + ", " + sampleRate);
    writeString("RIFF");
    writeInt(pcmSize + 36);
    writeString("WAVE");
    writeString("fmt ");
    writeInt(bit);
    writeShort((short) 1); // 1 = PCM
    writeShort(channel);
    writeInt(sampleRate);
    writeInt(sampleRate * channel * bit / 8);
    writeShort((short) (channel * bit / 8));
    writeShort(bit);
    writeString("data");
    writeInt(pcmSize);
    stream.write(header);
  }

  // 8 or 16
  public WavHeader setEncode(int pcmFormat) {
    if (pcmFormat == AudioFormat.ENCODING_PCM_8BIT) {
      bit = 8;
    } else if (pcmFormat == AudioFormat.ENCODING_PCM_16BIT) {
      bit = 16;
    }
    return this;
  }

  public WavHeader setChannels(int channels) {
    channel = (short) channels;
    return this;
  }

  public WavHeader setFileLength(int length) {
    pcmSize = length;
    return this;
  }

  public WavHeader appendFileLength(int length) {
    pcmSize += length;
    return this;
  }

  private void writeString(String s) {
    for (int i = 0; i < 4; i++) {
      header[index + i] = (byte) s.charAt(i);
    }
    index += 4;
  }

  private void writeInt(int value) {
    header[index] = (byte) (value & 0xff);
    header[index + 1] = (byte) ((value >> 8) & 0xff);
    header[index + 2] = (byte) ((value >> 16) & 0xff);
    header[index + 3] = (byte) ((value >> 24) & 0xff);
    index += 4;
  }

  private void writeShort(short value) {
    header[index] = (byte) (value & 0xff);
    header[index + 1] = (byte) ((value >> 8) & 0xff);
    index += 2;
  }
}
