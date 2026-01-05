package org.leolo.nrinfo.service;

import org.leolo.nrinfo.dto.response.StreamHealth;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Hashtable;

@Service
public class DataStreamHealthService {

    private Hashtable<String, Hashtable<String, Instant>> lastStreamUpdate = new Hashtable<>();

    private Hashtable<String, ConnectionStatus> connectionStatusMap = new Hashtable<>();

    private Hashtable<String, String> keyMap = new Hashtable<>();

    public void registerSource(String key, String name) {
        keyMap.put(key, name);
        connectionStatusMap.put(key, ConnectionStatus.NOT_CONNECTED);
        lastStreamUpdate.put(key, new Hashtable<>());
    }

    public void setConnectionStatus(String connectionKey, ConnectionStatus connectionStatus) {
        if(keyMap.containsKey(connectionKey)) {
            connectionStatusMap.put(connectionKey, connectionStatus);
        }
    }

    public void messageReceived(String key, String subType) {
        if(keyMap.containsKey(key)) {
            lastStreamUpdate.get(key).put(subType, Instant.now());
        }
    }

    public StreamHealth[] getStreamHealth() {
        ArrayList<StreamHealth> streamHealthList = new ArrayList<>();
        for(String key : keyMap.keySet()) {
            StreamHealth streamHealth = new StreamHealth();
            streamHealth.setStreamType(key);
            streamHealth.setStreamTypeName(keyMap.get(key));
            streamHealth.setConnectionStatus(
                    switch (connectionStatusMap.get(key)) {
                        case NOT_CONNECTED -> "Not connected yet";
                        case CONNECTED -> "Connected";
                        case CONNECTING -> "Connecting";
                        case DISCONNECTED -> "Disconnected";
                    }
            );
            for(String subType : lastStreamUpdate.get(key).keySet()) {
                StreamHealth.SubTypeHealth subTypeHealth = new StreamHealth.SubTypeHealth();
                subTypeHealth.setSubType(subType);
                subTypeHealth.setLastMessageTime(lastStreamUpdate.get(key).get(subType));
                streamHealth.getSubtype().add(subTypeHealth);
            }
            streamHealthList.add(streamHealth);
        }
        return streamHealthList.toArray(new StreamHealth[0]);
    }




    public static enum ConnectionStatus {
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        NOT_CONNECTED;
    }

}
