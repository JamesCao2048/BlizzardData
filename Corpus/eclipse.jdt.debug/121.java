/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.ui.launchConfigurations;

import java.util.List;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.ui.JavaUISourceLocator;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.SourceLookupBlock;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

@Deprecated
public class JavaSourceLookupTab extends JavaLaunchTab {

    protected SourceLookupBlock fSourceLookupBlock;

    /**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
	 */
    @Override
    public void createControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        setControl(comp);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_SOURCE_TAB);
        GridLayout topLayout = new GridLayout();
        topLayout.numColumns = 1;
        topLayout.marginHeight = 0;
        topLayout.marginWidth = 0;
        comp.setLayout(topLayout);
        comp.setFont(parent.getFont());
        createVerticalSpacer(comp, 1);
        fSourceLookupBlock = new SourceLookupBlock();
        fSourceLookupBlock.setLaunchConfigurationDialog(getLaunchConfigurationDialog());
        fSourceLookupBlock.createControl(comp);
        GridData gd = (GridData) fSourceLookupBlock.getControl().getLayoutData();
        gd.heightHint = 200;
        gd.widthHint = 250;
    }

    /**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        // be default, use a prompting source locator
        configuration.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, JavaUISourceLocator.ID_PROMPTING_JAVA_SOURCE_LOCATOR);
        configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH, (String) null);
        configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH, (List<String>) null);
    }

    /**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        fSourceLookupBlock.initializeFrom(configuration);
    }

    /**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, JavaUISourceLocator.ID_PROMPTING_JAVA_SOURCE_LOCATOR);
        fSourceLookupBlock.performApply(configuration);
    }

    /**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 * 
	 * @since 3.3
	 */
    @Override
    public String getId() {
        //$NON-NLS-1$
        return "org.eclipse.jdt.debug.ui.javaSourceLookupTab";
    }

    /**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
    @Override
    public String getName() {
        return LauncherMessages.JavaSourceLookupTab_Source_1;
    }

    /**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
    @Override
    public Image getImage() {
        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
    }
}
