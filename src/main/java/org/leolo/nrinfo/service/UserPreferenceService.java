package org.leolo.nrinfo.service;

import org.leolo.nrinfo.dao.UserPreferenceDao;
import org.leolo.nrinfo.model.UserPreference;
import org.leolo.nrinfo.model.UserPreferenceOption;
import org.leolo.nrinfo.util.CommonUtil;
import org.leolo.nrinfo.validator.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
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

    private Map<String, UserPreference> userPreferenceMap = null;

    public void clearCache() {
        userPreferenceMap = null;
    }

    private synchronized void fillCache()  {
        int userId = userPermissionService.getUserId();
        if (userId == 0) {
            log.warn("User ID is 0");
            userPreferenceMap = new HashMap<>();
            return;
        }
        if (userPreferenceMap != null) {
            return;
        }
        userPreferenceMap = new HashMap<>();
        try {
            List<UserPreference> list = userPreferenceDao.getAllUserPreferenceForUserByUserId(userId);
            for (UserPreference userPreference : list) {
                userPreference.setOptions(preferenceService.getUserPreferenceOptions(userPreference.getPreferenceName()));
                userPreferenceMap.put(userPreference.getPreferenceName(), userPreference);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public List<UserPreference> getAllUserPreferences() {
        fillCache();
        return new ArrayList<>(userPreferenceMap.values());
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
        fillCache();
        try {
            UserPreference userPreference = userPreferenceMap.get(preferenceName);
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

    private String doGetPreference(String preferenceName) {
        if (userPreferenceMap == null) {
            fillCache();
        }
        UserPreference userPreference = userPreferenceMap.get(preferenceName);
        if (userPreference == null) {
            log.error("User Preference {} not found", preferenceName);
            return null;
        }
        return userPreference.getActualValue();
    }

    /**
     * Gets a boolean preference value.
     * <p>
     * If the preference is not set, or its value is not recognised as either
     * {@code true} or {@code false}, the supplied default value is returned.
     * The following values are recognised as {@code true}: {@code "true"},
     * {@code "yes"}, {@code "y"}, {@code "on"}, and {@code "1"}.
     * The following values are recognised as {@code false}: {@code "false"},
     * {@code "no"}, {@code "n"}, {@code "off"}, and {@code "0"}.
     * String comparisons are case-insensitive.
     *
     * @param preferenceName the name of the preference to retrieve
     * @param defaultValue the value to return if the preference is not set or
     *                     its value is not recognised
     * @return the boolean preference value, or {@code defaultValue} if the
     *         preference is not set or its value is not recognised
     */
    public boolean getBoolean(String preferenceName, boolean defaultValue) {
        String value = doGetPreference(preferenceName);
        return CommonUtil.parseBoolean(value, defaultValue);
    }

    /**
     * Gets a boolean preference value, using {@code false} as the default value.
     *
     * @param preferenceName the name of the preference to retrieve
     * @return the boolean preference value, or {@code false} if the preference
     *         is not set or its value is not recognised
     *
     * @see #getBoolean(String, boolean)
     */
    public boolean getBoolean(String preferenceName) {
        return getBoolean(preferenceName, false);
    }

    /**
     * Gets an integer preference value.
     *
     * @param preferenceName the name of the preference to retrieve
     * @param defaultValue the value to return if the preference is not set
     * @return the integer preference value, or {@code defaultValue} if the
     *         preference is not set
     * @throws NumberFormatException if the preference value is not a valid integer
     */
    public int getInt(String preferenceName, int defaultValue) {
        String value = doGetPreference(preferenceName);
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    /**
     * Gets an integer preference value, using {@code 0} as the default value.
     *
     * @param preferenceName the name of the preference to retrieve
     * @return the integer preference value, or {@code 0} if the preference is not
     *         set
     * @throws NumberFormatException if the preference value is not a valid integer
     * @see #getInt(String, int)
     */
    public int getInt(String preferenceName) {
        return getInt(preferenceName, 0);
    }

    /**
     * Gets a string preference value.
     *
     * @param preferenceName the name of the preference to retrieve
     * @param defaultValue the value to return if the preference is not set
     * @return the string preference value, or {@code defaultValue} if the
     *         preference is not set
     */
    public String getString(String preferenceName, String defaultValue) {
        String value = doGetPreference(preferenceName);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Gets a string preference value, using {@code null} as the default value.
     *
     * @param preferenceName the name of the preference to retrieve
     * @return the string preference value, or {@code null} if the preference is
     *         not set
     * @see #getString(String, String)
     */
    public String getString(String preferenceName) {
        return getString(preferenceName, null);
    }
}
