/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.ui.internal.preferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.ui.internal.ApiUIPlugin;
import org.eclipse.pde.api.tools.ui.internal.IApiToolsConstants;
import org.eclipse.pde.api.tools.ui.internal.IApiToolsHelpContextIds;
import org.eclipse.pde.api.tools.ui.internal.SWTFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * Class provides the general API tool preference page
 *
 * @since 1.0.0
 */
public class ApiErrorsWarningsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    //$NON-NLS-1$
    public static final String ID = ApiUIPlugin.PLUGIN_ID + ".apitools.errorwarnings.prefpage";

    /**
	 * Id of a setting in the data map applied when the page is opened. Value
	 * must be a Boolean object. If true, the customize project settings link
	 * will be hidden.
	 */
    //$NON-NLS-1$
    public static final String NO_LINK = "PropertyAndPreferencePage.nolink";

    /**
	 * Id of a setting in the data map applied when the page is opened. Value
	 * must be an Integer object with a value match the id of a tab on the page
	 * See constants
	 * {@link ApiErrorsWarningsConfigurationBlock#API_USE_SCANS_PAGE_ID},
	 * {@link ApiErrorsWarningsConfigurationBlock#COMPATIBILITY_PAGE_ID},
	 * {@link ApiErrorsWarningsConfigurationBlock#VERSION_MANAGEMENT_PAGE_ID},
	 * {@link ApiErrorsWarningsConfigurationBlock#API_COMPONENT_RESOLUTION_PAGE_ID}
	 * and {@link ApiErrorsWarningsConfigurationBlock#API_USE_SCANS_PAGE_ID} If
	 * an id is provided, the preference page will open with the specified tab
	 * visible
	 */
    //$NON-NLS-1$
    public static final String INITIAL_TAB = "PropertyAndPreferencePage.initialTab";

    /**
	 * The main configuration block for the page
	 */
    ApiErrorsWarningsConfigurationBlock block = null;

    private Link link = null;

    /**
	 * Since {@link #applyData(Object)} can be called before createContents,
	 * store the data
	 */
    private Map<String, Object> fPageData = null;

    /**
	 * Constructor
	 */
    public  ApiErrorsWarningsPreferencePage() {
        super(PreferenceMessages.ApiErrorsWarningsPreferencePage_0);
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite comp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_BOTH, 0, 0);
        link = new Link(comp, SWT.NONE);
        link.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false));
        link.setFont(comp.getFont());
        link.setText(PreferenceMessages.ApiErrorsWarningsPreferencePage_1);
        link.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                HashSet<IJavaProject> set = new HashSet<IJavaProject>();
                try {
                    IJavaProject[] projects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
                    IProject project = null;
                    for (int i = 0; i < projects.length; i++) {
                        project = projects[i].getProject();
                        try {
                            if (project.hasNature(ApiPlugin.NATURE_ID) && block.hasProjectSpecificSettings(project)) {
                                set.add(projects[i]);
                            }
                        } catch (CoreException ce) {
                        }
                    }
                } catch (JavaModelException jme) {
                }
                ProjectSelectionDialog psd = new ProjectSelectionDialog(getShell(), set);
                if (psd.open() == IDialogConstants.OK_ID) {
                    HashMap<String, Boolean> data = new HashMap<String, Boolean>();
                    data.put(NO_LINK, Boolean.TRUE);
                    PreferencesUtil.createPropertyDialogOn(getShell(), ((IJavaProject) psd.getFirstResult()).getProject(), IApiToolsConstants.ID_ERRORS_WARNINGS_PROP_PAGE, new String[] { IApiToolsConstants.ID_ERRORS_WARNINGS_PROP_PAGE }, data).open();
                }
            }
        });
        block = new ApiErrorsWarningsConfigurationBlock(null, (IWorkbenchPreferenceContainer) getContainer());
        block.createControl(comp);
        // Initialize with data map in case applyData was called before
        // createContents
        applyData(fPageData);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IApiToolsHelpContextIds.APITOOLS_ERROR_WARNING_PREF_PAGE);
        return comp;
    }

    @Override
    public void dispose() {
        if (block != null) {
            block.dispose();
        }
        super.dispose();
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public boolean performCancel() {
        block.performCancel();
        return super.performCancel();
    }

    @Override
    public boolean performOk() {
        block.performOK();
        return super.performOk();
    }

    @Override
    protected void performApply() {
        block.performApply();
        super.performApply();
    }

    @Override
    protected void performDefaults() {
        block.performDefaults();
        super.performDefaults();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void applyData(Object data) {
        if (data instanceof Map) {
            fPageData = (Map<String, Object>) data;
            if (link != null && fPageData.containsKey(NO_LINK)) {
                link.setVisible(!Boolean.TRUE.equals(fPageData.get(NO_LINK)));
            }
            if (block != null && fPageData.containsKey(INITIAL_TAB)) {
                Integer tabIndex = (Integer) fPageData.get(INITIAL_TAB);
                if (tabIndex != null) {
                    try {
                        block.selectTab(tabIndex.intValue());
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
    }
}
