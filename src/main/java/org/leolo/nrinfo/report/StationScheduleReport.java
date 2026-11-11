package org.leolo.nrinfo.report;

import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.dto.request.ScheduleSearch;
import org.leolo.nrinfo.dto.response.ScheduleSearchResult;
import org.leolo.nrinfo.dto.response.TrainScheduleEntry;
import org.leolo.nrinfo.model.report.ExecutionResult;
import org.leolo.nrinfo.model.report.ReportParameters;
import org.leolo.nrinfo.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class StationScheduleReport extends ReportProvider {

    private static Logger log = LoggerFactory.getLogger(StationScheduleReport.class);

    @Autowired private ScheduleService scheduleService;

    @Override
    public ExecutionResult executeReport(List<ReportParameters> parametersList) {
        String station = null;
        LocalDate genDate = null;
        for (ReportParameters parameter : parametersList) {
            switch (parameter.getPosition()) {
                case 1:
                    station = parameter.getParameterValue();
                    break;
                case 2:
                    genDate = LocalDate.parse(parameter.getParameterValue());
                    break;
                default:
                    log.warn("Unknwon postion {} with name {}", parameter.getPosition(), parameter.getParameterName());
            }
        }
        if (station == null || genDate == null) {
            throw new IllegalArgumentException("Station and genDate are mandatory");
        }
        //Build the search parameters
        ScheduleSearch scheduleSearch = new ScheduleSearch();
        scheduleSearch.setLocation(station);
        scheduleSearch.setFromTime(new Date(genDate.atStartOfDay().atZone(Constants.DEFAULT_TIMEZONE).toEpochSecond() * 1000));
        scheduleSearch.setToTime(new Date(genDate.plusDays(1).atStartOfDay().atZone(Constants.DEFAULT_TIMEZONE).toEpochSecond() * 1000));
        List<ScheduleSearchResult> searchResults;
        try {
            searchResults = scheduleService.searchTrainScheduleForScheduleSearchResult(scheduleSearch);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        log.debug("Search result : {} rows", searchResults.size());
        ExecutionResult result = new ExecutionResult();
        ArrayList<String> headers = new ArrayList<String>();
        ArrayList<String> data = new ArrayList<>();
        result.setHeader(new ArrayList<>(List.of(
                "Train UID",
                "Operator",
                "Headcode",
                "Departure at origin",
                "Origin",
                "Destination",
                "Train type",
                "Arrival time",
                "Departure time",
                "Platform"
        )));
        SimpleDateFormat timeOnly = new SimpleDateFormat("HH:mm:ss");
        for (ScheduleSearchResult searchResult : searchResults) {
            String platform = "";
            String arrivalTime = "";
            String departureTime = "";
            if (searchResult.getDetail() != null) {
                TrainScheduleEntry entry = searchResult.getDetail();
                if (entry.getPlatform() != null) {
                    platform = searchResult.getDetail().getPlatform();
                }
                //Arrival time
                if (entry.getGbttArrivalTime() != null) {
                    arrivalTime = entry.getGbttArrivalTime().substring(11);
                } else if (entry.getWttArrivalTime() != null) {
                    arrivalTime = entry.getWttArrivalTime().substring(11);
                } else if (entry.getWttPassTime() != null) {
                    arrivalTime = entry.getWttPassTime().substring(11);
                    departureTime = "pass";
                } else {
                    log.warn("No time infomation for {}", searchResult.getSummary().getTrainUid());
                }
                //Departure time
                if (entry.getGbttDepartureTime() != null) {
                    departureTime = entry.getGbttDepartureTime().substring(11);
                } else if (entry.getWttDepartureTime() != null) {
                    departureTime = entry.getWttDepartureTime().substring(11);
                }
            }
            result.addRow(new ArrayList<>(List.of(
                    searchResult.getSummary().getTrainUid(),
                    searchResult.getSummary().getTrainOperator(),
                    searchResult.getSummary().getSignalHeadcode(),
                    searchResult.getSummary().getDepartureTime().substring(11),
                    searchResult.getSummary().getOriginDisplayName(),
                    searchResult.getSummary().getDestinationDisplayName(),
                    searchResult.getSummary().getTrainType(),
                    arrivalTime,
                    departureTime,
                    platform
            )));
        }
        return result;
    }
}
