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
package org.eclipse.pde.ui.launcher;

import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.launcher.TracingBlock;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 * A launch configuration tab that enables tracing and displays all plug-ins that support
 * tracing.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed by clients.
 * </p>
 * @since 3.2
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TracingTab extends AbstractLauncherTab {

    private Image fImage;

    private TracingBlock fTracingBlock;

    /**
	 * Constructor
	 *
	 */
    public  TracingTab() {
        fTracingBlock = new TracingBlock(this);
        PDEPlugin.getDefault().getLabelProvider().connect(this);
        fImage = PDEPluginImages.DESC_DOC_SECTION_OBJ.createImage();
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout());
        fTracingBlock.createControl(container);
        setControl(container);
        Dialog.applyDialogFont(container);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(container, IHelpContextIds.LAUNCHER_TRACING);
    }

    @Override
    public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
        fTracingBlock.activated(workingCopy);
    }

    @Override
    public void dispose() {
        fTracingBlock.dispose();
        if (fImage != null)
            fImage.dispose();
        PDEPlugin.getDefault().getLabelProvider().disconnect(this);
        super.dispose();
    }

    @Override
    public void initializeFrom(ILaunchConfiguration config) {
        fTracingBlock.initializeFrom(config);
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy config) {
        fTracingBlock.performApply(config);
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
        fTracingBlock.setDefaults(config);
    }

    @Override
    public String getName() {
        return PDEUIMessages.TracingLauncherTab_name;
    }

    @Override
    public Image getImage() {
        return fImage;
    }

    @Override
    public void validateTab() {
    }

    @Override
    public String getId() {
        return IPDELauncherConstants.TAB_TRACING_ID;
    }
}
