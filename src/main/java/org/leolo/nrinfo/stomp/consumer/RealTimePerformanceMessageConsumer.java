package org.leolo.nrinfo.stomp.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.leolo.nrinfo.dto.external.networkrail.RealTimePerformance;
import org.leolo.nrinfo.service.RealTimePerformanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RealTimePerformanceMessageConsumer extends MessageConsumer {
    private Logger log = LoggerFactory.getLogger(RealTimePerformanceMessageConsumer.class);

    private ObjectMapper mapper = new ObjectMapper();

    private RealTimePerformanceService realTimePerformanceService;

    public RealTimePerformanceMessageConsumer(
            @Autowired RealTimePerformanceService realTimePerformanceService
    ) {
        this.realTimePerformanceService = realTimePerformanceService;
        new ProcessingThread().start();
    }



    private void processMessage(String message) throws Exception{
        if (message == null) {
            log.warn("No message received");
            return;
        }
        JsonNode node = mapper.readTree(message);
        String messageType = node.fieldNames().next();
        if ("RTPPMDataMsgV1".equals(messageType)) {
            RealTimePerformance rtp = mapper.convertValue(node.get("RTPPMDataMsgV1"), RealTimePerformance.class);
            realTimePerformanceService.submitNewSnapshot(rtp);
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
