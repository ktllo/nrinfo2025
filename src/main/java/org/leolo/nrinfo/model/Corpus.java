package org.leolo.nrinfo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Corpus {

    @JsonProperty("STANOX")    private String stanoxCode;
    @JsonProperty("UIC")       private String uicCode;
    @JsonProperty("3ALPHA")    private String crsCode;
    @JsonProperty("TIPLOC")    private String tiplocCode;
    @JsonProperty("NLC")       private String nlcCode;
    @JsonProperty("NLCDESC")   private String nlcDescription;
    @JsonProperty("NLCDESC16") private String shortNlcDescription;

}
