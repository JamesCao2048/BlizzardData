/*******************************************************************************
 *  Copyright (c) 2000, 2010 IBM Corporation and others.
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.pde.core.plugin.*;
import org.w3c.dom.Node;

public class PluginExtensionPoint extends IdentifiablePluginObject implements IPluginExtensionPoint {

    private static final long serialVersionUID = 1L;

    private IExtensionPoint fPoint = null;

    protected String fSchema;

    public  PluginExtensionPoint() {
    }

    public  PluginExtensionPoint(IExtensionPoint point) {
        fPoint = point;
    }

    @Override
    public boolean isValid() {
        return getId() != null && getName() != null;
    }

    @Override
    public String getFullId() {
        if (fPoint != null)
            return fPoint.getUniqueIdentifier();
        String pointId = getId();
        IPluginModelBase modelBase = getPluginModel();
        IPluginBase pluginBase = modelBase.getPluginBase();
        String schemaVersion = pluginBase.getSchemaVersion();
        if (schemaVersion != null && Double.parseDouble(schemaVersion) >= 3.2) {
            if (pointId.indexOf('.') > 0)
                return pointId;
        }
        if (pluginBase instanceof IFragment)
            return ((IFragment) pluginBase).getPluginId() + '.' + pointId;
        return pluginBase.getId() + '.' + pointId;
    }

    @Override
    public String getSchema() {
        if (fSchema == null && fPoint != null)
            fSchema = fPoint.getSchemaReference();
        return fSchema;
    }

    void load(Node node) {
        //$NON-NLS-1$
        this.fID = getNodeAttribute(node, "id");
        //$NON-NLS-1$
        fName = getNodeAttribute(node, "name");
        //$NON-NLS-1$
        fSchema = getNodeAttribute(node, "schema");
        //$NON-NLS-1$
        fStartLine = Integer.parseInt(getNodeAttribute(node, "line"));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof IPluginExtensionPoint) {
            IPluginExtensionPoint target = (IPluginExtensionPoint) obj;
            // binary equal
            if (target.getModel().equals(getModel()))
                return false;
            if (stringEqualWithNull(target.getFullId(), getId()) && stringEqualWithNull(target.getName(), getName()) && stringEqualWithNull(target.getSchema(), getSchema()))
                return true;
        }
        return false;
    }

    @Override
    public void setSchema(String newSchema) throws CoreException {
        ensureModelEditable();
        String oldValue = fSchema;
        fSchema = newSchema;
        firePropertyChanged(P_SCHEMA, oldValue, fSchema);
    }

    @Override
    public void restoreProperty(String name, Object oldValue, Object newValue) throws CoreException {
        if (name.equals(P_SCHEMA)) {
            setSchema(newValue != null ? newValue.toString() : null);
            return;
        }
        super.restoreProperty(name, oldValue, newValue);
    }

    @Override
    public void write(String indent, PrintWriter writer) {
        writer.print(indent);
        //$NON-NLS-1$
        writer.print("<extension-point");
        if (getId() != null)
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.print(" id=\"" + getWritableString(getId()) + "\"");
        if (getName() != null)
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.print(" name=\"" + getWritableString(getName()) + "\"");
        if (getSchema() != null)
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.print(" schema=\"" + getSchema() + "\"");
        //$NON-NLS-1$
        writer.println("/>");
    }

    @Override
    public String getName() {
        if (fName == null)
            fName = fPoint.getLabel();
        return fName;
    }

    @Override
    public String getId() {
        if (fID == null) {
            fID = fPoint.getUniqueIdentifier();
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
}
