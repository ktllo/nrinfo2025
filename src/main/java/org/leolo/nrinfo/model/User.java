package org.leolo.nrinfo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class User {
    private int userId;
    private String username;
    private String password;
    private Date lastPasswordDate;
    private Date createDate;
    private Date lastLoginDate;
    private Date lastUpdateDate;
    private boolean forcePasswordChange = false;
    private int failedLoginCount;
    private Date lastFailedLoginDate;

    public User() {
    }

    public User(int userId, String username, String password, Date createDate, Date lastUpdateDate) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.createDate = createDate;
        this.lastUpdateDate = lastUpdateDate;
    }

    public User(int userId, String username, String password, Date lastPasswordDate, Date createDate, Date lastLoginDate, Date lastUpdateDate) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.lastPasswordDate = lastPasswordDate;
        this.createDate = createDate;
        this.lastLoginDate = lastLoginDate;
        this.lastUpdateDate = lastUpdateDate;
    }

    public User(int userId, String username, String password, Date lastPasswordDate, Date createDate, Date lastLoginDate, Date lastUpdateDate, boolean forcePasswordChange) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.lastPasswordDate = lastPasswordDate;
        this.createDate = createDate;
        this.lastLoginDate = lastLoginDate;
        this.lastUpdateDate = lastUpdateDate;
        this.forcePasswordChange = forcePasswordChange;
    }
}
