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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.pde.core.ModelChangedEvent;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PDECoreMessages;
import org.eclipse.pde.internal.core.isite.ISite;
import org.eclipse.pde.internal.core.isite.ISiteModel;
import org.eclipse.pde.internal.core.isite.ISiteObject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class SiteObject extends PlatformObject implements ISiteObject {

    private static final long serialVersionUID = 1L;

    transient ISiteModel model;

    transient ISiteObject parent;

    protected String label;

    boolean inTheModel;

    void setInTheModel(boolean value) {
        inTheModel = value;
    }

    @Override
    public boolean isInTheModel() {
        return inTheModel;
    }

    protected void ensureModelEditable() throws CoreException {
        if (!model.isEditable()) {
            throwCoreException(PDECoreMessages.SiteObject_readOnlyChange);
        }
    }

    protected void firePropertyChanged(String property, Object oldValue, Object newValue) {
        firePropertyChanged(this, property, oldValue, newValue);
    }

    protected void firePropertyChanged(ISiteObject object, String property, Object oldValue, Object newValue) {
        if (model.isEditable()) {
            model.fireModelObjectChanged(object, property, oldValue, newValue);
        }
    }

    protected void fireStructureChanged(ISiteObject child, int changeType) {
        fireStructureChanged(new ISiteObject[] { child }, changeType);
    }

    protected void fireStructureChanged(ISiteObject[] children, int changeType) {
        ISiteModel model = getModel();
        if (model.isEditable()) {
            model.fireModelChanged(new ModelChangedEvent(model, changeType, children, null));
        }
    }

    @Override
    public ISite getSite() {
        return model.getSite();
    }

    @Override
    public String getLabel() {
        return label;
    }

    public String getTranslatableLabel() {
        if (label == null)
            //$NON-NLS-1$
            return "";
        return model.getResourceString(label);
    }

    @Override
    public ISiteModel getModel() {
        return model;
    }

    String getNodeAttribute(Node node, String name) {
        NamedNodeMap atts = node.getAttributes();
        Node attribute = null;
        if (atts != null)
            attribute = atts.getNamedItem(name);
        if (attribute != null)
            return attribute.getNodeValue();
        return null;
    }

    int getIntegerAttribute(Node node, String name) {
        String value = getNodeAttribute(node, name);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
            }
        }
        return 0;
    }

    boolean getBooleanAttribute(Node node, String name) {
        String value = getNodeAttribute(node, name);
        if (value != null) {
            //$NON-NLS-1$
            return value.equalsIgnoreCase("true");
        }
        return false;
    }

    protected String getNormalizedText(String source) {
        String result = source.replace('\t', ' ');
        result = result.trim();
        return result;
    }

    @Override
    public ISiteObject getParent() {
        return parent;
    }

    protected void parse(Node node) {
        //$NON-NLS-1$
        label = getNodeAttribute(node, "label");
    }

    protected void reset() {
        label = null;
    }

    @Override
    public void setLabel(String newLabel) throws CoreException {
        ensureModelEditable();
        Object oldValue = this.label;
        label = newLabel;
        firePropertyChanged(P_LABEL, oldValue, newLabel);
    }

    protected void throwCoreException(String message) throws CoreException {
        Status status = new Status(IStatus.ERROR, PDECore.PLUGIN_ID, IStatus.OK, message, null);
        CoreException ce = new CoreException(status);
        ce.fillInStackTrace();
        throw ce;
    }

    public static String getWritableString(String source) {
        if (source == null)
            //$NON-NLS-1$
            return "";
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            switch(c) {
                case '&':
                    //$NON-NLS-1$
                    buf.append(//$NON-NLS-1$
                    "&amp;");
                    break;
                case '<':
                    //$NON-NLS-1$
                    buf.append(//$NON-NLS-1$
                    "&lt;");
                    break;
                case '>':
                    //$NON-NLS-1$
                    buf.append(//$NON-NLS-1$
                    "&gt;");
                    break;
                case '\'':
                    //$NON-NLS-1$
                    buf.append(//$NON-NLS-1$
                    "&apos;");
                    break;
                case '\"':
                    //$NON-NLS-1$
                    buf.append(//$NON-NLS-1$
                    "&quot;");
                    break;
                default:
                    buf.append(c);
                    break;
            }
        }
        return buf.toString();
    }

    public void restoreProperty(String name, Object oldValue, Object newValue) throws CoreException {
        if (name.equals(P_LABEL)) {
            setLabel(newValue != null ? newValue.toString() : null);
        }
    }

    @Override
    public void write(String indent, PrintWriter writer) {
    }

    public void setModel(ISiteModel model) {
        this.model = model;
    }

    public void setParent(ISiteObject parent) {
        this.parent = parent;
    }
}
