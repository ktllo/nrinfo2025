package org.leolo.nrinfo.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NptgTest {
    @Test
    public void testNormal() throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        Nptg nptg = new Nptg();
        nptg.setUpdatedDate("2021-08-17T11:15:30.703");
        assertEquals("2021-08-17T11:15:30.703", sdf.format(nptg.getUpdatedDate()));
        nptg.setUpdatedDate("2021-08-17T11:15:30");
        assertEquals("2021-08-17T11:15:30.000", sdf.format(nptg.getUpdatedDate()));
    }

    @Test
    public void testIncorrectLength() {
        assertThrows(ParseException.class, () -> {
            Nptg nptg = new Nptg();
            nptg.setUpdatedDate("2021-08-17T11:15:30.");
        });
        assertThrows(ParseException.class, () -> {
            Nptg nptg = new Nptg();
            nptg.setUpdatedDate("2021-08-17T11:15:3");
        });
        assertThrows(ParseException.class, () -> {
            Nptg nptg = new Nptg();
            nptg.setUpdatedDate("2021-08-17T11:15:30.1223");
        });
    }

    @Test public void testIncorrectFormat() {
        assertThrows(ParseException.class, () -> {
            Nptg nptg = new Nptg();
            nptg.setUpdatedDate("2021-08-17T11:15:30.abc");
        });
    }

    @Test public void emptyOrNull() throws ParseException {
        Nptg nptg = new Nptg();
        //Set something there
        nptg.setUpdatedDate(new Date());
        nptg.setUpdatedDate((String) null);
        assertNull(nptg.getUpdatedDate());
        nptg.setUpdatedDate(new Date());
        nptg.setUpdatedDate("");
        assertNull(nptg.getUpdatedDate());
    }
}
