package org.leolo.nrinfo.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.leolo.nrinfo.model.Tiploc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
@Builder
public class DetailedStationSearchResult {

    private String name;
    private String shortName;
    private String tiplocCode;
    private String crsCode;
    private String nalco;
    private String stanox;
    private TreeMap<String, AssociatedCodes> associatedTiplocs = new TreeMap<>();

    @JsonIgnore private static Logger log = LoggerFactory.getLogger(DetailedStationSearchResult.class);

    public Collection<AssociatedCodes> getAssociatedTiplocs() {
        if (associatedTiplocs == null) {
            return Collections.emptyList();
        }
        return associatedTiplocs.values();
    }

    public void addAssociatedTiploc(Tiploc tiploc, AssociatedType associatedType) {
        if (associatedTiplocs == null) {
            associatedTiplocs = new TreeMap<>();
        }
        if (tiploc == null) {
            log.warn("Given TIPLOC is null");
            return;
        }
        if (tiploc.getTiplocCode().equals(tiplocCode)) {
            return;
        }
        if (associatedTiplocs.containsKey(tiploc.getTiplocCode())) {
            associatedTiplocs.get(tiploc.getTiplocCode()).type.add(associatedType);
        } else {
            AssociatedCodes ac = AssociatedCodes.builder()
                    .crsCode(tiploc.getCrsCode())
                    .nalco(tiploc.getNalco())
                    .stanox(tiploc.getStanox())
                    .shortName(tiploc.getShortDescription())
                    .name(tiploc.getDescription())
                    .tiplocCode(tiploc.getTiplocCode())
                    .build();
            ac.type = new TreeSet<>();
            ac.type.add(associatedType);
            associatedTiplocs.put(tiploc.getTiplocCode(), ac);
        }
    }


    @Getter
    @Setter
    @Builder
    public static class AssociatedCodes {
        private String name;
        private String shortName;
        private String tiplocCode;
        private String crsCode;
        private String nalco;
        private String stanox;
        private Set<AssociatedType> type = new TreeSet<>();

        public Collection<String> getType() {
            if (type == null) {
                return Collections.emptyList();
            }
            return type.stream().map(AssociatedType::getDescription).collect(Collectors.toList());
        }
    }

    @Getter
    public enum AssociatedType {
        SAME_NALCO("Same National Location Code"),
        SAME_STANOX(("Same Station Number")),
        SAME_CRS("Same Station Code"),
        MANUAL_GROUP("Manual Grouped"),;

        private final String description;

        AssociatedType(String description) {
            this.description = description;
        }
    }
}
