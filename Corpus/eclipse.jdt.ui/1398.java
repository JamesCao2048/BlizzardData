/*******************************************************************************
 * Copyright (c) 2015 GK Software AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.ui.tests.quickfix;

import static org.eclipse.jdt.core.IClasspathAttribute.EXTERNAL_ANNOTATION_PATH;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.ui.tests.core.ProjectTestSetup;
import org.eclipse.jdt.ui.tests.quickfix.JarUtil.ClassFileFilter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.text.correction.ExternalNullAnnotationQuickAssistProcessor;
import org.eclipse.jdt.internal.ui.text.correction.JavaCorrectionAssistant;

public abstract class AbstractAnnotateAssistTests extends QuickFixTest {

    protected IJavaProject fJProject1;

    public  AbstractAnnotateAssistTests(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        JavaProjectHelper.clear(fJProject1, ProjectTestSetup.getDefaultClasspath());
        super.tearDown();
    }

    protected void ensureExists(IContainer parent) throws CoreException {
        if (parent.exists())
            return;
        if (parent.getParent() != null)
            ensureExists(parent.getParent());
        if (parent instanceof IFolder)
            ((IFolder) parent).create(true, true, null);
    }

    public List<ICompletionProposal> collectAnnotateProposals(JavaEditor javaEditor, int offset) {
        JavaSourceViewer viewer = (JavaSourceViewer) javaEditor.getViewer();
        viewer.setSelection(new TextSelection(offset, 0));
        JavaCorrectionAssistant correctionAssist = new JavaCorrectionAssistant(javaEditor);
        IQuickAssistProcessor assistProcessor = new ExternalNullAnnotationQuickAssistProcessor(correctionAssist);
        ICompletionProposal[] proposals = assistProcessor.computeQuickAssistProposals(viewer.getQuickAssistInvocationContext());
        List<ICompletionProposal> list = Arrays.asList(proposals);
        return list;
    }

    // === from jdt.core.tests.model: ===
    protected void addLibrary(IJavaProject javaProject, String jarName, String sourceZipName, String[] pathAndContents, String annotationpath, String compliance, ClassFileFilter classFileFilter) throws CoreException, IOException {
        IProject project = createLibrary(javaProject, jarName, sourceZipName, pathAndContents, compliance, classFileFilter);
        String projectPath = '/' + project.getName() + '/';
        IClasspathEntry entry = JavaCore.newLibraryEntry(new Path(projectPath + jarName), sourceZipName == null ? null : new Path(projectPath + sourceZipName), null, ClasspathEntry.getAccessRules(null, null), new IClasspathAttribute[] { JavaCore.newClasspathAttribute(EXTERNAL_ANNOTATION_PATH, annotationpath) }, true);
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        int length = entries.length;
        System.arraycopy(entries, 0, entries = new IClasspathEntry[length + 1], 0, length);
        entries[length] = entry;
        javaProject.setRawClasspath(entries, null);
    }

    protected IProject createLibrary(IJavaProject javaProject, String jarName, String sourceZipName, String[] pathAndContents, String compliance, ClassFileFilter classFileFilter) throws IOException, CoreException {
        IProject project = javaProject.getProject();
        String projectLocation = project.getLocation().toOSString();
        String jarPath = projectLocation + File.separator + jarName;
        String[] claspath = new String[] { javaProject.getResolvedClasspath(true)[0].getPath().toOSString() };
        JarUtil.createJar(pathAndContents, null, jarPath, claspath, compliance, null, classFileFilter);
        if (pathAndContents != null && pathAndContents.length != 0) {
            String sourceZipPath = projectLocation + File.separator + sourceZipName;
            JarUtil.createSourceZip(pathAndContents, sourceZipPath);
        }
        project.refreshLocal(IResource.DEPTH_INFINITE, null);
        return project;
    }

    // === from PropertiesFileQuickAssistTest: ===
    protected static void checkContentOfFile(String message, IFile file, String content) throws Exception {
        try (InputStream in = file.getContents()) {
            assertEqualLines(message, content, copyToString(in));
        }
    }

    protected static String copyToString(InputStream in) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int read = in.read();
            while (read != -1) {
                out.write(read);
                read = in.read();
            }
            return out.toString();
        }
    }

    protected static void assertEqualLines(String message, String expected, String actual) {
        String[] expectedLines = Strings.convertIntoLines(expected);
        String[] actualLines = Strings.convertIntoLines(actual);
        String expected2 = (expectedLines == null ? null : Strings.concatenate(expectedLines, "\n"));
        String actual2 = (actualLines == null ? null : Strings.concatenate(actualLines, "\n"));
        assertEquals(message, expected2, actual2);
    }
}
