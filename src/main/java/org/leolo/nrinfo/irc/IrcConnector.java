package org.leolo.nrinfo.irc;

import org.leolo.nrinfo.service.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class IrcConnector {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired private ConfigurationService configService;

    @EventListener(ApplicationReadyEvent.class)
    public void connect() {
        logger.info("Checking if IRC connection is required");
    }

}
