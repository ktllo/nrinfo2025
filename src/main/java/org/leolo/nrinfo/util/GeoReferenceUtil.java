package org.leolo.nrinfo.util;

import geotrellis.proj4.CRS;
import geotrellis.proj4.Transform;
import org.leolo.nrinfo.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

public class GeoReferenceUtil {

    private static Logger logger = LoggerFactory.getLogger(GeoReferenceUtil.class);

    public static Tuple2<Double, Double> convertUKOStoWGS84(int easting, int northing) {
        var toWgs84 = Transform.apply(CRS.fromEpsgCode(Constants.EpsgCode.UKOS), CRS.fromEpsgCode(Constants.EpsgCode.WGS84));
        Tuple2<Object, Object> wgs84 = toWgs84.apply((double)easting, (double)northing);
//        logger.info("UKOS({},{}) -> WGS84({},{})", easting, northing, wgs84._1(), wgs84._2());
        return new Tuple2<Double, Double>(round6(wgs84._1()), round6(wgs84._2()));
    }

    private static double round6(double d) {
        return Math.round(d * 1000000) / 1000000.0;
    }
    private static double round6(Object obj) {
        double d = 0;
        if (obj instanceof Double) {
            d = (Double) obj;
        } else if (obj instanceof Number) {
            d = ((Number) obj).doubleValue();
        } else {
            return 0;
        }
        return Math.round(d * 1000000) / 1000000.0;
    }

}
