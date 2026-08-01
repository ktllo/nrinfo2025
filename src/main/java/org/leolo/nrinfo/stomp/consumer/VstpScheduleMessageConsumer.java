package org.leolo.nrinfo.stomp.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.leolo.nrinfo.dao.StompFailedMessageDao;
import org.leolo.nrinfo.dto.external.networkrail.RealTimePerformance;
import org.leolo.nrinfo.dto.external.networkrail.Schedule;
import org.leolo.nrinfo.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class VstpScheduleMessageConsumer extends  MessageConsumer{
    private static final Logger log = LoggerFactory.getLogger(VstpScheduleMessageConsumer.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private StompFailedMessageDao stompFailedMessageDao;

    public VstpScheduleMessageConsumer() {
        new ProcessingThread().start();
    }


    private void processMessage(String message) throws Exception{
        if (message == null) {
            log.warn("No message received");
            return;
        }
        try {
            JsonNode node = mapper.readTree(message);
            String messageType = node.fieldNames().next();
            if ("VSTPCIFMsgV1".equals(messageType)) {
                log.info("VSTPCIFMsgV1 received");
            }
            stompFailedMessageDao.insertFailedMessage(
                    this.getClass(),
                    message,
                    new Exception("Testing data collection")
            );
        } catch (Exception e) {
            stompFailedMessageDao.insertFailedMessage(
                    this.getClass(),
                    message,
                    e
            );
            throw e;
        }
    }

    class ProcessingThread extends Thread {
        public void run() {
            while(true) {
                try {
                    processMessage(messageQueue.take());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
