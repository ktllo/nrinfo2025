package org.leolo.nrinfo.model;

import lombok.Getter;
import lombok.Setter;
import org.leolo.nrinfo.util.MiscUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represent a record in National Public Transport Gazetteer
 */
@Getter
@Setter
public class Nptg {

    private String NptgCode;
    private String localityName;
    private String shortName;
    private String qualifierName;
    private String adminAreaCode;
    private String districtCode;
    private String gridCode;
    private int easting;
    private int northing;
    private double longitude;
    private double latitude;
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
