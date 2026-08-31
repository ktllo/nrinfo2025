package org.leolo.nrinfo.controller.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebScheduleSearchController {

    private Logger log = LoggerFactory.getLogger(WebScheduleSearchController.class);

    @GetMapping("/schedule_search")
    public String scheduleSearch(Model model) {
        log.debug("schedule_search requested");
        return "schedule_search";
    }
}
