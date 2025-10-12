package org.leolo.nrinfo.util;

import java.util.Random;

public class RandomUtil {

    private RandomUtil() {
        //Preventing creating an instance
    }

    private static final Random RANDOM = new Random();

    public static final char[] READABLE_CHARACTERS = "23456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public static String getReadableString(int length) {
        char[] dst = new char[length];
        for (int i = 0; i < length; i++) {
            dst[i] = READABLE_CHARACTERS[RANDOM.nextInt(READABLE_CHARACTERS.length)];
        }
        return new String(dst);
    }
}
