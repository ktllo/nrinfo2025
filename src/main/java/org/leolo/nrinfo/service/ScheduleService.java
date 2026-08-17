package org.leolo.nrinfo.service;

import org.jetbrains.annotations.NotNull;
import org.leolo.nrinfo.dao.DatabaseOperationResult;
import org.leolo.nrinfo.dao.ScheduleDao;
import org.leolo.nrinfo.dao.TrainOperatorDao;
import org.leolo.nrinfo.dto.external.networkrail.Schedule;
import org.leolo.nrinfo.model.ScheduleAssociation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

@Service
public class ScheduleService {

    private Logger log = LoggerFactory.getLogger(ScheduleService.class);

    @Autowired private ScheduleDao scheduleDao;
    @Autowired
    private TrainOperatorDao trainOperatorDao;

    public DatabaseOperationResult processAssociationBatch(Collection<org.leolo.nrinfo.dto.external.networkrail.Association> associations) {
        DatabaseOperationResult result = new DatabaseOperationResult();
        // We only have insert and delete
        ArrayList<ScheduleAssociation> toInsert = new ArrayList<>(associations.size());
        ArrayList<ScheduleAssociation> toDelete = new ArrayList<>(associations.size());
        for (org.leolo.nrinfo.dto.external.networkrail.Association association : associations) {
            if ("create".equalsIgnoreCase(association.getTransactionType())) {
                toInsert.add(association.toModel());
            } else if ("delete".equalsIgnoreCase(association.getTransactionType())) {
                toDelete.add(association.toModel());
            } else {
                log.warn("Unknown transaction type {} for association record", association.getTransactionType());
            }
        }
        try {
            result = result.add(scheduleDao.insertAssociation(toInsert));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        log.info("Association Batch : Batch Size {}, Inserted {}, Updated {}, Deleted {}",
                associations.size(), result.getInserted(), result.getUpdated(), result.getDeleted());
        return result;
    }

    public DatabaseOperationResult processScheduleBatch(Collection<Schedule> schedules) {
        DatabaseOperationResult result = new DatabaseOperationResult();
        try {
            for (Schedule schedule : schedules) {
                org.leolo.nrinfo.model.Schedule scheduleModel = schedule.toModel();
                if (schedule.getTransactionType().equalsIgnoreCase("create")) {
                    result = result.add(scheduleDao.insertSchedule(scheduleModel));
                } else if (schedule.getTransactionType().equalsIgnoreCase("delete")) {
                    result = result.add(scheduleDao.deleteSchedule(scheduleModel));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        log.info("Schedule Batch : Batch Size {}, Inserted {}, Updated {}, Deleted {}",
                schedules.size(), result.getInserted(), result.getUpdated(), result.getDeleted());
        return result;
    }

    @Scheduled(cron = "30 0 22 * * *")
    public void finalizeScheduleCache() {
        Instant date = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(1, ChronoUnit.DAYS);
        log.info("Finalize Schedule Cache for {}", date);
        //Step 1: Rebuild the cache
        buildScheduleCache(date);
        //Step 2: Copy over
        try {
            scheduleDao.finalizeCache(date);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        log.info("Finalized Schedule Cache for {}", date);
    }

    public void buildScheduleCache(Instant date) {
        log.debug("Building schedule cache for {}", date);
        try {
            scheduleDao.cacheSchedule(date);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void pruneSchedule(Instant cutoff) {
        try {
            scheduleDao.pruneSchedule(cutoff);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertSchedule(org.leolo.nrinfo.model.Schedule schedule) throws SQLException {
        scheduleDao.insertSchedule(schedule);
    }

    /**
     * Find the applicable schedule UUID for the given train and date
     *
     * <p>
     *     Common cause for not finding an applicable schedule:
     *     <ul>
     *         <li>This train does not run on that day</li>
     *         <li>The requested date is outside the running period of the train</li>
     *     </ul>
     * </p>
     * @param trainUID Train UID to look for the applicable schedule
     * @param date Date to look for the applicable schedule
     * @return The UUID of the applicable schedule, or <code>null</code> if there are no applicable schedule
     */
    public UUID getScheduleUUID(@NotNull String trainUID, @NotNull Date date) throws SQLException {
        log.debug("Getting schedule UUID for train UID {} for {}", trainUID, date);
        //Step 1: Look into cache
        UUID uuid = scheduleDao.checkScheduleCache(trainUID, date);
        if (uuid != null) {
            log.debug("Found schedule UUID {} for train {} on {}", uuid, trainUID, date);
            return uuid;
        }
        log.debug("No schedule found for train {} on {} found in cache. We need to dig deeper", trainUID, date);
        //Step 2: Look for the details
        return scheduleDao.findApplicableSchedule(trainUID, date);
    }

    public UUID getBaseScheduleUUID(@NotNull String trainUID, @NotNull Date date) throws SQLException {
        log.debug("Getting base schedule UUID for train UID {} for {}", trainUID, date);
        return scheduleDao.findBaseSchedule(trainUID, date);
    }

    public void forceRebuildCache(String trainUID, java.util.Date date) throws SQLException {
        scheduleDao.forceCacheRebuild(trainUID, date);
    }

    public org.leolo.nrinfo.model.Schedule getScheduleByUUID(UUID uuid) throws SQLException {
        return scheduleDao.getSchedule(uuid);
    }

    public String getTrainOperatorName(String atocCode) throws SQLException {
        return trainOperatorDao.getTrainOperatorName(atocCode);
    }
}
