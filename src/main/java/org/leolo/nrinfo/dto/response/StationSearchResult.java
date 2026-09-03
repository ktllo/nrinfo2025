package org.leolo.nrinfo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.leolo.nrinfo.util.CommonUtil;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StationSearchResult {
    String name;
    String tiplocCode;
    String crsCode;
    ArrayList<String> associatedTiploc = new ArrayList<>();

    public StationSearchResult(String name, String tiplocCode, String crsCode) {
        this.name = name;
        this.tiplocCode = tiplocCode;
        this.crsCode = crsCode;
    }

    public String getDisplayName(){
        return CommonUtil.toCamelCase(name);
    }
}
