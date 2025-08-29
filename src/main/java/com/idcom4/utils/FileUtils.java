package com.idcom4.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {

    public static String ReadFile(String fileName) {
        try {
            return Files.readString(Paths.get(fileName), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void WriteFile(String fileName, String content) {
        try {
            Files.writeString(Paths.get(fileName), content, Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] ReadFileBytes(String fileName) {
        try (DataInputStream in = new DataInputStream(new FileInputStream(fileName))) {

            // read byte encoding
            int byteEncoding = in.readByte();

            // read data length
            int length = in.readInt() * byteEncoding;

            byte[] data = new byte[length];
            for (int i = 0; i < length; i++) {
                data[i] = in.readByte();
            }

            return data;
        }  catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void WriteFileBytes(String fileName, byte[] data, int byteEncoding) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName))) {
            // write byte encoding
            out.writeByte(byteEncoding);

            // prefix with data length
            out.writeInt(data.length / byteEncoding);

            for (byte value : data) {
                out.writeByte(value);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static short[] ReadFileShorts(String fileName) {
        try (DataInputStream in = new DataInputStream(new FileInputStream(fileName))) {
            // read byte encoding
            byte byteEncoding = in.readByte(); // should be 2
            if (byteEncoding != 2)
                throw new Exception("Invalid byte encoding: " + byteEncoding + " (should be 2)");

            // read data length
            int length = in.readInt();

            short[] data = new short[length];
            for (int i = 0; i < length; i++) {
                data[i] = in.readShort();
            }

            return data;
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void WriteFileShorts(String fileName, short[] data) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName))) {
            // write byte encoding
            out.writeByte(2); // 2 bytes = 1 value
            // prefix with data length
            out.writeInt(data.length);

            for (short value : data) {
                out.writeShort(value);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
