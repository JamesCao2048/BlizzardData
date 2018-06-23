/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.descriptors;

import org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IFieldDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IReferenceTypeDescriptor;

/**
 * Description of a field.
 *
 * @since 1.0.0
 */
public class FieldDescriptorImpl extends MemberDescriptorImpl implements IFieldDescriptor {

    /**
	 * Constructs a field descriptor with the given name, declared by the given
	 * type.
	 *
	 * @param name field name
	 * @param parent type containing the field declaration
	 */
     FieldDescriptorImpl(String name, IReferenceTypeDescriptor parent) {
        super(name, parent);
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getEnclosingType().getQualifiedName());
        //$NON-NLS-1$
        buffer.append("#");
        buffer.append(getName());
        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IFieldDescriptor) {
            IFieldDescriptor field = (IFieldDescriptor) obj;
            return getName().equals(field.getName()) && getEnclosingType().equals(field.getEnclosingType());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + getEnclosingType().hashCode();
    }

    @Override
    public int getElementType() {
        return IElementDescriptor.FIELD;
    }
}
