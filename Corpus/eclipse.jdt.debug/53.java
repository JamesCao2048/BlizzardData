/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public class StaticVariableTests extends AbstractDebugTest {

    public  StaticVariableTests(String name) {
        super(name);
    }

    public void testSetValue() throws Exception {
        String typeName = "StaticVariablesTests";
        ILineBreakpoint bp = createLineBreakpoint(40, typeName);
        IJavaThread thread = null;
        try {
            thread = launchToLineBreakpoint(typeName, bp);
            IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
            IVariable pubStr = findVariable(frame, "pubStr");
            assertNotNull("Could not find variable 'pubStr'", pubStr);
            assertEquals("Value should be 'public'", "public", pubStr.getValue().getValueString());
            pubStr.setValue(((IJavaDebugTarget) frame.getDebugTarget()).newValue("test"));
            assertEquals("Value should be 'test'", "test", pubStr.getValue().getValueString());
        } finally {
            terminateAndRemove(thread);
            removeAllBreakpoints();
        }
    }
}
