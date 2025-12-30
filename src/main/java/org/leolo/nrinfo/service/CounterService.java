package org.leolo.nrinfo.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Service
public class CounterService {

    private Map<String, Integer> counters = new HashMap<>();

    public synchronized int getCounter(String name) {
        int curVal = 0;
        if (counters.containsKey(name)) {
            curVal = counters.get(name);
        }
        counters.put(name, ++curVal);
        return curVal;
    }

}
