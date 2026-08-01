package org.leolo.nrinfo.controller;

import org.leolo.nrinfo.dto.response.StationSearchResult;
import org.leolo.nrinfo.service.StationSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StationSearchController {

    private Logger log = LoggerFactory.getLogger(StationSearchController.class);

    @Autowired private StationSearchService stationSearchService;

    @RequestMapping(
           path = "stations/search",
           method = RequestMethod.GET
    )
    public ResponseEntity search(
            @RequestParam(name="q") String query,
            @RequestParam(name="mode", defaultValue = "simple") String searchMode,
            @RequestParam(name="size", defaultValue = "0") int size
    ) {
        log.debug("Station search query: {} in {} mode", query, searchMode);
        List<StationSearchResult> searchResult = null;
        if ("simple".equals(searchMode)) {
            searchResult = stationSearchService.simpleSearch(query, size);
        } else if ("detailed".equals(searchMode)) {
            return ResponseUtil.buildNotImplementedResponse();
        } else {
            // Unknown mode
            return ResponseUtil.buildFullErrorResponse("Unknown search mode","Search mode must be simple or detailed");
        }
        if (searchResult != null) {
            return ResponseEntity.ok(Map.of("status", "success", "result", searchResult));
        }
        return ResponseEntity.ok(Map.of("status", "success", "result", ""));
    }

}
