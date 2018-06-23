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

import java.io.PrintWriter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.internal.core.isite.ISite;
import org.eclipse.pde.internal.core.isite.ISiteCategory;
import org.eclipse.pde.internal.core.isite.ISiteCategoryDefinition;
import org.w3c.dom.Node;

public class SiteCategory extends SiteObject implements ISiteCategory {

    private static final long serialVersionUID = 1L;

    private String name;

    /**
	 * @see org.eclipse.pde.internal.core.isite.ISiteCategory#getName()
	 */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid() {
        return name != null;
    }

    @Override
    protected void parse(Node node) {
        //$NON-NLS-1$
        name = getNodeAttribute(node, "name");
    }

    @Override
    protected void reset() {
        name = null;
    }

    /**
	 * @see org.eclipse.pde.internal.core.isite.ISiteCategory#setName(java.lang.String)
	 */
    @Override
    public void setName(String name) throws CoreException {
        ensureModelEditable();
        Object oldValue = this.name;
        this.name = name;
        firePropertyChanged(P_NAME, oldValue, name);
    }

    @Override
    public void write(String indent, PrintWriter writer) {
        writer.print(indent);
        //$NON-NLS-1$
        writer.print("<category");
        if (name != null)
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.print(" name=\"" + SiteObject.getWritableString(name) + "\"");
        //$NON-NLS-1$
        writer.println("/>");
    }

    @Override
    public void restoreProperty(String name, Object oldValue, Object newValue) throws CoreException {
        if (name.equals(P_NAME)) {
            setName(newValue != null ? newValue.toString() : null);
        } else
            super.restoreProperty(name, oldValue, newValue);
    }

    @Override
    public ISiteCategoryDefinition getDefinition() {
        ISite site = getSite();
        ISiteCategoryDefinition[] definitions = site.getCategoryDefinitions();
        for (int i = 0; i < definitions.length; i++) {
            ISiteCategoryDefinition def = definitions[i];
            if (def.getName().equals(getName()))
                return def;
        }
        return null;
    }
}
