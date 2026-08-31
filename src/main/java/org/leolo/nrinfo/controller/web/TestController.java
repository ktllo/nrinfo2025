package org.leolo.nrinfo.controller.web;

import org.leolo.nrinfo.service.WebAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;

@Controller
public class TestController {

    private Logger logger = LoggerFactory.getLogger(TestController.class);
    @Autowired
    private WebAuthenticationService webAuthenticationService;

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

    @GetMapping("/test/whoami")
    public String whoami(Model model) {
        logger.info("/whoami requested");
        model.addAttribute("message", Instant.now().toString());
//        webAuthenticationService.
        if (webAuthenticationService.isAuthenticated()) {
            model.addAttribute("msg", String.format("User is authenticated as %s", webAuthenticationService.getUsername()));
        } else {
            model.addAttribute("msg", "Not authenticated");
        }
        return "test";
    }

}
