/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.text.tests;

import java.util.ListResourceBundle;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.eclipse.jdt.text.tests.performance.EditorTestHelper;
import org.eclipse.jdt.text.tests.performance.ResourceTestHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.ui.actions.IndentAction;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;

/**
 *
 * @since 3.2
 */
public class IndentActionTest extends TestCase {

    private static final String PROJECT = "IndentTests";

    private static final class EmptyBundle extends ListResourceBundle {

        @Override
        protected Object[][] getContents() {
            return new Object[0][];
        }
    }

    protected static class IndentTestSetup extends TestSetup {

        private IJavaProject fJavaProject;

        public  IndentTestSetup(Test test) {
            super(test);
        }

        @Override
        protected void setUp() throws Exception {
            super.setUp();
            fJavaProject = EditorTestHelper.createJavaProject(PROJECT, "testResources/indentation");
            fJavaProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
            fJavaProject.setOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_EMPTY_LINES, DefaultCodeFormatterConstants.FALSE);
            fJavaProject.setOption(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_BLOCK_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.TRUE);
            fJavaProject.setOption(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.TRUE);
        }

        @Override
        protected void tearDown() throws Exception {
            if (fJavaProject != null)
                JavaProjectHelper.delete(fJavaProject);
            super.tearDown();
        }

        public static IJavaProject getProject() {
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT);
            return JavaCore.create(project);
        }
    }

    private static final Class<IndentActionTest> THIS = IndentActionTest.class;

    public static Test suite() {
        return new IndentTestSetup(new TestSuite(THIS));
    }

    private JavaEditor fEditor;

    private SourceViewer fSourceViewer;

    private IDocument fDocument;

    /*
	 * @see junit.framework.TestCase#setUp()
	 */
    @Override
    protected void setUp() throws Exception {
        String filename = createFileName("Before");
        fEditor = (JavaEditor) EditorTestHelper.openInEditor(ResourceTestHelper.findFile(filename), true);
        fSourceViewer = EditorTestHelper.getSourceViewer(fEditor);
        fDocument = fSourceViewer.getDocument();
    }

    /*
	 * @see junit.framework.TestCase#tearDown()
	 */
    @Override
    protected void tearDown() throws Exception {
        EditorTestHelper.closeEditor(fEditor);
        fEditor = null;
        fSourceViewer = null;
    }

    private void assertIndentResult() throws Exception {
        String afterFile = createFileName("Modified");
        String expected = ResourceTestHelper.read(afterFile).toString();
        new IndentAction(new EmptyBundle(), "prefix", fEditor, false).run();
        assertEquals(expected, fDocument.get());
    }

    private String createFileName(String qualifier) {
        String name = getName();
        name = name.substring(4, 5).toLowerCase() + name.substring(5);
        return "/" + PROJECT + "/src/" + name + "/" + qualifier + ".java";
    }

    private void selectAll() {
        fSourceViewer.setSelectedRange(0, fDocument.getLength());
    }

    public void testUnchanged() throws Exception {
        selectAll();
        assertIndentResult();
    }

    public void testBug122261() throws Exception {
        selectAll();
        assertIndentResult();
    }

    public void testEmptySingleLineComment01() throws Exception {
        selectAll();
        assertIndentResult();
    }

    public void testEmptySingleLineComment02() throws Exception {
        IJavaProject project = IndentTestSetup.getProject();
        project.setOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_EMPTY_LINES, DefaultCodeFormatterConstants.TRUE);
        try {
            selectAll();
            assertIndentResult();
        } finally {
            project.setOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_EMPTY_LINES, DefaultCodeFormatterConstants.FALSE);
        }
    }

    public void testEmptySingleLineComment03() throws Exception {
        IJavaProject project = IndentTestSetup.getProject();
        project.setOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_EMPTY_LINES, DefaultCodeFormatterConstants.TRUE);
        project.setOption(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_BLOCK_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.FALSE);
        project.setOption(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.FALSE);
        try {
            selectAll();
            assertIndentResult();
        } finally {
            project.setOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_EMPTY_LINES, DefaultCodeFormatterConstants.FALSE);
            project.setOption(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_BLOCK_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.TRUE);
            project.setOption(DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN, DefaultCodeFormatterConstants.TRUE);
        }
    }

    public void testBug424772() throws Exception {
        selectAll();
        assertIndentResult();
    }

    public void testBug428384() throws Exception {
        selectAll();
        assertIndentResult();
    }

    public void testBug439582_1() throws Exception {
        IJavaProject project = IndentTestSetup.getProject();
        String value = project.getOption(DefaultCodeFormatterConstants.FORMATTER_CONTINUATION_INDENTATION_FOR_ARRAY_INITIALIZER, true);
        project.setOption(DefaultCodeFormatterConstants.FORMATTER_CONTINUATION_INDENTATION_FOR_ARRAY_INITIALIZER, "1");
        try {
            selectAll();
            assertIndentResult();
        } finally {
            project.setOption(DefaultCodeFormatterConstants.FORMATTER_CONTINUATION_INDENTATION_FOR_ARRAY_INITIALIZER, value);
        }
    }

    public void testBug439582_2() throws Exception {
        selectAll();
        assertIndentResult();
    }

    public void testBug439582_3() throws Exception {
        selectAll();
        assertIndentResult();
    }

    public void testBug439582_4() throws Exception {
        selectAll();
        assertIndentResult();
    }

    public void testBug439582_5() throws Exception {
        selectAll();
        assertIndentResult();
    }

    public void testBug400670_1() throws Exception {
        // With formatter profile from https://bugs.eclipse.org/bugs/show_bug.cgi?id=400670#c0
        String indentOnColumn = DefaultCodeFormatterConstants.createAlignmentValue(true, DefaultCodeFormatterConstants.WRAP_NEXT_PER_LINE, DefaultCodeFormatterConstants.INDENT_ON_COLUMN);
        IJavaProject project = IndentTestSetup.getProject();
        String value1 = project.getOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS, true);
        project.setOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS, indentOnColumn);
        String value2 = project.getOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION, true);
        project.setOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION, indentOnColumn);
        String value3 = project.getOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_EXPRESSIONS_IN_ARRAY_INITIALIZER, true);
        project.setOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_EXPRESSIONS_IN_ARRAY_INITIALIZER, indentOnColumn);
        try {
            selectAll();
            assertIndentResult();
        } finally {
            project.setOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS, value1);
            project.setOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION, value2);
            project.setOption(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_EXPRESSIONS_IN_ARRAY_INITIALIZER, value3);
        }
    }

    public void testBug400670_2() throws Exception {
        // With default formatter profile
        selectAll();
        assertIndentResult();
    }

    public void testBug458763() throws Exception {
        IJavaProject project = IndentTestSetup.getProject();
        String value = project.getOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH, true);
        project.setOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH, DefaultCodeFormatterConstants.FALSE);
        try {
            selectAll();
            assertIndentResult();
        } finally {
            project.setOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH, value);
        }
    }
}
