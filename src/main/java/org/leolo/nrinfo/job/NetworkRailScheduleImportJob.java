package org.leolo.nrinfo.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.leolo.nrinfo.dao.DatabaseOperationResult;
import org.leolo.nrinfo.dao.TiplocDao;
import org.leolo.nrinfo.dto.external.networkrail.Tiploc;
import org.leolo.nrinfo.enums.JobMessageType;
import org.leolo.nrinfo.service.ConfigurationService;
import org.leolo.nrinfo.service.JobService;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

@Component
@Scope("prototype")
public class NetworkRailScheduleImportJob extends AbstractJob {

    private static  Logger logger = LoggerFactory.getLogger(NetworkRailScheduleImportJob.class);

    private String URL = "https://publicdatafeeds.networkrail.co.uk/ntrod/CifFileAuthenticate?type=CIF_ALL_FULL_DAILY&day=toc-full";

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private JobService jobService;

    @Autowired
    private TiplocService tiplocService;

    @Autowired private ConfigurationService configurationService;
    @Autowired
    private TiplocDao tiplocDao;

    @Override
    public void run() {
        jobService.writeMessage(this, "Loading Network Rail Schedule from "+URL);
        String nrUsername = configurationService.getString("networkrail.username");
        String nrPassword = configurationService.getString("networkrail.password");
        int batchSize = configurationService.getInt("networkrail.batch_size", 500);
        if (nrUsername == null && nrPassword == null) {
            throw new RuntimeException("No username or password provided");
        }
        try (
                InputStream is = HttpRequestUtil.sendSimpleRequestAsStream(
                        URL,
                        nrUsername,
                        nrPassword
                );
                BufferedReader isr = new BufferedReader(new InputStreamReader(new GZIPInputStream(is)))
        ){
            ArrayList<Tiploc> tiplcos = new ArrayList<Tiploc>(batchSize);
            DatabaseOperationResult reTiploc = new DatabaseOperationResult();
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
                } else {
                    logger.info("Message type {} is not supported", messageType);
                    break;
                }
                // Check for batch size
                if (tiplcos.size() >= batchSize) {
                    reTiploc = reTiploc.add(tiplocService.processTiplocBatch(tiplcos));
                    tiplcos.clear();
                }
            }
            //Process the remaining items
            reTiploc = reTiploc.add(tiplocService.processTiplocBatch(tiplcos));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
