package gr.uoa.di.kr.yagoextension.util.v2;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.apache.logging.log4j.util.Strings;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.util.Optional;

public class GeometryParser {

    private static final String EPSG_4326_URL = "<http://www.opengis.net/def/crs/EPSG/0/4326>";
    private static final String EPSG_4326 = "EPSG:4326";

    private static final String EPSG_2100_URL = "<http://www.opengis.net/def/crs/EPSG/0/2100";
    private static final String EPSG_2100 = "EPSG:2100";

    public static Optional<Geometry> extractGeometry(String wktGeometry) {
        try {
            if (wktGeometry.contains(EPSG_4326_URL)) {
                return Optional.of(new WKTReader().read(wktGeometry.replace(EPSG_4326_URL, Strings.EMPTY)));
            } else if (wktGeometry.contains(EPSG_2100_URL)) {
                Geometry geometry = new WKTReader().read(wktGeometry);
                CoordinateReferenceSystem sourceCRS = CRS.decode(EPSG_2100);
                CoordinateReferenceSystem targetCRS = CRS.decode(EPSG_4326);
                MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);

                geometry = JTS.transform(geometry, transform);
                geometry.geometryChanged();
                return Optional.of(geometry);
            } else {
                return Optional.of(new WKTReader().read(wktGeometry));
            }
        } catch (FactoryException | MismatchedDimensionException | TransformException | ParseException e) {
            // TODO Should add logger here
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<Geometry> extractGeometry(String longitude, String latitude) {
        try {
            return Optional.of(new WKTReader().read("POINT( " + longitude + " " + latitude + " )"));
        } catch (ParseException e) {
            // TODO Should add logger here
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
