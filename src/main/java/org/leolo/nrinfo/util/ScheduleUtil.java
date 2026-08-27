package org.leolo.nrinfo.util;

import java.sql.Time;

public class ScheduleUtil {
    public static Time parseTime(String time) {
        if (time == null || time.isEmpty()) {
            return null;
        }
        if (time.length() < 4) {
            throw new IllegalArgumentException("Invalid time format: " + time);
        }
        String hour = time.substring(0, 2);
        String minute = time.substring(2, 4);
        String second = time.length() >=5 && 'H' == time.charAt(4) ? "30": "00";

        return Time.valueOf(hour + ":" + minute + ":" + second);
    }

    public static Time parseAllowance(String time) {
        if (time == null || time.isBlank()) {
            return null;
        }
        if (time.length() > 2) {
            throw new IllegalArgumentException("Invalid time format: " + time);
        }
        time = time.strip();
        if ("H".equals(time)) {
            return Time.valueOf("00:00:30");
        }
        if (time.endsWith("H")) {
            String minute = time.substring(0, 1);
            return Time.valueOf("00:" + minute+":30");
        }
        return Time.valueOf("00:"+time+":00");
    }

    public static String formatDaysRunsMasks(String mask) {
        if (mask == null || mask.length() != 7) {
            throw new IllegalArgumentException("Invalid mask: " + mask);
        }
        int runsCount = 0;
        /*
         * Mask format:
         * A seven-character field; character 1 represents Monday, character 7 represents Sunday.
         * A 1 in a character position means that the service runs on that day, while a 0 means that it does not.
         *
         * Note that in this comment, the character index is 1-index
         */
        StringBuilder runs = new StringBuilder();
        StringBuilder notRuns = new StringBuilder();
        char[] chars = mask.toCharArray();
        (chars[0] == '0' ? notRuns : runs).append("M");
        (chars[1] == '0' ? notRuns : runs).append("Tu");
        (chars[2] == '0' ? notRuns : runs).append("W");
        (chars[3] == '0' ? notRuns : runs).append("Th");
        (chars[4] == '0' ? notRuns : runs).append("F");
        (chars[5] == '0' ? notRuns : runs).append("S");
        (chars[6] == '0' ? notRuns : runs).append("Su");
        for (char c : chars) {
            if (c == '1') {
                runsCount++;
            }
        }
        if (runsCount >= 4) {
           notRuns.append("X");
           return notRuns.toString();
        } else {
            runs.append("O");
            return runs.toString();
        }
    }
}
