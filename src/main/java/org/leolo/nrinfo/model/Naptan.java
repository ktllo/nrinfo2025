package org.leolo.nrinfo.model;

import lombok.Getter;
import lombok.Setter;
import org.leolo.nrinfo.util.MiscUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
public class Naptan {
    private String atocCode;
    private String naptanCode;
    private String plateCode;
    private String cleardownCode;
    private String commonName;
    private String shortCommonName;
    private String landmark;
    private String street;
    private String crossing;
    private String indicator;
    private String bearing;
    private String nptgCode;
    private String town;
    private String suburb;
    private String country;
    private String localityCentre;
    private String gridType;
    private int easting;
    private int northing;
    private double longitude;
    private double latitude;
    private String stopType;
    private String busStopType;
    private Date updatedDate;
    private int revisionNumber;


    /**
     * Parse the input date as a string, and store it in updatedDate
     *
     * <p>
     *     Example of valid date:
     *     <ul>
     *         <li>2021-08-17T11:15:30.703</li>
     *         <li>2021-08-17T11:15:30</li>
     *     </ul>
     * </p>
     * @param date Date to be parsed and stored
     */
    public void setUpdatedDate(String date) throws ParseException {
        this.updatedDate = MiscUtil.parseNaptanDate(date);
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
}
