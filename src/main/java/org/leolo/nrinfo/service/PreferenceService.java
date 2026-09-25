package org.leolo.nrinfo.service;

import org.leolo.nrinfo.dao.UserPreferenceDao;
import org.leolo.nrinfo.model.UserPreferenceOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

@Service
public class PreferenceService {

    @Autowired private UserPreferenceDao userPreferenceDao;
    private static final Logger log = LoggerFactory.getLogger(PreferenceService.class);

    private Hashtable<String, List<UserPreferenceOption>> optionMap = new Hashtable<>();

    public List<UserPreferenceOption> getUserPreferenceOptions(String preferenceName) throws SQLException {
        List<UserPreferenceOption> options = optionMap.get(preferenceName);
        if (options == null) {
            //Cache miss
            options = userPreferenceDao.getPreferenceOptionsByPreferenceName(preferenceName);
            optionMap.put(preferenceName, options);
        }
        return options;
    }

    public Set<String> getAllUserPreferenceNames() {
        try {
            return userPreferenceDao.getAllPreferenceNames();
        } catch (SQLException e) {
            log.error("Error while retrieving user preference list", e);
            return Set.of();
        }
    }
}
