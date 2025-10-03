package org.leolo.nrinfo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

public class ResponseUtil {

    private static Logger logger = LoggerFactory.getLogger("resp");

    public static Object buildErrorResponse(String error, String details) {
        logger.warn("REQ ERROR {}: {}", error, details);
        TreeMap<String, String> map = new TreeMap<>();
        map.put("error", error);
        map.put("details",details);
        return map;
    }

}
