/*******************************************************************************
 *  Copyright (c) 2000, 2014 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jesper Steen Moller - Enhancement 254677 - filter getters/setters
 *******************************************************************************/
package org.eclipse.jdt.debug.test.stepping;

import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestAgainException;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Step filtering tests
 * This test forces the UI plug-ins to load.
 */
public class StepFilterTests extends AbstractDebugTest {

    private String fOriginalActiveFilters;

    private String fOriginalInactiveFilters;

    /**
	 * Constructor
	 * @param name
	 */
    public  StepFilterTests(String name) {
        super(name);
        fOriginalActiveFilters = getPrefStore().getString(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST);
        fOriginalInactiveFilters = getPrefStore().getString(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST);
    }

    /**
	 * Tests a simple step filter
	 * @throws Exception
	 */
    public void testSimpleStepFilter() throws Exception {
        getPrefStore().setValue(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, fOriginalActiveFilters + ",StepFilterTwo," + fOriginalInactiveFilters);
        String typeName = "StepFilterOne";
        ILineBreakpoint bp = createLineBreakpoint(23, typeName);
        bp.setEnabled(true);
        IJavaThread thread = null;
        try {
            thread = launchToLineBreakpoint(typeName, bp, true);
            IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
            thread = stepIntoWithFilters(stackFrame);
            stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
            String recTypeName = stackFrame.getReceivingTypeName();
            if (!"StepFilterOne".equals(recTypeName)) {
                // @see bug 297071
                throw new TestAgainException("Retest - " + recTypeName + " is does not match StepFilterOne");
            }
            assertEquals("Wrong receiving type", "StepFilterOne", recTypeName);
            int lineNumber = stackFrame.getLineNumber();
            assertEquals("Wrong line number", 24, lineNumber);
        } finally {
            terminateAndRemove(thread);
            removeAllBreakpoints();
            resetStepFilters();
        }
    }

    /**
	 * Tests a simple step filter
	 * @throws Exception
	 */
    public void testDontStepThruStepFilters() throws Exception {
        getPrefStore().setValue(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, fOriginalActiveFilters + ",StepFilterTwo," + fOriginalInactiveFilters);
        String typeName = "StepFilterOne";
        ILineBreakpoint bp = createLineBreakpoint(24, typeName);
        bp.setEnabled(true);
        IJavaThread thread = null;
        try {
            thread = launchToLineBreakpoint(typeName, bp, true);
            IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
            thread = stepIntoWithFilters(stackFrame, false);
            stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
            String recTypeName = stackFrame.getReceivingTypeName();
            if (!"StepFilterOne".equals(recTypeName)) {
                // @see bug 297071
                throw new TestAgainException("Retest - " + recTypeName + " is does not match StepFilterOne");
            }
            assertEquals("Wrong receiving type", "StepFilterOne", recTypeName);
            int lineNumber = stackFrame.getLineNumber();
            assertEquals("Wrong line number", 25, lineNumber);
        } finally {
            terminateAndRemove(thread);
            removeAllBreakpoints();
            resetStepFilters();
        }
    }

    /**
	 * Tests a step filter that is not active
	 * @throws Exception
	 */
    public void testInactiveStepFilter() throws Exception {
        getPrefStore().setValue(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST, fOriginalActiveFilters + ",StepFilterTwo");
        String typeName = "StepFilterOne";
        ILineBreakpoint bp = createLineBreakpoint(23, typeName);
        bp.setEnabled(true);
        IJavaThread thread = null;
        try {
            thread = launchToLineBreakpoint(typeName, bp, false);
            IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
            thread = stepIntoWithFilters(stackFrame);
            stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
            String recTypeName = stackFrame.getReceivingTypeName();
            if (!"StepFilterTwo".equals(recTypeName)) {
                // @see bug 297071
                throw new TestAgainException("Retest - " + recTypeName + " is does not match StepFilterTwo");
            }
            assertEquals("Wrong receiving type", "StepFilterTwo", recTypeName);
            int lineNumber = stackFrame.getLineNumber();
            assertEquals("Wrong line number", 25, lineNumber);
        } finally {
            terminateAndRemove(thread);
            removeAllBreakpoints();
            resetStepFilters();
        }
    }

    /**
	 * Tests a deep step filter, i.e. a step filter that is more than one stack frame deep on the current
	 * suspended thread
	 * @throws Exception
	 */
    public void testDeepStepFilter() throws Exception {
        getPrefStore().setValue(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, fOriginalActiveFilters + ",StepFilterTwo," + fOriginalInactiveFilters);
        String typeName = "StepFilterOne";
        ILineBreakpoint bp = createLineBreakpoint(24, typeName);
        bp.setEnabled(true);
        IJavaThread thread = null;
        try {
            thread = launchToLineBreakpoint(typeName, bp, false);
            IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
            thread = stepIntoWithFilters(stackFrame);
            stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
            String recTypeName = stackFrame.getReceivingTypeName();
            if (!"StepFilterThree".equals(recTypeName)) {
                throw new TestAgainException("Retest - " + recTypeName + " is does not match StepFilterThree");
            }
            assertEquals("Wrong receiving type", "StepFilterThree", recTypeName);
            int lineNumber = stackFrame.getLineNumber();
            assertEquals("Wrong line number", 19, lineNumber);
        } finally {
            terminateAndRemove(thread);
            removeAllBreakpoints();
            resetStepFilters();
        }
    }

    /**
	 * Tests a simple step return filter
	 * @throws Exception
	 */
    public void testStepReturnFilter() throws Exception {
        getPrefStore().setValue(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, fOriginalActiveFilters + ",StepFilterTwo," + fOriginalInactiveFilters);
        String typeName = "StepFilterOne";
        ILineBreakpoint bp = createLineBreakpoint(19, "StepFilterThree");
        bp.setEnabled(true);
        IJavaThread thread = null;
        try {
            thread = launchToLineBreakpoint(typeName, bp, false);
            IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
            thread = stepReturnWithFilters(stackFrame);
            stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
            String recTypeName = stackFrame.getReceivingTypeName();
            if (!"StepFilterOne".equals(recTypeName)) {
                throw new TestAgainException("Retest - " + recTypeName + " is does not match StepFilterOne");
            }
            assertEquals("Wrong receiving type", "StepFilterOne", recTypeName);
            int lineNumber = stackFrame.getLineNumber();
            assertEquals("Wrong line number", 23, lineNumber);
        } finally {
            terminateAndRemove(thread);
            removeAllBreakpoints();
            resetStepFilters();
        }
    }

    /**
	 * Tests a simple step over filter
	 * @throws Exception
	 */
    public void testStepOverFilter() throws Exception {
        getPrefStore().setValue(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, fOriginalActiveFilters + ",StepFilterTwo,StepFilterThree," + fOriginalInactiveFilters);
        String typeName = "StepFilterOne";
        ILineBreakpoint bp = createLineBreakpoint(19, "StepFilterThree");
        bp.setEnabled(true);
        IJavaThread thread = null;
        try {
            thread = launchToLineBreakpoint(typeName, bp, false);
            IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
            thread = stepOverWithFilters(stackFrame);
            stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
            String recTypeName = stackFrame.getReceivingTypeName();
            if (!"StepFilterOne".equals(recTypeName)) {
                throw new TestAgainException("Retest - " + recTypeName + " is does not match StepFilterOne");
            }
            assertEquals("Wrong receiving type", "StepFilterOne", recTypeName);
            int lineNumber = stackFrame.getLineNumber();
            assertEquals("Wrong line number", 23, lineNumber);
        } finally {
            terminateAndRemove(thread);
            removeAllBreakpoints();
            resetStepFilters();
        }
    }

    /**
	 * Tests filtering of getter methods
	 * @throws Exception
	 */
    public void testGetterFilters() throws Exception {
        getPrefStore().setValue(IJDIPreferencesConstants.PREF_FILTER_GETTERS, true);
        getPrefStore().setValue(IJDIPreferencesConstants.PREF_FILTER_SETTERS, false);
        String typeName = "StepFilterFour";
        ILineBreakpoint bp = createLineBreakpoint(91, typeName);
        bp.setEnabled(true);
        IJavaThread thread = null;
        try {
            thread = launchToLineBreakpoint(typeName, bp, false);
            IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
            stackFrame = (IJavaStackFrame) stepIntoWithFilters(stackFrame).getTopStackFrame();
            String recTypeName = stackFrame.getReceivingTypeName();
            assertEquals("Wrong receiving type", "StepFilterFour", recTypeName);
            assertEquals("Wrong line number", 92, stackFrame.getLineNumber());
            stackFrame = (IJavaStackFrame) stepIntoWithFilters(stackFrame).getTopStackFrame();
            stackFrame = (IJavaStackFrame) stepIntoWithFilters(stackFrame).getTopStackFrame();
            stackFrame = (IJavaStackFrame) stepIntoWithFilters(stackFrame).getTopStackFrame();
            stackFrame = (IJavaStackFrame) stepIntoWithFilters(stackFrame).getTopStackFrame();
            assertEquals("Wrong line number", 96, stackFrame.getLineNumber());
            // now step into the line with the call to sum() which is not a simple getter
            stackFrame = (IJavaStackFrame) stepIntoWithFilters(stackFrame).getTopStackFrame();
            assertEquals("Wrong line number", 71, stackFrame.getLineNumber());
            assertEquals("Should be in sum()", "sum", stackFrame.getMethodName());
        } finally {
            terminateAndRemove(thread);
            removeAllBreakpoints();
            resetStepFilters();
        }
    }

    /**
	 * Tests filtering of setter methods
	 * @throws Exception
	 */
    public void testSetterFilters() throws Exception {
        getPrefStore().setValue(IJDIPreferencesConstants.PREF_FILTER_GETTERS, false);
        getPrefStore().setValue(IJDIPreferencesConstants.PREF_FILTER_SETTERS, true);
        String typeName = "StepFilterFour";
        ILineBreakpoint bp = createLineBreakpoint(84, typeName);
        bp.setEnabled(true);
        IJavaThread thread = null;
        try {
            thread = launchToLineBreakpoint(typeName, bp, false);
            IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
            stackFrame = (IJavaStackFrame) stepIntoWithFilters(stackFrame).getTopStackFrame();
            String recTypeName = stackFrame.getReceivingTypeName();
            assertEquals("Wrong receiving type", "StepFilterFour", recTypeName);
            stackFrame = (IJavaStackFrame) stepIntoWithFilters(stackFrame).getTopStackFrame();
            stackFrame = (IJavaStackFrame) stepIntoWithFilters(stackFrame).getTopStackFrame();
            stackFrame = (IJavaStackFrame) stepIntoWithFilters(stackFrame).getTopStackFrame();
            stackFrame = (IJavaStackFrame) stepIntoWithFilters(stackFrame).getTopStackFrame();
            assertEquals("Wrong line number", 91, stackFrame.getLineNumber());
            // now step into the line with the call to getI() which is a simple getter
            // since we're not filtering getters, we should end up in getI
            stackFrame = (IJavaStackFrame) stepIntoWithFilters(stackFrame).getTopStackFrame();
            assertEquals("Wrong line number", 34, stackFrame.getLineNumber());
            assertEquals("Should be in getI()", "getI", stackFrame.getMethodName());
        } finally {
            terminateAndRemove(thread);
            removeAllBreakpoints();
            resetStepFilters();
        }
    }

    /**
	 * Tests filtering from a contributed filter
	 * 
	 * @throws Exception
	 * @since 3.8.300
	 */
    public void testContributedFilter1() throws Exception {
        String typeName = "TestContributedStepFilterClass";
        getPrefStore().setValue(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, fOriginalActiveFilters + ",StepFilterTwo," + fOriginalInactiveFilters);
        ILineBreakpoint bp = createLineBreakpoint(17, typeName);
        bp.setEnabled(true);
        IJavaThread thread = null;
        try {
            thread = launchToLineBreakpoint(typeName, bp, false);
            IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
            assertEquals("Wrong line number", 17, stackFrame.getLineNumber());
            thread = stepIntoWithFilters(stackFrame);
            assertNotNull("We should have stepped over the method call", thread);
            stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
            assertEquals("Wrong line number", 18, stackFrame.getLineNumber());
            assertEquals("Should be in main", "main", stackFrame.getMethodName());
        } finally {
            terminateAndRemove(thread);
            removeAllBreakpoints();
            resetStepFilters();
        }
    }

    /**
	 * Reset the step filtering preferences
	 */
    protected void resetStepFilters() {
        getPrefStore().setValue(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, fOriginalActiveFilters);
        getPrefStore().setValue(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST, fOriginalInactiveFilters);
    }

    /**
	 * Returns the <code>JDIDebugUIPlugin</code> preference store
	 * @return
	 */
    protected IPreferenceStore getPrefStore() {
        return JDIDebugUIPlugin.getDefault().getPreferenceStore();
    }
}
