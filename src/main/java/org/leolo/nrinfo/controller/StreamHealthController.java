package org.leolo.nrinfo.controller;

import org.leolo.nrinfo.service.DataStreamHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health/stream")
public class StreamHealthController {

    @Autowired private DataStreamHealthService dataStreamHealthService;


    @RequestMapping("")
    public ResponseEntity getStreamHealth() {
        return ResponseEntity.ok().body(Map.of("result","success","data", dataStreamHealthService.getStreamHealth()));
    }


}
