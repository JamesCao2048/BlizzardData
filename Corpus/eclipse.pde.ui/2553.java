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
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IPackageDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IReferenceTypeDescriptor;

/**
 * Package description.
 *
 * @since 1.0.0
 */
public class PackageDescriptorImpl extends NamedElementDescriptorImpl implements IPackageDescriptor {

    /**
	 * Constructs a package description
	 *
	 * @param name dot qualified package name, empty string for default package
	 */
    public  PackageDescriptorImpl(String name) {
        super(name);
    }

    @Override
    public String toString() {
        String name = getName();
        //$NON-NLS-1$ //$NON-NLS-2$
        return name.equals("") ? "<default package>" : name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IPackageDescriptor) {
            IPackageDescriptor pkg = (IPackageDescriptor) obj;
            return getName().equals(pkg.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public int getElementType() {
        return IElementDescriptor.PACKAGE;
    }

    @Override
    public IReferenceTypeDescriptor getType(String typeQualifiedName, String signature) {
        //$NON-NLS-1$
        String[] names = typeQualifiedName.split("\\$");
        IReferenceTypeDescriptor typeDescriptor = new ReferenceTypeDescriptorImpl(names[0], this, signature);
        for (int i = 1; i < names.length; i++) {
            typeDescriptor = typeDescriptor.getType(names[i]);
        }
        return typeDescriptor;
    }

    @Override
    public IReferenceTypeDescriptor getType(String typeQualifiedName) {
        //$NON-NLS-1$
        String[] names = typeQualifiedName.split("\\$");
        IReferenceTypeDescriptor typeDescriptor = new ReferenceTypeDescriptorImpl(names[0], this);
        for (int i = 1; i < names.length; i++) {
            typeDescriptor = typeDescriptor.getType(names[i]);
        }
        return typeDescriptor;
    }
}
