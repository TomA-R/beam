/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
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
package org.esa.beam.framework.ui.tool;

import java.util.EventListener;

/**
 * Listens for state changes of a tool.
 *
 * @author Norman Fomferra
 * @version $Revision$  $Date$
 * @deprecated since BEAM 4.7, no replacement
 */
@Deprecated
public interface ToolListener extends EventListener {

    /**
     * Invoked if a tool was activated.
     *
     * @param toolEvent the event which caused the state change.
     */
    public void toolActivated(ToolEvent toolEvent);

    /**
     * Invoked if a tool was activated.
     *
     * @param toolEvent the event which caused the state change.
     */
    public void toolDeactivated(ToolEvent toolEvent);

    /**
     * Invoked if a tool was enabled.
     *
     * @param toolEvent the event which caused the state change.
     */
    public void toolEnabled(ToolEvent toolEvent);

    /**
     * Invoked if a tool was disabled.
     *
     * @param toolEvent the event which caused the state change.
     */
    public void toolDisabled(ToolEvent toolEvent);

    /**
     * Invoked if the tool was canceled while it was active and not finished.
     *
     * @param toolEvent the event which caused the state change.
     */
    public void toolCanceled(ToolEvent toolEvent);

    /**
     * Invoked if the user finished the work with this tool.
     *
     * @param toolEvent the event which caused the state change.
     */
    public void toolFinished(ToolEvent toolEvent);

}
