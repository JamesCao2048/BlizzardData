/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.Iterator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.model.JDIInterfaceType;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIVariable;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Opens the concrete type hierarhcy of variable - i.e. it's value's actual type.
 */
public class OpenVariableConcreteTypeHierarchyAction extends OpenVariableConcreteTypeAction {

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.OpenTypeAction#isHierarchy()
	 */
    @Override
    protected boolean isHierarchy() {
        return true;
    }

    @Override
    public void run(IAction action) {
        IStructuredSelection selection = getCurrentSelection();
        if (selection == null) {
            return;
        }
        Iterator<?> itr = selection.iterator();
        try {
            while (itr.hasNext()) {
                Object element = itr.next();
                if (element instanceof JDIVariable && ((JDIVariable) element).getJavaType() instanceof JDIInterfaceType) {
                    JDIObjectValue val = (JDIObjectValue) ((JDIVariable) element).getValue();
                    if (//$NON-NLS-1$
                    val.getJavaType().toString().contains(//$NON-NLS-1$
                    "$$Lambda$")) {
                        OpenVariableDeclaredTypeAction declaredAction = new OpenVariableDeclaredTypeHierarchyAction();
                        declaredAction.setActivePart(action, getPart());
                        declaredAction.run(action);
                        return;
                    }
                }
                Object sourceElement = resolveSourceElement(element);
                if (sourceElement != null) {
                    openInEditor(sourceElement);
                } else {
                    IStatus status = new //$NON-NLS-1$
                    Status(//$NON-NLS-1$
                    IStatus.INFO, //$NON-NLS-1$
                    IJavaDebugUIConstants.PLUGIN_ID, //$NON-NLS-1$
                    IJavaDebugUIConstants.INTERNAL_ERROR, //$NON-NLS-1$
                    "Source not found", //$NON-NLS-1$
                    null);
                    throw new CoreException(status);
                }
            }
        } catch (CoreException e) {
            JDIDebugUIPlugin.statusDialog(e.getStatus());
        }
    }
}
