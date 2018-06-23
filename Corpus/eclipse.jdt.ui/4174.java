/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.search;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * Opens the Search Dialog and brings the Java search page to front
 */
public class OpenJavaSearchPageAction implements IWorkbenchWindowActionDelegate {

    //$NON-NLS-1$
    private static final String JAVA_SEARCH_PAGE_ID = "org.eclipse.jdt.ui.JavaSearchPage";

    private IWorkbenchWindow fWindow;

    public  OpenJavaSearchPageAction() {
    }

    @Override
    public void init(IWorkbenchWindow window) {
        fWindow = window;
    }

    @Override
    public void run(IAction action) {
        if (fWindow == null || fWindow.getActivePage() == null) {
            beep();
            //$NON-NLS-1$
            JavaPlugin.logErrorMessage("Could not open the search dialog - for some reason the window handle was null");
            return;
        }
        NewSearchUI.openSearchDialog(fWindow, JAVA_SEARCH_PAGE_ID);
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    // do nothing since the action isn't selection dependent.
    }

    @Override
    public void dispose() {
        fWindow = null;
    }

    protected void beep() {
        Shell shell = JavaPlugin.getActiveWorkbenchShell();
        if (shell != null && shell.getDisplay() != null)
            shell.getDisplay().beep();
    }
}
