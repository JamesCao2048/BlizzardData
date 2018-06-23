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
package org.eclipse.jdt.internal.debug.core;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

/**
 * Tests properties for java debug elements
 */
public class JavaDebugPropertyTester extends PropertyTester {

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object,
	 * java.lang.String, java.lang.Object[], java.lang.Object)
	 */
    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (//$NON-NLS-1$
        property.equals("isMultiStrata")) {
            if (receiver instanceof IStackFrame) {
                IJavaStackFrame frame = ((IStackFrame) receiver).getAdapter(IJavaStackFrame.class);
                if (frame != null) {
                    try {
                        return frame.getReferenceType().getAvailableStrata().length > 1;
                    } catch (DebugException e) {
                    }
                }
            }
        }
        return false;
    }
}
