package org.leolo.nrinfo.controller.web;

import jakarta.servlet.http.HttpServletRequest;
import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.model.AuthenticationResult;
import org.leolo.nrinfo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebAuthenticationController {

    private final Logger logger = LoggerFactory.getLogger(WebAuthenticationController.class);

    private final Logger authLogger = LoggerFactory.getLogger("auth");
    @Autowired
    private UserService userService;

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String doLogin(
            HttpServletRequest request,
            @Deprecated ModelMap model,
            RedirectAttributes redirectAttributes,
            @RequestParam String username,
            @RequestParam String password
    ) {
        logger.info("doLogin: username={}, password=********", username);
        model.addAttribute("username", username);
        model.addAttribute("error_message", "Login function not implemented");
        AuthenticationResult ar = userService.authenticate(username, password);
        if (ar.isSuccess()) {
            //login success
            request.getSession().setAttribute(Constants.Identifier.Session.USER_ID, ar.getUserId());
            redirectAttributes.addFlashAttribute(Constants.Model.GENERIC_POPUP_MESSAGE, ar.getMessage());

            authLogger.info("SUCCESS;{};{};{}", ar.getUserId(), request.getRemoteAddr(), ar.getMessage());
            return "redirect:/";
        } else {
            //Rebind the username
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("error_message", "Username or password is incorrect");
            authLogger.info("FAILED;{};{};{}", ar.getUserId(), request.getRemoteAddr(), ar.getMessage());
            return "redirect:/login";
        }
    }

    @RequestMapping("/logout")
    public String doLogout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        request.getSession().invalidate();
        redirectAttributes.addFlashAttribute(Constants.Model.GENERIC_POPUP_MESSAGE, "Logout success!");
        return "redirect:/";
    }
}
