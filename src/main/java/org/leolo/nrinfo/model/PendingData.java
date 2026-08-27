package org.leolo.nrinfo.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PendingData<E> {

    private static Logger log = LoggerFactory.getLogger(PendingData.class);
    private E data;
    private boolean dataReady = false;

    private final Object LOCK = new Object();

    private List<Object> listeners = new ArrayList<>();

    public PendingData(E data) {
        this.data = data;
        dataReady = true;
    }

    public PendingData() {

    }

    public E getData() {
        if (!dataReady) {
            log.debug("Waiting for pending data to be read");
            Object lock = new Object();
            synchronized (lock) {
                listeners.add(lock);
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return data;
    }

    public void setData(E data) {
        synchronized (LOCK) {
            if (!dataReady) {
                this.data = data;
                dataReady = true;
                for (Object listener : listeners) {
                    synchronized (listener) {
                        listener.notify();
                    }
                }
            } else {
                throw new IllegalStateException("Data is already set");
            }
        }
    }
}
