package org.leolo.nrinfo.enums;

public enum JobMessageType {

    EXCEPTION("X"),
    MESSAGE("M");

    private JobMessageType(String code) {
        this.code = code;
    }

    private String code;

    public String getCode() {
        return code;
    }

    public JobMessageType fromCode(String code) {
        for (JobMessageType t : JobMessageType.values()) {
            if (t.code.equals(code)) {
                return t;
            }
        }
        return null;
    }
}
