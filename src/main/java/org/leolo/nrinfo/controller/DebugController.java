package org.leolo.nrinfo.controller;

import org.leolo.nrinfo.service.CounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DebugController {

    @Autowired private CounterService counterService;

    @RequestMapping("/debug/counter")
    public ResponseEntity counter () {
        return ResponseEntity.ok(Map.of("status","ok","value", Integer.toString(counterService.getCounter("TEST"))));
    }

}
