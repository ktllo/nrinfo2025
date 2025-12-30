package org.leolo.nrinfo.service;

import org.leolo.nrinfo.dao.DatabaseOperationResult;
import org.leolo.nrinfo.dao.TiplocDao;
import org.leolo.nrinfo.model.Tiploc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

@Service
public class TiplocService {

    @Autowired private TiplocDao tiplocDao;

    private Logger logger = LoggerFactory.getLogger(TiplocService.class);

    public DatabaseOperationResult processTiplocBatch(Collection<org.leolo.nrinfo.dto.external.networkrail.Tiploc> tiplocs) {
        DatabaseOperationResult result = new DatabaseOperationResult();
        ArrayList<Tiploc> toInsert = new ArrayList<Tiploc>(tiplocs.size());
        ArrayList<Tiploc> toUpdate = new ArrayList<>(tiplocs.size());
        ArrayList<Tiploc> toDelete = new ArrayList<>(tiplocs.size());
        for (org.leolo.nrinfo.dto.external.networkrail.Tiploc tiploc : tiplocs) {
            if ("Create".equalsIgnoreCase(tiploc.getTransactionType())) {
                toInsert.add(tiploc.toModel());
            } else if ("update".equalsIgnoreCase(tiploc.getTransactionType())) {
                toUpdate.add(tiploc.toModel());
            } else if ("delete".equalsIgnoreCase(tiploc.getTransactionType())) {
                toDelete.add(tiploc.toModel());
            } else {
                logger.warn("Unknown transaction type {} for TIPLOC {}", tiploc.getTransactionType(), tiploc.getTiplocCode());
            }
        }
        //We will be actually performing an upsert for insert because all records are create in a full data load
        try {
            result.add(tiplocDao.upsertTiplocs(toInsert));
            result.add(tiplocDao.updateTiplocs(toUpdate));
            result.add(tiplocDao.deleteTiplocs(toDelete));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        logger.info("TIPLOC Batch : Batch Size {}, Inserted {}, Updated {}, Deleted {}",
                tiplocs.size(), result.getInserted(), result.getUpdated(), result.getDeleted());
        return result;
    }

}
