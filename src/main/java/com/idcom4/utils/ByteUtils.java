package com.idcom4.utils;

public class ByteUtils {
    public static void PrintBytes(byte[] bytes, int byteEncoding, boolean withAddresses) {
        for (int i = 0; i < bytes.length; i += byteEncoding) {

            int val = 0;
            for (int x = 0; x < byteEncoding; x++) {
                val = ((val << 8) | (bytes[i + x] & 0xFF));
            }

            val = val & 0xFFFF;

            if (withAddresses)
                System.out.print("[" + String.format("%04x", (i / 2) + 25).substring(0, 4) + "] ");
            System.out.print(String.format("%04x", val).substring(0, 4));

            if (i != 0 && (i + byteEncoding) % (byteEncoding * 3) == 0)
                System.out.println();
            else
                System.out.print(" ");
        }
    }

    public static void PrintShorts(short[] shorts, boolean withAddresses) {
        for (int i = 0; i < shorts.length; i += 2) {

            int val = shorts[i] & 0xFFFF;

            if (withAddresses)
                System.out.print("[" + String.format("%04x", (i / 2) + 25).substring(0, 4) + "] ");
            System.out.print(String.format("%04x", val).substring(0, 4));

            if (i != 0 && (i + 2) % 6 == 0)
                System.out.println();
            else
                System.out.print(" ");
        }
    }
}
