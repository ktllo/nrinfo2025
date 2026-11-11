package org.leolo.nrinfo.model.report;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;


public class ExecutionResult {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionResult.class);

    @Setter @Getter private List<String> header;
    @Getter private List<List<String>> data = new Vector<>();
    @Getter private List<List<String>> rawData = new Vector<>();
    @Getter private List<List<String>> cssClass = new Vector<>();

    public boolean validate() {
        if (header == null || header.isEmpty() || data == null) {
            logger.debug("Missing header/data");
            return false;
        }
        int headerSize = header.size();
        for (int i = 0; i < headerSize; i++) {
            if (data.get(i).size() != headerSize) {
                logger.debug("Row {} size has wrong number of columns, {} expected but {} found", i, headerSize, data.get(i).size());
                return false;
            }
        }
        return true;
    }

    public String getData(int row, int col) {
        return data.get(row).get(col);
    }

    public void setData(int row, int col, String data) {
        if (row >= this.data.size() || col >= this.data.get(row).size()) {
            throw new IndexOutOfBoundsException();
        }
        this.data.get(row).set(col, data);
    }

    public void setHeader(int col, String header) {
        this.header.set(col, header);
    }

    public String getHeader(int col) {
        return header.get(col);
    }

    public void addRow(List<String> row) {
        this.data.add(new ArrayList<>(row));
        this.rawData.add(new ArrayList<>(row));
        this.cssClass.add(new Vector<>(Collections.nCopies(row.size(), "")));
    }

    public int getRowCount() {
        return this.data.size();
    }

    public int getColCount() {
        return header.size();
    }

    public List<String> getRow(int row) {
        return this.data.get(row);
    }

    public List<String> getOriginalRow(int row) {
        return this.rawData.get(row);
    }

    public String getCssClass(int row, int col) {
        if (row >= this.cssClass.size() || col >= this.cssClass.get(row).size()) {
            return "";
        }
        return this.cssClass.get(row).get(col);
    }

    public void appendCssClass(int row, int col, String cssClass) {
        if (row >= this.cssClass.size() || col >= this.cssClass.get(row).size()) {
            ensureCssCapacity();
        }
        this.cssClass.get(row).set(col, (this.cssClass.get(row).get(col) + " " + cssClass).strip());
    }

    private void ensureCssCapacity() {
        //Row
        for (int row = cssClass.size(); row < data.size(); row++) {
            cssClass.set(row, new Vector<>(Collections.nCopies(header.size(), "")));
        }
    }

}
