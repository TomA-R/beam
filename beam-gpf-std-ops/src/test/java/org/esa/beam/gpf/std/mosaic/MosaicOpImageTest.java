package org.esa.beam.gpf.std.mosaic;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.ConstantDescriptor;
import javax.media.jai.operator.MosaicDescriptor;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.util.Arrays;

public class MosaicOpImageTest {

    private RenderedImage[] sourceImages;
    private PlanarImage[] alphaImages;

    @Before
    public void setup() {
        sourceImages = new RenderedImage[]{
                ConstantDescriptor.create(10.0f, 10.0f, new Float[]{2.0f}, null),
                ConstantDescriptor.create(10.0f, 10.0f, new Float[]{3.0f}, null),
                ConstantDescriptor.create(10.0f, 10.0f, new Float[]{5.0f}, null),
                ConstantDescriptor.create(10.0f, 10.0f, new Float[]{7.0f}, null)
        };

        alphaImages = new PlanarImage[]{
                PlanarImage.wrapRenderedImage(ConstantDescriptor.create(10.0f, 10.0f, new Float[]{1.0f}, null)),
                PlanarImage.wrapRenderedImage(ConstantDescriptor.create(10.0f, 10.0f, new Float[]{2.0f}, null)),
                PlanarImage.wrapRenderedImage(ConstantDescriptor.create(10.0f, 10.0f, new Float[]{3.0f}, null)),
                PlanarImage.wrapRenderedImage(ConstantDescriptor.create(10.0f, 10.0f, new Float[]{4.0f}, null))
        };


    }

    @Test
    public void testAveraging() {
        final RenderedOp mosaicImage = MosaicDescriptor.create(sourceImages, MosaicDescriptor.MOSAIC_TYPE_BLEND,
                                                               alphaImages, null, null, null, null);
        final Raster data = mosaicImage.getData();
        float sample = data.getSampleFloat(0, 0, 0);
        assertEquals(5.1f, sample, 0.0f);
        sample = data.getSampleFloat(5, 5, 0);
        assertEquals(5.1f, sample, 0.0f);
    }

    @Test
    public void testMosaicUpdate() {
        final RenderedOp firstImage = MosaicDescriptor.create(Arrays.copyOf(sourceImages, 3),
                                                              MosaicDescriptor.MOSAIC_TYPE_BLEND,
                                                              Arrays.copyOf(alphaImages, 3), null, null, null, null);
        final PlanarImage[] alphaUpdateImages = {
                PlanarImage.wrapRenderedImage(ConstantDescriptor.create(10.0f, 10.0f, new Float[]{6.0f}, null)),
                PlanarImage.wrapRenderedImage(ConstantDescriptor.create(10.0f, 10.0f, new Float[]{4.0f}, null))
        };
        final RenderedImage[] sourceUpdateImages = {firstImage, sourceImages[3]};
        final RenderedOp updatedImage = MosaicDescriptor.create(sourceUpdateImages,
                                                                MosaicDescriptor.MOSAIC_TYPE_BLEND,
                                                                alphaUpdateImages, null, null, null, null);

        final Raster data = updatedImage.getData();
        float sample = data.getSampleFloat(0, 0, 0);
        assertEquals(5.1f, sample, 0.0f);
        sample = data.getSampleFloat(5, 5, 0);
        assertEquals(5.1f, sample, 0.0f);
    }
}