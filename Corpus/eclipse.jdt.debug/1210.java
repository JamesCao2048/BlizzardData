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

import java.util.Enumeration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class BundleWizardPage3 extends BundleWizardBasePage {

    Text fIdentifier;

    Text fSignature;

    Text fVMOptions;

    Table fProperties;

    protected  BundleWizardPage3(BundleDescription bd) {
        //$NON-NLS-1$
        super("page3", bd);
    }

    @Override
    public void createContents(Composite c) {
        Composite c1 = createComposite(c, 4);
        //$NON-NLS-1$
        createLabel(c1, Util.getString("page3.identifier.label"), GridData.VERTICAL_ALIGN_CENTER);
        fIdentifier = createText(c1, IDENTIFIER, 1);
        //$NON-NLS-1$
        createLabel(c1, Util.getString("page3.signature.label"), GridData.VERTICAL_ALIGN_CENTER);
        fSignature = createText(c1, SIGNATURE, 1);
        //$NON-NLS-1$
        createLabel(c, Util.getString("page3.vmOptions.label"), GridData.VERTICAL_ALIGN_CENTER);
        fVMOptions = createText(c, VMOPTIONS, 2);
        //$NON-NLS-1$
        Group g = createGroup(c, Util.getString("page3.propertiesGroup.label"), 1);
        fProperties = new Table(g, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
        fProperties.setHeaderVisible(true);
        fProperties.setLinesVisible(true);
        fProperties.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
            //
            }
        });
        setHeightHint(fProperties, 60);
        TableColumn col0 = new TableColumn(fProperties, SWT.NONE);
        //$NON-NLS-1$
        col0.setText(Util.getString("page3.keys.column.label"));
        col0.setWidth(150);
        TableColumn col1 = new TableColumn(fProperties, SWT.NONE);
        //$NON-NLS-1$
        col1.setText(Util.getString("page3.values.column.label"));
        col1.setWidth(150);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (fProperties == null) {
            return;
        }
        //$NON-NLS-1$
        fIdentifier.setText(fBundleDescription.get(IDENTIFIER, ""));
        //$NON-NLS-1$
        fSignature.setText(fBundleDescription.get(SIGNATURE, ""));
        //$NON-NLS-1$
        fVMOptions.setText(fBundleDescription.get(VMOPTIONS, ""));
        fProperties.removeAll();
        if (fBundleDescription.fProperties2 != null && fBundleDescription.fProperties2.size() > 0) {
            Enumeration<?> iter = fBundleDescription.fProperties2.keys();
            while (iter.hasMoreElements()) {
                String key = (String) iter.nextElement();
                String value = (String) fBundleDescription.fProperties2.get(key);
                TableItem ti = new TableItem(fProperties, SWT.NONE);
                ti.setText(0, key);
                ti.setText(1, value);
            }
        }
    }

    @Override
    public boolean isPageComplete() {
        return true;
    }
}
