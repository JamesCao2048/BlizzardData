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
package org.eclipse.pde.internal.core.text.plugin;

import java.io.PrintWriter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.plugin.IPluginExtensionPoint;
import org.eclipse.pde.internal.core.text.IDocumentElementNode;

public class PluginExtensionPointNode extends PluginObjectNode implements IPluginExtensionPoint {

    private static final long serialVersionUID = 1L;

    @Override
    public String getFullId() {
        String id = getId();
        String version = getPluginBase().getSchemaVersion();
        if (version != null && Double.parseDouble(version) >= 3.2 && id != null && id.indexOf('.') != -1)
            return id;
        String pluginID = getPluginBase().getId();
        //$NON-NLS-1$
        return (pluginID != null) ? pluginID + "." + id : id;
    }

    @Override
    public String getSchema() {
        //$NON-NLS-1$
        return getXMLAttributeValue("schema");
    }

    @Override
    public void setSchema(String schema) throws CoreException {
        setXMLAttribute(P_SCHEMA, schema);
    }

    @Override
    public String getId() {
        return getXMLAttributeValue(P_ID);
    }

    @Override
    public void setId(String id) throws CoreException {
        setXMLAttribute(P_ID, id);
    }

    @Override
    public void setName(String name) throws CoreException {
        setXMLAttribute(P_NAME, name);
    }

    @Override
    public String getName() {
        return getXMLAttributeValue(P_NAME);
    }

    @Override
    public String write(boolean indent) {
        return indent ? getIndent() + writeShallow(true) : writeShallow(true);
    }

    @Override
    public String writeShallow(boolean terminate) {
        //$NON-NLS-1$
        StringBuffer buffer = new StringBuffer("<extension-point");
        appendAttribute(buffer, P_ID);
        appendAttribute(buffer, P_NAME);
        appendAttribute(buffer, P_SCHEMA);
        if (terminate)
            //$NON-NLS-1$
            buffer.append("/");
        //$NON-NLS-1$
        buffer.append(">");
        return buffer.toString();
    }

    @Override
    public void reconnect(IDocumentElementNode parent, IModel model) {
        super.reconnect(parent, model);
    }

    @Override
    public void write(String indent, PrintWriter writer) {
        // Used for text transfers for copy, cut, paste operations
        writer.write(write(true));
    }

    @Override
    public void writeDelimeter(PrintWriter writer) {
        writer.println(getIndent());
    }
}
