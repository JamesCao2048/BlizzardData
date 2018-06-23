/*******************************************************************************
 *  Copyright (c) 2005, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.text.bundle;

import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.pde.internal.core.ibundle.IBundleModel;

public class PackageObject extends PDEManifestElement {

    private static final long serialVersionUID = 1L;

    protected transient String fVersionAttribute;

    public  PackageObject(ManifestHeader header, ManifestElement element, String versionAttribute) {
        super(header, element);
        fVersionAttribute = versionAttribute;
    }

    public  PackageObject(ManifestHeader header, String name, String version, String versionAttribute) {
        //$NON-NLS-1$
        super(header, name.length() > 0 ? name : ".");
        fVersionAttribute = versionAttribute;
        if (version != null)
            addAttribute(fVersionAttribute, version);
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer(getValue());
        String version = getVersion();
        if (version != null && version.length() > 0) {
            //$NON-NLS-1$
            buffer.append(" ");
            boolean wrap = Character.isDigit(version.charAt(0));
            if (wrap)
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "(");
            buffer.append(version);
            if (wrap)
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                ")");
        }
        return buffer.toString();
    }

    public String getVersion() {
        String[] version = getAttributes(fVersionAttribute);
        if (version == null || version.length == 0)
            return null;
        if (version.length == 1)
            return version[0];
        return version[0] + ',' + version[1];
    }

    public String getName() {
        return getValue();
    }

    public void setName(String name) {
        setValueComponents(new String[] { name });
    }

    public void setVersion(String version) {
        String old = getVersion();
        setAttribute(fVersionAttribute, version);
        fHeader.update();
        firePropertyChanged(this, fVersionAttribute, old, version);
    }

    public void reconnect(IBundleModel model, ManifestHeader header, String versionAttribute) {
        super.reconnect(model, header);
        // Transient Field:  Version Attribute
        fVersionAttribute = versionAttribute;
    }
}
