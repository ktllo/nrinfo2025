package org.leolo.nrinfo.enums;

import lombok.Getter;

public enum SortOrder {

    ASC("asc"), DESC("desc");

    @Getter private final String code;

    private SortOrder(String code) {
        this.code = code;
    }

    public static SortOrder getByCode(String code) {
        for (SortOrder sortOrder : SortOrder.values()) {
            if (sortOrder.code.equalsIgnoreCase(code)) {
                return sortOrder;
            }
        }
        return null;
    }
}
