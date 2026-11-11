package org.leolo.nrinfo.model.report;

import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;

public class GeneratedReport {

    @Getter @Setter private String mimeType;
    @Getter @Setter private String fileName;
    @Getter private DataMode dataMode = DataMode.STREAM;
    private ByteArrayOutputStream stream;

    private byte [] data;





    public ByteArrayOutputStream getInputStream() {
        if (dataMode != DataMode.STREAM) {
            throw new IllegalStateException("Only stream data mode is supported");
        }
        if (stream == null) {
            stream = new ByteArrayOutputStream();
        }
        return stream;
    }

    public byte[] getData() {
        if (dataMode != DataMode.STREAM) {
            return data;
        }
        return stream.toByteArray();
    }

    public void setData(byte[] data) {
        this.data = data;
        dataMode = DataMode.ARRAY;
    }


    private enum DataMode {
        STREAM,
        ARRAY;
    }

}
