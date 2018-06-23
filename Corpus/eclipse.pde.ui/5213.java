/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 487943
 *******************************************************************************/
package org.eclipse.pde.internal.ui.wizards.exports;

import java.io.File;
import java.util.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.*;
import org.eclipse.pde.core.plugin.TargetPlatform;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.ui.IHelpContextIds;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.parts.WizardCheckboxTablePart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;

public class CrossPlatformExportPage extends AbstractExportWizardPage {

    //$NON-NLS-1$
    private static String CROSS_PLATFORM = "cross-platform";

    class Configuration {

        String os;

        String ws;

        String arch;

        @Override
        public String toString() {
            //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return os + " (" + ws + "/" + arch + ")";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Configuration) {
                Configuration config = (Configuration) obj;
                return os.equals(config.os) && ws.equals(config.ws) && arch.equals(config.arch);
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return os.hashCode() + ws.hashCode() + arch.hashCode();
        }
    }

    class ContentProvider implements IStructuredContentProvider {

        @Override
        public Object[] getElements(Object parent) {
            return getListElements();
        }
    }

    class PlatformPart extends WizardCheckboxTablePart {

        public  PlatformPart(String label, String[] buttonLabels) {
            super(label, buttonLabels);
        }

        @Override
        public void updateCounter(int count) {
            super.updateCounter(count);
            pageChanged();
        }

        @Override
        protected void buttonSelected(Button button, int index) {
            switch(index) {
                case 0:
                    handleSelectAll(true);
                    break;
                case 1:
                    handleSelectAll(false);
                    break;
            }
        }
    }

    private PlatformPart fPlatformPart;

    private IFeatureModel fModel;

    public  CrossPlatformExportPage(String pageName, IFeatureModel model) {
        super(pageName);
        fPlatformPart = new PlatformPart(PDEUIMessages.CrossPlatformExportPage_available, new String[] { PDEUIMessages.WizardCheckboxTablePart_selectAll, PDEUIMessages.WizardCheckboxTablePart_deselectAll });
        setTitle(PDEUIMessages.CrossPlatformExportPage_title);
        setDescription(PDEUIMessages.CrossPlatformExportPage_desc);
        fModel = model;
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout(2, false));
        fPlatformPart.createControl(container);
        TableViewer viewer = fPlatformPart.getTableViewer();
        viewer.setContentProvider(new ContentProvider());
        viewer.setLabelProvider(new LabelProvider());
        fPlatformPart.getTableViewer().setInput(PDECore.getDefault().getFeatureModelManager());
        initialize();
        setControl(container);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(container, IHelpContextIds.CROSS_PLATFORM_EXPORT);
    }

    private void initialize() {
        String value = getDialogSettings().get(CROSS_PLATFORM);
        if (value != null) {
            HashSet<String> set = new HashSet();
            //$NON-NLS-1$
            StringTokenizer tok = new StringTokenizer(value, ",");
            while (tok.hasMoreTokens()) {
                set.add(tok.nextToken());
            }
            ArrayList<Configuration> selected = new ArrayList();
            TableItem[] items = fPlatformPart.getTableViewer().getTable().getItems();
            for (int i = 0; i < items.length; i++) {
                Configuration config = (Configuration) items[i].getData();
                if (set.contains(config.toString())) {
                    selected.add(config);
                }
            }
            fPlatformPart.setSelection(selected.toArray());
        } else // we should select at least one, which is the current default
        {
            // TODO clean this horrible mess of a class up
            Configuration config = new Configuration();
            config.ws = TargetPlatform.getWS();
            config.os = TargetPlatform.getOS();
            config.arch = TargetPlatform.getOSArch();
            TableItem[] items = fPlatformPart.getTableViewer().getTable().getItems();
            for (int i = 0; i < items.length; i++) {
                Configuration c = (Configuration) items[i].getData();
                if (c.equals(config)) {
                    fPlatformPart.getTableViewer().setChecked(c, true);
                    fPlatformPart.updateCounter(1);
                }
            }
        }
        pageChanged();
    }

    @Override
    public void saveSettings(IDialogSettings settings) {
        Object[] objects = fPlatformPart.getSelection();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < objects.length; i++) {
            if (buffer.length() > 0)
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                ",");
            buffer.append(objects[i].toString());
        }
        settings.put(CROSS_PLATFORM, buffer.toString());
    }

    private Configuration[] getListElements() {
        ArrayList<Configuration> list = new ArrayList();
        if (fModel != null) {
            //$NON-NLS-1$
            File bin = new File(fModel.getInstallLocation(), "bin");
            if (bin.exists() && bin.isDirectory()) {
                File[] children = bin.listFiles();
                for (int i = 0; i < children.length; i++) {
                    if (children[i].isDirectory())
                        getWS(list, children[i]);
                }
            }
        }
        return list.toArray(new Configuration[list.size()]);
    }

    private void getWS(ArrayList<Configuration> list, File file) {
        File[] children = file.listFiles();
        for (int i = 0; i < children.length; i++) {
            if (children[i].isDirectory())
                getOS(list, children[i], file.getName());
        }
    }

    private void getOS(ArrayList<Configuration> list, File file, String ws) {
        File[] children = file.listFiles();
        for (int i = 0; i < children.length; i++) {
            if (//$NON-NLS-1$
            children[i].isDirectory() && !"CVS".equalsIgnoreCase(children[i].getName())) {
                Configuration config = new Configuration();
                config.ws = ws;
                config.os = file.getName();
                config.arch = children[i].getName();
                list.add(config);
            }
        }
    }

    @Override
    protected void pageChanged() {
        setPageComplete(fPlatformPart.getSelectionCount() > 0);
    }

    public String[][] getTargets() {
        Object[] objects = fPlatformPart.getSelection();
        String[][] targets = new String[objects.length][4];
        for (int i = 0; i < objects.length; i++) {
            Configuration config = (Configuration) objects[i];
            String[] combo = new String[4];
            combo[0] = config.os;
            combo[1] = config.ws;
            combo[2] = config.arch;
            //$NON-NLS-1$
            combo[3] = "";
            targets[i] = combo;
        }
        return targets;
    }
}
