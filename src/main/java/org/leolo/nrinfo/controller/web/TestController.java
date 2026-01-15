package org.leolo.nrinfo.controller.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;

@Controller
public class TestController {

    private Logger logger = LoggerFactory.getLogger(TestController.class);

    @RequestMapping("/test")
    public String test(Model model) {
        logger.info("/test requested");
        model.addAttribute("message", Instant.now().toString());
        return "test";
    }
    @RequestMapping("/test/error")
    public String testError(Model model) {
        throw new RuntimeException("Lorem ipsum dolor sit amet");
    }

}
