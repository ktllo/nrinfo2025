package org.leolo.nrinfo.service;

import jakarta.validation.constraints.NotNull;
import org.leolo.nrinfo.dao.UserDao;
import org.leolo.nrinfo.model.AuthenticationResult;
import org.leolo.nrinfo.model.User;
import org.leolo.nrinfo.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

@Service
public class UserService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Marker marker = MarkerFactory.getMarker("AUTH");

    @Autowired private UserDao userDao;
    @Autowired private PasswordService passwordService;
    @Autowired private ConfigurationService configurationService;

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
}
