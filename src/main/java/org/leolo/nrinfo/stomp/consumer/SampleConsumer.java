package org.leolo.nrinfo.stomp.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SampleConsumer extends MessageConsumer{

    private static final Logger log = LoggerFactory.getLogger(SampleConsumer.class);

    private void processMessage(String message) throws Exception{
        log.info("Message received: {}", message);
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
