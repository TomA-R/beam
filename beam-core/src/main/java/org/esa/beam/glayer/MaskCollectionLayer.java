/*
 * $Id: $
 *
 * Copyright (C) 2008 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.glayer;

import com.bc.ceres.binding.ValueContainer;
import com.bc.ceres.glayer.CollectionLayer;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.core.Assert;
import org.esa.beam.framework.datamodel.Mask;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductNode;
import org.esa.beam.framework.datamodel.ProductNodeEvent;
import org.esa.beam.framework.datamodel.ProductNodeListener;
import org.esa.beam.framework.datamodel.RasterDataNode;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;


public class MaskCollectionLayer extends CollectionLayer {

    public static final String ID = MaskCollectionLayer.class.getName();

    private final ProductNodeListener maskPNL;
    private RasterDataNode raster;

    public MaskCollectionLayer(MaskCollectionLayerType layerType, ValueContainer configuration) {
        super(layerType, configuration, "Masks");
        this.raster = (RasterDataNode) configuration.getValue(MaskCollectionLayerType.PROPERTY_NAME_RASTER);
        Assert.notNull(this.raster, "raster");
        maskPNL = new MaskPNL();
        setId(ID);
        getProduct().addProductNodeListener(maskPNL);
    }

    @Override
    public void disposeLayer() {
        if (raster != null) {
            getProduct().removeProductNodeListener(maskPNL);
            raster = null;
        }
    }

    private Product getProduct() {
        return raster.getProduct();
    }

    private RasterDataNode getRaster() {
        return raster;
    }

    private Layer createLayer(final Mask mask) {
        return MaskLayerType.createLayer(getRaster(), mask);
    }

    private ImageLayer getMaskLayer(Mask mask) {
        for (Layer layer : getChildren()) {
            if (layer instanceof ImageLayer) {
                final Object value = layer.getConfiguration().getValue(MaskLayerType.PROPERTY_NAME_MASK);
                if (mask == value) {
                    return (ImageLayer) layer;
                }
            }
        }
        return null;
    }

    synchronized void updateChildren() {

        // Collect all current mask layers
        HashMap<Mask, Layer> currentLayers = new HashMap<Mask, Layer>();
        for (Layer maskLayer : getChildren()) {
            Mask mask = (Mask) maskLayer.getConfiguration().getValue("mask");
            currentLayers.put(mask, maskLayer);
        }

        // Allign mask layers with available masks
        Mask[] availableMasks = raster.getProduct().getMaskGroup().toArray(new Mask[0]);
        HashSet<Layer> unusedLayers = new HashSet<Layer>(getChildren());
        for (int i = 0; i < availableMasks.length; i++) {
            Mask availableMask = availableMasks[i];
            Layer layer = currentLayers.get(availableMask);
            if (layer == null) {
                layer = createLayer(availableMask);
                getChildren().add(i, layer);
            }else  if (layer != getChildren().get(i)) {
                getChildren().remove(i);
                getChildren().add(i, layer);
                unusedLayers.remove(layer);
            } else {
                unusedLayers.remove(layer);
            }
            layer.setVisible(raster.getOverlayMaskGroup().contains(availableMask));
        }

        // Remove unused layers
        for (Layer layer : unusedLayers) {
            layer.dispose();
            getChildren().remove(layer);
        }
    }

    public class MaskPNL implements ProductNodeListener {

        @Override
        public synchronized void nodeChanged(ProductNodeEvent event) {
            final ProductNode sourceNode = event.getSourceNode();
            if (sourceNode instanceof Mask) {
                final Mask mask = (Mask) sourceNode;
                final ImageLayer maskLayer = getMaskLayer(mask);
                if (maskLayer != null) {
                    maskLayer.regenerate();
                }
            }
        }

        @Override
        public void nodeDataChanged(ProductNodeEvent event) {
            nodeChanged(event);
        }

        @Override
        public void nodeAdded(ProductNodeEvent event) {
            updateChildren();
        }

        @Override
        public void nodeRemoved(ProductNodeEvent event) {
            updateChildren();
        }
    }

}