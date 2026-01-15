package org.leolo.nrinfo.dto.response;


import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamHealth {
    private String streamType;
    private String streamTypeName;
    private String connectionStatus;
    private Collection<SubTypeHealth> subtype = new ArrayList<>();


    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubTypeHealth {
        private String subType;
        private Instant lastMessageTime;

        public long getElapsedTime() {
            return Instant.now().getEpochSecond() - lastMessageTime.getEpochSecond();
        }
    }
}
