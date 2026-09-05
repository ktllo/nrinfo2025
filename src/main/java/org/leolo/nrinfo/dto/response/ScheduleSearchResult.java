package org.leolo.nrinfo.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

@Getter
@Setter
public class ScheduleSearchResult implements Cloneable {
    private TrainScheduleSummary summary;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TrainScheduleSummary baseSchedule;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Collection<TrainScheduleEntry> otherDetails = new ArrayList<TrainScheduleEntry>();
    private TrainScheduleEntry detail;


    @Override
    public ScheduleSearchResult clone() {
        try {
            ScheduleSearchResult clone = (ScheduleSearchResult) super.clone();
            clone.otherDetails = new ArrayList<>();
            clone.otherDetails.addAll(otherDetails);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @JsonIgnore
    public LocalTime getNominalTime(){
        return getNominalTime(NominalTimeMode.DEPARTURE);
    }

    @JsonIgnore
    public LocalTime getNominalTime(NominalTimeMode nominalTimeMode){
        String time;
        if (detail != null) {
            if (nominalTimeMode == NominalTimeMode.ARRIVAL) {
                time = detail.getGbttArrivalTime();
                if (time == null) {
                    time = detail.getWttArrivalTime();
                }
                if (time == null) {
                    time = detail.getWttPassTime();
                }
                if (time == null) {
                    time = detail.getGbttDepartureTime();
                }
                if (time == null) {
                    time = detail.getWttDepartureTime();
                }
            } else {
                time = detail.getGbttDepartureTime();
                if (time == null) {
                    time = detail.getWttDepartureTime();
                }
                if (time == null) {
                    time = detail.getWttPassTime();
                }
                if (time == null) {
                    time = detail.getGbttArrivalTime();
                }
                if (time == null) {
                    time = detail.getWttArrivalTime();
                }
            }
            return LocalTime.parse(time);
        }
        return null;
    }

    public enum NominalTimeMode {
        DEPARTURE,
        ARRIVAL;
    }

    @JsonIgnore public static Comparator<ScheduleSearchResult> SORT_BY_DEPARTURE_TIME = new Comparator<ScheduleSearchResult>() {
        @Override
        public int compare(ScheduleSearchResult o1, ScheduleSearchResult o2) {
            return o1.getNominalTime(NominalTimeMode.DEPARTURE).compareTo(o2.getNominalTime(NominalTimeMode.DEPARTURE));
        }
    };

    @JsonIgnore public static Comparator<ScheduleSearchResult> SORT_BY_ARRIVAL_TIME = new Comparator<ScheduleSearchResult>() {
        @Override
        public int compare(ScheduleSearchResult o1, ScheduleSearchResult o2) {
            return o1.getNominalTime(NominalTimeMode.ARRIVAL).compareTo(o2.getNominalTime(NominalTimeMode.ARRIVAL));
        }
    };
}
