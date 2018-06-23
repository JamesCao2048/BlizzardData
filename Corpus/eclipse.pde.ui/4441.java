/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.target;

import org.eclipse.core.runtime.IConfigurationElement;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.*;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.core.target.ITargetLocationFactory;
import org.eclipse.pde.internal.core.PDECore;

/**
 * Keeps a track of the contributed Target Locations and provides helper functions to
 * access them
 *
 */
public class TargetLocationTypeManager {

    //$NON-NLS-1$
    private static final String ATTR_TYPE = "type";

    //$NON-NLS-1$
    private static final String ATTR_LOCFACTORY = "locationFactory";

    //$NON-NLS-1$
    private static final String TARGET_LOC_EXTPT = "targetLocations";

    private Map<String, IConfigurationElement> fExtentionMap;

    private Map<String, ITargetLocationFactory> fFactoryMap;

    static TargetLocationTypeManager INSTANCE;

    private  TargetLocationTypeManager() {
        //singleton
        fExtentionMap = new HashMap(4);
        fFactoryMap = new HashMap(4);
        readExtentions();
    }

    /**
	 * @return the singleton instanceof this manager
	 */
    public static TargetLocationTypeManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TargetLocationTypeManager();
        }
        return INSTANCE;
    }

    /**
	 * Returns an instance of {@link ITargetLocationFactory} from an extension that provides
	 * them for locations of the given type or <code>null</code> if no extension can be found.
	 *
	 * @param type string identifying the type of target location, see {@link ITargetLocation#getType()}
	 * @return an instance of <code>ITargetLocationFactory</code> or <code>null</code>
	 */
    public ITargetLocationFactory getTargetLocationFactory(String type) {
        ITargetLocationFactory factory = fFactoryMap.get(type);
        if (factory == null) {
            IConfigurationElement extension = fExtentionMap.get(type);
            if (extension != null) {
                factory = (ITargetLocationFactory) createExecutableExtension(extension);
                if (factory != null) {
                    fFactoryMap.put(type, factory);
                    return factory;
                }
            }
        }
        return factory;
    }

    private Object createExecutableExtension(IConfigurationElement element) {
        try {
            return element.createExecutableExtension(ATTR_LOCFACTORY);
        } catch (CoreException e) {
            return null;
        }
    }

    private void readExtentions() {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(PDECore.PLUGIN_ID, TARGET_LOC_EXTPT);
        if (point == null)
            return;
        IExtension[] extensions = point.getExtensions();
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement[] elements = extensions[i].getConfigurationElements();
            for (int j = 0; j < elements.length; j++) {
                String type = elements[j].getAttribute(ATTR_TYPE);
                if (type != null) {
                    fExtentionMap.put(type, elements[j]);
                }
            }
        }
    }
}
