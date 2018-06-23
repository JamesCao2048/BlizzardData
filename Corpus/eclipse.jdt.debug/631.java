/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.sourcelookup;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;

/**
 * Tests resolution of Java debug model type into Java model types.
 */
public class TypeResolutionTests extends AbstractDebugTest {

    public  TypeResolutionTests(String name) {
        super(name);
    }

    public void testTypeAA() throws Exception {
        resolve(65, "EvalNestedTypeTests$A$AA");
    }

    public void testTypeAB() throws Exception {
        resolve(94, "EvalNestedTypeTests$A$AB");
    }

    public void testTypeAC() throws Exception {
        resolve(120, "EvalNestedTypeTests$A$AC");
    }

    public void testAnonTypeA1() throws Exception {
        resolve(145, "EvalNestedTypeTests$A$1");
    }

    public void testTypeAE() throws Exception {
        resolve(179, "EvalNestedTypeTests$A$AE");
    }

    public void testAnonTypeA2() throws Exception {
        resolve(203, "EvalNestedTypeTests$A$2");
    }

    public void testTypeBB() throws Exception {
        resolve(252, "EvalNestedTypeTests$B$BB");
    }

    public void testTypeBC() throws Exception {
        resolve(279, "EvalNestedTypeTests$B$BC");
    }

    public void testAnonTypeB() throws Exception {
        resolve(304, "EvalNestedTypeTests$B$1");
    }

    public void testTypeB() throws Exception {
        resolve(312, "EvalNestedTypeTests$B");
    }

    public void testTypeCB() throws Exception {
        resolve(354, "EvalNestedTypeTests$C$CB");
    }

    public void testTypeCC() throws Exception {
        resolve(381, "EvalNestedTypeTests$C$CC");
    }

    public void testAnonTypeC1() throws Exception {
        resolve(406, "EvalNestedTypeTests$C$1");
    }

    public void testAnonTypeDB() throws Exception {
        resolve(455, "EvalNestedTypeTests$1$DB");
    }

    public void testAnonTypeDC() throws Exception {
        resolve(481, "EvalNestedTypeTests$1$DC");
    }

    public void testAnonType11() throws Exception {
        resolve(506, "EvalNestedTypeTests$1$1");
    }

    public void testTopLevelType() throws Exception {
        resolve(523, "EvalNestedTypeTests");
    }

    public void testTypeEB() throws Exception {
        resolve(566, "EvalNestedTypeTests$E$EB");
    }

    public void testTypeEC() throws Exception {
        resolve(592, "EvalNestedTypeTests$E$EC");
    }

    public void testAnonTypeE1() throws Exception {
        resolve(616, "EvalNestedTypeTests$E$1");
    }

    public void testAnonTypeFB() throws Exception {
        resolve(664, "EvalNestedTypeTests$2$FB");
    }

    public void testAnonTypeFC() throws Exception {
        resolve(690, "EvalNestedTypeTests$2$FC");
    }

    public void testAnonType21() throws Exception {
        resolve(714, "EvalNestedTypeTests$2$1");
    }

    /**
	 * Performs a resolution test. Debugs to a breakpoint and resolves the
	 * declaring type of the stack frame.
	 * 
	 * @param line breakpoint line number
	 * @param expectedName expected fully qualified name of resolved type
	 * @throws Exception on failure
	 */
    protected void resolve(int line, String expectedName) throws Exception {
        String typeName = "EvalNestedTypeTests";
        IJavaLineBreakpoint bp = createLineBreakpoint(line, typeName);
        IJavaThread thread = null;
        try {
            thread = launchToBreakpoint(typeName, false);
            assertNotNull("Breakpoint not hit within timeout period", thread);
            IBreakpoint hit = getBreakpoint(thread);
            assertEquals("Wrong breakpoint", bp, hit);
            IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
            IJavaReferenceType referenceType = frame.getReferenceType();
            IType type = JavaDebugUtils.resolveType(referenceType);
            assertNotNull("failed to resolve type", type);
            assertEquals("Wrong type", expectedName, type.getFullyQualifiedName());
        } finally {
            terminateAndRemove(thread);
            removeAllBreakpoints();
        }
    }
}
