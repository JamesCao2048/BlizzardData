/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests IProcess.
 */
public class ProcessTests extends AbstractDebugTest {

    public  ProcessTests(String name) {
        super(name);
    }

    public void testExitValueNormal() throws Exception {
        String typeName = "Breakpoints";
        ILineBreakpoint bp = createLineBreakpoint(43, typeName);
        IJavaThread thread = null;
        try {
            thread = launchToLineBreakpoint(typeName, bp);
            IProcess process = thread.getDebugTarget().getProcess();
            assertNotNull("Missing process", process);
            int exitValue = -1;
            try {
                exitValue = process.getExitValue();
            } catch (DebugException e) {
                exit(thread);
                exitValue = process.getExitValue();
                assertEquals("Exit value not normal", 0, exitValue);
                return;
            }
            assertTrue("Should not be able to get exit value - process not terminated", false);
        } finally {
            terminateAndRemove(thread);
            removeAllBreakpoints();
        }
    }

    /**
	 * Tests that an already terminated java.lang.Process behaves properly when
	 * wrapped in an IProcess.
	 * 
	 * @throws Exception
	 */
    public void testAlreadyTerminatedProcess() throws Exception {
        Process process = DebugPlugin.exec(new String[] { "java" }, null);
        boolean terminated = false;
        int value = -1;
        while (!terminated) {
            try {
                value = process.exitValue();
                terminated = true;
            } catch (IllegalThreadStateException e) {
                process.getInputStream().read(new byte[1000], 0, 1000);
                Thread.sleep(500);
            }
        }
        Launch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
        IProcess iProcess = DebugPlugin.newProcess(launch, process, "Testing123");
        assertTrue("Process should be terminated", iProcess.isTerminated());
        assertEquals("Wrong exit value", value, iProcess.getExitValue());
    }
}
