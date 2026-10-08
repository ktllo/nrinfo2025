package org.leolo.nrinfo.service;

import jakarta.validation.constraints.NotNull;
import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.dao.UserAttributeDao;
import org.leolo.nrinfo.dao.UserDao;
import org.leolo.nrinfo.dao.UserPreferenceDao;
import org.leolo.nrinfo.dto.request.UserRegister;
import org.leolo.nrinfo.exception.WebValidationException;
import org.leolo.nrinfo.model.AuthenticationResult;
import org.leolo.nrinfo.model.User;
import org.leolo.nrinfo.model.UserPreference;
import org.leolo.nrinfo.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Marker marker = MarkerFactory.getMarker("AUTH");

    @Autowired private UserDao userDao;
    @Autowired private UserPreferenceDao userPreferenceDao;
    @Autowired private UserAttributeDao userAttributeDao;
    @Autowired private PasswordService passwordService;
    @Autowired private ConfigurationService configurationService;

    private Random random = new Random();


    public AuthenticationResult authenticate(@NotNull String username, @NotNull String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("username or password is null");
        }
        try {
            User user = userDao.getUserByUsername(username);
            AuthenticationResult ar = new AuthenticationResult();
            if (user == null) {
                //Dummy password call
                passwordService.encryptPassword(password);
                log.info(marker, "Failed login attempt for user {} (user not found)", username);
                ar.setSuccess(false);
                ar.setMessage("Username or password is incorrect");
                return ar;
            }
            if (passwordService.checkPassword(password, user.getPassword())) {
                //Login Success
                userDao.markLoginSuccess(user.getUserId());
                ar.setSuccess(true);
                ar.setMessage("Successfully logged in");
                ar.setUserId(user.getUserId());
                if (user.getFailedLoginCount() > 0) {
                    ar.appendMessage(
                            "There are " + user.getFailedLoginCount() + " failed logins, last failed login attempt at " +
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(user.getLastFailedLoginDate()));
                }
                if (user.isForcePasswordChange()) {
                    ar.setMessage(". You must change password after logging in.");
                    ar.setForcePasswordChange(true);
                }
                return ar;
            } else {
                //Login failed
                userDao.markLoginFail(user.getUserId());
                ar.setSuccess(false);
                ar.setMessage("Username or password is incorrect");
                int delay = getResponseDelay(user.getFailedLoginCount());
                log.info("Delaying the response for {} ms", delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    //Ignore it
                }
                return ar;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private int getResponseDelay(int failedCount) {
        int minDelay = Integer.parseInt(configurationService.getConfiguration("authfail.min_delay","100"));
        int maxDelay = Integer.parseInt(configurationService.getConfiguration("authfail.max_delay","10000"));
        double factorBase = Double.parseDouble(configurationService.getConfiguration("authfail.factor_base","100"));
        double factorExponent = Double.parseDouble(configurationService.getConfiguration("authfail.factor_exp","1.1"));
        double calculatedDelay = factorBase * Math.pow(factorExponent, failedCount);
        if (calculatedDelay < minDelay) {
            return minDelay;
        } else if (calculatedDelay > maxDelay) {
            return maxDelay;
        }
        return (int)Math.round(calculatedDelay);
    }

    public User getUserById(int userId) {
        try {
            return userDao.getUserById(userId);
        } catch (SQLException e) {
            log.error("Unable to fetch user - {}", e.getMessage(), e);
            return null;
        }
    }

    public String generatePasswordResetToken(int userId, int validity) {
        if (userId <= 0 || validity <= 0) {
            throw new IllegalArgumentException("Invalid user id or validity");
        }
        String token = RandomUtil.getReadableString(32);
        try {
            userDao.insertPasswordReset(token, userId, validity);
        } catch (SQLException e) {
            log.error("Unable to add reset token - {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
        return token;
    }
    public String generatePasswordResetToken(int userId) {
        return generatePasswordResetToken(userId, Integer.parseInt(configurationService.getConfiguration("pwd_reset.validity","86400")));
    }

    public User getUserByUsername(String username) {
        try {
            return userDao.getUserByUsername(username);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<String> getUserPermissions(int userId) {
        try {
            return userDao.getPermissionForUser(userId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkPasswordComplexity(String password) {
        //TODO: Implement function
        return password.length() >= Constants.MIN_PASSWORD_LENGTH && password.length() <= Constants.MAX_PASSWORD_LENGTH;
    }

    public synchronized AuthenticationResult doRegisterUser(UserRegister userRegister) throws SQLException, WebValidationException {
        try {
            if (!configurationService.getBoolean("allow_register")) {
                //Open registration is not enabled.
                if (userRegister.getInviteKey() == null || userRegister.getInviteKey().isEmpty()) {
                    throw new WebValidationException("invite key is required", "invite_key");
                }
                if (userDao.getInviteKeyUseLeft(userRegister.getInviteKey()) <= 0) {
                    throw new WebValidationException("invite key is invalid, or allowed usage had been reached", "invite_key");
                }
            }
            if (!checkPasswordComplexity(userRegister.getPassword())) {
                throw new WebValidationException("password does not meet requirements", "password");
            }
            if (userDao.isUserExists(userRegister.getUsername())) {
                throw new WebValidationException("Username is already in use","username");
            }
            int newUserId = userDao.createUser(userRegister.getUsername(), passwordService.encryptPassword(userRegister.getPassword()));
            if (newUserId <= 0) {
                throw new SQLException("Unable to create new user");
            }
            String [] roles = configurationService.getString("register.default_role").split(",");
            for (String role : roles) {
                userDao.addRole(newUserId, role);
            }
            if (userRegister.getInviteKey() != null && !userRegister.getInviteKey().isEmpty()) {
                userDao.markInviteKeyUsed(userRegister.getInviteKey());
            }
            AuthenticationResult ar = new AuthenticationResult();
            ar.setUserId(newUserId);
            ar.setSuccess(true);
            return ar;
        } catch (WebValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void changePassword(int userId, String oldPassword, String newPassword) {

    }

    public List<String> generateInviteKeys(int count) {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            keys.add(generateInviteKey(Constants.INVITE_KEY_LENGTH));
        }
        try {
            userDao.insertInviteKey(keys);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return new ArrayList<>();
        }
        return keys;
    }

    private String generateInviteKey(int length) {
        final char [] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ023456".toCharArray();
        char [] key = new char[length];
        for (int i = 0; i < length; i++) {
            key[i] = chars[random.nextInt(chars.length)];
        }
        return new String(key);
    }

    public List<User> searchUsers(String searchKey) {
        try {
            return userDao.getUsersByPartialUsername(searchKey);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> getUserRole(int userId) {
        try {
            return userDao.getUserRole(userId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> getUserPreference(int userId) {
        HashMap<String, String> map = new HashMap<>();
        try {
            List<UserPreference> preferences = userPreferenceDao.getAllUserPreferenceForUserByUserId(userId);
            for (UserPreference preference : preferences) {
                map.put(preference.getPreferenceName(), preference.getActualValue());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public Map<String, org.leolo.nrinfo.dto.response.UserAttribute> getUserAttribute(int userId) {
        try {
            return userAttributeDao.getUserAttributeForDisplay(userId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
