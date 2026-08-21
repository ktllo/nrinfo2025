package org.leolo.nrinfo.controller;

import org.leolo.nrinfo.dto.response.TrainSchedule;
import org.leolo.nrinfo.dto.response.TrainScheduleEntry;
import org.leolo.nrinfo.dto.response.TrainScheduleSummary;
import org.leolo.nrinfo.enums.OperatingCharacteristic;
import org.leolo.nrinfo.enums.PowerType;
import org.leolo.nrinfo.model.Schedule;
import org.leolo.nrinfo.model.ScheduleAssociation;
import org.leolo.nrinfo.model.ScheduleDetail;
import org.leolo.nrinfo.model.Tiploc;
import org.leolo.nrinfo.enums.TrainCategory;
import org.leolo.nrinfo.service.ScheduleService;
import org.leolo.nrinfo.service.TiplocService;
import org.leolo.nrinfo.util.ScheduleUtil;
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
        TrainScheduleSummary trainSchedule = null;
        try {
            trainSchedule = fillSummary(schedule, parsedDate);
        } catch (SQLException e) {
            log.error("Error while getting schedule UUID - {}", e.getMessage(), e);
            return ResponseUtil.buildFullErrorResponse("Database error", "There are issue retriving data. Try again later.");
        }
        return ResponseEntity.ok(Map.of("result","success", "schedule", trainSchedule));
    }

    private TrainScheduleSummary fillSummary(Schedule schedule, Date parsedDate) throws SQLException {
        TrainScheduleSummary trainSchedule = new TrainScheduleSummary();
        TreeSet<String> tiplocs = new TreeSet<>();
        for (ScheduleDetail sd: schedule.getDetailList()) {
            tiplocs.add(sd.getLocation());
        }
        Map<String, Tiploc> locations = tiplocService.getTiplocsByTiplocCodes(tiplocs);
        trainSchedule.setTrainUid(schedule.getTrainUid());


        if (schedule.getOperator()==null) {
            //Try to get it from base schedule
            Schedule baseSchedule = null;
            baseSchedule = scheduleService.getScheduleByUUID(scheduleService.getBaseScheduleUUID(schedule.getTrainUid(), parsedDate));

            if (baseSchedule == null || baseSchedule.getOperator() == null) {
                trainSchedule.setTrainOperator("Unknown");
            } else {
                log.debug("No TOC info for applicable schedule but found operator {} in base schedule", baseSchedule.getOperator());
                trainSchedule.setTrainOperator(scheduleService.getTrainOperatorName(baseSchedule.getOperator()));
            }
        } else {
            trainSchedule.setTrainOperator(scheduleService.getTrainOperatorName(schedule.getOperator()));
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
        return trainSchedule;
    }

    @GetMapping("{uid}/{date}")
    public ResponseEntity<?> getScheduleByDate(
            @PathVariable("uid") String uid,
            @PathVariable("date") String date
    )  throws SQLException, ParseException {
        log.info("Full schedule for {} on {} is requested", uid, date);
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
        SimpleDateFormat dfDateOnly = new SimpleDateFormat("yyyy-MM-dd");
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
        log.debug("Displaying schedule {}", scheduleUUID);
        TrainSchedule trainSchedule = new TrainSchedule();
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
        trainSchedule.setScheduleStartDate(dfDateOnly.format(schedule.getStartDate()));
        trainSchedule.setScheduleEndDate(dfDateOnly.format(schedule.getEndDate()));
        trainSchedule.setScheduleDayRuns(ScheduleUtil.formatDaysRunsMasks(schedule.getDaysRun()));
        trainSchedule.setHeadcode(schedule.getSignalHeadcode());
        if (schedule.getOperator() != null && schedule.getRetailHeadcode() != null) {
            trainSchedule.setRetailCode(schedule.getOperator() + schedule.getRetailHeadcode());
        }
        trainSchedule.setPowerType(PowerType.fromCode(schedule.getPowerType()).getDescription());
        trainSchedule.setPlannedSpeed(schedule.getPlannedSpeed());
        trainSchedule.setTimingLoad(scheduleService.getTimingLoad(schedule));
        if (schedule.getOperatingCharacteristics() != null) {
            for (char ch : schedule.getOperatingCharacteristics().toCharArray()) {
                OperatingCharacteristic oc = OperatingCharacteristic.getByCode(ch);
                if (oc != null) {
                    trainSchedule.getOperatingCharacteristic().add(oc.getDescription());
                }
            }
        }
        List<ScheduleAssociation> associations = scheduleService.getScheduleAssociation(schedule.getTrainUid(), parsedDate);
        SimpleDateFormat fullTime = new SimpleDateFormat("HH:mm:ss");
        if (!tiplocs.isEmpty()) {
            ScheduleDetail firstLocation = schedule.getDetailList().getFirst();
            ScheduleDetail lastLocation = schedule.getDetailList().getLast();
            trainSchedule.setOrigin(locations.get(firstLocation.getLocation()).getDescription());
            trainSchedule.setDestination(locations.get(lastLocation.getLocation()).getDescription());
            if (firstLocation.getPublicDepartureTime() == null) {
                trainSchedule.setDepartureTime(formatTime(fullTime, firstLocation.getDepartureTime()));
            } else {
                trainSchedule.setDepartureTime(formatTime(fullTime, firstLocation.getPublicDepartureTime()));
            }
            if (lastLocation.getPublicArrivalTime() == null) {
                trainSchedule.setArrivalTime(formatTime(fullTime, lastLocation.getArrivalTime()));
            } else {
                trainSchedule.setArrivalTime(formatTime(fullTime, lastLocation.getPublicArrivalTime()));
            }
        }
        for (ScheduleDetail sd: schedule.getDetailList()) {
            TrainScheduleEntry entry = new TrainScheduleEntry();
            Tiploc tiploc = locations.get(sd.getLocation());
            if (tiploc == null) {
                log.warn("No tiploc record found for location {}", sd.getLocation());
                continue;
            }
            entry.setTiplocCode(tiploc.getTiplocCode());
            entry.setLocationName(tiploc.getDescription());
            entry.setCrsCode(tiploc.getCrsCode());
            //Fill in the time
            entry.setWttArrivalTime(formatTime(fullTime, sd.getArrivalTime()));
            entry.setWttPassTime(formatTime(fullTime, sd.getPassTime()));
            entry.setWttDepartureTime(formatTime(fullTime, sd.getDepartureTime()));
            entry.setGbttArrivalTime(formatTime(fullTime, sd.getPublicArrivalTime()));
            entry.setGbttDepartureTime(formatTime(fullTime, sd.getPublicDepartureTime()));
            //Pathing
            //Path in, Line out
            entry.setPath(sd.getPath());
            entry.setPlatform(sd.getPlatform());
            entry.setLine(sd.getLine());
            //Allowance
            entry.setPathingAllowance(formatTime(fullTime, sd.getPathingAllowance()));
            entry.setPerformanceAllowance(formatTime(fullTime, sd.getPerformanceAllowance()));
            entry.setEngineeringAllowance(formatTime(fullTime, sd.getEngineeringAllowance()));
            int instance = sd.getLocationInstance();
            ScheduleAssociation sa = scheduleService.getAssociation(associations, schedule.getTrainUid(), tiploc.getTiplocCode(), instance);
            if (sa != null) {
                log.info("Association found! {}", sa);
                if (!sa.getStpIndicator().equalsIgnoreCase("C") && sa.getAssocCategory() != null) {
                    TrainScheduleEntry.Association association = new TrainScheduleEntry.Association();
                    if (sa.getAssocCategory().equals("N")) {
                        //Further subdivide this into P (Previous) and N (Next)
                        if (schedule.getTrainUid().equalsIgnoreCase(sa.getBaseUid())) {
                            association.setAssociationType("N");
                        } else {
                            association.setAssociationType("P");
                        }
                    } else {
                        association.setAssociationType(sa.getAssocCategory());
                    }
                    //Try to get the summary of the associated train
                    UUID assocUUID = scheduleService.getScheduleUUID(
                            sa.getBaseUid().equalsIgnoreCase(schedule.getTrainUid())
                                    ?sa.getAssocUid()
                                    :sa.getBaseUid(),
                            parsedDate
                    );
                    association.setOtherTrain(fillSummary(scheduleService.getScheduleByUUID(assocUUID), parsedDate));
                    entry.setAssociation(association);
                }
            }
            trainSchedule.getLocations().add(entry);
        }
        return ResponseEntity.ok(Map.of("result","success", "schedule", trainSchedule));
    }

    private static String formatTime(SimpleDateFormat format, Date time) {
        if (format == null || time == null) {
            return null;
        }
        return format.format(time);
    }

}
