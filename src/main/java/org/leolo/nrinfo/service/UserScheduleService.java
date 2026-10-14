package org.leolo.nrinfo.service;

import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.dao.ScheduleDao;
import org.leolo.nrinfo.exception.OperationNotPermittedException;
import org.leolo.nrinfo.model.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.UUID;

@Service
public class UserScheduleService {

    @Autowired private CurrentUserService currentUserService;
    @Autowired private ScheduleDao scheduleDao;
    @Autowired private UserAttributeService userAttributeService;


    private Logger logger = LoggerFactory.getLogger(UserScheduleService.class);

    public int getUserStarredScheduleCount() {
        try {
            return scheduleDao.getUserStarredScheduleCount(currentUserService.getUserId());
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public boolean isScheduleStarred(Schedule schedule) {
        try {
            return scheduleDao.isScheduleStarred(currentUserService.getUserId(), schedule.getScheduleUuid());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void starSchedule(UUID scheduleUUID) {
        if (!currentUserService.isAuthenticated()) {
            throw new OperationNotPermittedException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        //Check is max count exceeded
        int maxStar = userAttributeService.getInt(Constants.UserAttributes.MAX_STARRED_SCHEDULE, 0);
        if (maxStar == 0 || (maxStar > 0 && maxStar <= getUserStarredScheduleCount())) {
            throw new OperationNotPermittedException(HttpStatus.CONFLICT, "You have starred too many schedules");
        }
        try {
            scheduleDao.starSchedule(currentUserService.getUserId(), scheduleUUID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void unstarSchedule(UUID scheduleUUID) {
        if (!currentUserService.isAuthenticated()) {
            throw new OperationNotPermittedException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        try {
            scheduleDao.unstarSchedule(currentUserService.getUserId(), scheduleUUID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
