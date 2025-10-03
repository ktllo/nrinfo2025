package org.leolo.nrinfo.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.leolo.nrinfo.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.TreeMap;

@RestController
public class UtilController {

    @Autowired
    PasswordService passwordService;

    @RequestMapping(value = "util/password", method = RequestMethod.GET)
    public Object getPassword(HttpServletResponse response, @RequestParam(name = "password") String password) {
        if (password == null || password.isEmpty()) {
            //Return error
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return ResponseUtil.buildErrorResponse("Invalid Parameter", "Password is required");
        }
        String encodedPassword = passwordService.encryptPassword(password);

        response.setStatus(HttpServletResponse.SC_OK);
        TreeMap<String, String> map = new TreeMap<>();
        map.put("password", encodedPassword);
        map.put("status", "OK");
        return map;
    }

}
