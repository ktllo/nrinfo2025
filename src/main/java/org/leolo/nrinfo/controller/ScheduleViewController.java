package org.leolo.nrinfo.controller;

import org.leolo.nrinfo.dto.response.TrainScheduleSummary;
import org.leolo.nrinfo.model.Schedule;
import org.leolo.nrinfo.model.ScheduleDetail;
import org.leolo.nrinfo.model.Tiploc;
import org.leolo.nrinfo.model.TrainCategory;
import org.leolo.nrinfo.service.ScheduleService;
import org.leolo.nrinfo.service.TiplocService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleViewController {

    private static Logger log = LoggerFactory.getLogger(ScheduleViewController.class);

    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private TiplocService tiplocService;

    @GetMapping("/{uid}/{date}/summary")
    public ResponseEntity<?> getScheduleSummaryByDate(
            @PathVariable("uid") String uid,
            @PathVariable("date") String date
    ) {
        log.info("Schedule for {} on {} is requested", uid, date);
        //Parse the date
        Date parsedDate = null;
        try {
            parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            //Bad input
            return ResponseUtil.buildBadRequestResponse("Invalid date format");
        }
        UUID scheduleUUID = null;
        try {
            scheduleUUID = scheduleService.getScheduleUUID(uid, parsedDate);
        } catch (SQLException e) {
            log.error("Error while getting schedule UUID - {}", e.getMessage(), e);
            return ResponseUtil.buildFullErrorResponse("Database error", "There are issue retriving data. Try again later.");
        }
        if (scheduleUUID == null) {
            //No schedule is available
            return ResponseUtil.buildNotFoundResponse();
        }
        Schedule schedule = null;
        try {
            schedule = scheduleService.getScheduleByUUID(scheduleUUID);
        } catch (SQLException e) {
            log.error("Error while getting schedule UUID - {}", e.getMessage(), e);
            return ResponseUtil.buildFullErrorResponse("Database error", "There are issue retriving data. Try again later.");
        }
        if (Objects.equals(schedule.getStpIndicator(), "C")) {
            //The train has been cancelled
            return ResponseUtil.buildNotFoundResponse("Train cancelled by overlay");
        }
        TrainScheduleSummary trainSchedule = new TrainScheduleSummary();
        TreeSet<String> tiplocs = new TreeSet<>();
        for (ScheduleDetail sd: schedule.getDetailList()) {
            tiplocs.add(sd.getLocation());
        }
        Map<String, Tiploc> locations = tiplocService.getTiplocsByTiplocCodes(tiplocs);
        trainSchedule.setTrainUid(schedule.getTrainUid());

        try {
            if (schedule.getOperator()==null) {
                //Try to get it from base schedule
                Schedule baseSchedule = null;
                    baseSchedule = scheduleService.getScheduleByUUID(scheduleService.getBaseScheduleUUID(uid, parsedDate));

                if (baseSchedule == null || baseSchedule.getOperator() == null) {
                    trainSchedule.setTrainOperator("Unknown");
                } else {
                    log.debug("No TOC info for applicable schedule but found operator {} in base schedule", baseSchedule.getOperator());
                    trainSchedule.setTrainOperator(scheduleService.getTrainOperatorName(baseSchedule.getOperator()));
                }
            } else {
                trainSchedule.setTrainOperator(scheduleService.getTrainOperatorName(schedule.getOperator()));
            }
        } catch (SQLException e) {
            log.error("Error while getting schedule UUID - {}", e.getMessage(), e);
            return ResponseUtil.buildFullErrorResponse("Database error", "There are issue retriving data. Try again later.");
        }
        trainSchedule.setTrainType(TrainCategory.getTrainCategory(schedule.getTrainCategory()).getDisplayName());
        SimpleDateFormat fullTime = new SimpleDateFormat("HH:mm:ss");
        if (!tiplocs.isEmpty()) {
            ScheduleDetail firstLocation = schedule.getDetailList().getFirst();
            ScheduleDetail lastLocation = schedule.getDetailList().getLast();
            trainSchedule.setOrigin(locations.get(firstLocation.getLocation()).getDescription());
            trainSchedule.setDestination(locations.get(lastLocation.getLocation()).getDescription());
            if (firstLocation.getPublicDepartureTime() == null) {
                trainSchedule.setDepartureTime(fullTime.format(firstLocation.getDepartureTime()));
            } else {
                trainSchedule.setDepartureTime(fullTime.format(firstLocation.getPublicDepartureTime()));
            }
            if (lastLocation.getPublicArrivalTime() == null) {
                trainSchedule.setArrivalTime(fullTime.format(lastLocation.getArrivalTime()));
            } else {
                trainSchedule.setArrivalTime(fullTime.format(lastLocation.getPublicArrivalTime()));
            }
        }
        return ResponseEntity.ok(Map.of("result","success", "schedule", trainSchedule));
    }

}
