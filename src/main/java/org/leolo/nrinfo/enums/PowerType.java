package org.leolo.nrinfo.enums;

import lombok.Getter;

@Getter
public enum PowerType {

    DIESEL("D","Diesel"),
    DIESEL_ELECTRIC_MU("DEM","Diesel Electric Multiple Unit"),
    DIESEL_MECHANICAL_MU("DMU","Diesel Mechanical Unit"),
    ELECTRIC("E","Electric"),
    ELECTRO_DIESEL("ED","Electro-Diesel"),
    EMU_PLUS_LOCO("EM","EMU + Locomotive"),
    ELECTRIC_MU("EMU","Electric Multiple Unit"),
    HIGH_SPEED("HST","High Speed Train"),
    UNKNOWN("U","Unknown");

    private final String code;
    private final String description;
    PowerType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PowerType fromCode(String code) {
        for (PowerType type : PowerType.values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
