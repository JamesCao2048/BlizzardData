/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;

public class UnresolvedReferenceBinding extends ReferenceBinding {

    ReferenceBinding resolvedType;

    TypeBinding[] wrappers;

     UnresolvedReferenceBinding(char[][] compoundName, PackageBinding packageBinding) {
        this.compoundName = compoundName;
        // reasonable guess
        this.sourceName = compoundName[compoundName.length - 1];
        this.fPackage = packageBinding;
        this.wrappers = null;
    }

    void addWrapper(TypeBinding wrapper) {
        if (this.wrappers == null) {
            this.wrappers = new TypeBinding[] { wrapper };
        } else {
            int length = this.wrappers.length;
            System.arraycopy(this.wrappers, 0, this.wrappers = new TypeBinding[length + 1], 0, length);
            this.wrappers[length] = wrapper;
        }
    }

    public String debugName() {
        return toString();
    }

    ReferenceBinding resolve(LookupEnvironment environment, boolean convertGenericToRawType) {
        ReferenceBinding targetType = this.resolvedType;
        if (targetType == null) {
            targetType = this.fPackage.getType0(this.compoundName[this.compoundName.length - 1]);
            if (targetType == this)
                targetType = environment.askForType(this.compoundName);
            if (// could not resolve any better, error was already reported against it
            targetType != null && targetType != this) {
                setResolvedType(targetType, environment);
            } else {
                environment.problemReporter.isClassPathCorrect(this.compoundName, null);
                // will not get here since the above error aborts the compilation
                return null;
            }
        }
        if (// raw reference to generic ?
        convertGenericToRawType && targetType.isGenericType())
            return environment.createRawType(targetType, null);
        return targetType;
    }

    void setResolvedType(ReferenceBinding targetType, LookupEnvironment environment) {
        // already resolved
        if (this.resolvedType == targetType)
            return;
        // targetType may be a source or binary type
        this.resolvedType = targetType;
        // otherwise we could create 2 : 1 for this unresolved type & 1 for the resolved type
        if (this.wrappers != null)
            for (int i = 0, l = this.wrappers.length; i < l; i++) this.wrappers[i].swapUnresolved(this, targetType, environment);
        environment.updateCaches(this, targetType);
    }

    public String toString() {
        //$NON-NLS-1$ //$NON-NLS-2$
        return "Unresolved type " + ((compoundName != null) ? CharOperation.toString(compoundName) : "UNNAMED");
    }
}
