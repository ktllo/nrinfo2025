package org.leolo.nrinfo.service;

import org.leolo.nrinfo.dao.DatabaseOperationResult;
import org.leolo.nrinfo.dao.ScheduleDao;
import org.leolo.nrinfo.dto.external.networkrail.Schedule;
import org.leolo.nrinfo.model.ScheduleAssociation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

@Service
public class ScheduleService {

    private Logger log = LoggerFactory.getLogger(ScheduleService.class);

    @Autowired private ScheduleDao scheduleDao;

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

}
