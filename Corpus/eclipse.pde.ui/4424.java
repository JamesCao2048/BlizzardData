/*******************************************************************************
 *  Copyright (c) 2000, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.site;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.internal.core.ifeature.IVersionable;
import org.w3c.dom.Node;

public class VersionableObject extends IdentifiableObject implements IVersionable {

    private static final long serialVersionUID = 1L;

    protected String version;

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    protected void parse(Node node) {
        super.parse(node);
        //$NON-NLS-1$
        version = getNodeAttribute(node, "version");
    }

    @Override
    public void setVersion(String version) throws CoreException {
        ensureModelEditable();
        Object oldValue = this.version;
        this.version = version;
        firePropertyChanged(this, P_VERSION, oldValue, version);
    }

    @Override
    public void restoreProperty(String name, Object oldValue, Object newValue) throws CoreException {
        if (name.equals(P_VERSION)) {
            setVersion(newValue != null ? newValue.toString() : null);
        } else
            super.restoreProperty(name, oldValue, newValue);
    }

    @Override
    protected void reset() {
        super.reset();
        version = null;
    }
}
