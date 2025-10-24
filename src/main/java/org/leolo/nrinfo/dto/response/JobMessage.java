package org.leolo.nrinfo.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.leolo.nrinfo.enums.JobMessageType;

import java.awt.*;
import java.util.Date;

@Getter @Setter @AllArgsConstructor
public class JobMessage {
    private String messageId;
    @JsonIgnore
    private String jobId;
    private JobMessageType messageType;
    private String message;
    private Date messageTime;

    public String getMessageType() {
        if (messageType == null) {
            return null;
        }
        return messageType.getDisplayName();
    }


}
