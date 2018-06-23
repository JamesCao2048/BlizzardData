/*******************************************************************************
 *  Copyright (c) 2013 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat Inc. - <bundle...> in category.xml (copied from SiteFeature)
 *******************************************************************************/
package org.eclipse.pde.internal.core.site;

import java.io.PrintWriter;
import java.util.Vector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.internal.core.isite.ISiteBundle;
import org.eclipse.pde.internal.core.isite.ISiteCategory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SiteBundle extends VersionableObject implements ISiteBundle {

    private static final long serialVersionUID = 1L;

    private Vector<ISiteCategory> fCategories = new Vector();

    @Override
    public boolean isValid() {
        for (int i = 0; i < fCategories.size(); i++) {
            ISiteCategory category = fCategories.get(i);
            if (!category.isValid())
                return false;
        }
        return true;
    }

    @Override
    public void addCategories(ISiteCategory[] newCategories) throws CoreException {
        ensureModelEditable();
        for (int i = 0; i < newCategories.length; i++) {
            ISiteCategory category = newCategories[i];
            ((SiteCategory) category).setInTheModel(true);
            fCategories.add(newCategories[i]);
        }
        fireStructureChanged(newCategories, IModelChangedEvent.INSERT);
    }

    @Override
    public void removeCategories(ISiteCategory[] newCategories) throws CoreException {
        ensureModelEditable();
        for (int i = 0; i < newCategories.length; i++) {
            ISiteCategory category = newCategories[i];
            ((SiteCategory) category).setInTheModel(false);
            fCategories.remove(newCategories[i]);
        }
        fireStructureChanged(newCategories, IModelChangedEvent.REMOVE);
    }

    /**
	 * @see org.eclipse.pde.internal.core.isite.ISiteFeature#getCategories()
	 */
    @Override
    public ISiteCategory[] getCategories() {
        return fCategories.toArray(new ISiteCategory[fCategories.size()]);
    }

    @Override
    protected void parse(Node node) {
        super.parse(node);
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (//$NON-NLS-1$
            child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equalsIgnoreCase("category")) {
                SiteCategory category = (SiteCategory) getModel().getFactory().createCategory(this);
                category.parse(child);
                category.setInTheModel(true);
                fCategories.add(category);
            }
        }
    }

    @Override
    protected void reset() {
        super.reset();
        fCategories.clear();
    }

    /**
	 * @see org.eclipse.pde.core.IWritable#write(java.lang.String, java.io.PrintWriter)
	 */
    @Override
    public void write(String indent, PrintWriter writer) {
        writer.print(indent);
        //$NON-NLS-1$
        writer.print("<bundle");
        if (id != null)
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.print(" id=\"" + getId() + "\"");
        if (version != null)
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.print(" version=\"" + getVersion() + "\"");
        if (label != null)
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.print(" label=\"" + getLabel() + "\"");
        if (fCategories.size() > 0) {
            //$NON-NLS-1$
            writer.println(">");
            //$NON-NLS-1$
            String indent2 = indent + "   ";
            for (int i = 0; i < fCategories.size(); i++) {
                ISiteCategory category = fCategories.get(i);
                category.write(indent2, writer);
            }
            //$NON-NLS-1$
            writer.println(indent + "</bundle>");
        } else
            //$NON-NLS-1$
            writer.println("/>");
    }
}
