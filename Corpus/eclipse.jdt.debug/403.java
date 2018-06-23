/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.console;

import java.net.InetAddress;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.commands.actions.TerminateAllActionDelegate;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.internal.console.ConsoleView;

/**
 * Test the Terminate All action in Console view
 *
 */
public class ConsoleTerminateAllActionTests extends AbstractDebugTest {

    public  ConsoleTerminateAllActionTests(String name) {
        super(name);
    }

    /*
	 * adding the test temporarily for finding the cause
	 */
    public void testLocalHostConnection() throws Exception {
        InetAddress address = InetAddress.getByName("localhost");
        assertNotNull(address);
    }

    public void testTerminateAll_01() throws Exception {
        createLineBreakpoint(15, "TerminateAll_01");
        createLineBreakpoint(15, "TerminateAll_02");
        IJavaThread thread1 = null;
        IJavaThread thread2 = null;
        {
            try {
                thread1 = launchToBreakpoint("TerminateAll_01");
                assertNotNull(thread1);
                thread2 = launchToBreakpoint("TerminateAll_02");
                assertNotNull(thread2);
                ConsoleView view = null;
                IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
                for (int i = 0; i < workbenchWindows.length; i++) {
                    IWorkbenchWindow window = workbenchWindows[i];
                    if (window != null) {
                        IWorkbenchPage page = window.getActivePage();
                        if (page != null) {
                            IViewPart part = page.findView(IConsoleConstants.ID_CONSOLE_VIEW);
                            if (part != null && part instanceof IConsoleView) {
                                view = (ConsoleView) part;
                                break;
                            }
                        }
                    }
                }
                assertNotNull(view);
                TerminateAllActionDelegate d = new TerminateAllActionDelegate();
                d.init(view);
                d.run(null);
                Thread.sleep(1000);
                assertEquals(2, DebugPlugin.getDefault().getLaunchManager().getLaunches().length);
                assertEquals(true, DebugPlugin.getDefault().getLaunchManager().getLaunches()[0].isTerminated());
                assertEquals(true, DebugPlugin.getDefault().getLaunchManager().getLaunches()[1].isTerminated());
            } finally {
                terminateAndRemove(thread1);
                terminateAndRemove(thread2);
                removeAllBreakpoints();
            }
        }
    }
}
