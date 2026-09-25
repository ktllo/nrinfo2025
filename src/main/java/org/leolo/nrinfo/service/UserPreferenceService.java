package org.leolo.nrinfo.service;

import org.leolo.nrinfo.dao.UserPreferenceDao;
import org.leolo.nrinfo.model.UserPreference;
import org.leolo.nrinfo.model.UserPreferenceOption;
import org.leolo.nrinfo.validator.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserPreferenceService {

    private Logger log = LoggerFactory.getLogger(UserPreferenceService.class);
    @Autowired private UserPermissionService userPermissionService;
    @Autowired private UserPreferenceDao userPreferenceDao;
    @Autowired private PreferenceService preferenceService;
    
    public List<UserPreference> getAllUserPreferences() {
        int userId = userPermissionService.getUserId();
        if (userId == 0) {
            log.warn("User ID is 0");
            return new ArrayList<>();
        }
        try {
            List<UserPreference> list = userPreferenceDao.getAllUserPreferenceForUserByUserId(userId);
            for(UserPreference userPreference : list) {
                userPreference.setOptions(preferenceService.getUserPreferenceOptions(userPreference.getPreferenceName()));
            }
            return list;
        } catch (SQLException e) {
            log.error("Error while retrieving user preferences", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates the value of a preference for the currently authenticated user.
     * <p>
     * The preference must exist for the user. If the preference defines
     * validation rules or a list of allowed options, the supplied value must
     * satisfy those rules. The value is stored only if it passes the applicable
     * validation.
     *
     * @param preferenceName the name of the preference to update
     * @param value the new value for the preference
     * @return {@code true} if the preference was successfully updated;
     *         {@code false} if the user cannot be identified, the preference
     *         does not exist, the value fails validation, or the update fails
     */
    public boolean updateUserPreference(String preferenceName, String value) {
        int userId = userPermissionService.getUserId();
        if (userId == 0) {
            log.warn("User ID is 0");
            return false;
        }
        try {
            UserPreference userPreference = userPreferenceDao.getUserPreferenceForUser(userId, preferenceName);
            if (userPreference == null) {
                log.error("User Preference {} not found", preferenceName);
                return false;
            }
            userPreference.setOptions(preferenceService.getUserPreferenceOptions(preferenceName));
            //Check is validation needed
            if (userPreference.getValidationClass() != null) {
                log.debug("Trying to validate user preference {}", preferenceName);
                Class<?> validationClass = Class.forName(userPreference.getValidationClass());
                if (!InputValidator.class.isAssignableFrom(validationClass)) {
                    log.error("Validation class {} is not an instance of InputValidator", validationClass.getName());
                    return false;
                }
                InputValidator validator = (InputValidator) validationClass.getDeclaredConstructor().newInstance();
                try {
                    if (!validator.validate(value)) {
                        log.error("Validation failed");
                        return false;
                    }
                } catch (Throwable e) {
                    log.error("Validation failed", e);
                    return false;
                }
            } else if (userPreference.getOptions() != null && !userPreference.getOptions().isEmpty()) {
                //There are listed options, check is given value one of them
                //This can be overridden by having an validation class
                log.debug("There are listed options for preference {}, check is the given value one of them", preferenceName);
                boolean found = false;
                for (UserPreferenceOption option : userPreference.getOptions()) {
                    if (value.equals(option.getMappedValue())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    log.error("Preference {} has listed options but {} is not in the list", preferenceName, value);
                    return false;
                }
            }
            userPreferenceDao.upsertUserPreferenceValue(userId, preferenceName, value);
            return true;
        } catch (SQLException e) {
            log.error("Error while updating user preference", e);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            log.error("Invalid user preference validation class", e);
        }
        return false;
    }
}
