package org.leolo.nrinfo.enums;

import lombok.Getter;
@Getter
public enum OperatingCharacteristic {

    VACUUM_BRAKED("B", "Vacuum Braked"),
    TIMED_AT_100_MPH("C", "Timed at 100 mph"),
    DOO("D", "Driver Only Operations "),
    MARK_4_COACHES("E", "Mark 4 Coaches"),
    GUARD_REQUIRED("G", "Guard Required"),
    TIMED_AT_110_MPH("M", "Timed at 110 mph"),
    PUSH_PULL("P", "Push/Pull Train"),
    AS_REQUIRED("Q", "As-Required"),
    AIR_CONDITIONED_WITH_PA_SYS("R", "Air Conditioned With Systems"),
    STREAM_HEATED("S", "Stream Heated"),
    TO_TERMINAL_OR_YARD_AS_REQUIRED("Y", "To Terminal or Yard As-Required"),
    SB1C_GAUGE("Z", "SB1C Gauge");




    private final String code;
    private final  String description;

    OperatingCharacteristic(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OperatingCharacteristic getByCode(String code){
        for (OperatingCharacteristic operatingCharacteristic : OperatingCharacteristic.values()) {
            if (operatingCharacteristic.code.equals(code)){
                return operatingCharacteristic;
            }
        }
        return null;
    }

    public static OperatingCharacteristic getByCode(char code){
        return getByCode(Character.toString(code));
    }
}
