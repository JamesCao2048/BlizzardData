/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.plugin;

import java.io.PrintWriter;
import java.util.ArrayList;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ischema.ISchema;
import org.eclipse.pde.internal.core.schema.SchemaRegistry;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PluginExtension extends PluginParent implements IPluginExtension {

    private static final long serialVersionUID = 1L;

    protected String fPoint;

    private transient ISchema schema;

    private IExtension fExtension = null;

    public  PluginExtension() {
    }

    public  PluginExtension(IExtension extension) {
        fExtension = extension;
    }

    @Override
    public String getPoint() {
        if (fPoint == null && fExtension != null)
            fPoint = fExtension.getExtensionPointUniqueIdentifier();
        return fPoint;
    }

    @Override
    public boolean isValid() {
        return getPoint() != null;
    }

    @Override
    public Object getSchema() {
        if (schema == null) {
            SchemaRegistry registry = PDECore.getDefault().getSchemaRegistry();
            schema = registry.getSchema(fPoint);
        } else if (schema.isDisposed()) {
            schema = null;
        }
        return schema;
    }

    /*
	 * If this function is used to load the model, the extension registry cache will not be used when querying model.
	 */
    void load(Node node) {
        //$NON-NLS-1$
        this.fID = getNodeAttribute(node, "id");
        //$NON-NLS-1$
        fName = getNodeAttribute(node, "name");
        //$NON-NLS-1$
        fPoint = getNodeAttribute(node, "point");
        if (fChildren == null)
            fChildren = new ArrayList();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                PluginElement childElement = new PluginElement();
                childElement.setModel(getModel());
                childElement.setInTheModel(true);
                childElement.setParent(this);
                this.fChildren.add(childElement);
                childElement.load(child);
            }
        }
        //$NON-NLS-1$
        fStartLine = Integer.parseInt(getNodeAttribute(node, "line"));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof IPluginExtension) {
            IPluginExtension target = (IPluginExtension) obj;
            // comparing the model is a little complicated since we need to allow text and non-text models representing the same file
            if (target.getModel().getClass() == getModel().getClass()) {
                if (!target.getModel().equals(getModel()))
                    return false;
            } else {
                // need to account for text model representing the same resource.
                IResource res = getModel().getUnderlyingResource();
                if (res == null) {
                    // model is external model
                    if (!(target.getModel().getInstallLocation().equals(getModel().getInstallLocation())))
                        return false;
                // model is a workspace model.  Need to compare underlyingResource because text and non-text model return differently formatted strings
                } else if (!(res.equals(target.getModel().getUnderlyingResource())))
                    return false;
            }
            if (!stringEqualWithNull(target.getId(), getId()))
                return false;
            if (!stringEqualWithNull(target.getPoint(), getPoint()))
                return false;
            if (!nameEqual(target.getName()))
                return false;
            // Children
            return super.equals(obj);
        }
        return false;
    }

    private boolean nameEqual(String targetName) {
        // Note, we only do this if the PluginExtension has an fExtension element which means it's name comes from the extension registry.
        if (fExtension != null && targetName == null)
            //$NON-NLS-1$
            targetName = "";
        return stringEqualWithNull(targetName, getName());
    }

    @Override
    public void setPoint(String point) throws CoreException {
        ensureModelEditable();
        String oldValue = fPoint;
        fPoint = point;
        firePropertyChanged(P_POINT, oldValue, point);
    }

    @Override
    public void restoreProperty(String name, Object oldValue, Object newValue) throws CoreException {
        if (name.equals(P_POINT)) {
            setPoint(newValue != null ? newValue.toString() : null);
            return;
        }
        super.restoreProperty(name, oldValue, newValue);
    }

    @Override
    public String toString() {
        if (getName() != null)
            return getName();
        return getPoint();
    }

    @Override
    public void write(String indent, PrintWriter writer) {
        writer.print(indent);
        //$NON-NLS-1$
        writer.print("<extension");
        String attIndent = indent + PluginElement.ATTRIBUTE_SHIFT;
        if (getId() != null) {
            writer.println();
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.print(attIndent + "id=\"" + getId() + "\"");
        }
        if (getName() != null) {
            writer.println();
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.print(attIndent + "name=\"" + getWritableString(getName()) + "\"");
        }
        if (getPoint() != null) {
            writer.println();
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.print(attIndent + "point=\"" + getPoint() + "\"");
        }
        //$NON-NLS-1$
        writer.println(">");
        IPluginObject[] children = getChildren();
        for (int i = 0; i < children.length; i++) {
            IPluginElement child = (IPluginElement) children[i];
            child.write(indent + PluginElement.ELEMENT_SHIFT, writer);
        }
        //$NON-NLS-1$
        writer.println(indent + "</extension>");
    }

    @Override
    public String getName() {
        if (fName == null && fExtension != null) {
            fName = fExtension.getLabel();
        }
        return fName;
    }

    @Override
    public String getId() {
        if (fID == null && fExtension != null) {
            fID = fExtension.getUniqueIdentifier();
            if (fID != null) {
                String pluginId = getPluginBase().getId();
                if (fID.startsWith(pluginId)) {
                    String sub = fID.substring(pluginId.length());
                    if (sub.lastIndexOf('.') == 0)
                        fID = sub.substring(1);
                }
            }
        }
        return fID;
    }

    @Override
    protected ArrayList<IPluginObject> getChildrenList() {
        if (fChildren == null) {
            fChildren = new ArrayList();
            if (fExtension != null) {
                if (fExtension != null) {
                    IConfigurationElement[] elements = fExtension.getConfigurationElements();
                    for (int i = 0; i < elements.length; i++) {
                        PluginElement element = new PluginElement(elements[i]);
                        element.setModel(getModel());
                        element.setParent(this);
                        fChildren.add(element);
                    }
                }
            }
        }
        return fChildren;
    }
}
