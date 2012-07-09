/*
 * Copyright (C) 2011 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.statistics;

import com.bc.ceres.binding.ConversionException;
import com.bc.ceres.binding.Converter;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.datamodel.VirtualBand;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.util.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;

/**
 * @author Thomas Storm
 */
public class StatisticsOpTest {

    static final File TESTDATA_DIR = new File("target/statistics-test-io");

    static {
        GPF.getDefaultInstance().getOperatorSpiRegistry().loadOperatorSpis();
    }

    @Before
    public void setUp() throws Exception {
        TESTDATA_DIR.mkdirs();
        if (!TESTDATA_DIR.isDirectory()) {
            fail("Can't create test I/O directory: " + TESTDATA_DIR);
        }
    }

    @After
    public void tearDown() throws Exception {
        if (!FileUtils.deleteTree(TESTDATA_DIR)) {
            System.out.println("Warning: failed to completely delete test I/O directory:" + TESTDATA_DIR);
        }
    }

    @Test
    public void testThatStatisticsOpIsRegistered() throws Exception {
        assertNotNull(GPF.getDefaultInstance().getOperatorSpiRegistry().getOperatorSpi("StatisticsOp"));
    }

    @Test
    public void testStatisticsOp() throws Exception {
        final StatisticsOp statisticsOp = new StatisticsOp();
        final StatisticsOp.BandConfiguration bandConfiguration = new StatisticsOp.BandConfiguration();
        bandConfiguration.sourceBandName = "algal_2";
        statisticsOp.bandConfigurations = new StatisticsOp.BandConfiguration[]{bandConfiguration};
        statisticsOp.sourceProducts = new Product[]{getTestProduct()};
        statisticsOp.shapefile = getClass().getResource("4_pixels.shp");
        statisticsOp.doOutputAsciiFile = false;
        final StringBuilder builder = new StringBuilder();

        statisticsOp.outputters.add(new MyOutputter(builder));

        statisticsOp.initialize();

        final String result = builder.toString();
        assertEquals("4_pixels.1\n" +
                     "algal_2:\n" +
                     "max: 0.804474\n" +
                     "mean: 0.749427\n" +
                     "median: 0.721552\n" +
                     "min: 0.695857\n" +
                     "p90: 0.804474\n" +
                     "p95: 0.804474\n" +
                     "sigma: 0.049578\n" +
                     "total: 4.000000\n",
                     result);
    }

    @Test
    public void testStatisticsOp_WithExpression() throws Exception {
        final StatisticsOp statisticsOp = new StatisticsOp();
        final StatisticsOp.BandConfiguration bandConfiguration = new StatisticsOp.BandConfiguration();
        bandConfiguration.expression = "algal_2 * PI";
        statisticsOp.bandConfigurations = new StatisticsOp.BandConfiguration[]{bandConfiguration};
        statisticsOp.sourceProducts = new Product[]{getTestProduct()};
        statisticsOp.shapefile = getClass().getResource("4_pixels.shp");
        statisticsOp.doOutputAsciiFile = false;
        final StringBuilder builder = new StringBuilder();

        statisticsOp.outputters.add(new MyOutputter(builder));

        statisticsOp.initialize();

        final String result = builder.toString();
        assertEquals("4_pixels.1\n" +
                     "algal_2_*_PI:\n" +
                     "max: 2.527328\n" +
                     "mean: 2.354394\n" +
                     "median: 2.266823\n" +
                     "min: 2.186098\n" +
                     "p90: 2.527328\n" +
                     "p95: 2.527328\n" +
                     "sigma: 0.155752\n" +
                     "total: 4.000000\n",
                     result);
    }

    @Test
    public void testGetBand() throws Exception {
        final StatisticsOp.BandConfiguration configuration = new StatisticsOp.BandConfiguration();

        final Product testProduct = getTestProduct();
        try {
            StatisticsOp.getBand(configuration, testProduct);
            fail();
        } catch (OperatorException expected) {
            assertTrue(expected.getMessage().contains("must contain either a source band name or an expression"));
        }

        configuration.sourceBandName = "algal_2";
        final Band band = StatisticsOp.getBand(configuration, testProduct);
        assertEquals("algal_2", band.getName());

        configuration.expression = "algal_2 * PI";
        try {
            StatisticsOp.getBand(configuration, testProduct);
            fail();
        } catch (OperatorException expected) {
            assertTrue(expected.getMessage().contains("must contain either a source band name or an expression"));
        }

        configuration.sourceBandName = null;
        final Band virtualBand = StatisticsOp.getBand(configuration, testProduct);
        assertEquals("algal_2_*_PI", virtualBand.getName());
        assertTrue(virtualBand instanceof VirtualBand);
        assertEquals("algal_2 * PI", ((VirtualBand)virtualBand).getExpression());
    }

    @Test
    public void testStatisticsOp_WithGPF() throws Exception {
        final StatisticsOp.BandConfiguration bandConfiguration_1 = new StatisticsOp.BandConfiguration();
        bandConfiguration_1.sourceBandName = "algal_2";

        final HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("outputAsciiFile", getTestFile("statisticsOutput.out"));
        parameters.put("outputShapefile", getTestFile("statisticsShapefile.shp"));
        parameters.put("doOutputAsciiFile", true);
        parameters.put("doOutputShapefile", true);
        parameters.put("shapefile", getClass().getResource("4_pixels.shp"));
        parameters.put("bandConfigurations", new StatisticsOp.BandConfiguration[]{
                bandConfiguration_1,
        });
        GPF.createProduct("StatisticsOp", parameters, getTestProduct());

        assertFalse(getTestFile("statisticsOutput.put").exists());
        assertTrue(getTestFile("statisticsOutput.out").exists());
        assertTrue(getTestFile("statisticsOutput_metadata.txt").exists());
        assertTrue(getTestFile("statisticsShapefile.shp").exists());
    }

    @Test
    public void testUtcConverter() throws Exception {
        final StatisticsOp.UtcConverter utcConverter = new StatisticsOp.UtcConverter();
        assertEquals(ProductData.UTC.class, utcConverter.getValueType());

        final ProductData.UTC actual = utcConverter.parse("2010-01-31 14:46:22");
        final long expected = ProductData.UTC.parse("2010-01-31 14:46:22", "yyyy-MM-dd hh:mm:ss").getAsDate().getTime();

        assertEquals(expected, actual.getAsDate().getTime());

        assertConversionException(utcConverter, "2010-01-31'T'14:46:22.1234");
        assertConversionException(utcConverter, "2010-31-01'T'14:46:22.123");
        assertConversionException(utcConverter, "2010-01-31T14:46:22.123");
        assertConversionException(utcConverter, "2010-01-31'T'14.46.22.123");
    }

    @Test
    public void testValidateInput() throws Exception {
        final StatisticsOp statisticsOp = new StatisticsOp();
        statisticsOp.startDate = ProductData.UTC.parse("2010-01-31 14:46:23", "yyyy-MM-ss hh:mm:ss");
        statisticsOp.endDate = ProductData.UTC.parse("2010-01-31 14:45:23", "yyyy-MM-ss hh:mm:ss");

        try {
            statisticsOp.validateInput();
            fail();
        } catch (OperatorException expected) {
            assertTrue(expected.getMessage().contains("before start date"));
        }

        statisticsOp.endDate = ProductData.UTC.parse("2010-01-31 14:47:23", "yyyy-MM-ss hh:mm:ss");

        try {
            statisticsOp.validateInput();
            fail();
        } catch (OperatorException expected) {
            assertTrue(expected.getMessage().contains("must be given"));
        }
    }

    private Product getTestProduct() throws IOException {
        return ProductIO.readProduct(getClass().getResource("testProduct1.dim").getFile());
    }

    private static void assertConversionException(Converter converter, String text) {
        try {
            converter.parse(text);
            fail();
        } catch (ConversionException e) {
        }
    }

    private static class MyOutputter implements StatisticsOp.Outputter {

        private final StringBuilder builder;

        public MyOutputter(StringBuilder builder) {
            this.builder = builder;
        }

        @Override
        public void initialiseOutput(Product[] sourceProducts, String[] bandNames, String[] algorithmNames, ProductData.UTC startDate, ProductData.UTC endDate, String[] regionIds) {
        }

        @Override
        public void addToOutput(String bandName, String regionId, Map<String, Double> statistics) {
            final TreeMap<String, Double> map = new TreeMap<String, Double>();
            map.putAll(statistics);
            builder.append(regionId)
                    .append("\n")
                    .append(bandName)
                    .append(":\n");
            for (Map.Entry<String, Double> entry : map.entrySet()) {
                final DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
                decimalFormatSymbols.setDecimalSeparator('.');
                builder.append(entry.getKey())
                        .append(": ")
                        .append(new DecimalFormat("0.000000", decimalFormatSymbols).format(entry.getValue()))
                        .append("\n");
            }
        }

        @Override
        public void finaliseOutput() throws IOException {
        }
    }

    static File getTestFile(String fileName) {
        return new File(TESTDATA_DIR, fileName);
    }
}