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

package org.esa.beam.dataio.netcdf.metadata.profiles.cf;

import org.junit.Test;

import static org.junit.Assert.*;

public class CfCompliantUnitMapperTest {

    @Test
    public void testTryFindCFCompliantUnitString() {
        assertEquals("degree", CfCompliantUnitMapper.tryFindUnitString("deg"));
    }

    @Test
    public void testUnknownStaysUnchanged() {
        assertEquals("barz", CfCompliantUnitMapper.tryFindUnitString("barz"));
    }
}
