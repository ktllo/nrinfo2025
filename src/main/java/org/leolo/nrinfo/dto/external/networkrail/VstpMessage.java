package org.leolo.nrinfo.dto.external.networkrail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class VstpMessage {
    @JsonProperty("schedule") private VstpSchedule schedule;
    @JsonProperty("Sender") private StompMessageSender sender;
    private String classification;
    private Date timestamp;
    private String owner;
    private String originMsgId;

    public void setTimestamp(String timestamp) {
        if (timestamp != null && !timestamp.isEmpty()) {
            this.timestamp = new Date(Long.parseLong(timestamp));
        }
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
