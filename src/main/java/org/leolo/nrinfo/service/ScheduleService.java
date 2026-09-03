package org.leolo.nrinfo.service;

import org.jetbrains.annotations.NotNull;
import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.dao.DatabaseOperationResult;
import org.leolo.nrinfo.dao.ScheduleDao;
import org.leolo.nrinfo.dao.TrainOperatorDao;
import org.leolo.nrinfo.dto.external.networkrail.Schedule;
import org.leolo.nrinfo.dto.request.ScheduleSearch;
import org.leolo.nrinfo.dto.response.ScheduleSearchResult;
import org.leolo.nrinfo.dto.response.TrainSchedule;
import org.leolo.nrinfo.dto.response.TrainScheduleSummary;
import org.leolo.nrinfo.enums.PowerType;
import org.leolo.nrinfo.model.ScheduleAssociation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ScheduleService {

    private Logger log = LoggerFactory.getLogger(ScheduleService.class);

    @Autowired private ScheduleDao scheduleDao;
    @Autowired
    private TrainOperatorDao trainOperatorDao;
    @Autowired private GenericCacheService genericCacheService;

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

    public void deleteSchedule(org.leolo.nrinfo.model.Schedule schedule) {
        try {
            boolean operationResult = scheduleDao.deleteSchedule(schedule.getTrainUid(), schedule.getStartDate(), schedule.getEndDate());
            if (!operationResult) {
                log.warn("Unable to delete schedule {}, applicable from {} to {}", schedule.getTrainUid(), schedule.getStartDate(), schedule.getEndDate());
            } else {
                log.info("Deleted Schedule {}, applicable from {} to {}", schedule.getTrainUid(), schedule.getStartDate(), schedule.getEndDate());
            }
        } catch (SQLException e) {
            log.warn("Unable to delete schedule {}, applicable from {} to {} due to SQLException. This schedule may be stated.", schedule.getTrainUid(), schedule.getStartDate(), schedule.getEndDate());
        }
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
        if (atocCode == null) {
            return null;
        }
        if (atocCode.equalsIgnoreCase("ZZ")) {
            //This is a special case, ZZ are known to be shared by many freight operators
            return "Freight Service";
        }
        final String CACHE_KEY = String.format(Constants.CacheKey.OPERATOR_NAME_TEMPLATE, atocCode);
        if (genericCacheService.hasEntry(CACHE_KEY)) {
            return genericCacheService.getEntry(CACHE_KEY).toString();
        }
        String name = trainOperatorDao.getTrainOperatorName(atocCode);
        genericCacheService.addToCache(CACHE_KEY, name, 3600_000, GenericCacheService.CacheMode.FIXED_LIFETIME, false);
        return name;
    }

    public String getTimingLoad(String powerType, String timingLoad) {
        PowerType pt = PowerType.fromCode(powerType);
        if (timingLoad == null || pt == PowerType.UNKNOWN) {
            //We do not have information to answer
            return null;
        }
        if (pt == PowerType.DIESEL_ELECTRIC_MU || pt == PowerType.DIESEL_MECHANICAL_MU) {
            switch (timingLoad) {
                case "69":
                    return "Class 172";
                case "A":
                    return "Class 141 to 144";
                case "E":
                    return "Class 158, 168, 170 or 175";
                case "N":
                    return "Class 165/0";
                case "S":
                    return "Class 150, 153, 155, or 156";
                case "T":
                    return "Class 165/1 or 166";
                case "V":
                    return "Class 220 or 221";
                case "X":
                    return "Class 159";
                case "D1":
                case "D2":
                case "D3":
                    return "DMU";
                default:
                    try {
                        int clazz = Integer.parseInt(timingLoad);
                        if (clazz <= 999 && clazz >= 0) {
                            return "Class " + clazz;
                        }
                    } catch (NumberFormatException e) {
                        //Given is not a class number, this is not an error and we do not have to do anything
                    }
            }
        } else if (pt == PowerType.ELECTRIC_MU){
            //EMUs
            switch (timingLoad) {
                case "AT":
                    return "Accelerated Timings";
                case "E":
                    return "Class 458";
                case "0":
                    return "Class 380";
                case "506":
                    return "Class 350/1";
                default:
                    try {
                        int clazz = Integer.parseInt(timingLoad);
                        if (clazz <= 999 && clazz >= 0) {
                            return "Class " + clazz;
                        }
                    } catch (NumberFormatException e) {
                        //Given is not a class number, this is not an error and we do not have to do anything
                    }
            }
        } else if (pt == PowerType.DIESEL || pt == PowerType.ELECTRIC || pt == PowerType.ELECTRO_DIESEL){
            //Hauled train
            try {
                int load = Integer.parseInt(timingLoad);
                if (load <= 9999 && load >= 0) {
                    return load + " tonnes";
                }
            } catch (NumberFormatException e) {
                //Given is not a class number, this is not an error and we do not have to do anything
            }
        }
        //Returning null by default
        return null;
    }

    public String getTimingLoad(org.leolo.nrinfo.model.Schedule schedule) {
        return getTimingLoad(schedule.getPowerType(), schedule.getTimingLoad());
    }

    public List<ScheduleAssociation> getScheduleAssociation(String trainUID, java.util.Date date)  throws SQLException {
        List<ScheduleAssociation> associations = scheduleDao.getScheduleAssociation(trainUID, date);
        log.info("Initial search returned {} associations", associations.size());
        associations.sort(Comparator.comparing(
                ScheduleAssociation::getBaseUid, String.CASE_INSENSITIVE_ORDER
        ).thenComparing(
                ScheduleAssociation::getAssocUid, String.CASE_INSENSITIVE_ORDER
        ).thenComparing(
                ScheduleAssociation::getStpIndicator, String.CASE_INSENSITIVE_ORDER
        ).thenComparing(
                ScheduleAssociation::getAssocLocation
        ).thenComparing(
                ScheduleAssociation::getAssocType
        ).thenComparing(
                ScheduleAssociation::getStartDate
        ).thenComparing(
                ScheduleAssociation::getEndDate
        ));
        return associations;
    }

    public ScheduleAssociation getAssociation(List<ScheduleAssociation> list, String trainUid, String location) {
        return getAssociation(list, trainUid, location, 1);
    }

    public ScheduleAssociation getAssociation(List<ScheduleAssociation> list, String trainUid, String location, int instance) {
        if (trainUid == null || location == null) {
            return null;
        }
        if (list == null || list.isEmpty()) {
            return null;
        }
        if (instance <= 0) {
            instance = 1;
        }
        log.debug("Get association for {} {}-{}", trainUid, location, instance);
        for (ScheduleAssociation sa : list) {
            if (location.equalsIgnoreCase(sa.getAssocLocation())) {
                if (trainUid.equalsIgnoreCase(sa.getBaseUid())){
                    if (compareAssociationInstance(instance, sa.getBaseSuffix())) {
                        return sa;
                    }
                } else {
                    if (compareAssociationInstance(instance, sa.getAssocSuffix())) {
                        return sa;
                    }
                }
            }
        }
        return null;
    }

    private boolean compareAssociationInstance(int targetInstance, String dbInstance) {
        int numericDbInstance;
        if (dbInstance == null) {
            numericDbInstance = 1;
        } else {
            numericDbInstance = Integer.parseInt(dbInstance);
        }
        return targetInstance == numericDbInstance;
    }

    /**
     * Search train schedules that matches the search parameters
     * 
     * @param scheduleSearch search parameters
     * @return A list of train UUIDs that matches the search parameters
     * @throws SQLException When there are error searching trains
     * @deprecated Please use {@link #searchTrainScheduleForScheduleSearchResult(ScheduleSearch)}
     */
    @Deprecated
    public List<UUID> searchTrainSchedule(ScheduleSearch scheduleSearch) throws SQLException {
        Instant cacheDate = scheduleDao.getOldestCacheDate(scheduleSearch.getFromTime());
        Duration cacheAge = Duration.between(cacheDate, Instant.now());
        if (cacheAge.compareTo(Duration.ofDays(1)) > 0) {
            log.info("Cache is too old ({}), rebuilding them!", cacheAge);
            scheduleDao.cacheSchedule(scheduleSearch.getFromTime().toInstant());
        }
        return scheduleDao.searchTrainSchedule(scheduleSearch);
    }
    
    //This is an enhanced version of the deprecated searchTrainSchedule(ScheduleSearch)
    public List<ScheduleSearchResult> searchTrainScheduleForScheduleSearchResult(ScheduleSearch scheduleSearch) throws SQLException {
        Instant cacheDate = scheduleDao.getOldestCacheDate(scheduleSearch.getFromTime());
        Duration cacheAge = Duration.between(cacheDate, Instant.now());
        //We should rebuild cache daily, we check add 10 minutes to the rebuild check to avoid rebuilding them multiple times
        if (cacheAge.compareTo(Duration.ofDays(1).plus(10, ChronoUnit.MINUTES)) > 0) {
            log.info("Cache is too old ({}), rebuilding them!", cacheAge);
            scheduleDao.cacheSchedule(scheduleSearch.getFromTime().toInstant());
        }
        //Get a list of matching UUIDs first
        List<UUID> matchingScheduleUUIDs = scheduleDao.searchTrainSchedule(scheduleSearch);
        List<ScheduleSearchResult> searchResult = new ArrayList<>();
        //Round 1: get the matching schedule
        for (UUID uuid: matchingScheduleUUIDs) {
            org.leolo.nrinfo.model.Schedule ts = getScheduleByUUID(uuid);
        }
        return null;
        
    }
}
