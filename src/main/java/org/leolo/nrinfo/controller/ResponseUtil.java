package org.leolo.nrinfo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
    public static ResponseEntity<Map<String, String>> buildNotFoundResponse() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status","error","message","The requested resource does not exist"));
    }
    public static ResponseEntity<Map<String, String>> buildNotImplementedResponse() {
        return  ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of("status","error","message","The requested resource is not implemented"));
    }
    public static ResponseEntity<Map<String, String>> buildFullErrorResponse(String error, String details) {
        logger.warn("REQ ERROR {}: {}", error, details);
        return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status","error","message",error,"details",details));
    }

}
