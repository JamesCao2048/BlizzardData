/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     David Saff (saff@mit.edu) - initial API and implementation
 *             (bug 102632: [JUnit] Support for JUnit 4.)
 *     Oliver Masutti <eclipse@masutti.ch> - [JUnit] JUnit3TestReference handles JUnit4TestAdapter incorrectly - https://bugs.eclipse.org/397747
 *******************************************************************************/
package org.eclipse.jdt.internal.junit.runner.junit3;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import junit.extensions.TestDecorator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.eclipse.jdt.internal.junit.runner.FailedComparison;
import org.eclipse.jdt.internal.junit.runner.IClassifiesThrowables;
import org.eclipse.jdt.internal.junit.runner.IListensToTestExecutions;
import org.eclipse.jdt.internal.junit.runner.IStopListener;
import org.eclipse.jdt.internal.junit.runner.ITestIdentifier;
import org.eclipse.jdt.internal.junit.runner.ITestReference;
import org.eclipse.jdt.internal.junit.runner.IVisitsTestTrees;
import org.eclipse.jdt.internal.junit.runner.MessageIds;
import org.eclipse.jdt.internal.junit.runner.TestExecution;
import org.eclipse.jdt.internal.junit.runner.TestReferenceFailure;

public class JUnit3TestReference implements ITestReference {

    private final Test fTest;

    public static Object getField(Object object, String fieldName) {
        Class clazz = object.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
        }
        return null;
    }

    public  JUnit3TestReference(Test test) {
        if (test == null)
            throw new NullPointerException();
        this.fTest = test;
    }

    public int countTestCases() {
        return fTest.countTestCases();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof JUnit3TestReference))
            return false;
        JUnit3TestReference ref = (JUnit3TestReference) obj;
        return ref.fTest.equals(fTest);
    }

    public int hashCode() {
        return fTest.hashCode();
    }

    public String getName() {
        if (isJUnit4TestCaseAdapter(fTest)) {
            //$NON-NLS-1$
            Method method = (Method) callJUnit4GetterMethod(fTest, "getTestMethod");
            return MessageFormat.format(MessageIds.TEST_IDENTIFIER_MESSAGE_FORMAT, new String[] { method.getName(), method.getDeclaringClass().getName() });
        }
        if (fTest instanceof TestCase) {
            TestCase testCase = (TestCase) fTest;
            return MessageFormat.format(MessageIds.TEST_IDENTIFIER_MESSAGE_FORMAT, new String[] { testCase.getName(), fTest.getClass().getName() });
        }
        if (fTest instanceof TestSuite) {
            TestSuite suite = (TestSuite) fTest;
            if (suite.getName() != null)
                return suite.getName();
            return suite.getClass().getName();
        }
        if (fTest instanceof TestDecorator) {
            TestDecorator decorator = (TestDecorator) fTest;
            return decorator.getClass().getName();
        }
        if (isJUnit4TestSuiteAdapter(fTest)) {
            //$NON-NLS-1$
            Class testClass = (Class) callJUnit4GetterMethod(fTest, "getTestClass");
            return testClass.getName();
        }
        return fTest.toString();
    }

    public Test getTest() {
        return fTest;
    }

    public void run(TestExecution execution) {
        final TestResult testResult = new TestResult();
        testResult.addListener(new JUnit3Listener(execution));
        execution.addStopListener(new IStopListener() {

            public void stop() {
                testResult.stop();
            }
        });
        TestResult tr = testResult;
        fTest.run(tr);
    }

    public void sendTree(IVisitsTestTrees notified) {
        if (fTest instanceof TestDecorator) {
            TestDecorator decorator = (TestDecorator) fTest;
            notified.visitTreeEntry(getIdentifier(), true, 1);
            sendTreeOfChild(decorator.getTest(), notified);
        } else if (fTest instanceof TestSuite) {
            TestSuite suite = (TestSuite) fTest;
            notified.visitTreeEntry(getIdentifier(), true, suite.testCount());
            for (int i = 0; i < suite.testCount(); i++) {
                sendTreeOfChild(suite.testAt(i), notified);
            }
        } else if (isJUnit4TestSuiteAdapter(fTest)) {
            //$NON-NLS-1$
            List tests = (List) callJUnit4GetterMethod(fTest, "getTests");
            notified.visitTreeEntry(getIdentifier(), true, tests.size());
            for (Iterator iter = tests.iterator(); iter.hasNext(); ) {
                sendTreeOfChild((Test) iter.next(), notified);
            }
        } else {
            notified.visitTreeEntry(getIdentifier(), false, fTest.countTestCases());
        }
    }

    void sendFailure(Throwable throwable, IClassifiesThrowables classifier, String status, IListensToTestExecutions notified) {
        TestReferenceFailure failure;
        try {
            failure = new TestReferenceFailure(getIdentifier(), status, classifier.getTrace(throwable));
            if (classifier.isComparisonFailure(throwable)) {
                // transmit the expected and the actual string
                Object expected = //$NON-NLS-1$
                JUnit3TestReference.getField(//$NON-NLS-1$
                throwable, //$NON-NLS-1$
                "fExpected");
                Object actual = //$NON-NLS-1$
                JUnit3TestReference.getField(//$NON-NLS-1$
                throwable, //$NON-NLS-1$
                "fActual");
                if (expected != null && actual != null) {
                    failure.setComparison(new FailedComparison((String) expected, (String) actual));
                }
            }
        } catch (RuntimeException e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            failure = new TestReferenceFailure(getIdentifier(), MessageIds.TEST_FAILED, stringWriter.getBuffer().toString(), null);
        }
        notified.notifyTestFailed(failure);
    }

    private Object callJUnit4GetterMethod(Test test, String methodName) {
        Object result;
        try {
            Method method = test.getClass().getMethod(methodName, new Class[0]);
            result = method.invoke(test, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            result = null;
        }
        return result;
    }

    private boolean isJUnit4TestCaseAdapter(Test test) {
        //$NON-NLS-1$
        return test.getClass().getName().equals("junit.framework.JUnit4TestCaseAdapter");
    }

    private boolean isJUnit4TestSuiteAdapter(Test test) {
        String name = test.getClass().getName();
        //$NON-NLS-1$ //$NON-NLS-2$
        return name.endsWith("JUnit4TestAdapter") && name.startsWith("junit.framework");
    }

    private void sendTreeOfChild(Test test, IVisitsTestTrees notified) {
        new JUnit3TestReference(test).sendTree(notified);
    }

    public ITestIdentifier getIdentifier() {
        return new JUnit3Identifier(this);
    }
}
