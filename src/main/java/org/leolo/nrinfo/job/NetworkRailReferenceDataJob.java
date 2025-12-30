package org.leolo.nrinfo.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.leolo.nrinfo.dao.CorpusDao;
import org.leolo.nrinfo.dao.DatabaseOperationResult;
import org.leolo.nrinfo.model.Corpus;
import org.leolo.nrinfo.service.ConfigurationService;
import org.leolo.nrinfo.service.JobService;
import org.leolo.nrinfo.util.HttpRequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;

@Component
@Scope("prototype")
public class NetworkRailReferenceDataJob extends AbstractJob {

    private Logger logger = LoggerFactory.getLogger(NetworkRailReferenceDataJob.class);
    @Autowired
    private ConfigurationService configService;
    @Autowired
    JobService jobService;
    @Autowired private CorpusDao corpusDao;
    private ObjectMapper mapper = new ObjectMapper();


    public static final String CORPUS_LINK = "https://publicdatafeeds.networkrail.co.uk/ntrod/SupportingFileAuthenticate?type=CORPUS";

    @Override
    public void run() {
        logger.info("NetworkRailReferenceDataJob started");
        long startTime = System.currentTimeMillis();
        loadCorpus();
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        logger.info("NetworkRailReferenceDataJob completed in {} ms", elapsedTime);
        jobService.writeMessage(this, "CORPUS loading completed in " + elapsedTime + " ms");
    }

    private void loadCorpus() {
        try (
                InputStream is = HttpRequestUtil.sendSimpleRequestAsStream(
                        CORPUS_LINK,
                        configService.getConfiguration("networkrail.username"),
                        configService.getConfiguration("networkrail.password")
                );
                BufferedReader isr = new BufferedReader(new InputStreamReader(new GZIPInputStream(is)))
        ){
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = isr.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            JsonNode node = mapper.readTree(sb.toString());
            if (!node.isObject()) {
                throw new RuntimeException("Data is not a JSON object");
            }
            String messageType = node.fieldNames().next();
            logger.info("Message type: {}", messageType);

            Object data = mapper.convertValue(node.get("TIPLOCDATA"), Corpus[].class);
            DatabaseOperationResult dor = corpusDao.upsertCorpus((Corpus[]) data);
            logger.info("Inserted: {}, Updated: {}", dor.getInserted(), dor.getUpdated());
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
