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
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.ui.IActionFilter;

/*package*/
class ActionFilterAdapterFactory implements IAdapterFactory {

    /**
	 * @see IAdapterFactory#getAdapter(Object, Class)
	 */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Object obj, Class<T> adapterType) {
        if (adapterType.isInstance(obj)) {
            return (T) obj;
        }
        if (adapterType == IActionFilter.class) {
            if (obj instanceof IJavaThread) {
                return (T) new JavaThreadActionFilter();
            } else if (obj instanceof IJavaStackFrame) {
                return (T) new JavaStackFrameActionFilter();
            } else if (obj instanceof IMember) {
                return (T) new MemberActionFilter();
            } else if ((obj instanceof IJavaVariable) || (obj instanceof JavaInspectExpression)) {
                return (T) new JavaVarActionFilter();
            }
        }
        return null;
    }

    /**
	 * @see IAdapterFactory#getAdapterList()
	 */
    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] { IActionFilter.class };
    }
}
