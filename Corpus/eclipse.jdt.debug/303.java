/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.refactoring;

import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestAgainException;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;

/**
 * Common refactoring utils.
 * 
 * @since 3.2
 */
public class AbstractRefactoringDebugTest extends AbstractDebugTest {

    public  AbstractRefactoringDebugTest(String name) {
        super(name);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.tests.AbstractDebugTest#setUp()
	 */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cleanTestFiles();
    }

    /**
	 * Performs the given refactoring. If a {@link ResourceException} occurs during the refactoring,
	 * we trap it and throw a {@link TestAgainException} to try the test again.
	 * 
	 * @param refactoring
	 * @throws Exception
	 */
    public void performRefactor(final Refactoring refactoring) throws Exception {
        if (refactoring == null) {
            return;
        }
        PerformRefactoringOperation op = new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
        try {
            ResourcesPlugin.getWorkspace().run(op, new NullProgressMonitor());
            waitForBuild();
            assertEquals(true, op.getValidationStatus().isOK());
            assertEquals(true, op.getConditionStatus().isOK());
        } catch (ResourceException re) {
            throw new TestAgainException(re.getLocalizedMessage());
        } catch (JavaModelException jme) {
            throw new TestAgainException(jme.getLocalizedMessage());
        } catch (CoreException ce) {
            throw new TestAgainException(ce.getLocalizedMessage());
        }
    }

    /**
	 * Clean up all the test files
	 * @throws CoreException
	 */
    protected void cleanTestFiles() throws CoreException {
        waitUntilIndexesReady();
        try {
            doClean();
            waitForBuild();
        } catch (Exception e) {
            throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.jdt.debug.tests", 0, "Error", e));
        }
    }

    /**
	 * Cleans up refactored files and reverts the source.
	 * @throws Exception
	 */
    private void doClean() throws Exception {
        IProject project = get14Project().getProject();
        IPackageFragmentRoot root = getPackageFragmentRoot(get14Project(), "src");
        IPackageFragment fragment = root.getPackageFragment("renamedPackage");
        if (fragment.exists()) {
            fragment.delete(true, new NullProgressMonitor());
        }
        fragment = root.getPackageFragment("a.b.c");
        if (!fragment.exists()) {
            root.createPackageFragment("a.b.c", true, new NullProgressMonitor());
        }
        // cleanup MoveeSource / Movee.java
        IFile target = project.getFile("src/a/b/Movee.java");
        if (target.exists()) {
            target.delete(true, false, null);
        }
        target = project.getFile("src/a/b/c/Movee.java");
        if (target.exists()) {
            target.delete(true, false, null);
        }
        // no .java - it's a bin
        IFile source = project.getFile("src/a/MoveeSource");
        source.copy(target.getFullPath(), true, null);
        // cleanup MoveeRecipientSource / MoveeRecipient.java
        target = project.getFile("src/a/b/MoveeRecipient.java");
        if (target.exists()) {
            target.delete(true, false, null);
        }
        // no .java - it's a bin
        source = project.getFile("src/a/MoveeRecipientSource");
        source.copy(target.getFullPath(), true, null);
        target = project.getFile("src/a/b/c/RenamedType.java");
        if (target.exists()) {
            target.delete(true, false, null);
        }
        // move up a dir
        target = project.getFile("src/a/b/c/RenamedCompilationUnit.java");
        if (target.exists()) {
            target.delete(true, false, null);
        }
        // cleanup MoveeChildSource / MoveeChild.java
        target = project.getFile("src/a/b/MoveeChild.java");
        if (target.exists()) {
            target.delete(true, false, null);
        }
        target = project.getFile("src/a/b/c/MoveeChild.java");
        if (target.exists()) {
            target.delete(true, false, null);
        }
        // no .java - it's a bin
        source = project.getFile("src/a/MoveeChildSource");
        source.copy(target.getFullPath(), true, null);
    }

    /**
	 * Wait until the search index is ready
	 */
    protected static void waitUntilIndexesReady() {
        // dummy query for waiting until the indexes are ready
        SearchEngine engine = new SearchEngine();
        IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
        try {
            engine.searchAllTypeNames(null, SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE, "!@$#!@".toCharArray(), SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE, IJavaSearchConstants.CLASS, scope, new TypeNameRequestor() {
            }, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
        } catch (CoreException e) {
        }
    }
}
