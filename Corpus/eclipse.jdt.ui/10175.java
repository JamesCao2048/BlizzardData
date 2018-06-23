/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.astview.views;

import org.eclipse.swt.graphics.Image;
import org.eclipse.jdt.core.dom.IBinding;

/**
 *
 */
public class BindingProperty extends ASTAttribute {

    private final String fName;

    private final Binding fParent;

    private final ASTAttribute[] fValues;

    private final boolean fIsRelevant;

    public  BindingProperty(Binding parent, String name, Object value, boolean isRelevant) {
        fParent = parent;
        if (value instanceof String) {
            if (((String) value).length() == 0) {
                fName = name + ": (empty string)";
            } else {
                fName = name + ": " + //$NON-NLS-1$
                Binding.getEscapedStringLiteral(//$NON-NLS-1$
                (String) value);
            }
        } else if (value instanceof Character) {
            //$NON-NLS-1$
            fName = name + ": " + Binding.getEscapedCharLiteral(((Character) value).charValue());
        } else {
            //$NON-NLS-1$
            fName = name + ": " + String.valueOf(value);
        }
        fValues = null;
        fIsRelevant = isRelevant;
    }

    public  BindingProperty(Binding parent, String name, boolean value, boolean isRelevant) {
        fParent = parent;
        //$NON-NLS-1$
        fName = name + ": " + String.valueOf(value);
        fValues = null;
        fIsRelevant = isRelevant;
    }

    public  BindingProperty(Binding parent, String name, int value, boolean isRelevant) {
        fParent = parent;
        //$NON-NLS-1$
        fName = name + ": " + String.valueOf(value);
        fValues = null;
        fIsRelevant = isRelevant;
    }

    public  BindingProperty(Binding parent, String name, IBinding[] bindings, boolean isRelevant) {
        fParent = parent;
        if (bindings == null || bindings.length == 0) {
            //$NON-NLS-1$
            fName = name + " (0)";
            fValues = null;
        } else {
            fValues = createBindings(bindings, isRelevant);
            //$NON-NLS-1$
            fName = name + " (" + fValues.length + ')';
        }
        fIsRelevant = isRelevant;
    }

    public  BindingProperty(Binding parent, String name, ASTAttribute[] children, boolean isRelevant) {
        fParent = parent;
        if (children == null) {
            children = new ASTAttribute[0];
        }
        fValues = children;
        //$NON-NLS-1$
        fName = name + " (" + fValues.length + ')';
        fIsRelevant = isRelevant;
    }

    public  BindingProperty(Binding parent, StringBuffer label, boolean isRelevant) {
        fParent = parent;
        fName = label.toString();
        fValues = null;
        fIsRelevant = isRelevant;
    }

    private Binding[] createBindings(IBinding[] bindings, boolean isRelevant) {
        Binding[] res = new Binding[bindings.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = new Binding(this, String.valueOf(i), bindings[i], isRelevant);
        }
        return res;
    }

    @Override
    public Object getParent() {
        return fParent;
    }

    @Override
    public Object[] getChildren() {
        if (fValues != null) {
            return fValues;
        }
        return EMPTY;
    }

    @Override
    public String getLabel() {
        return fName;
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public String toString() {
        return getLabel();
    }

    public boolean isRelevant() {
        return fIsRelevant;
    }

    /*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || !obj.getClass().equals(getClass())) {
            return false;
        }
        BindingProperty other = (BindingProperty) obj;
        if (fParent == null) {
            if (other.fParent != null)
                return false;
        } else if (!fParent.equals(other.fParent)) {
            return false;
        }
        if (fName == null) {
            if (other.fName != null)
                return false;
        } else if (!fName.equals(other.fName)) {
            return false;
        }
        return true;
    }

    /*
	 * @see java.lang.Object#hashCode()
	 */
    @Override
    public int hashCode() {
        return (fParent != null ? fParent.hashCode() : 0) + (fName != null ? fName.hashCode() : 0);
    }
}
