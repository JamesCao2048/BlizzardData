/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com>
 *      - Bug 214696 Expose WorkingDirectoryBlock as API
 *      - Bug 221973 Make WorkingDirectoryBlock from JDT a Debug API class
 *******************************************************************************/
package org.eclipse.jdt.debug.ui.launchConfigurations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.debug.ui.WorkingDirectoryBlock;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jdt.internal.debug.ui.actions.ControlAccessibleListener;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaWorkingDirectoryBlock;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.VMArgumentsBlock;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * A launch configuration tab that displays and edits program arguments,
 * VM arguments, and working directory launch configuration attributes.
 * <p>
 * This class may be instantiated.
 * </p>
 * @since 2.0
 * @noextend This class is not intended to be sub-classed by clients.
 */
public class JavaArgumentsTab extends JavaLaunchTab {

    // Program arguments widgets
    protected Label fPrgmArgumentsLabel;

    protected Text fPrgmArgumentsText;

    // VM arguments widgets
    protected VMArgumentsBlock fVMArgumentsBlock;

    // Working directory
    protected WorkingDirectoryBlock fWorkingDirectoryBlock;

    //$NON-NLS-1$
    protected static final String EMPTY_STRING = "";

    public  JavaArgumentsTab() {
        fVMArgumentsBlock = createVMArgsBlock();
        fWorkingDirectoryBlock = createWorkingDirBlock();
    }

    protected VMArgumentsBlock createVMArgsBlock() {
        return new VMArgumentsBlock();
    }

    /**
	 * Creates a control to specify a working directory.
	 * 
	 * @return the new {@link WorkingDirectoryBlock}
	 * @since 3.4
	 */
    protected WorkingDirectoryBlock createWorkingDirBlock() {
        return new JavaWorkingDirectoryBlock();
    }

    /**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
	 */
    @Override
    public void createControl(Composite parent) {
        Font font = parent.getFont();
        Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        comp.setLayout(layout);
        comp.setFont(font);
        GridData gd = new GridData(GridData.FILL_BOTH);
        comp.setLayoutData(gd);
        setControl(comp);
        setHelpContextId();
        Group group = new Group(comp, SWT.NONE);
        group.setFont(font);
        layout = new GridLayout();
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        String controlName = (LauncherMessages.JavaArgumentsTab__Program_arguments__5);
        group.setText(controlName);
        fPrgmArgumentsText = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
        fPrgmArgumentsText.addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(TraverseEvent e) {
                switch(e.detail) {
                    case SWT.TRAVERSE_ESCAPE:
                    case SWT.TRAVERSE_PAGE_NEXT:
                    case SWT.TRAVERSE_PAGE_PREVIOUS:
                        e.doit = true;
                        break;
                    case SWT.TRAVERSE_RETURN:
                    case SWT.TRAVERSE_TAB_NEXT:
                    case SWT.TRAVERSE_TAB_PREVIOUS:
                        if ((fPrgmArgumentsText.getStyle() & SWT.SINGLE) != 0) {
                            e.doit = true;
                        } else {
                            if (!fPrgmArgumentsText.isEnabled() || (e.stateMask & SWT.MODIFIER_MASK) != 0) {
                                e.doit = true;
                            }
                        }
                        break;
                }
            }
        });
        gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 40;
        gd.widthHint = 100;
        fPrgmArgumentsText.setLayoutData(gd);
        fPrgmArgumentsText.setFont(font);
        fPrgmArgumentsText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent evt) {
                scheduleUpdateJob();
            }
        });
        ControlAccessibleListener.addListener(fPrgmArgumentsText, group.getText());
        String buttonLabel = LauncherMessages.JavaArgumentsTab_5;
        Button pgrmArgVariableButton = createPushButton(group, buttonLabel, null);
        pgrmArgVariableButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        pgrmArgVariableButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
                dialog.open();
                String variable = dialog.getVariableExpression();
                if (variable != null) {
                    fPrgmArgumentsText.insert(variable);
                }
            }
        });
        fVMArgumentsBlock.createControl(comp);
        fWorkingDirectoryBlock.createControl(comp);
    }

    /**
	 * Set the help context id for this launch config tab.  Subclasses may
	 * override this method.
	 */
    protected void setHelpContextId() {
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_ARGUMENTS_TAB);
    }

    /**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
    @Override
    public void dispose() {
    }

    /**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
    @Override
    public boolean isValid(ILaunchConfiguration config) {
        return fWorkingDirectoryBlock.isValid(config);
    }

    /**
	 * Defaults are empty.
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
        config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String) null);
        fVMArgumentsBlock.setDefaults(config);
        fWorkingDirectoryBlock.setDefaults(config);
    }

    /**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            //$NON-NLS-1$
            fPrgmArgumentsText.setText(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""));
            fVMArgumentsBlock.initializeFrom(configuration);
            fWorkingDirectoryBlock.initializeFrom(configuration);
        } catch (CoreException e) {
            setErrorMessage(LauncherMessages.JavaArgumentsTab_Exception_occurred_reading_configuration___15 + e.getStatus().getMessage());
            JDIDebugUIPlugin.log(e);
        }
    }

    /**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, getAttributeValueFrom(fPrgmArgumentsText));
        fVMArgumentsBlock.performApply(configuration);
        fWorkingDirectoryBlock.performApply(configuration);
    }

    /**
	 * Returns the string in the text widget, or <code>null</code> if empty.
	 * 
	 * @param text the widget to get the value from
	 * @return text or <code>null</code>
	 */
    protected String getAttributeValueFrom(Text text) {
        String content = text.getText().trim();
        if (content.length() > 0) {
            return content;
        }
        return null;
    }

    /**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
    @Override
    public String getName() {
        return LauncherMessages.JavaArgumentsTab__Arguments_16;
    }

    /**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setLaunchConfigurationDialog(ILaunchConfigurationDialog)
	 */
    @Override
    public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
        super.setLaunchConfigurationDialog(dialog);
        fWorkingDirectoryBlock.setLaunchConfigurationDialog(dialog);
        fVMArgumentsBlock.setLaunchConfigurationDialog(dialog);
    }

    /**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getErrorMessage()
	 */
    @Override
    public String getErrorMessage() {
        String m = super.getErrorMessage();
        if (m == null) {
            return fWorkingDirectoryBlock.getErrorMessage();
        }
        return m;
    }

    /**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getMessage()
	 */
    @Override
    public String getMessage() {
        String m = super.getMessage();
        if (m == null) {
            return fWorkingDirectoryBlock.getMessage();
        }
        return m;
    }

    /**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
    @Override
    public Image getImage() {
        return JavaDebugImages.get(JavaDebugImages.IMG_VIEW_ARGUMENTS_TAB);
    }

    /**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 * 
	 * @since 3.3
	 */
    @Override
    public String getId() {
        //$NON-NLS-1$
        return "org.eclipse.jdt.debug.ui.javaArgumentsTab";
    }

    /* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
    @Override
    public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
        fWorkingDirectoryBlock.initializeFrom(workingCopy);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#deactivated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
    @Override
    public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
    // do nothing when deactivated
    }
}
