package io.l0neman.utilstest.general.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.l0neman.utils.general.io.Closer;

public class CloserTest {

  public void test() {
    Closer closer = Closer.create();
    try {
      InputStream is = closer.register(
          new FileInputStream("input"));

      OutputStream os = closer.register(
          new FileOutputStream("output"));

      byte[] buffer = new byte[1024];
      while ((is.read(buffer)) != 0) {
        os.write(buffer);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      closer.close();
    }
  }
}
