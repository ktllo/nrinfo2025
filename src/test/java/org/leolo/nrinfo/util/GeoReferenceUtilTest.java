package org.leolo.nrinfo.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import scala.Tuple2;


public class GeoReferenceUtilTest {

    @Test
    public void test() {
        Tuple2<Double, Double> wgs84 = GeoReferenceUtil.convertUKOStoWGS84(359389,172389);
        assertEquals(-2.58578, wgs84._1(), 0.001);
        assertEquals(51.44902, wgs84._2(), 0.001);
    }
}
