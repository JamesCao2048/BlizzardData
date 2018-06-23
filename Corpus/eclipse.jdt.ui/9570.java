/*******************************************************************************
 * Copyright (c) 2016 GK Software AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.ui.tests.quickfix;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import org.osgi.framework.Bundle;
import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.eclipse.jdt.testplugin.TestOptions;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.tests.core.Java18ProjectTestSetup;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.correction.CUCorrectionProposal;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Tests against projects with 1.8 compliance and "old" declaration null annotations. */
public class NullAnnotationsQuickFixTest18Mix extends QuickFixTest {

    private static final Class<NullAnnotationsQuickFixTest18Mix> THIS = NullAnnotationsQuickFixTest18Mix.class;

    private IJavaProject fJProject1;

    private IPackageFragmentRoot fSourceFolder;

    private String ANNOTATION_JAR_PATH;

    public  NullAnnotationsQuickFixTest18Mix(String name) {
        super(name);
    }

    public static Test suite() {
        return setUpTest(new TestSuite(THIS));
    }

    public static Test setUpTest(Test test) {
        return new Java18ProjectTestSetup(test);
    }

    @Override
    protected void setUp() throws Exception {
        Hashtable<String, String> options = TestOptions.getDefaultOptions();
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
        options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, String.valueOf(99));
        options.put(JavaCore.COMPILER_PB_STATIC_ACCESS_RECEIVER, JavaCore.ERROR);
        options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, JavaCore.IGNORE);
        options.put(JavaCore.COMPILER_PB_MISSING_HASHCODE_METHOD, JavaCore.WARNING);
        options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
        options.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.ERROR);
        options.put(JavaCore.COMPILER_PB_NULL_REFERENCE, JavaCore.ERROR);
        options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.WARNING);
        options.put(JavaCore.COMPILER_PB_NULL_ANNOTATION_INFERENCE_CONFLICT, JavaCore.WARNING);
        options.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.WARNING);
        options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
        options.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.WARNING);
        JavaCore.setOptions(options);
        IPreferenceStore store = JavaPlugin.getDefault().getPreferenceStore();
        store.setValue(PreferenceConstants.CODEGEN_ADD_COMMENTS, false);
        StubUtility.setCodeTemplate(CodeTemplateContextType.CATCHBLOCK_ID, "", null);
        StubUtility.setCodeTemplate(CodeTemplateContextType.CONSTRUCTORSTUB_ID, "", null);
        StubUtility.setCodeTemplate(CodeTemplateContextType.METHODSTUB_ID, "", null);
        fJProject1 = Java18ProjectTestSetup.getProject();
        if (this.ANNOTATION_JAR_PATH == null) {
            // tests run at 1.8, but still use "old" null annotations
            String version = "[1.1.0,2.0.0)";
            Bundle[] bundles = Platform.getBundles("org.eclipse.jdt.annotation", version);
            File bundleFile = FileLocator.getBundleFile(bundles[0]);
            if (bundleFile.isDirectory())
                this.ANNOTATION_JAR_PATH = bundleFile.getPath() + "/bin";
            else
                this.ANNOTATION_JAR_PATH = bundleFile.getPath();
        }
        JavaProjectHelper.addLibrary(fJProject1, new Path(ANNOTATION_JAR_PATH));
        fSourceFolder = JavaProjectHelper.addSourceContainer(fJProject1, "src");
    }

    @Override
    protected void tearDown() throws Exception {
        JavaProjectHelper.clear(fJProject1, Java18ProjectTestSetup.getDefaultClasspath());
    }

    // ==== Problem:	unchecked conversion, type elided lambda arg
    // ==== Fixes:		change downstream method parameter to @Nullable
    //					add @SW("null")
    public void testBug473068_elided() throws Exception {
        IPackageFragment pack1 = fSourceFolder.createPackageFragment("testNullAnnotations", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append("package testNullAnnotations;\n");
        buf.append("\n");
        buf.append("import org.eclipse.jdt.annotation.NonNull;\n");
        buf.append("\n");
        buf.append("interface Consumer<T> {\n");
        buf.append("    void accept(T t);\n");
        buf.append("}\n");
        buf.append("public class Snippet {\n");
        buf.append("	\n");
        buf.append("	public void select(final double min, final double max) {\n");
        buf.append("	    doStuff(0, 1, min, max, (data) -> updateSelectionData(data));\n");
        buf.append("	}\n");
        buf.append("	\n");
        buf.append("	private void doStuff(int a, int b, final double min, final double max, Consumer<Object> postAction) {\n");
        buf.append("\n");
        buf.append("	}\n");
        buf.append("    private void updateSelectionData(final @NonNull Object data) {\n");
        buf.append("    }\n");
        buf.append("}\n");
        ICompilationUnit cu = pack1.createCompilationUnit("Snippet.java", buf.toString(), false, null);
        CompilationUnit astRoot = getASTRoot(cu);
        ArrayList<IJavaCompletionProposal> proposals = collectCorrections(cu, astRoot);
        assertNumberOfProposals(proposals, 3);
        assertCorrectLabels(proposals);
        CUCorrectionProposal proposal = (CUCorrectionProposal) proposals.get(0);
        String preview = getPreviewContent(proposal);
        buf = new StringBuffer();
        buf.append("package testNullAnnotations;\n");
        buf.append("\n");
        buf.append("import org.eclipse.jdt.annotation.NonNull;\n");
        buf.append("import org.eclipse.jdt.annotation.Nullable;\n");
        buf.append("\n");
        buf.append("interface Consumer<T> {\n");
        buf.append("    void accept(T t);\n");
        buf.append("}\n");
        buf.append("public class Snippet {\n");
        buf.append("	\n");
        buf.append("	public void select(final double min, final double max) {\n");
        buf.append("	    doStuff(0, 1, min, max, (data) -> updateSelectionData(data));\n");
        buf.append("	}\n");
        buf.append("	\n");
        buf.append("	private void doStuff(int a, int b, final double min, final double max, Consumer<Object> postAction) {\n");
        buf.append("\n");
        buf.append("	}\n");
        buf.append("    private void updateSelectionData(final @Nullable Object data) {\n");
        buf.append("    }\n");
        buf.append("}\n");
        assertEqualString(preview, buf.toString());
        proposal = (CUCorrectionProposal) proposals.get(1);
        preview = getPreviewContent(proposal);
        buf = new StringBuffer();
        buf.append("package testNullAnnotations;\n");
        buf.append("\n");
        buf.append("import org.eclipse.jdt.annotation.NonNull;\n");
        buf.append("\n");
        buf.append("interface Consumer<T> {\n");
        buf.append("    void accept(T t);\n");
        buf.append("}\n");
        buf.append("public class Snippet {\n");
        buf.append("	\n");
        buf.append("	@SuppressWarnings(\"null\")\n");
        buf.append("    public void select(final double min, final double max) {\n");
        buf.append("	    doStuff(0, 1, min, max, (data) -> updateSelectionData(data));\n");
        buf.append("	}\n");
        buf.append("	\n");
        buf.append("	private void doStuff(int a, int b, final double min, final double max, Consumer<Object> postAction) {\n");
        buf.append("\n");
        buf.append("	}\n");
        buf.append("    private void updateSelectionData(final @NonNull Object data) {\n");
        buf.append("    }\n");
        buf.append("}\n");
        assertEqualString(preview, buf.toString());
    }

    // ==== Problem:	unchecked conversion, explicitly typed lambda arg
    // ==== Fixes:		annotate lambda arg (@NonNull)
    //					change downstream method parameter to @Nullable
    //					add @SW("null")
    public void testBug473068_explicit_type() throws Exception {
        IPackageFragment pack1 = fSourceFolder.createPackageFragment("testNullAnnotations", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append("package testNullAnnotations;\n");
        buf.append("\n");
        buf.append("import org.eclipse.jdt.annotation.NonNull;\n");
        buf.append("\n");
        buf.append("interface Consumer<T> {\n");
        buf.append("    void accept(T t);\n");
        buf.append("}\n");
        buf.append("public class Snippet {\n");
        buf.append("	\n");
        buf.append("	public void select(final double min, final double max) {\n");
        buf.append("	    doStuff(0, 1, min, max, (Object data) -> updateSelectionData(data));\n");
        buf.append("	}\n");
        buf.append("	\n");
        buf.append("	private void doStuff(int a, int b, final double min, final double max, Consumer<Object> postAction) {\n");
        buf.append("\n");
        buf.append("	}\n");
        buf.append("    private void updateSelectionData(final @NonNull Object data) {\n");
        buf.append("    }\n");
        buf.append("}\n");
        ICompilationUnit cu = pack1.createCompilationUnit("Snippet.java", buf.toString(), false, null);
        CompilationUnit astRoot = getASTRoot(cu);
        ArrayList<IJavaCompletionProposal> proposals = collectCorrections(cu, astRoot);
        assertNumberOfProposals(proposals, 4);
        assertCorrectLabels(proposals);
        CUCorrectionProposal proposal = (CUCorrectionProposal) proposals.get(0);
        String preview = getPreviewContent(proposal);
        buf = new StringBuffer();
        buf.append("package testNullAnnotations;\n");
        buf.append("\n");
        buf.append("import org.eclipse.jdt.annotation.NonNull;\n");
        buf.append("\n");
        buf.append("interface Consumer<T> {\n");
        buf.append("    void accept(T t);\n");
        buf.append("}\n");
        buf.append("public class Snippet {\n");
        buf.append("	\n");
        buf.append("	public void select(final double min, final double max) {\n");
        buf.append("	    doStuff(0, 1, min, max, (@NonNull Object data) -> updateSelectionData(data));\n");
        buf.append("	}\n");
        buf.append("	\n");
        buf.append("	private void doStuff(int a, int b, final double min, final double max, Consumer<Object> postAction) {\n");
        buf.append("\n");
        buf.append("	}\n");
        buf.append("    private void updateSelectionData(final @NonNull Object data) {\n");
        buf.append("    }\n");
        buf.append("}\n");
        assertEqualString(preview, buf.toString());
        proposal = (CUCorrectionProposal) proposals.get(1);
        preview = getPreviewContent(proposal);
        buf = new StringBuffer();
        buf.append("package testNullAnnotations;\n");
        buf.append("\n");
        buf.append("import org.eclipse.jdt.annotation.NonNull;\n");
        buf.append("import org.eclipse.jdt.annotation.Nullable;\n");
        buf.append("\n");
        buf.append("interface Consumer<T> {\n");
        buf.append("    void accept(T t);\n");
        buf.append("}\n");
        buf.append("public class Snippet {\n");
        buf.append("	\n");
        buf.append("	public void select(final double min, final double max) {\n");
        buf.append("	    doStuff(0, 1, min, max, (Object data) -> updateSelectionData(data));\n");
        buf.append("	}\n");
        buf.append("	\n");
        buf.append("	private void doStuff(int a, int b, final double min, final double max, Consumer<Object> postAction) {\n");
        buf.append("\n");
        buf.append("	}\n");
        buf.append("    private void updateSelectionData(final @Nullable Object data) {\n");
        buf.append("    }\n");
        buf.append("}\n");
        assertEqualString(preview, buf.toString());
        proposal = (CUCorrectionProposal) proposals.get(2);
        preview = getPreviewContent(proposal);
        buf = new StringBuffer();
        buf.append("package testNullAnnotations;\n");
        buf.append("\n");
        buf.append("import org.eclipse.jdt.annotation.NonNull;\n");
        buf.append("\n");
        buf.append("interface Consumer<T> {\n");
        buf.append("    void accept(T t);\n");
        buf.append("}\n");
        buf.append("public class Snippet {\n");
        buf.append("	\n");
        buf.append("	@SuppressWarnings(\"null\")\n");
        buf.append("    public void select(final double min, final double max) {\n");
        buf.append("	    doStuff(0, 1, min, max, (Object data) -> updateSelectionData(data));\n");
        buf.append("	}\n");
        buf.append("	\n");
        buf.append("	private void doStuff(int a, int b, final double min, final double max, Consumer<Object> postAction) {\n");
        buf.append("\n");
        buf.append("	}\n");
        buf.append("    private void updateSelectionData(final @NonNull Object data) {\n");
        buf.append("    }\n");
        buf.append("}\n");
        assertEqualString(preview, buf.toString());
    }
}
