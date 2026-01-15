package org.leolo.nrinfo.stomp;

import org.apache.activemq.transport.stomp.Stomp;
import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.activemq.transport.stomp.StompFrame;
import org.leolo.nrinfo.job.NetworkRailScheduleImportJob;
import org.leolo.nrinfo.service.ConfigurationService;
import org.leolo.nrinfo.service.DataStreamHealthService;
import org.leolo.nrinfo.stomp.consumer.MessageConsumer;
import org.leolo.nrinfo.stomp.consumer.RealTimePerformanceMessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class StompConnector {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String STOMP_HOST = "publicdatafeeds.networkrail.co.uk";
    public static final int STOMP_PORT = 61618;

    private ConfigurationService configService;

    private ApplicationContext applicationContext;

    private DataStreamHealthService dataStreamHealthService;

    private Thread connectionThread;

    private StompConnection stompConnection;

    private long retryWait = 1000L;

    private Instant connectedTime = null;

    private static final long RECV_TIMEOUT = 120_000L;

    private HashMap<String, MessageConsumer> consumerMap = null;

    public static final String STREAM_TYPE = "NETWORK_RAIL";

    public StompConnector(
            @Autowired ConfigurationService configService,
            @Autowired ApplicationContext applicationContext,
            @Autowired DataStreamHealthService dataStreamHealthService
    ) {
        this.configService = configService;
        this.applicationContext = applicationContext;
        this.dataStreamHealthService = dataStreamHealthService;
        _initMap();
    }

    private void _initMap() {
        consumerMap = new HashMap<>();
        consumerMap.put("RTPPM_ALL", applicationContext.getBean(RealTimePerformanceMessageConsumer.class));
    }


    @EventListener(ApplicationReadyEvent.class)
    public void connect() {
        _connect();
    }

    private void _connect() {
        if (connectionThread != null) {
            logger.warn("Already connected");
            return;
        }
        logger.info("Preparing STOMP connection to NetworkRail");
        dataStreamHealthService.registerSource(STREAM_TYPE,"Network Rail data stream");
        connectionThread = new Thread(() -> {
            while(true) {
                try {
                    stompConnection = new StompConnection();
                    dataStreamHealthService.setConnectionStatus(STREAM_TYPE, DataStreamHealthService.ConnectionStatus.CONNECTING);
                    stompConnection.open(STOMP_HOST, STOMP_PORT);
                    HashMap<String, String> connHeaders = new HashMap<String, String>();
                    connHeaders.put("login", configService.getString("networkrail.username"));
                    connHeaders.put("passcode", configService.getString("networkrail.password"));
                    connHeaders.put("heart-beat", "60000,60000"); // STOMP v1.1 heartbeats
                    connHeaders.put("client-id", configService.getString("networkrail.username")); // STOMP v1.1 heartbeats
                    stompConnection.connect(connHeaders);
                    HashMap<String, String> subHeader = new HashMap<String, String>();
                    String host = InetAddress.getLocalHost().getHostAddress();
                    logger.info("Hostname : {}", host);
                    subHeader.put("activemq.subscriptionName", host + "-rtppm");//TODO: Add a pseudorandom ID per restart
                    stompConnection.subscribe("/topic/RTPPM_ALL",
                            Stomp.Headers.Subscribe.ACK_MODE,
                            subHeader
                    );
                    dataStreamHealthService.setConnectionStatus(STREAM_TYPE, DataStreamHealthService.ConnectionStatus.CONNECTED);
                    connectedTime = Instant.now();
                    while (true) {
                        StompFrame frame = stompConnection.receive(RECV_TIMEOUT);
                        stompConnection.ack(frame);
                        Map<String, String> frameHeaders = frame.getHeaders();
                        logger.info("Frame headers: {}", frameHeaders.keySet());
                        if (frameHeaders.containsKey("destination")) {
                            String destination = frameHeaders.get("destination");
                            logger.info("Frame destination: {}", frame.getHeaders().get("destination"));
                            logger.debug("Frame body: {}", frame.getBody());
                            if (consumerMap.containsKey(destination)) {
                                dataStreamHealthService.messageReceived(STREAM_TYPE, destination);
                                consumerMap.get(destination).addToQueue(frame.getBody());
                            }
                        } else {
                            logger.debug("Unknown message type, avail header keys {}", frameHeaders.keySet());
                            logger.info("message={}", frameHeaders.get("message"));
                        }
                    }
                } catch (Exception e) {
                    logger.error("Connection error", e);
                }
                dataStreamHealthService.setConnectionStatus(STREAM_TYPE, DataStreamHealthService.ConnectionStatus.DISCONNECTED);
                long now = Instant.now().toEpochMilli();
                long runningTime = now - connectedTime.toEpochMilli();
                if (runningTime > 300_000) {
                    retryWait = 1000;
                }
                try {
                    logger.warn("There are issue with STOMP connection. Sleeping for {} ms", retryWait);
                    Thread.sleep(retryWait);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                retryWait *= 2;
            }
        });
        connectionThread.start();
    }

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void sendHeartbeat() {
        if (connectionThread != null && connectionThread.isAlive()) {
            try {
                stompConnection.sendFrame("\n");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            logger.info("Sent heartbeat");
        }
    }
}
