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
}
