/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.plugin;

import org.eclipse.core.runtime.Path;
import java.util.HashSet;
import org.eclipse.core.runtime.*;
import org.eclipse.pde.core.plugin.IPluginLibrary;
import org.eclipse.pde.internal.core.ClasspathUtilCore;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

public class NewRuntimeLibraryDialog extends SelectionStatusDialog {

    private Text libraryText;

    private IPluginLibrary[] libraries;

    private DuplicateStatusValidator validator;

    private String libraryName;

    private HashSet<Path> librarySet;

    class DuplicateStatusValidator {

        public IStatus validate(String text) {
            String id = PDEPlugin.getPluginId();
            if (text.length() == 0)
                return new Status(IStatus.ERROR, id, IStatus.ERROR, PDEUIMessages.AddLibraryDialog_emptyLibraries, null);
            if (text.indexOf(' ') != -1)
                return new Status(IStatus.ERROR, id, IStatus.ERROR, PDEUIMessages.AddLibraryDialog_nospaces, null);
            if (libraries == null || libraries.length == 0)
                return new //$NON-NLS-1$
                Status(//$NON-NLS-1$
                IStatus.OK, //$NON-NLS-1$
                id, //$NON-NLS-1$
                IStatus.OK, //$NON-NLS-1$
                "", //$NON-NLS-1$
                null);
            if (librarySet.contains(new Path(ClasspathUtilCore.expandLibraryName(text))))
                return new Status(IStatus.ERROR, id, IStatus.ERROR, PDEUIMessages.ManifestEditor_RuntimeLibraryDialog_validationError, null);
            //$NON-NLS-1$
            return new Status(IStatus.OK, id, IStatus.OK, "", null);
        }
    }

    public  NewRuntimeLibraryDialog(Shell parent, IPluginLibrary[] libraries) {
        super(parent);
        this.libraries = libraries;
        this.validator = new DuplicateStatusValidator();
        librarySet = new HashSet();
        for (int i = 0; i < libraries.length; i++) {
            librarySet.add(new Path(ClasspathUtilCore.expandLibraryName(libraries[i].getName())));
        }
        setStatusLineAboveButtons(true);
    }

    @Override
    protected void computeResult() {
    }

    /*
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IHelpContextIds.NEW_LIBRARY);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = layout.marginWidth = 8;
        layout.numColumns = 1;
        layout.makeColumnsEqualWidth = false;
        container.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_BOTH);
        container.setLayoutData(gd);
        Label libraryLabel = new Label(container, SWT.NULL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        libraryLabel.setLayoutData(gd);
        libraryLabel.setText(PDEUIMessages.ManifestEditor_RuntimeLibraryDialog_label);
        libraryText = new Text(container, SWT.SINGLE | SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        libraryText.setLayoutData(gd);
        libraryText.setText(PDEUIMessages.ManifestEditor_RuntimeLibraryDialog_default);
        libraryText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateStatus(validator.validate(libraryText.getText()));
            }
        });
        applyDialogFont(container);
        return container;
    }

    @Override
    public int open() {
        //$NON-NLS-1$
        libraryText.setText("library.jar");
        libraryText.setSelection(0, libraryText.getText().length() - 4);
        return super.open();
    }

    public String getLibraryName() {
        return libraryName;
    }

    @Override
    protected void okPressed() {
        libraryName = libraryText.getText();
        super.okPressed();
    }
}
