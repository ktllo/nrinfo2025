package org.leolo.nrinfo.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CommonUtilTest {


    @Test
    public void testToCamelCase() {
        assertEquals("Lorem Ipsum Dolor Sit Amet", CommonUtil.toCamelCase("Lorem Ipsum dolor sit amet"));
        assertEquals("Lorem Ipsum Dolor (Sit Amet)", CommonUtil.toCamelCase("Lorem Ipsum dolor (sit amet)"));
        assertNull(CommonUtil.toCamelCase(null));
    }
}
