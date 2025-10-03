package org.leolo.nrinfo.service;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    public String encryptPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

}
