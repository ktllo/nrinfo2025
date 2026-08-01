package org.leolo.nrinfo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GenericCacheService {

    private final Logger log = LoggerFactory.getLogger(GenericCacheService.class);

    private final Object lock = new Object();

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<String, CacheEntry>();

    public Object getEntry(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() >= entry.expiry) {
            cache.remove(key);
            return null;
        }
        entry.lastAccess = System.currentTimeMillis();
        if (entry.mode == CacheMode.FROM_LAST_ACCESS_TIME) {
            entry.expiry = System.currentTimeMillis() + entry.maxAge;
        }
        return entry.data;
    }

    public boolean hasEntry(String key) {
        return cache.containsKey(key);
    }

    public void addToCache(String key, Object data, long maxAge, CacheMode cacheMode, boolean strict) {
        CacheEntry ce = new CacheEntry();
        ce.data = data;
        ce.created = System.currentTimeMillis();
        ce.lastAccess = System.currentTimeMillis();
        ce.expiry = ce.created + maxAge;
        ce.mode = cacheMode;
        ce.maxAge = maxAge;
        if (strict) {
            synchronized (lock) {
                if (cache.containsKey(key)) {
                    throw new RuntimeException("Cache already exists");
                } else {
                    cache.put(key, ce);
                }
            }
        } else {
            cache.put(key, ce);
        }
    }

    @Scheduled(fixedRate = 60)
    public void removeExpiredEntry() {
        synchronized (lock) {
            log.info("removing expired entry");
            ArrayList<String> toDelete = new ArrayList<String>();
            for (String key : cache.keySet()) {
                CacheEntry entry = cache.get(key);
                if (System.currentTimeMillis() >= entry.expiry) {
                    toDelete.add(key);
                }
            }
            log.info("Going to delete {} entries", toDelete.size());
            for (String key : toDelete) {
                cache.remove(key);
            }
        }
    }

    static class CacheEntry {
        Object data;
        long created;
        long lastAccess;
        long expiry;
        long maxAge;
        CacheMode mode = CacheMode.FIXED_LIFETIME;
    }

    public enum CacheMode {
        FIXED_LIFETIME,
        FROM_LAST_ACCESS_TIME;
    }

}
