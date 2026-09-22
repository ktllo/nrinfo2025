package org.leolo.nrinfo.controller.web;

import org.jetbrains.annotations.NotNull;
import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.controller.ResponseUtil;
import org.leolo.nrinfo.dto.response.TrainSchedule;
import org.leolo.nrinfo.dto.response.TrainScheduleEntry;
import org.leolo.nrinfo.dto.web.ScheduleEntry;
import org.leolo.nrinfo.enums.OperatingCharacteristic;
import org.leolo.nrinfo.enums.PowerType;
import org.leolo.nrinfo.enums.TrainCategory;
import org.leolo.nrinfo.model.Schedule;
import org.leolo.nrinfo.model.ScheduleAssociation;
import org.leolo.nrinfo.model.ScheduleDetail;
import org.leolo.nrinfo.model.Tiploc;
import org.leolo.nrinfo.service.ConfigurationService;
import org.leolo.nrinfo.service.ScheduleService;
import org.leolo.nrinfo.service.TiplocService;
import org.leolo.nrinfo.util.CommonUtil;
import org.leolo.nrinfo.util.ScheduleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Controller
public class WebScheduleSearchController {

    private Logger log = LoggerFactory.getLogger(WebScheduleSearchController.class);
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private TiplocService tiplocService;
    @Autowired
    private ConfigurationService configurationService;

    @GetMapping("/schedule_search")
    public String scheduleSearch(Model model) {
        //This is an almost static page
        log.debug("schedule_search requested");
        return "schedule_search";
    }

    @GetMapping("/schedule/{uid}/{date}")
    public String viewSchedule(@PathVariable("uid") String uid, @PathVariable("date") String date, Model model) {
        log.info("Full schedule for {} on {} is requested", uid, date);
        //Sanitize the input
        if (uid == null || uid.isEmpty() || date == null || date.isEmpty()) {
            model.addAttribute("error", "Please enter a valid schedule UID and a date for the schedule");
            return "error_";
        }
        Date parsedDate = null;
        try {
            parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            //Bad input
            model.addAttribute("message", "Invalid date format");
            return "error_";
        }
        Duration pastSearchLimit = Duration.ofDays(configurationService.getInt("schedule.retain_period", 0));
        Duration pastSearchDuration = Duration.between(parsedDate.toInstant(), Instant.now());
        if (pastSearchLimit.compareTo(pastSearchDuration) < 0) {
            log.debug("pastSearchLimit is greater than pastSearchDuration");
            model.addAttribute("non_tech_message","Requested schedule is outside retaining period");
            return "error_";
        }
        log.debug("Data is clean");
        model.addAttribute("message","Not implemented yet");

        UUID scheduleUUID = null;
        try {
            scheduleUUID = scheduleService.getScheduleUUID(uid, parsedDate);
        } catch (SQLException e) {
            log.error("Error while getting schedule UUID - {}", e.getMessage(), e);
            model.addAttribute("non_tech_message","Error when getting the train schedule");
            return "error_";
        }
        if (scheduleUUID == null) {
            //Schedule not found
            model.addAttribute("non_tech_message", "Schedule not found");
            return "error_";
        }
        Schedule searchedSchedule = null;
        Schedule displaySchedule = null;
        SimpleDateFormat dfDateOnly = new SimpleDateFormat("yyyy-MM-dd");
        try {
            searchedSchedule = scheduleService.getScheduleByUUID(scheduleUUID);
        } catch (SQLException e) {
            log.error("Error while getting schedule UUID - {}", e.getMessage(), e);
            model.addAttribute("non_tech_message","Error when getting the train schedule");
            return "error_";
        }
        if ("C".equals(searchedSchedule.getStpIndicator())) {
            //Cancelled
            try {
                displaySchedule = scheduleService.getScheduleByUUID(scheduleService.getBaseScheduleUUID(uid, parsedDate));
            } catch (SQLException e) {
                log.error("Error while getting schedule UUID - {}", e.getMessage(), e);
                model.addAttribute("non_tech_message","Error when getting the train schedule");
                return "error_";
            }
        } else {
            displaySchedule = searchedSchedule;
        }
        try {
            model.addAttribute("cancelled", "C".equals(searchedSchedule.getStpIndicator()));
            model.addAttribute("headcode", displaySchedule.getSignalHeadcode());
            Duration originDepartTime = displaySchedule.getDetailList().getFirst().getDepartureTime();
            if (originDepartTime != null) {
                model.addAttribute("originDepartTime", formatTime(originDepartTime));
            } else {
                log.warn("OriginDepartTime is null");
            }
            model.addAttribute("origin", tiplocService.getDisplayNameByTiplocCode(displaySchedule.getDetailList().getFirst().getLocation()));
            model.addAttribute("destination", tiplocService.getDisplayNameByTiplocCode(displaySchedule.getDetailList().getLast().getLocation()));
            String operatorCode = displaySchedule.getOperator();
            if (!"ZZ".equals(operatorCode)) {
                model.addAttribute("operator", scheduleService.getTrainOperatorName(operatorCode));
            } else {
                model.addAttribute("operator", "unknown operator");
            }
            model.addAttribute("train_category", TrainCategory.getTrainCategory(displaySchedule.getTrainCategory()).getDisplayName());
            model.addAttribute("one_day_schedule", displaySchedule.getStartDate().equals(displaySchedule.getEndDate()));
            SimpleDateFormat sdfDateOnly = new SimpleDateFormat("yyyy-MM-dd");
            model.addAttribute("schedule_start_date", sdfDateOnly.format(displaySchedule.getStartDate()));
            model.addAttribute("schedule_end_date", sdfDateOnly.format(displaySchedule.getEndDate()));
            model.addAttribute("schedule_runs_day", ScheduleUtil.pettyFormatDaysRunsMasks(displaySchedule.getDaysRun()));
            ArrayList<org.leolo.nrinfo.dto.web.ScheduleEntry> scheduleEntries = new ArrayList<>();
            boolean hasPass = false;
            for (ScheduleDetail scheduleEntry : displaySchedule.getDetailList()) {
                org.leolo.nrinfo.dto.web.ScheduleEntry se = new org.leolo.nrinfo.dto.web.ScheduleEntry();
                Tiploc tiploc = tiplocService.getTiplocByTiplocCode(scheduleEntry.getLocation());
                se.setStationName(tiplocService.getDisplayNameByTiplocCode(scheduleEntry.getLocation()));
                se.setPlatform(scheduleEntry.getPlatform());
                se.setCrsCode(tiploc.getCrsCode());
                se.setGbttArrivalTime(formatTime(scheduleEntry.getPublicArrivalTime()));
                se.setGbttDepartureTime(formatTime(scheduleEntry.getPublicDepartureTime()));
                if (scheduleEntry.getPassTime() != null) {
                    se.setPassOnly(true);
                    se.setWttArrivalTime("pass");
                    se.setWttDepartureTime(formatTime(scheduleEntry.getPassTime()));
                    hasPass = true;
                } else {
                    se.setPassOnly(false);
                    se.setWttArrivalTime(formatTime(scheduleEntry.getArrivalTime()));
                    se.setWttDepartureTime(formatTime(scheduleEntry.getDepartureTime()));
                }
                se.setEngineeringAllowance(formatAllowance(scheduleEntry.getEngineeringAllowance()));
                se.setPathingAllowance(formatAllowance(scheduleEntry.getPathingAllowance()));
                se.setPerformanceAllowance(formatAllowance(scheduleEntry.getPerformanceAllowance()));
                se.setLineOut(scheduleEntry.getLine());
                se.setPathIn(scheduleEntry.getPath());
                scheduleEntries.add(se);
            }
            model.addAttribute("schedules", scheduleEntries);
            model.addAttribute("hasPass", hasPass);
            return "schedule";
        } catch (SQLException e) {
            log.error("Error while getting schedule - {}", e.getMessage(), e);
            model.addAttribute("non_tech_message","Error when getting the train schedule");
            return "error_";
        }
    }

    private static  String formatTime(Duration time) {
        if (time == null) {
            return null;
        }
        return String.format("%02d%02d%s",
                time.toHoursPart(),
                time.toMinutesPart(),
                time.toSecondsPart() == 0 ? "" : Constants.HTMLEntity.FRAC_1_2);
    }

    private static String formatAllowance(Duration allowance) {
        if (allowance == null) {
            return null;
        }
        if (allowance.toMinutes() == 0) {
            return Constants.HTMLEntity.FRAC_1_2;

        } else {
            return String.format("%2d%s",
                    allowance.toMinutes(),
                    allowance.toSecondsPart() == 0 ? "" : Constants.HTMLEntity.FRAC_1_2);
        }

    }
}
