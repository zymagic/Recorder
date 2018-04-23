package audio.record;

import java.io.IOException;

public interface FileBuilder {

  boolean addAudio(byte[] data, int length, int audioFormat, int channel, int sampleRate);

  void finish() throws IOException;

  void cancel();
}
