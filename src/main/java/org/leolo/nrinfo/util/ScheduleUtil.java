package org.leolo.nrinfo.util;

import java.sql.Time;
import java.time.Duration;
import java.util.ArrayList;

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
    public static String pettyFormatDaysRunsMasks(String mask) {
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

        ArrayList<String> runs = new ArrayList<>();
        ArrayList<String> notRuns = new ArrayList<>();
        char[] chars = mask.toCharArray();
        (chars[0] == '0' ? notRuns : runs).add("Monday");
        (chars[1] == '0' ? notRuns : runs).add("Tuesday");
        (chars[2] == '0' ? notRuns : runs).add("Wednesday");
        (chars[3] == '0' ? notRuns : runs).add("Thursday");
        (chars[4] == '0' ? notRuns : runs).add("Friday");
        (chars[5] == '0' ? notRuns : runs).add("Saturday");
        (chars[6] == '0' ? notRuns : runs).add("Sunday");
        for (char c : chars) {
            if (c == '1') {
                runsCount++;
            }
        }
        if (runsCount == 0) {
            return "Never runs";
        } else if (runsCount == 1) {
            //Only runs on 1 day
            return "Runs on " + runs.getFirst() + " only";
        } else if (runsCount < 4) {
            return "Runs on " + MiscUtil.formatList(runs, ", ", " and ") + " only";
        } else if (runsCount == 6) {
            //Only not runs on 1 day
            return "Runs except " + notRuns.getFirst() + " only";
        } else if (runsCount == 7) {
            return "Runs everyday";
        } else {
            return "Runs except " + MiscUtil.formatList(notRuns, ", ", " and ") + " only";
        }
    }

    public static Duration parseDuration(String time) {
        if (time == null || time.isEmpty()) {
            return null;
        }
        if (time.length() < 4) {
            throw new IllegalArgumentException("Invalid time format: " + time);
        }
        int hour = parseInt(time.substring(0, 2));
        int minute = parseInt(time.substring(2, 4));
        int second = time.length() >=5 && 'H' == time.charAt(4) ? 30: 0;

        return Duration.ofHours(hour).plusMinutes(minute).plusSeconds(second);
    }

    public static Duration parseAllowanceDuration(String time) {
        if (time == null || time.isBlank()) {
            return null;
        }
        if (time.length() > 2) {
            throw new IllegalArgumentException("Invalid time format: " + time);
        }
        time = time.strip();
        if ("H".equals(time)) {
            return Duration.ofSeconds(30);
        }
        int minute = parseInt(time.substring(0, 1));
        if (time.endsWith("H")) {
            return Duration.ofMinutes(minute).plusSeconds(30);
        }
        return Duration.ofMinutes(minute);
    }

    public static int parseInt(String str) {
        if (str == null || str.isEmpty()) {
            return 0;
        }
        if (str.startsWith("-")) {
            return parseInt(str.substring(1)) * -1;
        }
        if (str.startsWith("0")) {
            return parseInt(str.substring(1));
        }
        return Integer.parseInt(str);
    }
}
