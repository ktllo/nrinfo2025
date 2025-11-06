package org.leolo.nrinfo.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Getter
@Setter
@ToString
public class Tiploc {


    private String tiplocCode;
    private String nalco;
    private String stanox;
    private String crsCode;
    private String ShortDescription;
    private String description;

    public Tiploc(String tiplocCode, String nalco, String stanox, String crsCode, String shortDescription, String description) {
        this.tiplocCode = tiplocCode;
        this.nalco = nalco;
        this.stanox = stanox;
        this.crsCode = crsCode;
        ShortDescription = shortDescription;
        this.description = description;
    }

    public Tiploc() {
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tiploc tiploc)) return false;
        return Objects.equals(tiplocCode, tiploc.tiplocCode) &&
                Objects.equals(nalco, tiploc.nalco) &&
                Objects.equals(stanox, tiploc.stanox) &&
                Objects.equals(crsCode, tiploc.crsCode) &&
                Objects.equals(ShortDescription, tiploc.ShortDescription) &&
                Objects.equals(description, tiploc.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tiplocCode, nalco, stanox, crsCode, ShortDescription, description);
    }
}
