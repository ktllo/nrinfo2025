package org.leolo.nrinfo.model;

import lombok.Getter;

public enum TrainCategory {

    LU_METRO("OL","London Underground/Metro Service"),
    UNADVERTISED_ORDINARY("OU","Unadvertised Ordinary Passenger"),
    ORDINARY("OO","Ordinary Passenger"),
    STAFF("OS","Staff Passenger"),
    MIXED("OW","Mixed"),
    CHANNEL_TUNNEL("XC","Channel Tunnel"),
    INTERNATIONAL_SLEEPER("XD","Sleeper"),
    INTERNATIONAL("XI","International"),
    MOTORRAIL("XR","MotorRail"),
    UNADVERTISED_EXPRESS("XU","Unadvertised Express"),
    EXPRESS_PASSENGER("XX","Express Passenger"),
    DOMESTIC_SLEEPER("XZ","Domestic Sleeper"),
    REPLACEMENT_BUS("BR", "Replacement Bus due to engineering work"),
    WTT_BUS("BS","Bus - WTT Service"),
    SHIPPING("SS","Ship"),
    EMPTY("EE","Empty Coaching Stock"),
    EMPTY_METRO("EL","ECS, London Underground/Metro Service"),
    EMPTY_STAFF("ES","ECS & Staff"),
    POSTAL("JJ","Postal"),
    PO_PARCEL("PM","Post Office controlled parcel"),
    PARCEL("PP","Parcel"),
    EMPTY_NPCCS("PV","Empty NPCCS"),
    DEPARTMENTAL("DD","Departmental"),
    CIVIL_ENGINEERING("DH","Civil Engineer"),
    MECH_ELEC_ENGINEERING("DI","Mechanical & Electrical Engineer"),
    STORES("DQ", "Stores"),
    TEST("DT","Test"),
    SIGNAL_TELECOM("DY", "Signal & Telecommunications Engineer"),
    LOCO_BRAKE_VAN("ZB", "Locomotive & Brake Van"),
    LIGHT_LOCO("ZZ","Light Locomotive"),
    OTHER("**", "Other");

    private String categoryCode;
    private String displayName;

    TrainCategory(String categoryCode, String displayName) {
        this.categoryCode = categoryCode;
        this.displayName = displayName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TrainCategory getTrainCategory(String categoryCode) {
        for (TrainCategory trainCategory : TrainCategory.values()) {
            if (trainCategory.getCategoryCode().equals(categoryCode)) {
                return trainCategory;
            }
        }
        return OTHER;
    }
}
