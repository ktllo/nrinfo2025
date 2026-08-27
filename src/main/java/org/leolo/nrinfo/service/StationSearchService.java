package org.leolo.nrinfo.service;

import org.leolo.nrinfo.dao.TiplocDao;
import org.leolo.nrinfo.dto.response.DetailedStationSearchResult;
import org.leolo.nrinfo.dto.response.StationSearchResult;
import org.leolo.nrinfo.model.Tiploc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.ResourceTransactionManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Service
public class StationSearchService {

    private Logger log = LoggerFactory.getLogger(StationSearchService.class);

    @Autowired private ConfigurationService configurationService;
    @Autowired private TiplocDao tiplocDao;
    @Autowired
    private ResourceTransactionManager resourceTransactionManager;
    @Autowired
    private TiplocService tiplocService;

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

    public List<DetailedStationSearchResult> detailedSearch(String query, int sizeLimit) {
        if (sizeLimit <= 0) {
            sizeLimit = configurationService.getInt("schedule.default_size", 10);
        } else if (sizeLimit > configurationService.getInt("schedule.max_size", 500)) {
            throw new IllegalArgumentException("size limit exceeded");
        }
        log.debug("Station search : \"{}\", detailed mode, max size@ {}", query, sizeLimit);
        List<DetailedStationSearchResult> results = new ArrayList<>();
        try {
            //Part 1: Get the one that match the criteria
            List<Tiploc> tiplocs = tiplocDao.doDetailedSearch(query, sizeLimit, 0);
            log.debug("{} tiplocs found", tiplocs.size());
            HashMap<String, Tiploc> tiplocMap = new HashMap<>();
            HashSet<String> includedTiplocs = new HashSet<>();
            //Part 2: Preload the cache with existing entries
            for (Tiploc tiploc : tiplocs) {
                tiplocMap.put(tiploc.getTiplocCode(), tiploc);
            }
            //Part 3: Create the result object
            //Part 4: Build the list of associated codes
            for (Tiploc tiploc : tiplocs) {
                if (includedTiplocs.contains(tiploc.getTiplocCode())) {
                    continue;
                }
                includedTiplocs.add(tiploc.getTiplocCode());
                DetailedStationSearchResult dssr = DetailedStationSearchResult.builder()
                        .nalco(tiploc.getNalco())
                        .stanox(tiploc.getStanox())
                        .crsCode(tiploc.getCrsCode())
                        .name(tiploc.getDescription())
                        .shortName(tiploc.getShortDescription())
                        .tiplocCode(tiploc.getTiplocCode())
                        .build();
                List<String> nalco = tiplocDao.findTiplocCodeByNalco(tiploc.getNalco());
                for (String assTiploc: nalco) {
                    if (tiplocMap.containsKey(assTiploc)) {
                        //Cache hit
                        dssr.addAssociatedTiploc(tiplocMap.get(assTiploc), DetailedStationSearchResult.AssociatedType.SAME_NALCO);
                    } else {
                        //Cache miss
                        Tiploc t = tiplocDao.getTiplocByTiplocCode(assTiploc);
                        tiplocMap.put(t.getTiplocCode(), t);
                        dssr.addAssociatedTiploc(tiplocMap.get(assTiploc), DetailedStationSearchResult.AssociatedType.SAME_NALCO);
                    }
                }
                List<String> stanox = tiplocDao.findTiplocCodeByStanox(tiploc.getStanox());
                for (String assTiploc: stanox) {
                    if (tiplocMap.containsKey(assTiploc)) {
                        //Cache hit
                        dssr.addAssociatedTiploc(tiplocMap.get(assTiploc), DetailedStationSearchResult.AssociatedType.SAME_STANOX);
                    } else {
                        //Cache miss
                        Tiploc t = tiplocDao.getTiplocByTiplocCode(assTiploc);
                        tiplocMap.put(t.getTiplocCode(), t);
                        dssr.addAssociatedTiploc(tiplocMap.get(assTiploc), DetailedStationSearchResult.AssociatedType.SAME_STANOX);
                    }
                }
                List<String> crs = tiplocDao.findTiplocCodeByStanox(tiploc.getCrsCode());
                for (String assTiploc: crs) {
                    if (tiplocMap.containsKey(assTiploc)) {
                        //Cache hit
                        dssr.addAssociatedTiploc(tiplocMap.get(assTiploc), DetailedStationSearchResult.AssociatedType.SAME_CRS);
                    } else {
                        //Cache miss
                        Tiploc t = tiplocDao.getTiplocByTiplocCode(assTiploc);
                        tiplocMap.put(t.getTiplocCode(), t);
                        dssr.addAssociatedTiploc(tiplocMap.get(assTiploc), DetailedStationSearchResult.AssociatedType.SAME_CRS);
                    }
                }
                List<String> manualGroupMembers = tiplocDao.getManualGroupMembers(tiploc.getTiplocCode());
                for (String assTiploc: manualGroupMembers) {
                    if (tiplocMap.containsKey(assTiploc)) {
                        //Cache hit
                        dssr.addAssociatedTiploc(tiplocMap.get(assTiploc), DetailedStationSearchResult.AssociatedType.MANUAL_GROUP);
                    } else {
                        //Cache miss
                        Tiploc t = tiplocDao.getTiplocByTiplocCode(assTiploc);
                        tiplocMap.put(t.getTiplocCode(), t);
                        dssr.addAssociatedTiploc(tiplocMap.get(assTiploc), DetailedStationSearchResult.AssociatedType.MANUAL_GROUP);
                    }
                }
                results.add(dssr);
            }
        } catch (SQLException e) {
            log.error("Error while getting detailed tiplocs - {}", e.getMessage(), e);
        }
        return results;
    }

}
