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
package org.eclipse.jdt.internal.debug.ui.console;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.actions.CloseConsoleAction;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * JavaStackTracePageParticipant
 */
public class JavaStackTracePageParticipant implements IConsolePageParticipant {

    private CloseConsoleAction fCloseAction;

    private FormatStackTraceActionDelegate fFormatAction;

    private IHandlerActivation fHandlerActivation;

    private IContextActivation fContextActivation;

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsolePageParticipant#init(org.eclipse.ui.part.IPageBookViewPage, org.eclipse.ui.console.IConsole)
     */
    @Override
    public void init(IPageBookViewPage page, IConsole console) {
        fCloseAction = new CloseConsoleAction(console);
        IToolBarManager manager = page.getSite().getActionBars().getToolBarManager();
        manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fCloseAction);
        fFormatAction = new FormatStackTraceActionDelegate((JavaStackTraceConsole) console);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsolePageParticipant#dispose()
     */
    @Override
    public void dispose() {
        deactivated();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsolePageParticipant#activated()
	 */
    @Override
    public void activated() {
        // add EOF submissions
        IWorkbench workbench = PlatformUI.getWorkbench();
        IHandlerService handlerService = workbench.getAdapter(IHandlerService.class);
        IHandler formatHandler = new AbstractHandler() {

            @Override
            public Object execute(ExecutionEvent event) throws ExecutionException {
                fFormatAction.run(null);
                return null;
            }
        };
        //$NON-NLS-1$
        fHandlerActivation = handlerService.activateHandler("org.eclipse.jdt.ui.edit.text.java.format", formatHandler);
        IContextService contextService = workbench.getAdapter(IContextService.class);
        //$NON-NLS-1$
        fContextActivation = contextService.activateContext("org.eclipse.jdt.ui.javaEditorScope");
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsolePageParticipant#deactivated()
	 */
    @Override
    public void deactivated() {
        // remove EOF submissions
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (fHandlerActivation != null) {
            IHandlerService handlerService = workbench.getAdapter(IHandlerService.class);
            handlerService.deactivateHandler(fHandlerActivation);
            fHandlerActivation = null;
        }
        if (fContextActivation != null) {
            IContextService contextService = workbench.getAdapter(IContextService.class);
            contextService.deactivateContext(fContextActivation);
            fContextActivation = null;
        }
    }
}
