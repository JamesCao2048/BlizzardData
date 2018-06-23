/*******************************************************************************
 * Copyright (c) Mar 6, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.eval;

import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests that evaluations in non-generified source
 * 
 * @since 3.8.100
 */
public class GeneralEvalTests extends AbstractDebugTest {

    /**
	 * @param name
	 */
    public  GeneralEvalTests(String name) {
        super(name);
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
    public void testInnerType1() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug329294";
            createLineBreakpoint(22, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("The program did not suspend", thread);
            String snippet = "inner";
            doEval(thread, snippet);
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
    public void testInnerType2() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug329294";
            createLineBreakpoint(26, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("The program did not suspend", thread);
            String snippet = "fInner1.innerBool";
            doEval(thread, snippet);
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
    public void testInnerType3() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug329294";
            createLineBreakpoint(30, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("The program did not suspend", thread);
            String snippet = "!fInner1.innerBool";
            doEval(thread, snippet);
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=329294
	 * @throws Exception
	 */
    public void testInnerAnonymousType() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug329294";
            createLineBreakpoint(7, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("The program did not suspend", thread);
            String snippet = "fInner1.innerBool";
            doEval(thread, snippet);
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
    public void testMultipleInfixEval1() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug401270";
            createLineBreakpoint(13, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("the program did not suspend", thread);
            String snippet = "(true==true==true==true==true)";
            IValue value = doEval(thread, snippet);
            assertTrue("The result of (true==true==true==true==true) should be true", Boolean.parseBoolean(value.getValueString()));
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
    public void testMultipleInfixEval2() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug401270";
            createLineBreakpoint(14, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("the program did not suspend", thread);
            String snippet = "!(true==true==true==true==true)";
            IValue value = doEval(thread, snippet);
            assertFalse("The result of !(true==true==true==true==true) should be false", Boolean.parseBoolean(value.getValueString()));
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
    public void testMultipleInfixEval3() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug401270";
            createLineBreakpoint(15, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("the program did not suspend", thread);
            String snippet = "(true&&true&&true&&true&&true)";
            IValue value = doEval(thread, snippet);
            assertTrue("The result of (true&&true&&true&&true&&true) should be true", Boolean.parseBoolean(value.getValueString()));
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
    public void testMultipleInfixEval4() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug401270";
            createLineBreakpoint(16, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("the program did not suspend", thread);
            String snippet = "!(true&&true&&true&&true&&true)";
            IValue value = doEval(thread, snippet);
            assertFalse("The result of !(true&&true&&true&&true&&true) should be false", Boolean.parseBoolean(value.getValueString()));
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
    public void testMultipleInfixEval5() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug401270";
            createLineBreakpoint(17, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("the program did not suspend", thread);
            String snippet = "true&&true||false";
            IValue value = doEval(thread, snippet);
            assertTrue("The result of true&&true||false should be true", Boolean.parseBoolean(value.getValueString()));
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
    public void testMultipleInfixEval6() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug401270";
            createLineBreakpoint(18, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("the program did not suspend", thread);
            String snippet = "(1<=2==true||false)";
            IValue value = doEval(thread, snippet);
            assertTrue("The result of (1<=2==true||false) should be true", Boolean.parseBoolean(value.getValueString()));
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
    public void testMultipleInfixEval7() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug401270";
            createLineBreakpoint(19, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("the program did not suspend", thread);
            String snippet = "!(1<=2==true||false)";
            IValue value = doEval(thread, snippet);
            assertFalse("The result of !(1<=2==true||false) should be false", Boolean.parseBoolean(value.getValueString()));
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
    public void testMultipleInfixEval8() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug401270";
            createLineBreakpoint(20, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("the program did not suspend", thread);
            String snippet = "(true != false && false)";
            IValue value = doEval(thread, snippet);
            assertFalse("The result of (true != false && false) should be false", Boolean.parseBoolean(value.getValueString()));
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
    public void testMultipleInfixEval9() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug401270";
            createLineBreakpoint(21, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("the program did not suspend", thread);
            String snippet = "!(true != false && false)";
            IValue value = doEval(thread, snippet);
            assertTrue("The result of !(true != false && false) should be true", Boolean.parseBoolean(value.getValueString()));
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
    public void testMultipleInfixEval10() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug401270";
            createLineBreakpoint(22, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("the program did not suspend", thread);
            String snippet = "(true||true||true||true||true)";
            IValue value = doEval(thread, snippet);
            assertTrue("The result of (true||true||true||true||true) should be true", Boolean.parseBoolean(value.getValueString()));
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
    public void testMultipleInfixEval11() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug401270";
            createLineBreakpoint(23, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("the program did not suspend", thread);
            String snippet = "!(true||true||true||true||true)";
            IValue value = doEval(thread, snippet);
            assertFalse("The result of !(true||true||true||true||true) should be false", Boolean.parseBoolean(value.getValueString()));
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
    public void testMultipleInfixEval12() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug401270";
            createLineBreakpoint(24, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("the program did not suspend", thread);
            String snippet = "(true==true||true!=true&&true)";
            IValue value = doEval(thread, snippet);
            assertTrue("The result of (true==true||true!=true&&true) should be true", Boolean.parseBoolean(value.getValueString()));
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
    public void testMultipleInfixEval13() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug401270";
            createLineBreakpoint(25, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("the program did not suspend", thread);
            String snippet = "!(true==true||true!=true&&true)";
            IValue value = doEval(thread, snippet);
            assertFalse("The result of !(true==true||true!=true&&true) should be false", Boolean.parseBoolean(value.getValueString()));
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
    public void testMultipleInfixEval14() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug401270";
            createLineBreakpoint(25, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("the program did not suspend", thread);
            String snippet = "(true || !(true==true||true!=true&&true))";
            IValue value = doEval(thread, snippet);
            assertTrue("The result of (true || !(true==true||true!=true&&true)) should be true", Boolean.parseBoolean(value.getValueString()));
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
    public void testMultipleInfixEval15() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug401270";
            createLineBreakpoint(25, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("the program did not suspend", thread);
            String snippet = "(true && true || false || !(true==true||true!=true&&true))";
            IValue value = doEval(thread, snippet);
            assertTrue("The result of (true && true || false || !(true==true||true!=true&&true)) should be true", Boolean.parseBoolean(value.getValueString()));
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=401270
	 * @throws Exception
	 */
    public void testMultipleInfixEval16() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug401270";
            createLineBreakpoint(25, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("the program did not suspend", thread);
            String snippet = "(true && true || !(false&&true) || !(true==true||true!=true&&true))";
            IValue value = doEval(thread, snippet);
            assertTrue("The result of (true && true || !(false&&true) || !(true==true||true!=true&&true)) should be true", Boolean.parseBoolean(value.getValueString()));
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }

    /**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=403028
	 * @throws Exception
	 */
    public void testCompoundCondition() throws Exception {
        IJavaThread thread = null;
        try {
            String typename = "bug401270";
            createLineBreakpoint(25, typename);
            thread = launchToBreakpoint(typename);
            assertNotNull("the program did not suspend", thread);
            String snippet = "(true && true || !(false&&true) || !(true==true||true!=true&&true))";
            IValue value = doEval(thread, snippet);
            assertTrue("The result of (true && true || !(false&&true) || !(true==true||true!=true&&true)) should be true", Boolean.parseBoolean(value.getValueString()));
        } finally {
            removeAllBreakpoints();
            terminateAndRemove(thread);
        }
    }
}
