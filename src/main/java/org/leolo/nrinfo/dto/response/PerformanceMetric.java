package org.leolo.nrinfo.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.leolo.nrinfo.enums.RAG;
import org.leolo.nrinfo.enums.Trend;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PerformanceMetric implements Comparable<PerformanceMetric> {
    private String name;
    private String code;
    private int onTimeRate;
    private RAG rag;
    private int rollingOnTimeRate;
    private RAG rollingRag;
    private Trend trend;

    private int total;
    private int onTime;
    private int late;
    private int cancelOrVeryLate;

    public BigDecimal getOnTimePercentage() {
        return total == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(onTime).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(total+cancelOrVeryLate),2, RoundingMode.HALF_EVEN);
    }
    public BigDecimal getLatePercentage() {
        return total == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(late).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(total+cancelOrVeryLate),2, RoundingMode.HALF_EVEN);
    }
    public BigDecimal getCancelOrVeryLatePercentage() {
        return total == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(cancelOrVeryLate).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(total+cancelOrVeryLate),2, RoundingMode.HALF_EVEN);
    }
    public BigDecimal getTotalLatePercentage() {
        return total == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(late+cancelOrVeryLate).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(total+cancelOrVeryLate),2, RoundingMode.HALF_EVEN);
    }

    @Override
    public int compareTo(PerformanceMetric o) {
        int compare = 0;
        if (code != null && o.code != null) {
            compare = code.compareTo(o.code);
            if (compare != 0) return compare;
        }
        compare = name.compareTo(o.name);
        return compare;
    }

    @JsonIgnore
    public String getRagEmoji() {
        if (rag == null) {
            return null;
        }
        return switch (rag) {
            case RED -> "\uD83D\uDD34";
            case AMBER -> "\uD83D\uDFE1";
            case GREEN -> "\uD83D\uDFE2";
            default -> "";
        };
    }
    @JsonIgnore
    public String getRollingRagEmoji() {
        if (rollingRag == null) {
            return null;
        }
        return switch (rollingRag) {
            case RED -> "\uD83D\uDD34";
            case AMBER -> "\uD83D\uDFE1";
            case GREEN -> "\uD83D\uDFE2";
            default -> "";
        };
    }

    @JsonIgnore
    public String getTrendEmoji() {
        if (trend == null) {
            return null;
        }
        return switch (trend) {
            case UP -> "\u2B06\uFE0F";
            case DOWN -> "\u2B07\uFE0F";
            case STABLE -> "\u27A1\uFE0F";
            default -> "";
        };
    }

    @JsonIgnore
    public int getActualTotal() {
        return total + cancelOrVeryLate;
    }
}
