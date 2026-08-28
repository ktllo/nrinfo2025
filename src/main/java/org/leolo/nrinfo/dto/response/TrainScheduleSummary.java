package org.leolo.nrinfo.dto.response;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainScheduleSummary {
    private String trainUid;
    private String origin;
    private String destination;
    private String departureTime;
    private String arrivalTime;
    private String trainType;
    private String trainOperator;
    private String stpIndicator;

    public String getStpIndicator() {
        if (stpIndicator == null) {
            return null;
        }
        switch (stpIndicator) {
            case "P":
            case "N":
                return "Base Schedule";
            case "O":
                return "Overlay";
            case "C":
                return "Cancellation";
            default:
                return stpIndicator;
        }
    }
}
