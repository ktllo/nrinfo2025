package org.leolo.nrinfo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MiscUtil {


    public static Date parseNaptanDate(String date) throws ParseException {
        if (date == null || date.isEmpty()) {
            return null;
        } else if (date.length() == 23) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            return sdf.parse(date);
        } else if (date.length() == 19) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            return sdf.parse(date);
        } else {
            throw new ParseException("Invalid date format", -1);
        }
    }

    public static <E extends Comparable<E>> E min(E... elem) {
        if (elem == null || elem.length == 0) {
            return null;
        }
        if (elem.length == 1) {
            return elem[0];
        }
        E min = null;
        for (E e : elem) {
            if (e == null) {
                continue;
            }
            if (min == null) {
                min = e;
            } else {
                if (e.compareTo(min) < 0) {
                    min = e;
                }
            }
        }
        return min;
    }

    public static <E extends Comparable<E>> E max(E... elem) {
        if (elem == null || elem.length == 0) {
            return null;
        }
        if (elem.length == 1) {
            return elem[0];
        }
        E max = null;
        for (E e : elem) {
            if (e == null) {
                continue;
            }
            if (max == null) {
                max = e;
            } else {
                if (e.compareTo(max) > 0) {
                    max = e;
                }
            }
        }
        return max;
    }
}
