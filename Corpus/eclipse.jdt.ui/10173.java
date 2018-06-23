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
package org.eclipse.jdt.internal.ui.callhierarchy;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

public class MethodWrapperWorkbenchAdapter implements IWorkbenchAdapter {

    private final MethodWrapper fMethodWrapper;

    public  MethodWrapperWorkbenchAdapter(MethodWrapper methodWrapper) {
        Assert.isNotNull(methodWrapper);
        fMethodWrapper = methodWrapper;
    }

    public MethodWrapper getMethodWrapper() {
        return fMethodWrapper;
    }

    @Override
    public //should not be called
    Object[] getChildren(//should not be called
    Object o) {
        return new Object[0];
    }

    @Override
    public ImageDescriptor getImageDescriptor(Object object) {
        return null;
    }

    @Override
    public String getLabel(Object o) {
        return fMethodWrapper.getMember().getElementName();
    }

    @Override
    public Object getParent(Object o) {
        return fMethodWrapper.getParent();
    }

    @Override
    public boolean equals(Object obj) {
        //Note: A MethodWrapperWorkbenchAdapter is equal to its MethodWrapper and vice versa (bug 101677).
        return fMethodWrapper.equals(obj);
    }

    @Override
    public int hashCode() {
        //Note: A MethodWrapperWorkbenchAdapter is equal to its MethodWrapper and vice versa (bug 101677).
        return fMethodWrapper.hashCode();
    }
}
