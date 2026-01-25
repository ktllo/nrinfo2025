package org.leolo.nrinfo.controller.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class WebErrorController {

    private Logger logger = LoggerFactory.getLogger(WebErrorController.class.getName());

//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public String notFound(Model model) {
//        logger.error("Not found!");
//        model.addAttribute("smessage","Sorry, resource not found");
//        return "error";
//    }
}
