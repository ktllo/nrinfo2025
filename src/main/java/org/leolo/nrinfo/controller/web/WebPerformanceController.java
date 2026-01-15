package org.leolo.nrinfo.controller.web;

import org.leolo.nrinfo.dto.response.PerformanceData;
import org.leolo.nrinfo.model.PerformanceEntry;
import org.leolo.nrinfo.model.RealTimePerformanceSnapshot;
import org.leolo.nrinfo.service.RealTimePerformanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Duration;
import java.time.Instant;

@Controller
public class WebPerformanceController {

    private Logger logger = LoggerFactory.getLogger(WebPerformanceController.class);

    @Autowired
    public RealTimePerformanceService realTimePerformanceService;

    @GetMapping({"/performance","/performance/"})
    public String performance(Model model) {
        logger.info("national performance requested");
        RealTimePerformanceSnapshot snapshot = realTimePerformanceService.getSnapshot();
        if (snapshot == null) {
            logger.warn("No real time performance snapshot found");
            model.addAttribute("message","No data received yet");
            return "error";
        }
        PerformanceData pd = realTimePerformanceService.getNationalPerformanceData(snapshot);
        model.addAttribute("type", "national");
        model.addAttribute("title", "Real time performance");
        model.addAttribute("main_subtitle", "National Summary");
        model.addAttribute("data", pd);
        model.addAttribute("ss_time", snapshot.getSnapshotTime());
        model.addAttribute("elapse_time", Duration.between(snapshot.getSnapshotTime(), Instant.now()));
        return "performance";
    }

    @GetMapping("/performance/operator/{opc}")
    public String performanceOperator(@PathVariable String opc, Model model) {
        logger.info("Operator {} performance requested", opc);
        RealTimePerformanceSnapshot snapshot = realTimePerformanceService.getSnapshot();
        if (snapshot == null) {
            logger.warn("No real time performance snapshot found");
            model.addAttribute("message","No data received yet");
            return "error";
        }
        PerformanceEntry pe = snapshot.getOperatorDetails().get(opc);
        if (pe == null) {
            logger.warn("Operator {} not found", opc);
            model.addAttribute("message","Operator not found");
            return "error";
        }
        model.addAttribute("type", "operator");
        model.addAttribute("message","Not implemented yet");
        model.addAttribute("title", "Real time performance for "+pe.getName());
        model.addAttribute("main_subtitle", "Summary for "+pe.getName());
        model.addAttribute("data", realTimePerformanceService.getOperatorData(pe));
        model.addAttribute("ss_time", snapshot.getSnapshotTime());
        model.addAttribute("elapse_time", Duration.between(snapshot.getSnapshotTime(), Instant.now()));
        return "performance";

    }
}
