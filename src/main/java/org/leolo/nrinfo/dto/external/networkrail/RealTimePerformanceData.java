package org.leolo.nrinfo.dto.external.networkrail;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class RealTimePerformanceData {
    @JsonProperty("snapshotTStamp") private long snapshotTimeStamp;
    @JsonProperty("RAGThresholds") private Collection<PerformanceRAGThreshold> performanceThresholds;
    @JsonProperty("PPT") private PerformanceMetric ppt;
    @JsonProperty("NationalPage") private SummaryPage nationalPage;
    @JsonProperty("OOCPage") private SummaryPage oocPage;
    @JsonProperty("FOCPage") private SummaryPage focPage;
    @JsonProperty("CommonOperatorPage") private CommonMessage commonPage;
    @JsonProperty("OperatorPage") private Collection<OperatorPage> operatorPages;

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SummaryPage {
        @JsonProperty("WebDisplayPeriod") private int displayPeriod;
        @JsonProperty("WebFixedMsg1") private String fixedMsg1;
        @JsonProperty("WebFixedMsg2") private String fixedMsg2;
        @JsonProperty("WebMsgOfMoment") private String msgOfMoment;
        private boolean stale;
        @JsonProperty("NationalPPM") private PerformanceDetail nationalPerformance;
        @JsonProperty("Sector") private Collection<SectorPerformance> sectorPerformance;
        @JsonProperty("Operator") private Collection<OperatorPerformance> operatorPerformance;
        @JsonProperty("StaleFlag") public void setStaleFlag(String staleFlag) {
            this.stale = "Y".equalsIgnoreCase(staleFlag);
        }
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SectorPerformance {
        @JsonProperty("SectorPPM") private PerformanceDetail sectorPerformance;
        @JsonProperty("sectorCode") private String sectorCode;
        @JsonProperty("sectorDesc") private String sectorDesc;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OperatorPerformance {
        @JsonProperty("Total") private int total;
        @JsonProperty("PPM") private PerformanceMetric performanceMetric;
        @JsonProperty("RollingPPM") private PerformanceMetric rollingPerformanceMetric;
        @JsonProperty("code") private String code;
        @JsonProperty("name") private String name;
        @JsonProperty("keySymbol") private String keySymbol;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommonMessage {
        @JsonProperty("WebDisplayPeriod") private int displayPeriod;
        @JsonProperty("WebFixedMessage1") private String fixedMsg1;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OperatorPage {
        @JsonProperty("Operator") private OperatorPerformanceDetail operatorPerformanceDetail;
        @JsonProperty("OprToleranceTotal")
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private Collection<PerformanceTolerance> performanceTolerance;
        @JsonProperty("OprServiceGrp")
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private Collection<OperatorServiceGroup> operatorServiceGroup;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OperatorPerformanceDetail {
        @JsonProperty("Total") private int total;
        @JsonProperty("OnTime") private int onTime;
        @JsonProperty("Late") private int late;
        @JsonProperty("CancelVeryLate") private int cancelVeryLate;
        @JsonProperty("PPM") private PerformanceMetric performanceMetric;
        @JsonProperty("RollingPPM") private PerformanceMetric rollingPerformanceMetric;
        @JsonProperty("code") private String code;
        @JsonProperty("name") private String name;
        @JsonProperty("keySymbol") private String keySymbol;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PerformanceTolerance {
        @JsonProperty("Total") private int total;
        @JsonProperty("OnTime") private int onTime;
        @JsonProperty("Late") private int late;
        @JsonProperty("CancelVeryLate") private int cancelVeryLate;
        @JsonProperty("timeband") private int timeband;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OperatorServiceGroup {
        @JsonProperty("Total") private int total;
        @JsonProperty("OnTime") private int onTime;
        @JsonProperty("Late") private int late;
        @JsonProperty("CancelVeryLate") private int cancelVeryLate;
        @JsonProperty("PPM") private PerformanceMetric performanceMetric;
        @JsonProperty("RollingPPM") private PerformanceMetric rollingPerformanceMetric;
        @JsonProperty("name") private String name;
        @JsonProperty("timeband") private int timeband;
        @JsonProperty("sectorCode") private String sectorCode;
    }
}
