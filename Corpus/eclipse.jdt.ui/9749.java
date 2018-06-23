/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.astview.views;

import java.util.List;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class NodeProperty extends ASTAttribute {

    private ASTNode fParent;

    private StructuralPropertyDescriptor fProperty;

    public  NodeProperty(ASTNode parent, StructuralPropertyDescriptor property) {
        fParent = parent;
        fProperty = property;
    }

    @Override
    public Object getParent() {
        return fParent;
    }

    @Override
    public Object[] getChildren() {
        Object child = getNode();
        if (child instanceof List) {
            return ((List<?>) child).toArray();
        } else if (child instanceof ASTNode) {
            return new Object[] { child };
        }
        return EMPTY;
    }

    @Override
    public String getLabel() {
        StringBuffer buf = new StringBuffer();
        buf.append(getPropertyName());
        if (fProperty.isSimpleProperty()) {
            //$NON-NLS-1$
            buf.append(": ");
            Object node = getNode();
            if (node != null) {
                buf.append('\'');
                buf.append(getNode().toString());
                buf.append('\'');
            } else {
                //$NON-NLS-1$
                buf.append(//$NON-NLS-1$
                "null");
            }
        } else if (fProperty.isChildListProperty()) {
            List<?> node = (List<?>) getNode();
            //$NON-NLS-1$
            buf.append(" (").append(node.size()).append(')');
        } else // child property
        {
            if (getNode() == null) {
                //$NON-NLS-1$
                buf.append(//$NON-NLS-1$
                ": null");
            }
        }
        return buf.toString();
    }

    @Override
    public Image getImage() {
        return null;
    }

    public Object getNode() {
        return fParent.getStructuralProperty(fProperty);
    }

    public String getPropertyName() {
        return toConstantName(fProperty.getId());
    }

    private static String toConstantName(String string) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (i != 0 && Character.isUpperCase(ch)) {
                buf.append('_');
            }
            buf.append(Character.toUpperCase(ch));
        }
        return buf.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !o.getClass().equals(getClass())) {
            return false;
        }
        NodeProperty castedObj = (NodeProperty) o;
        return fParent.equals(castedObj.fParent) && (fProperty == castedObj.fProperty);
    }

    @Override
    public int hashCode() {
        return fParent.hashCode() * 31 + fProperty.hashCode();
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
