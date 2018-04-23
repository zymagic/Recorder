package audio.record;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SoundBuilder implements FileBuilder {

  private AACEncoder mEncoder;
  private File mFile;
  private FileOutputStream fos;

  public SoundBuilder(File outFile, int sampleRate, int channels) throws IOException {
    mFile = outFile;
    mEncoder = new AACEncoder(sampleRate, channels);
    prepareFile();
  }

  private void prepareFile() throws IOException {
    if (!mFile.getParentFile().exists()) {
      mFile.getParentFile().mkdirs();
    }
    if (!mFile.exists()) {
      mFile.createNewFile();
    }
    fos = new FileOutputStream(mFile);
  }

  @Override
  public boolean addAudio(byte[] data, int length, int audioFormat, int channel, int sampleRate) {
    if (fos == null) {
      return false;
    }
    try {
      mEncoder.encode(data, 0, length, fos);
    } catch (Exception e) {
      android.util.Log.e("XXXX", "error encode", e);
    }
    return false;
  }

  @Override
  public void finish() throws IOException {
    if (fos != null) {
      fos.flush();
      fos.close();
    }
  }

  @Override
  public void cancel() {
    try {
      if (fos != null) {
        fos.close();
      }
    } catch (Exception e) {
      // ignore
    }
  }
}
