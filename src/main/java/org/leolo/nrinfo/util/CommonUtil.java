package org.leolo.nrinfo.util;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

public class CommonUtil {


    private static com.fasterxml.uuid.NoArgGenerator UUIDGenerators = null;
    public static byte[] uuidToBytes(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        byte[] bytes = new byte[16];
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (msb >>> (8 * (7 - i)));
            bytes[8 + i] = (byte) (lsb >>> (8 * (7 - i)));
        }
        return bytes;
    }

    public static UUID bytesToUUID(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        long msb = 0, lsb = 0;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (bytes[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (bytes[i] & 0xff);
        }
        return new UUID(msb, lsb);
    }

    public static UUID generateUUID() {
        if (UUIDGenerators == null) {
            UUIDGenerators = com.fasterxml.uuid.Generators.timeBasedEpochGenerator();
        }
        return UUIDGenerators.generate();
    }

    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean capitalize = true;
        for (char ch: s.toCharArray()) {
            if (capitalize) {
                sb.append(Character.toUpperCase(ch));
            } else {
                sb.append(Character.toLowerCase(ch));
            }
            capitalize = switch (ch) {
                case '(', ')', '.', '&' -> true;
                default -> Character.isWhitespace(ch);
            };
        }
        return sb.toString();
    }

    public static String formatTime(SimpleDateFormat format, Date time) {
        if (format == null || time == null) {
            return null;
        }
        return format.format(time);
    }

    public static String formatTime(Duration duration, boolean hourParts) {
        if (duration == null) {
            return null;
        }
        if (hourParts) {
            return String.format("%02d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        } else {
            return String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
        }
    }
    public static String formatTime(Date baseDate,Duration duration, boolean hourParts) {
        if (baseDate == null || duration == null) {
            return null;
        }
        if (baseDate instanceof java.sql.Date) {
            baseDate = new Date(baseDate.getTime());
        }
        if (hourParts && duration.compareTo(Duration.ofDays(1)) >= 0) {
            baseDate = Date.from(baseDate.toInstant().plus(1, ChronoUnit.DAYS));
        }
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(baseDate);
        if (hourParts) {
            return formattedDate+String.format(" %02d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        } else {
            return formattedDate+String.format(" %02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
        }
    }


}
