/*******************************************************************************
 *  Copyright (c) 2006, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.search;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class OpenPluginSearchPageAction implements IWorkbenchWindowActionDelegate {

    //$NON-NLS-1$
    private static final String PLUGIN_SEARCH_PAGE_ID = "org.eclipse.pde.internal.ui.search.SearchPage";

    private IWorkbenchWindow fWindow;

    @Override
    public void dispose() {
        fWindow = null;
    }

    @Override
    public void init(IWorkbenchWindow window) {
        fWindow = window;
    }

    @Override
    public void run(IAction action) {
        if (fWindow == null || fWindow.getActivePage() == null) {
            beep();
            return;
        }
        NewSearchUI.openSearchDialog(fWindow, PLUGIN_SEARCH_PAGE_ID);
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    // do nothing since the action isn't selection dependent.
    }

    protected void beep() {
        Shell shell = PDEPlugin.getActiveWorkbenchShell();
        if (shell != null && shell.getDisplay() != null)
            shell.getDisplay().beep();
    }
}
