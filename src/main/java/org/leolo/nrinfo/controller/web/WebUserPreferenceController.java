package org.leolo.nrinfo.controller.web;

import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.model.UserPreference;
import org.leolo.nrinfo.service.PreferenceService;
import org.leolo.nrinfo.service.UserPreferenceService;
import org.leolo.nrinfo.service.WebAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Controller
public class WebUserPreferenceController {

    @Autowired
    private WebAuthenticationService webAuthenticationService;

    @Autowired private UserPreferenceService userPreferenceService;
    @Autowired private PreferenceService preferenceService;

    private Logger log = LoggerFactory.getLogger(WebUserPreferenceController.class);

    @GetMapping("/preference")
    public String preference(Model model, RedirectAttributes redirectAttributes) {
        //Check is the user authenticated
        if (!webAuthenticationService.isAuthenticated()) {
            redirectAttributes.addFlashAttribute(Constants.Model.LOGIN_REDIRECT_DESTINATION, "/preference");
            return "redirect:/login";
        }
        log.debug("User ID is {}", webAuthenticationService.getUserId());
        List<UserPreference> preferenceList = userPreferenceService.getAllUserPreferences();
        log.debug("{} preferences found", preferenceList.size());
        model.addAttribute("preferences", preferenceList);
        return "preference";
    }

    @PostMapping("/preference")
    public String preferenceSubmit(
            Model model,
            RedirectAttributes redirectAttributes,
            @RequestParam Map<String, String> parameters
    ) {
        //Check is the user authenticated
        if (!webAuthenticationService.isAuthenticated()) {
            redirectAttributes.addFlashAttribute(Constants.Model.LOGIN_REDIRECT_DESTINATION, "/preference");
            return "redirect:/login";
        }
        //TODO: Update the data
        log.debug("Request keys are {}", parameters.keySet());
        Set<String> preferenceName = preferenceService.getAllUserPreferenceNames();
        for (String prefName : preferenceName) {
            if (parameters.containsKey("pref-"+prefName)) {
                if (userPreferenceService.updateUserPreference(prefName, parameters.get("pref-"+prefName))) {
                    log.debug("Preference {} updated", prefName);
                } else {
                    log.warn("Preference {} not updated", prefName);
                    model.addAttribute(Constants.Model.GENERIC_POPUP_MESSAGE, "Unable to update preference");
                    break;
                }
            }
        }
        return preference(model, redirectAttributes);
    }




}
