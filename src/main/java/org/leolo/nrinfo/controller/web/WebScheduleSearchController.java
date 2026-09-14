package org.leolo.nrinfo.controller.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebScheduleSearchController {

    private Logger log = LoggerFactory.getLogger(WebScheduleSearchController.class);

    @GetMapping("/schedule_search")
    public String scheduleSearch(Model model) {
        log.debug("schedule_search requested");
        return "schedule_search";
    }

    @GetMapping("/schedule/{uid}/{date}")
    public String viewSchedule(@PathVariable("uid") String uid, @PathVariable("date") String date, Model model) {
        log.debug("viewSchedule requested - UID: {}, DATE: {}", uid, date);
        return "error_";
    }
}
