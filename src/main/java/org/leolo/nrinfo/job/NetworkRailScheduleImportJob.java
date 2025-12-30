package org.leolo.nrinfo.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.leolo.nrinfo.dao.DatabaseOperationResult;
import org.leolo.nrinfo.dto.external.networkrail.Association;
import org.leolo.nrinfo.dto.external.networkrail.Schedule;
import org.leolo.nrinfo.dto.external.networkrail.Tiploc;
import org.leolo.nrinfo.service.ConfigurationService;
import org.leolo.nrinfo.service.JobService;
import org.leolo.nrinfo.service.ScheduleService;
import org.leolo.nrinfo.service.TiplocService;
import org.leolo.nrinfo.util.HttpRequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

@Component
@Scope("prototype")
public class NetworkRailScheduleImportJob extends AbstractJob {

    private static  Logger logger = LoggerFactory.getLogger(NetworkRailScheduleImportJob.class);

    public static final String FULL_URL = "https://publicdatafeeds.networkrail.co.uk/ntrod/CifFileAuthenticate?type=CIF_ALL_FULL_DAILY&day=toc-full";
    public static final  String DIFF_URL = "https://publicdatafeeds.networkrail.co.uk/ntrod/CifFileAuthenticate?type=CIF_ALL_UPDATE_DAILY&day=toc-update-%s";
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private JobService jobService;

    @Autowired
    private TiplocService tiplocService;

    @Autowired private ConfigurationService configurationService;
    @Autowired private ScheduleService scheduleService;

    @Getter
    @Setter
    private String url = FULL_URL;

    @Override
    public void run() {
        jobService.writeMessage(this, "Loading Network Rail Schedule from "+ url);
        String nrUsername = configurationService.getString("networkrail.username");
        String nrPassword = configurationService.getString("networkrail.password");
        int batchSize = configurationService.getInt("networkrail.batch_size", 500);
        if (nrUsername == null && nrPassword == null) {
            throw new RuntimeException("No username or password provided");
        }
        try (
                InputStream is = HttpRequestUtil.sendSimpleRequestAsStream(
                        url,
                        nrUsername,
                        nrPassword
                );
                BufferedReader isr = new BufferedReader(new InputStreamReader(new GZIPInputStream(is)))
        ){
            ArrayList<Tiploc> tiplcos = new ArrayList<Tiploc>(batchSize);
            ArrayList<Association> associations = new ArrayList<>(batchSize);
            ArrayList<Schedule> schedules = new ArrayList<>(batchSize);
            DatabaseOperationResult reTiploc = new DatabaseOperationResult();
            DatabaseOperationResult reAssociation = new DatabaseOperationResult();
            DatabaseOperationResult reSchedule = new DatabaseOperationResult();
            while (true) {
                String line = isr.readLine();
                if (line == null) {
                    break;
                }
                JsonNode node = mapper.readTree(line);
                String messageType = node.fieldNames().next();
                if (messageType.equals("JsonTimetableV1")) {
                    logger.warn("JsonTimetableV1 is currently ignored");
                } else if (messageType.equals("TiplocV1")) {
                    Tiploc tiploc = mapper.convertValue(node.get("TiplocV1"), Tiploc.class);
                    tiplcos.add(tiploc);
                } else if (messageType.equals("JsonAssociationV1")) {
                    associations.add(mapper.convertValue(node.get("JsonAssociationV1"), Association.class));
                } else if (messageType.equals("JsonScheduleV1")) {
                    Schedule schedule = mapper.convertValue(node.get("JsonScheduleV1"), Schedule.class);
                    schedules.add(schedule);
                } else {
                    logger.info("Message type {} is not supported", messageType);
                    break;
                }
                // Check for batch size
                if (tiplcos.size() >= batchSize) {
                    reTiploc = reTiploc.add(tiplocService.processTiplocBatch(tiplcos));
                    tiplcos.clear();
                }
                if (associations.size() >= batchSize) {
                    reAssociation = reAssociation.add(scheduleService.processAssociationBatch(associations));
                    associations.clear();
                }
                if (schedules.size() >= batchSize) {
                    reSchedule = reSchedule.add(scheduleService.processScheduleBatch(schedules));
                    schedules.clear();
                }

            }
            //Process the remaining items
            reTiploc = reTiploc.add(tiplocService.processTiplocBatch(tiplcos));
            reAssociation = reAssociation.add(scheduleService.processAssociationBatch(associations));
            reSchedule = reSchedule.add(scheduleService.processScheduleBatch(schedules));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
