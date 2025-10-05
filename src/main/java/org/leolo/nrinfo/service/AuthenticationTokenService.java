package org.leolo.nrinfo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuthenticationTokenService {

    private Hashtable<String, TokenStoreEntry> tokenStore = new Hashtable<String, TokenStoreEntry>();
    private Logger logger = LoggerFactory.getLogger(AuthenticationTokenService.class);
    @Autowired private ConfigurationService conf;

    public String generateTokenForUser(int userId) {
        long tokenLifeTime = Long.parseLong(conf.getConfiguration("auth.token_life","600")) * 1000;
        String token = UUID.randomUUID().toString();
        TokenStoreEntry entry = new TokenStoreEntry(
                token, userId, System.currentTimeMillis(), System.currentTimeMillis() + tokenLifeTime
        );
        tokenStore.put(token, entry);
        return token;
    }

    public int getTokenOwner(String token) {
        TokenStoreEntry entry = tokenStore.get(token);
        if (entry == null) {
            return 0;
        }
        return entry.userId;
    }

    public void extendTokenLife(String token) {
        TokenStoreEntry entry = tokenStore.get(token);
        long tokenLifeTime = Long.parseLong(conf.getConfiguration("auth.token_life","600")) * 1000;
        if (entry == null) {
            logger.info("Invalid token given for extension. No action taken");
        } else {
            if (entry.expires > System.currentTimeMillis()) {
                entry.expires = System.currentTimeMillis() + tokenLifeTime;
            }
        }
    }

    public void invalidateToken(String token) {
        tokenStore.remove(token);
    }


    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void removeExpiredTokens() {
        HashSet<String> expiredTokens = new HashSet<String>();
        for (TokenStoreEntry entry : tokenStore.values()) {
            if (entry.expires > System.currentTimeMillis()) {
                expiredTokens.add(entry.token);
            }
        }
        for (String token : expiredTokens) {
            tokenStore.remove(token);
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private static class TokenStoreEntry {
        String token;
        int userId;
        long generated;
        long expires;

        public TokenStoreEntry(String token, int userId, long generated, long expires) {
            this.token = token;
            this.userId = userId;
            this.generated = generated;
            this.expires = expires;
        }
    }
}
