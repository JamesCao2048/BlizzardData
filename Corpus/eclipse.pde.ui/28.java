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
package org.eclipse.pde.internal.ui.preferences;

import org.eclipse.jdt.core.IJavaProject;
import java.util.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * Allows PDE compiler preferences to be set
 */
public class CompilersPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    //$NON-NLS-1$
    public static final String NO_LINK = "PropertyAndPreferencePage.nolink";

    private PDECompilersConfigurationBlock fBlock = null;

    private Link link = null;

    /**
	 * Since {@link #applyData(Object)} can be called before createContents, store the data
	 */
    private Map<?, ?> fPageData = null;

    /**
	 *
	 */
    public  CompilersPreferencePage() {
        super();
        // only used when page is shown programmatically
        setTitle(PDEUIMessages.CompilersPreferencePage_title);
    }

    @Override
    public void createControl(Composite composite) {
        super.createControl(composite);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IHelpContextIds.COMPILERS_PREFERENCE_PAGE);
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite comp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_BOTH, 0, 0);
        link = new Link(comp, SWT.NONE);
        link.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false));
        link.setFont(comp.getFont());
        link.setText(PDEUIMessages.CompilersPreferencePage_configure_project_specific_settings);
        link.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                HashSet<IJavaProject> set = new HashSet();
                try {
                    IJavaProject[] projects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
                    IProject project = null;
                    for (int i = 0; i < projects.length; i++) {
                        project = projects[i].getProject();
                        try {
                            if (project.hasNature(PDE.PLUGIN_NATURE) && fBlock.hasProjectSpecificSettings(project)) {
                                set.add(projects[i]);
                            }
                        } catch (CoreException ce) {
                        }
                    }
                } catch (JavaModelException jme) {
                }
                ProjectSelectionDialog psd = new ProjectSelectionDialog(getShell(), set);
                if (psd.open() == IDialogConstants.OK_ID) {
                    HashMap<String, Boolean> data = new HashMap();
                    data.put(NO_LINK, Boolean.TRUE);
                    PreferencesUtil.createPropertyDialogOn(getShell(), ((IJavaProject) psd.getFirstResult()).getProject(), "org.eclipse.pde.internal.ui.properties.compilersPropertyPage", //$NON-NLS-1$
                    new String[] { "org.eclipse.pde.internal.ui.properties.compilersPropertyPage" }, //$NON-NLS-1$
                    data).open();
                }
            }
        });
        fBlock = new PDECompilersConfigurationBlock(null, (IWorkbenchPreferenceContainer) getContainer());
        fBlock.createControl(comp);
        // Initialize with data map in case applyData was called before createContents
        applyData(fPageData);
        return comp;
    }

    @Override
    public void dispose() {
        if (fBlock != null) {
            fBlock.dispose();
        }
        super.dispose();
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public boolean performCancel() {
        fBlock.performCancel();
        return super.performCancel();
    }

    @Override
    public boolean performOk() {
        fBlock.performOK();
        return super.performOk();
    }

    @Override
    protected void performApply() {
        fBlock.performApply();
        super.performApply();
    }

    /**
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
    @Override
    protected void performDefaults() {
        fBlock.performDefaults();
        super.performDefaults();
    }

    /**
	 * @see org.eclipse.jface.preference.PreferencePage#applyData(java.lang.Object)
	 */
    @Override
    public void applyData(Object data) {
        if (data instanceof Map) {
            fPageData = (Map<?, ?>) data;
            if (link != null && fPageData.containsKey(NO_LINK)) {
                link.setVisible(!Boolean.TRUE.equals(((Map<?, ?>) data).get(NO_LINK)));
            }
        }
    }
}
