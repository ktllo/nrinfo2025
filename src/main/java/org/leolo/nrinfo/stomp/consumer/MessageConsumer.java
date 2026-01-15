package org.leolo.nrinfo.stomp.consumer;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MessageConsumer {

    protected final Object SYNC_TOKEN = new Object();

    protected LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<String>();

    public void addToQueue(String message) {
        messageQueue.add(message);
    }

}
