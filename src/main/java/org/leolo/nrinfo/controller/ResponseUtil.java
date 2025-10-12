package org.leolo.nrinfo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.TreeMap;

public class ResponseUtil {

    private static Logger logger = LoggerFactory.getLogger("resp");

    @Deprecated
    public static Object buildErrorResponse(String error, String details) {
        logger.warn("REQ ERROR {}: {}", error, details);
        TreeMap<String, String> map = new TreeMap<>();
        map.put("error", error);
        map.put("details",details);
        return map;
    }

    public static ResponseEntity<Map<String, String>> buildUnauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("status","error","message","You must login to use this API"));
    }
    public static ResponseEntity<Map<String, String>> buildForbiddenResponse() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("status","error","message","You do not have permission to access this resource"));
    }

}
