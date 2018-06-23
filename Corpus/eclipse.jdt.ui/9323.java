/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.junit.ui;

import org.eclipse.jdt.debug.ui.console.JavaStackTraceConsoleFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jdt.internal.junit.model.TestElement;

/**
 * Action to show the stack trace of a failed test from JUnit view's failure trace in debug's Java
 * stack trace console.
 */
public class ShowStackTraceInConsoleViewAction extends Action {

    private FailureTrace fView;

    private JavaStackTraceConsoleFactory fFactory;

    public  ShowStackTraceInConsoleViewAction(FailureTrace view) {
        super(JUnitMessages.ShowStackTraceInConsoleViewAction_label, IAction.AS_PUSH_BUTTON);
        setDescription(JUnitMessages.ShowStackTraceInConsoleViewAction_description);
        setToolTipText(JUnitMessages.ShowStackTraceInConsoleViewAction_tooltip);
        //$NON-NLS-1$
        setHoverImageDescriptor(JUnitPlugin.getImageDescriptor("elcl16/open_console.png"));
        //$NON-NLS-1$
        setImageDescriptor(JUnitPlugin.getImageDescriptor("elcl16/open_console.png"));
        fView = view;
    }

    @Override
    public void run() {
        TestElement failedTest = fView.getFailedTest();
        String stackTrace = failedTest.getTrace();
        if (stackTrace != null) {
            if (fFactory == null) {
                fFactory = new JavaStackTraceConsoleFactory();
            }
            fFactory.openConsole(stackTrace);
        }
    }
}
