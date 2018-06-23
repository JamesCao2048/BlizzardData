/*******************************************************************************
 *  Copyright (c) 2006, 2013 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * test class tests the returnable methods of the new <code>LaunchConfigurationTabGroupWrapper</code> to ensure that it returns existing tab group
 * elements and all contributions
 * 
 * @since 3.3
 */
public class TabGroupWrapperTests extends AbstractDebugTest {

    /**
	 * Constructor
	 * @param name
	 */
    public  TabGroupWrapperTests(String name) {
        super(name);
    }

    /**
	 * Checks that all of the tabs specified in the original tab group and ones that are contributed are created
	 * @throws CoreException
	 */
    public void testAllTabsCreated() throws CoreException {
        ILaunchConfigurationTabGroup javagroup = getJavaLaunchGroup();
        //$NON-NLS-1$
        assertNotNull("java tab group cannot be null", javagroup);
        javagroup.createTabs(getLaunchConfigurationDialog(IDebugUIConstants.ID_RUN_LAUNCH_GROUP), ILaunchManager.RUN_MODE);
        //$NON-NLS-1$
        assertTrue("There must be at least 11 tabs", javagroup.getTabs().length >= 11);
    }

    /**
	 * Checks to make sure that all of the controls of the tabs (including contributed ones) are disposed on a call to the tab group
	 * wrapper class
	 * @throws CoreException
	 */
    public void testDisposeAllTabs() throws CoreException {
        ILaunchConfigurationTabGroup javagroup = getJavaLaunchGroup();
        //$NON-NLS-1$
        assertNotNull("java tab group cannot be null", javagroup);
        javagroup.createTabs(getLaunchConfigurationDialog(IDebugUIConstants.ID_RUN_LAUNCH_GROUP), ILaunchManager.RUN_MODE);
        ILaunchConfigurationTab[] tabs = javagroup.getTabs();
        //$NON-NLS-1$
        assertTrue("there must be more than 0 tabs", tabs.length > 0);
        //dispose all returned tabs
        for (int i = 0; i < tabs.length; i++) {
            tabs[i].dispose();
        }
        //get the tabs from the tab group again
        tabs = javagroup.getTabs();
        boolean alldisposed = true;
        for (int i = 0; i < tabs.length; i++) {
            alldisposed &= tabs[i].getControl() == null;
        }
        //$NON-NLS-1$
        assertTrue("All of the controls must be disposed", alldisposed);
    }
}
