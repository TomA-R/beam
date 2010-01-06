package org.esa.beam.framework.datamodel;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import junit.framework.TestCase;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class PlainFeatureFactoryTest extends TestCase {
    public void testFeatureType() {
        final SimpleFeatureType sft = PlainFeatureFactory.createPlainFeatureType("MyPoint",
                                                                                 Point.class,
                                                                                 DefaultGeographicCRS.WGS84);

        assertEquals("MyPoint", sft.getTypeName());
        assertNotNull(sft.getDescriptor(PlainFeatureFactory.ATTRIB_NAME_GEOMETRY));
        assertNotNull(sft.getDescriptor(PlainFeatureFactory.ATTRIB_NAME_STYLE_CSS));

        assertNotNull(sft.getGeometryDescriptor());
        assertNotNull(sft.getGeometryDescriptor().getType());
        assertSame(DefaultGeographicCRS.WGS84, sft.getCoordinateReferenceSystem());
        assertSame(DefaultGeographicCRS.WGS84, sft.getGeometryDescriptor().getCoordinateReferenceSystem());
        assertSame(DefaultGeographicCRS.WGS84, sft.getGeometryDescriptor().getType().getCoordinateReferenceSystem());
    }

    public void testFeature() {
        final SimpleFeatureType sft = PlainFeatureFactory.createPlainFeatureType("X",
                                                                                 Point.class,
                                                                                 DefaultGeographicCRS.WGS84);

        final GeometryFactory gf = new GeometryFactory();
        final Point p1 = gf.createPoint(new Coordinate(0.5, 0.6));
        final SimpleFeature sf = PlainFeatureFactory.createPlainFeature(sft, "_1", p1, "fill:#0033AA");

        assertEquals("_1", sf.getID());
        assertEquals(p1, sf.getDefaultGeometry());
        assertSame(p1, sf.getDefaultGeometry());

        assertEquals(p1, sf.getAttribute(PlainFeatureFactory.ATTRIB_NAME_GEOMETRY));
        assertEquals("fill:#0033AA", sf.getAttribute(PlainFeatureFactory.ATTRIB_NAME_STYLE_CSS));
    }
}