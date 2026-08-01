package org.leolo.nrinfo.service;

import org.leolo.nrinfo.dao.TiplocDao;
import org.leolo.nrinfo.dto.response.StationSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationSearchService {

    private Logger log = LoggerFactory.getLogger(StationSearchService.class);

    @Autowired private ConfigurationService configurationService;
    @Autowired private TiplocDao tiplocDao;

    public List<StationSearchResult> simpleSearch(String query, int sizeLimit) {
        if (sizeLimit <= 0) {
            sizeLimit = configurationService.getInt("schedule.default_size", 10);
        } else if (sizeLimit > configurationService.getInt("schedule.max_size", 500)) {
            throw new IllegalArgumentException("size limit exceeded");
        }
        log.debug("Station search : \"{}\", simple mode, max size@ {}", query, sizeLimit);
        try {
            List<StationSearchResult> stationSearchResults =  tiplocDao.doSimpleSearch(query, sizeLimit, 0);
            for (StationSearchResult stationSearchResult : stationSearchResults) {
                //Fill in associated TIPLOCs
                stationSearchResult.getAssociatedTiploc().addAll(tiplocDao.findAssociatedTiplocs(stationSearchResult.getTiplocCode()));
            }
            return stationSearchResults;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public List<StationSearchResult> simpleSearch(String query) {
        return simpleSearch(query, configurationService.getInt("schedule.default_size", 10));
    }

}
