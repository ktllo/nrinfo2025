package org.leolo.nrinfo.controller.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.dto.request.UserRegister;
import org.leolo.nrinfo.exception.ValidationException;
import org.leolo.nrinfo.exception.WebValidationException;
import org.leolo.nrinfo.model.AuthenticationResult;
import org.leolo.nrinfo.service.ConfigurationService;
import org.leolo.nrinfo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

@Controller
public class WebAuthenticationController {

    private final Logger logger = LoggerFactory.getLogger(WebAuthenticationController.class);

    private final Logger authLogger = LoggerFactory.getLogger("auth");
    @Autowired
    private UserService userService;
    @Autowired
    private ConfigurationService configurationService;

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String doLogin(
            HttpServletRequest request,
            @Deprecated ModelMap model,
            RedirectAttributes redirectAttributes,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "") String destination
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
            if (destination==null || destination.isEmpty()) {
                return "redirect:/";
            } else if (destination.startsWith("/")) {
                return "redirect:" + destination;
            } else {
                return "redirect:/" + destination;
            }
        } else {
            //Rebind the username
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("error_message", "Username or password is incorrect");
            redirectAttributes.addFlashAttribute(Constants.Model.LOGIN_REDIRECT_DESTINATION, destination);
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

    @GetMapping("/register")
    public String doRegister(HttpServletRequest request, RedirectAttributes redirectAttributes, Model model) {
        fillRegistrationParameters(model);
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(
            HttpServletRequest request,
            HttpServletResponse response,
            Model model,
            @RequestParam Map<String, String> attributes
    ) {
        boolean needInvite = !configurationService.getBoolean(Constants.Configuration.ALLOW_REGISTER);
        String username = attributes.get("username");
        String password = attributes.get("password");
        String confirmPassword = attributes.get("confirm_password");
        String inviteKey = null;
        if (needInvite) {
            inviteKey = attributes.get("invite_key");
        }
        if (username == null || password == null || confirmPassword == null) {
            logger.info("Some fields are missing or incorrect (un/p/cp): {}/{}/{}", username==null, password==null, confirmPassword==null);
        }
        if (!password.equals(confirmPassword)) {
            logger.warn("passwords do not match");
            model.addAttribute("msg_password", "Passwords do not match");
            fillRegistrationParameters(model);
            model.addAttribute("username", username);
            model.addAttribute("invite_key", inviteKey);
            return "register";
        }
        UserRegister ur = new UserRegister();
        ur.setUsername(username);
        ur.setPassword(password);
        ur.setInviteKey(inviteKey);
        try {
            AuthenticationResult ar = userService.doRegisterUser(ur);
            if (ar.isSuccess()) {
                request.getSession().setAttribute(Constants.Identifier.Session.USER_ID, ar.getUserId());
                model.addAttribute(Constants.Model.GENERIC_POPUP_MESSAGE, "Registration successful.");
                return "redirect:/";
            }
        } catch (SQLException e) {
            fillRegistrationParameters(model);
            model.addAttribute("username", username);
            model.addAttribute("invite_key", inviteKey);
            model.addAttribute(Constants.Model.GENERIC_POPUP_MESSAGE, "Error when registering user.");
            return "register";
        } catch (WebValidationException e) {
            logger.warn("Validation error: {}", e.getMessage());
            fillRegistrationParameters(model);
            model.addAttribute("username", username);
            model.addAttribute("invite_key", inviteKey);
            switch (e.getSource()) {
                case "username":
                    model.addAttribute("msg_username", e.getMessage());
                    break;
                case "password":
                    model.addAttribute("msg_password", e.getMessage());
                    break;
                case "invite_key":
                    model.addAttribute("msg_invite_key", e.getMessage());
                    break;
            }
            return "register";
        } catch (Exception e) {
            fillRegistrationParameters(model);
            return "register";
        }
        logger.warn("Registration failed");
        return null;
    }

    private void fillRegistrationParameters(Model model) {
        model.addAttribute(
                "need_invite_key",
                !configurationService.getBoolean(Constants.Configuration.ALLOW_REGISTER)
        );
        model.addAttribute("min_password_length", Constants.MIN_PASSWORD_LENGTH);
        model.addAttribute("max_password_length", Constants.MAX_PASSWORD_LENGTH);
    }
}
