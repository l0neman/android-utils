package io.l0neman.utils.stay.reflect.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Created by l0neman on 2019/05/31.
 */
public class IOUtils {

  public static String read(File file, String csName) throws IOException {
    Closer closer = Closer.create();
    try {
      final BufferedReader reader = closer.register(
          new BufferedReader(Channels.newReader(new FileInputStream(file).getChannel(), csName))
      );

      String line;
      StringBuilder sb = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
      return sb.toString();
    } finally {
      closer.close();
    }
  }

  public static void write(File file, byte[] bytes) throws IOException {
    Closer closer = Closer.create();
    try {
      FileChannel channel = closer.register(new FileOutputStream(file).getChannel());
      channel.write((ByteBuffer) ByteBuffer.allocate(bytes.length)
          .put(bytes)
          .flip());
    } finally {
      closer.close();
    }
  }

  public interface LineCallback {
    void onLine(String line);
  }

  public static void readLines(File file, String csName, LineCallback callback) throws IOException {
    Closer closer = Closer.create();
    try {
      BufferedReader br = closer.register(
          new BufferedReader(Channels.newReader(
              new FileInputStream(file).getChannel(), csName
          ))
      );
      String line;
      while ((line = br.readLine()) != null) {
        callback.onLine(line);
      }
    } finally {
      closer.close();
    }
  }

  public static void write(File file, String text, String csName) throws IOException {
    Closer closer = Closer.create();
    try {
      BufferedWriter br = closer.register(
          new BufferedWriter(Channels.newWriter(new FileOutputStream(file).getChannel(), csName))
      );
      br.write(text);
      br.flush();
    } finally {
      closer.close();
    }
  }

  public static void append(File file, String text, String csName) throws IOException {
    Closer closer = Closer.create();
    try {
      BufferedWriter br = closer.register(
          new BufferedWriter(Channels.newWriter(
              new FileOutputStream(file, true).getChannel(), csName))
      );
      br.write(text);
      br.flush();
    } finally {
      closer.close();
    }
  }

  public static void transfer(File src, File dest) throws IOException {
    Closer closer = Closer.create();
    try {
      FileChannel in = closer.register(new FileInputStream(src).getChannel());
      FileChannel out = closer.register(new FileInputStream(dest).getChannel());

      in.transferTo(0, in.size(), out);
    } finally {
      closer.close();
    }
  }

  public static void transfer(InputStream in, OutputStream out, boolean isClose) throws IOException {
    Closer closer = null;
    if (isClose) {
      closer = Closer.create();
    }
    try {
      final ReadableByteChannel ic = Channels.newChannel(in);
      ReadableByteChannel inChannel = isClose ? closer.register(ic) : ic;
      final WritableByteChannel oc = Channels.newChannel(out);
      WritableByteChannel outChannel = isClose ? closer.register(oc) : oc;
      ByteBuffer buffer = ByteBuffer.allocate(1024);
      while (inChannel.read(buffer) != -1) {
        buffer.flip();
        outChannel.write(buffer);
        buffer.clear();
      }
    } finally {
      if (isClose) {
        closer.close();
      }
    }
  }
}
