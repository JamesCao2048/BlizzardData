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
package org.eclipse.jdt.internal.ui.macbundler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class BundleWizardPage1 extends BundleWizardBasePage {

    static String[] JVMS = { //$NON-NLS-1$
    "1.3+", //$NON-NLS-1$
    "1.3*", //$NON-NLS-1$
    "1.4.2", //$NON-NLS-1$
    "1.4+", //$NON-NLS-1$
    "1.4*", //$NON-NLS-1$
    "1.5+", //$NON-NLS-1$
    "1.5*", //$NON-NLS-1$
    "1.6+", //$NON-NLS-1$
    "1.6*" };

    ILaunchConfiguration[] fConfigurations = new ILaunchConfiguration[0];

    Combo fLocation;

    Combo fLaunchConfigs;

    Combo fJVMVersion;

    Text fAppName;

    Text fMainClass;

    Text fArguments;

    Text fIconFileName;

    Button fUseSWT;

    public  BundleWizardPage1(BundleDescription bd) {
        //$NON-NLS-1$
        super("page1", bd);
    }

    @Override
    public void createContents(Composite c) {
        final Shell shell = c.getShell();
        Composite c1 = createComposite(c, 2);
        //$NON-NLS-1$
        createLabel(c1, Util.getString("page1.launchConfig.label"), GridData.VERTICAL_ALIGN_CENTER);
        fLaunchConfigs = new Combo(c1, SWT.READ_ONLY);
        fillCombo(fLaunchConfigs);
        fLaunchConfigs.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int ix = fLaunchConfigs.getSelectionIndex();
                if (ix > 0 && ix < fConfigurations.length) {
                    fBundleDescription.clear();
                    fBundleDescription.inititialize(fConfigurations[ix]);
                }
            }
        });
        //$NON-NLS-1$
        Group c2 = createGroup(c, "Main", 2);
        //$NON-NLS-1$
        createLabel(c2, Util.getString("page1.mainClass.label"), GridData.VERTICAL_ALIGN_CENTER);
        Composite c7a = createHBox(c2);
        fMainClass = createText(c7a, MAINCLASS, 1);
        Button b1 = createButton(//$NON-NLS-1$
        c7a, //$NON-NLS-1$
        SWT.NONE, //$NON-NLS-1$
        Util.getString("page1.mainClass.chooseButton.label"));
        b1.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
                mb.setMessage(Util.getString("page1.mainClass.dialog.message"));
                mb.setText(Util.getString("page1.mainClass.dialog.title"));
                mb.open();
            }
        });
        createLabel(//$NON-NLS-1$
        c2, //$NON-NLS-1$
        Util.getString("page1.arguments.label"), //$NON-NLS-1$
        GridData.VERTICAL_ALIGN_BEGINNING);
        fArguments = createText(c2, ARGUMENTS, 2);
        //$NON-NLS-1$	
        Group c5 = createGroup(c, "Destination", 2);
        //$NON-NLS-1$
        createLabel(c5, Util.getString("page1.appName.label"), GridData.VERTICAL_ALIGN_CENTER);
        fAppName = createText(c5, APPNAME, 1);
        //$NON-NLS-1$
        createLabel(c5, Util.getString("page1.appFolder.label"), GridData.VERTICAL_ALIGN_CENTER);
        Composite c3a = createHBox(c5);
        fLocation = createCombo(c3a, DESTINATIONDIRECTORY);
        final Button browse = createButton(//$NON-NLS-1$
        c3a, //$NON-NLS-1$
        SWT.NONE, //$NON-NLS-1$
        Util.getString("page1.appFolder.browseButton.label"));
        browse.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dd = new DirectoryDialog(browse.getShell(), SWT.SAVE);
                dd.setMessage(Util.getString("page1.appFolder.browseDialog.message"));
                dd.setText(Util.getString("page1.appFolder.browseDialog.title"));
                String name = dd.open();
                if (name != null) {
                    fLocation.setText(name);
                }
            }
        });
        //$NON-NLS-1$
        Group g6 = createGroup(c, "Options", 2);
        //$NON-NLS-1$
        createLabel(g6, Util.getString("page1.jvm.label"), GridData.VERTICAL_ALIGN_CENTER);
        Composite c8 = createComposite(g6, 4);
        fJVMVersion = new Combo(c8, SWT.READ_ONLY);
        for (int i = 0; i < JVMS.length; i++) {
            fJVMVersion.add(JVMS[i]);
        }
        fJVMVersion.setText(JVMS[4]);
        hookField(fJVMVersion, JVMVERSION);
        createLabel(//$NON-NLS-1$
        c8, //$NON-NLS-1$
        "      ", //$NON-NLS-1$
        GridData.VERTICAL_ALIGN_CENTER);
        createLabel(//$NON-NLS-1$
        c8, //$NON-NLS-1$
        Util.getString("page1.useSWT.label"), //$NON-NLS-1$
        GridData.VERTICAL_ALIGN_CENTER);
        fUseSWT = createButton(c8, SWT.CHECK, null);
        hookButton(fUseSWT, USES_SWT);
        //$NON-NLS-1$
        createLabel(g6, Util.getString("page1.appIcon.label"), GridData.VERTICAL_ALIGN_CENTER);
        Composite c7 = createComposite(g6, 2);
        fIconFileName = createText(c7, ICONFILE, 1);
        final Button b = createButton(//$NON-NLS-1$
        c7, //$NON-NLS-1$
        SWT.NONE, //$NON-NLS-1$
        Util.getString("page1.appIcon.chooseButton.label"));
        b.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fd = new FileDialog(b.getShell(), SWT.OPEN);
                fd.setText(Util.getString("page1.appIcon.chooseDialog.title"));
                fd.setFilterExtensions(new //$NON-NLS-1$
                String[] //$NON-NLS-1$
                { "icns" });
                String name = fd.open();
                if (name != null) {
                    fIconFileName.setText(name);
                }
            }
        });
    }

    @Override
    void enterPage() {
        super.enterPage();
        initCombo(fLaunchConfigs);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (fAppName != null) {
            //$NON-NLS-1$
            fAppName.setText(fBundleDescription.get(APPNAME, ""));
        }
        if (fMainClass != null) {
            //$NON-NLS-1$
            fMainClass.setText(fBundleDescription.get(MAINCLASS, ""));
        }
        if (fJVMVersion != null) {
            //$NON-NLS-1$
            fJVMVersion.setText(fBundleDescription.get(JVMVERSION, ""));
        }
        if (fUseSWT != null) {
            fUseSWT.setSelection(fBundleDescription.get(USES_SWT, false));
        }
    }

    @Override
    public boolean isPageComplete() {
        return fAppName != null && fAppName.getText().length() > 0 && fLocation.getText().length() > 0;
    }

    // private stuff
    private void collectLaunchConfigs() {
        ArrayList<ILaunchConfiguration> configs = new ArrayList<ILaunchConfiguration>();
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
        try {
            ILaunchConfiguration[] configurations = manager.getLaunchConfigurations(type);
            for (int i = 0; i < configurations.length; i++) {
                ILaunchConfiguration configuration = configurations[i];
                if (BundleDescription.verify(configuration)) {
                    configs.add(configuration);
                }
            }
        } catch (CoreException e) {
        }
        fConfigurations = configs.toArray(new ILaunchConfiguration[configs.size()]);
        Arrays.sort(fConfigurations, new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                ILaunchConfiguration lc1 = (ILaunchConfiguration) o1;
                ILaunchConfiguration lc2 = (ILaunchConfiguration) o2;
                return lc1.getName().compareTo(lc2.getName());
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }
        });
    }

    private void fillCombo(Combo c) {
        collectLaunchConfigs();
        for (int i = 0; i < fConfigurations.length; i++) {
            ILaunchConfiguration configuration = fConfigurations[i];
            c.add(configuration.getName());
        }
    }

    private void initCombo(Combo c) {
        IStructuredSelection sel = ((MacBundleWizard) getWizard()).getSelection();
        Object o = sel.getFirstElement();
        if (o instanceof IJavaElement) {
            IJavaProject project = ((IJavaElement) o).getJavaProject();
            if (project != null) {
                for (int i = 0; i < fConfigurations.length; i++) {
                    ILaunchConfiguration configuration = fConfigurations[i];
                    if (BundleDescription.matches(configuration, project)) {
                        c.setText(configuration.getName());
                        fBundleDescription.inititialize(configuration);
                        return;
                    }
                }
            }
        }
    }
}
