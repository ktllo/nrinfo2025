package org.leolo.nrinfo.service;

import org.leolo.nrinfo.dao.ConfigurationDao;
import org.leolo.nrinfo.model.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ConfigurationService {

    private Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
    private final Object SYNC_TOKEN = new Object();

    @Autowired private DataSource dataSource;
    @Autowired private Environment env;
    @Autowired private ConfigurationDao configurationDao;

    private Map<String, CacheEntry> cache = new Hashtable<>();

    public void clearCache() {
        logger.info("Clearing configuration cache");
        cache.clear();
    }

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void removeExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        ArrayList<String> expiredEntries = new ArrayList<>();
        synchronized (SYNC_TOKEN) {
            for (String key : cache.keySet()) {
                CacheEntry entry = cache.get(key);
                if (entry.source != CacheSource.DATABASE && entry.expires < currentTime) {
                    expiredEntries.add(key);
                }
            }
            for (String key : expiredEntries) {
                cache.remove(key);
            }
        }
        logger.info("Removed {} expired configuration cache", expiredEntries.size());
    }

    public String getConfiguration(String key, String defaultValue) {
        //Step 1: Check the cache
        if (cache.containsKey(key)) {
            CacheEntry entry = cache.get(key);
            if (entry.source!=CacheSource.DATABASE || entry.expires > System.currentTimeMillis()) {
                return entry.value;
            } else {
                logger.debug("{} was expired. Going to delete it", key);
            }
        }
        //Step 2: Get the value
        String value = null;
        //Step 2a: Check the database
        Configuration conf = null;
        try {
            conf = configurationDao.getConfigurationByConfigurationName(key);
        } catch (SQLException e) {
            logger.warn("Error when getting configuration from database - {}", e.getMessage(), e);
        }
        if (conf != null) {
            //We get the configuration!
            CacheEntry entry = new CacheEntry(
                    conf.getConfigurationValue(),
                    System.currentTimeMillis() + conf.getMaxCacheTime(),
                    CacheSource.DATABASE
            );
            cache.put(key, entry);
            return entry.value;
        }
        //Step 2b: Check the environment
        if (env.containsProperty(key)) {
            value = env.getProperty(key);
            CacheEntry entry = new CacheEntry(
                    value,
                    Long.MAX_VALUE,
                    CacheSource.ENVIRONMENT
            );
            cache.put(key, entry);
            return entry.value;
        }
        return defaultValue;
    }

    public String getConfiguration(String key) {
        return getConfiguration(key, null);
    }

    private static class CacheEntry {
        String value;
        long expires;
        CacheSource source;

        public CacheEntry(String value, long expires, CacheSource source) {
            this.value = value;
            this.expires = expires;
            this.source = source;
        }
    }

    private static enum CacheSource {
        DATABASE,
        ENVIRONMENT;
    }

}
