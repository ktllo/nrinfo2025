package org.leolo.nrinfo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.leolo.nrinfo.dao.DatabaseOperationResult;
import org.leolo.nrinfo.dao.TiplocDao;
import org.leolo.nrinfo.model.Tiploc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
public class TiplocServiceTest {

    private Class<org.leolo.nrinfo.dto.external.networkrail.Tiploc> dtoClazz = org.leolo.nrinfo.dto.external.networkrail.Tiploc.class;

    private Logger logger = LoggerFactory.getLogger(TiplocServiceTest.class);
    static MockedTiplocDao mockedTiplocDao = null;

    private ObjectMapper mapper = new ObjectMapper();

    public static final String [] TEST_DATA = {
            "{\"TiplocV1\": { \"transaction_type\": \"Create\", \"tiploc_code\": \"BURGESH\",\"nalco\": \"548300\",\"stanox\": \"87971\",\"crs_code\": \"BUG\",\"description\": \"BURGESS HILL\",\"tps_description\": \"BURGESS HILL\"}}"
    };

    public static final Tiploc [] TEST_MODEL = {
            new Tiploc("BURGESH", "548300", "87971", "BUG", "BURGESS HILL", "BURGESS HILL"),
            new Tiploc("ABHL811", "937802", "04311", null, null, "EDINBURGH SIGNAL 811")
    };

    @TestConfiguration
    static class TestContextConfiguration {


        @Bean
        public TiplocDao tiplocDao() {
            if (mockedTiplocDao == null) {
                mockedTiplocDao = new MockedTiplocDao();
            }
            return mockedTiplocDao;
        }
    }

    @Autowired public TiplocService tiplocService;

    @AfterEach
    public void tearDown() throws Exception {
        logger.info("Cleaning up...");
        mockedTiplocDao.clear();
    }

    @Test
    public void testBatchFromEmpty() throws Exception {
        DatabaseOperationResult dor = new DatabaseOperationResult();
        tiplocService.processTiplocBatch(List.of(
            mapper.convertValue(mapper.readTree(TEST_DATA[0]).get("TiplocV1"), dtoClazz)
        ));
        assertEquals(1, mockedTiplocDao.getSize());
        assertEquals(TEST_MODEL[0], mockedTiplocDao.getTiplocByTiplocCode("BURGESH"));
    }

    @Test
    public void testBatchFromOne() throws Exception {
        mockedTiplocDao.insertTiploc(TEST_MODEL[1]);

        tiplocService.processTiplocBatch(List.of(
                mapper.convertValue(mapper.readTree(TEST_DATA[0]).get("TiplocV1"), dtoClazz)
        ));
        assertEquals(2, mockedTiplocDao.getSize());
        assertEquals(TEST_MODEL[0], mockedTiplocDao.getTiplocByTiplocCode("BURGESH"));
    }

//    private
}

class MockedTiplocDao extends TiplocDao {

    private static Logger logger = LoggerFactory.getLogger(MockedTiplocDao.class);

    public ConcurrentSkipListMap<String, Tiploc> dataMap = new ConcurrentSkipListMap<>();

    public void clear() {
        dataMap.clear();
    }

    @Override
    public Tiploc getTiplocByTiplocCode(String tiplocCode) throws SQLException {
        return dataMap.get(tiplocCode);
    }

    public void insertTiploc(Tiploc tiploc) {
        dataMap.put(tiploc.getTiplocCode(), tiploc);
    }

    public int getSize() {
        return dataMap.size();
    }

    @Override
    public DatabaseOperationResult upsertTiplocs(Collection<Tiploc> tiplocs) throws SQLException {
        return updateTiplocs(tiplocs);
    }

    @Override
    public DatabaseOperationResult updateTiplocs(Collection<Tiploc> tiplocs) throws SQLException {
        int updated = 0;
        for (Tiploc tiploc : tiplocs) {
            dataMap.put(tiploc.getTiplocCode(), tiploc);
            updated++;
        }
        return new DatabaseOperationResult(true, 0, updated, 0);
    }

    @Override
    public DatabaseOperationResult deleteTiplocs(Collection<Tiploc> tiplocs) throws SQLException {
        int deleted = 0;
        for (Tiploc tiploc : tiplocs) {
            dataMap.remove(tiploc.getTiplocCode());
            deleted++;
        }
        return new DatabaseOperationResult(true, 0, 0, deleted);
    }
}
