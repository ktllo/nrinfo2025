package org.leolo.nrinfo.enums;

import lombok.Getter;

public enum JobMessageType {

    EXCEPTION("X","Exception"),
    /** This value is added because some data is incorrect */
    @Deprecated
    EXCEPTION_ALIAS_1("E","Exception"),
    MESSAGE("M","Message");

    private JobMessageType(String code, String displayName) {

        this.code = code;
        this.displayName = displayName;
    }

    @Getter private final String code;
    @Getter private final String displayName;


    public static JobMessageType fromCode(String code) {
        for (JobMessageType t : JobMessageType.values()) {
            if (t.code.equals(code)) {
                return t;
            }
        }
        return null;
    }
}
