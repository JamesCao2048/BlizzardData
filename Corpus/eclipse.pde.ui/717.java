/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ua.core.ctxhelp.text;

import java.util.Collection;
import java.util.Iterator;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.xml.sax.SAXParseException;

/**
 * Manages markers for xml problems in the context help editor
 */
public class CtxHelpMarkerManager {

    public static void refreshMarkers(CtxHelpModel model) {
        deleteMarkers(model);
        createMarkers(model);
    }

    public static void deleteMarkers(CtxHelpModel model) {
        try {
            IMarker[] problems = model.getUnderlyingResource().findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
            if (problems != null) {
                for (int index = 0; index < problems.length; index++) {
                    problems[index].delete();
                }
            }
        } catch (CoreException e) {
        }
    }

    public static void createMarkers(CtxHelpModel model) {
        Collection errors = model.getErrors();
        if (errors == null || errors.size() == 0) {
            return;
        }
        Iterator iter = errors.iterator();
        while (iter.hasNext()) {
            Throwable exception = (Throwable) iter.next();
            if (exception instanceof SAXParseException) {
                int line = ((SAXParseException) exception).getLineNumber();
                try {
                    IMarker marker = model.getUnderlyingResource().createMarker(IMarker.PROBLEM);
                    marker.setAttribute(IMarker.LINE_NUMBER, line);
                    marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                    marker.setAttribute(IMarker.MESSAGE, exception.getLocalizedMessage());
                } catch (CoreException e) {
                }
            }
        }
    }
}
