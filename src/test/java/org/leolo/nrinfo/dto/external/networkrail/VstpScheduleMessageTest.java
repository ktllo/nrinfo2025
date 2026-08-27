package org.leolo.nrinfo.dto.external.networkrail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.leolo.nrinfo.model.Schedule;
import org.leolo.nrinfo.model.ScheduleDetail;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;

public class VstpScheduleMessageTest {

    private static String vstp1;
    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void loadVstp1() {
        StringBuffer sb = new StringBuffer();
        try (
            InputStream is = VstpScheduleMessageTest.class
                    .getClassLoader()
                    .getResourceAsStream("org/leolo/nrinfo/dto/external/networkrail/vstp.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(is))
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        vstp1 = sb.toString();
    }

    @Test
    public void testReadFile() throws IOException {
        assertNotNull(vstp1);
        assertNotEquals(0, vstp1.length());
    }

    //Today's target
    @Test
    public void parseVstp() throws IOException {
        JsonNode node = mapper.readTree(vstp1);
        VstpMessage vstp = mapper.convertValue(node.get("VSTPCIFMsgV1"), VstpMessage.class);
        Schedule schedule = vstp.getSchedule().toModel();
        assertNotNull(vstp);
        assertEquals("73341", vstp.getSchedule().getTrainUid());
        assertEquals(vstp.getSchedule().getScheduleStartDate(), schedule.getStartDate());
        assertEquals(vstp.getSchedule().getScheduleEndDate(), schedule.getEndDate());
        assertEquals("5Z01", schedule.getSignalHeadcode());
        assertEquals("EM", schedule.getOperator());
        assertEquals(11, schedule.getDetailList().size());
        //Check the location and time
        ScheduleDetail sd = schedule.getDetailList().get(0);
        assertEquals("LINCLNC", sd.getLocation());
    }
}
