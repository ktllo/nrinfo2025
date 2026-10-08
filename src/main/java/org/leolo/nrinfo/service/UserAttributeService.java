package org.leolo.nrinfo.service;

import org.leolo.nrinfo.dao.UserAttributeDao;
import org.leolo.nrinfo.model.UserAttribute;
import org.leolo.nrinfo.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserAttributeService {

    private static Logger logger = LoggerFactory.getLogger(UserAttributeService.class);
    @Autowired
    private UserAttributeDao userAttributeDao;
    @Autowired CurrentUserService currentUserService;

    private Map<String, UserAttribute> attributeMap = null;

    private synchronized void init() {
        if (attributeMap != null) {
            return;
        }
        try {
            attributeMap = userAttributeDao.getAllAttributeForUser(currentUserService.getUserId());
        } catch (SQLException sqle) {
            logger.error(sqle.getMessage());
        }
    }

    public boolean getBoolean(String attributeName, boolean defaultValue) {
        init();
        UserAttribute userAttribute = attributeMap.get(attributeName);
        if (userAttribute == null) {
            return defaultValue;
        }
        return CommonUtil.parseBoolean(userAttribute.getAttributeValue(), defaultValue);
    }

    public boolean getBoolean(String attributeName) {
        return getBoolean(attributeName, false);
    }

    public int getInt(String attributeName, int defaultValue) {
        init();
        UserAttribute userAttribute = attributeMap.get(attributeName);
        if (userAttribute == null) {
            return defaultValue;
        }
        return Integer.parseInt(userAttribute.getAttributeValue());
    }

    public int getInt(String attributeName) {
        return getInt(attributeName, 0);
    }

    public String getString(String attributeName, String defaultValue) {
        init();
        UserAttribute userAttribute = attributeMap.get(attributeName);
        if (userAttribute == null) {
            return defaultValue;
        }
        return userAttribute.getAttributeValue();
    }

    public String getString(String attributeName) {
        return getString(attributeName, null);
    }

}
