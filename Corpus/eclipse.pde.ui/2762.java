/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.builder.tests.performance;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.pde.api.tools.internal.model.ApiModelFactory;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.provisional.search.ApiSearchEngine;
import org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchReporter;
import org.eclipse.pde.api.tools.internal.search.UseSearchRequestor;
import org.eclipse.pde.api.tools.internal.search.XmlSearchReporter;
import org.eclipse.pde.api.tools.model.tests.TestSuiteHelper;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.core.target.TargetBundle;
import org.eclipse.test.performance.PerformanceTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class UseScanTests extends PerformanceTestCase {

    //$NON-NLS-1$
    static IPath TMP_PATH = TestSuiteHelper.getUserDirectoryPath().append("use-scan-tests-perf");

    public  UseScanTests(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        scrubReportLocation(TMP_PATH.toFile());
    }

    /**
	 * Cleans the location if it exists
	 *
	 * @param file
	 */
    private void scrubReportLocation(File file) {
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    scrubReportLocation(files[i]);
                } else {
                    files[i].delete();
                }
            }
            file.delete();
        }
    }

    public static Test suite() {
        return new TestSuite(UseScanTests.class);
    }

    public void testUseScan() throws Exception {
        // get workspace target
        ITargetPlatformService service = (ITargetPlatformService) ApiPlugin.getDefault().acquireService(ITargetPlatformService.class.getName());
        ITargetDefinition definition = service.getWorkspaceTargetDefinition();
        IApiBaseline fBaseline = createBaseline(definition, new NullProgressMonitor());
        Set<IApiComponent> scope = new HashSet<IApiComponent>();
        // add equinox and eclipse core components
        IApiComponent[] components = fBaseline.getApiComponents();
        for (int i = 0; i < components.length; i++) {
            IApiComponent component = components[i];
            if (//$NON-NLS-1$
            component.toString().contains("org.eclipse.core") || component.toString().contains("org.eclipse.equinox")) {
                if (//$NON-NLS-1$
                !component.toString().contains("test")) {
                    scope.add(component);
                }
            }
        }
        // add equinox and eclipse core ids
        final Set<String> ids = new HashSet<String>();
        for (IApiComponent s : scope) {
            try {
                String id = //$NON-NLS-1$
                s.toString().substring(//$NON-NLS-1$
                0, //$NON-NLS-1$
                s.toString().indexOf("_"));
                ids.add(id);
            } catch (Exception e) {
            }
        }
        IApiSearchReporter reporter = new XmlSearchReporter(TMP_PATH.toOSString(), false);
        UseSearchRequestor requestor = new UseSearchRequestor(ids, scope.toArray(new IApiElement[scope.size()]), 7);
        ApiSearchEngine engine = new ApiSearchEngine();
        // run 2 times
        for (int i = 0; i < 2; i++) {
            startMeasuring();
            engine.search(fBaseline, requestor, reporter, new NullProgressMonitor());
            stopMeasuring();
            scrubReportLocation(TMP_PATH.toFile());
        }
        commitMeasurements();
        assertPerformance();
    }

    private IApiBaseline createBaseline(ITargetDefinition definition, IProgressMonitor monitor) throws CoreException {
        //$NON-NLS-1$
        SubMonitor localmonitor = SubMonitor.convert(monitor, "", 10);
        definition.resolve(localmonitor.split(2));
        localmonitor.split(1);
        TargetBundle[] bundles = definition.getBundles();
        List<IApiComponent> components = new ArrayList<IApiComponent>();
        IApiBaseline profile = ApiModelFactory.newApiBaseline(definition.getName());
        localmonitor.setWorkRemaining(bundles.length);
        for (int i = 0; i < bundles.length; i++) {
            localmonitor.split(1);
            if (bundles[i].getStatus().isOK() && !bundles[i].isSourceBundle()) {
                IApiComponent component = ApiModelFactory.newApiComponent(profile, URIUtil.toFile(bundles[i].getBundleInfo().getLocation()).getAbsolutePath());
                if (component != null) {
                    components.add(component);
                }
            }
        }
        profile.addApiComponents(components.toArray(new IApiComponent[components.size()]));
        return profile;
    }
}
