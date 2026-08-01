package org.leolo.nrinfo.dto.external.networkrail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StompMessageSender {
    @JsonProperty("organisation")
    private String organisation;

    @JsonProperty("application")
    private String application;

    @JsonProperty("component")
    private String component;

    @JsonProperty("userID")
    private String userId;

    @JsonProperty("sessionID")
    private String sessionId;
}
