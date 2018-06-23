/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.variables;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action which opens preference settings for Java variables.
 */
public class VariableOptionsAction implements IViewActionDelegate {

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        //$NON-NLS-1$
        SWTFactory.showPreferencePage(//$NON-NLS-1$
        "org.eclipse.jdt.debug.ui.JavaDetailFormattersPreferencePage", new String[] { //$NON-NLS-1$
        "org.eclipse.jdt.debug.ui.JavaDetailFormattersPreferencePage", "org.eclipse.jdt.debug.ui.JavaLogicalStructuresPreferencePage", "org.eclipse.jdt.debug.ui.heapWalking", "org.eclipse.jdt.debug.ui.JavaPrimitivesPreferencePage" });
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }
}
