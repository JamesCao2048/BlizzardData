/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.model.tests;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.pde.api.tools.internal.ApiDescription;
import org.eclipse.pde.api.tools.internal.CompilationUnit;
import org.eclipse.pde.api.tools.internal.IApiCoreConstants;
import org.eclipse.pde.api.tools.internal.model.ApiBaseline;
import org.eclipse.pde.api.tools.internal.model.Component;
import org.eclipse.pde.api.tools.internal.model.DirectoryApiTypeContainer;
import org.eclipse.pde.api.tools.internal.provisional.IApiDescription;
import org.eclipse.pde.api.tools.internal.provisional.IApiFilterStore;
import org.eclipse.pde.api.tools.internal.provisional.IRequiredComponentDescription;
import org.eclipse.pde.api.tools.internal.provisional.builder.IReference;
import org.eclipse.pde.api.tools.internal.provisional.comparator.ApiScope;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiMember;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiScope;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiType;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiTypeContainer;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiTypeRoot;
import org.eclipse.pde.api.tools.internal.provisional.scanner.TagScanner;
import org.eclipse.pde.api.tools.internal.provisional.search.ApiSearchEngine;
import org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchReporter;
import org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchRequestor;
import org.eclipse.pde.api.tools.internal.provisional.search.IMetadata;

/**
 * Tests that our framework properly handles bad class files. I.e. class files
 * that are not well-formed
 */
public class BadClassfileTests extends TestCase {

    IPath source = null;

    DirectoryApiTypeContainer container = null;

    //$NON-NLS-1$
    String CLASSFILE = "nobytecodes";

    /*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
    @Override
    protected void setUp() throws Exception {
        if (source == null) {
            //$NON-NLS-1$ //$NON-NLS-2$
            source = TestSuiteHelper.getPluginDirectoryPath().append("test-classes").append("bad");
            container = new DirectoryApiTypeContainer(null, source.toOSString());
        }
    }

    /**
	 * Writes any expected error pre-amble prior to the test running
	 *
	 * @param test
	 */
    void writePreamble(String test) {
        //$NON-NLS-1$ //$NON-NLS-2$
        System.err.println("Expected 'java.lang.ArrayIndexOutOfBoundsException: 34' in " + test + " from ASM ClassReader");
        System.err.flush();
    }

    /**
	 * Tests trying to get the structure for a bad classfile
	 *
	 * @throws Exception if something bad happens
	 */
    public void testClassfileScanner() throws Exception {
        //$NON-NLS-1$
        writePreamble("testClassfileScanner()");
        IApiTypeRoot root = container.findTypeRoot(CLASSFILE);
        IApiType type = root.getStructure();
        //$NON-NLS-1$
        assertNull("The type must be null", type);
    }

    /**
	 * Tests trying to search a bad class file
	 *
	 * @throws Exception
	 */
    public void testSearchEngine() throws Exception {
        //$NON-NLS-1$
        writePreamble("testSearchEngine()");
        final Component component = new Component(null) {

            @Override
            public boolean isSystemComponent() {
                return false;
            }

            @Override
            public boolean isSourceComponent() throws CoreException {
                return false;
            }

            @Override
            public boolean isFragment() throws CoreException {
                return false;
            }

            @Override
            public boolean hasFragments() throws CoreException {
                return false;
            }

            @Override
            public boolean hasApiDescription() {
                return false;
            }

            @Override
            public String getVersion() {
                return //$NON-NLS-1$
                "1.0.0";
            }

            @Override
            public IRequiredComponentDescription[] getRequiredComponents() throws CoreException {
                return null;
            }

            @Override
            public String[] getLowestEEs() throws CoreException {
                return null;
            }

            @Override
            public String getLocation() {
                return null;
            }

            @Override
            public String getSymbolicName() {
                //$NON-NLS-1$
                return "test";
            }

            @Override
            public String[] getExecutionEnvironments() throws CoreException {
                return null;
            }

            @Override
            public ResolverError[] getErrors() throws CoreException {
                return null;
            }

            @Override
            protected List<IApiTypeContainer> createApiTypeContainers() throws CoreException {
                ArrayList<IApiTypeContainer> containers = new ArrayList<IApiTypeContainer>();
                containers.add(BadClassfileTests.this.container);
                return containers;
            }

            @Override
            public IApiBaseline getBaseline() {
                return new //$NON-NLS-1$
                ApiBaseline(//$NON-NLS-1$
                "testbaseline");
            }

            @Override
            protected IApiFilterStore createApiFilterStore() throws CoreException {
                return null;
            }

            @Override
            protected IApiDescription createApiDescription() throws CoreException {
                return null;
            }
        };
        ApiSearchEngine engine = new ApiSearchEngine();
        IApiBaseline baseline = new //$NON-NLS-1$
        ApiBaseline(//$NON-NLS-1$
        "testbaseline") {

            @Override
            public IApiComponent[] getApiComponents() {
                return new IApiComponent[] { component };
            }
        };
        IApiSearchRequestor requestor = new IApiSearchRequestor() {

            @Override
            public boolean includesInternal() {
                return true;
            }

            @Override
            public boolean includesAPI() {
                return true;
            }

            @Override
            public boolean includesIllegalUse() {
                return false;
            }

            @Override
            public IApiScope getScope() {
                ApiScope scope = new ApiScope();
                scope.addElement(component);
                return scope;
            }

            @Override
            public int getReferenceKinds() {
                return IReference.MASK_REF_ALL;
            }

            @Override
            public boolean acceptReference(IReference reference) {
                return true;
            }

            @Override
            public boolean acceptMember(IApiMember member) {
                return true;
            }

            @Override
            public boolean acceptComponent(IApiComponent component) {
                return true;
            }

            @Override
            public boolean acceptContainer(IApiTypeContainer container) {
                return false;
            }
        };
        IApiSearchReporter reporter = new IApiSearchReporter() {

            @Override
            public void reportResults(IApiElement element, IReference[] references) {
            }

            @Override
            public void reportNotSearched(IApiElement[] elements) {
            }

            @Override
            public void reportMetadata(IMetadata data) {
            }

            @Override
            public void reportCounts() {
            }
        };
        engine.search(baseline, requestor, reporter, null);
    }

    /**
	 * Tests that the
	 * {@link org.eclipse.pde.api.tools.internal.provisional.scanner.TagScanner}
	 * handles bad class files
	 */
    public void testTagScanner() throws Exception {
        //$NON-NLS-1$
        writePreamble("testTagScanner()");
        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        CompilationUnit unit = new CompilationUnit(TestSuiteHelper.getPluginDirectoryPath().append("test-classes").append("bad").append("nobytecodes.java").toOSString(), IApiCoreConstants.UTF_8);
        TagScanner scanner = TagScanner.newScanner();
        try {
            //$NON-NLS-1$
            scanner.scan(unit, new ApiDescription("test"), this.container, null, null);
        } catch (CoreException ce) {
            assertTrue("The tag scanner should return a multi status exception", ce.getStatus() instanceof MultiStatus);
            IStatus[] children = ((MultiStatus) ce.getStatus()).getChildren();
            assertEquals("There should only be one problem", 1, children.length);
            assertTrue("the message should be about nobytecodes#method() not resolving", children[0].getMessage().equals("Unable to resolve method signature: nobytecodes#void method()"));
        }
    }
}
