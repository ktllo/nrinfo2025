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
    private String originDisplayName;
    private String destinationDisplayName;
    private String departureTime;
    private String arrivalTime;
    private String trainType;
    private String trainOperator;
    private String stpIndicator;
    private String scheduleDate;

    public String getStpIndicator() {
        if (stpIndicator == null) {
            return null;
        }
        return switch (stpIndicator) {
            case "P", "N" -> "Base Schedule";
            case "O" -> "Overlay";
            case "C" -> "Cancellation";
            default -> stpIndicator;
        };
    }
}
