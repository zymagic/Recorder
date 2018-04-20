package audio.record;

import android.media.AudioFormat;

import java.io.DataOutputStream;
import java.io.IOException;

public class WavHeader {

  private short channel = 1;
  private int sampleRate = 44100;
  private short bit;
  private int pcmSize;

  public void write(DataOutputStream stream) throws IOException {
    writeString(stream, "RIFF");
    writeInt(stream, pcmSize + 36);
    writeString(stream, "WAVE");
    writeString(stream, "fmt ");
    writeInt(stream, bit);
    writeShort(stream, (short) 1); // 1 = PCM
    writeShort(stream, channel);
    writeInt(stream, sampleRate);
    writeInt(stream, sampleRate * 2);
    writeShort(stream, (short) (channel * bit / 8));
    writeShort(stream, bit);
    writeString(stream, "data");
    writeInt(stream, pcmSize);
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

  private void writeString(DataOutputStream out, String s) throws IOException {
    for (int i = 0; i < s.length(); i++) {
      out.write(s.charAt(i));
    }
  }

  private void writeInt(DataOutputStream out, int value) throws IOException {
    out.write(value >> 0);
    out.write(value >> 8);
    out.write(value >> 16);
    out.write(value >> 24);
  }

  private void writeShort(DataOutputStream out, short value) throws IOException {
    out.write(value >> 0);
    out.write(value >> 8);
  }
}
