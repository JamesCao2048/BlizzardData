/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.compare;

import java.util.ResourceBundle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;

class CompareDialog extends ResizableDialog {

    class ViewerSwitchingPane extends CompareViewerSwitchingPane {

         ViewerSwitchingPane(Composite parent, int style) {
            super(parent, style, false);
        }

        @Override
        protected Viewer getViewer(Viewer oldViewer, Object input) {
            if (input instanceof ICompareInput)
                return CompareUI.findContentViewer(oldViewer, (ICompareInput) input, this, fCompareConfiguration);
            return null;
        }

        @Override
        public void setImage(Image image) {
        // don't show icon
        }
    }

    private CompareViewerSwitchingPane fContentPane;

    private CompareConfiguration fCompareConfiguration;

    private ICompareInput fInput;

     CompareDialog(Shell parent, ResourceBundle bundle) {
        super(parent, bundle);
        fCompareConfiguration = new CompareConfiguration();
        fCompareConfiguration.setLeftEditable(false);
        fCompareConfiguration.setRightEditable(false);
    }

    void compare(ICompareInput input) {
        fInput = input;
        fCompareConfiguration.setLeftLabel(fInput.getLeft().getName());
        fCompareConfiguration.setLeftImage(fInput.getLeft().getImage());
        fCompareConfiguration.setRightLabel(fInput.getRight().getName());
        fCompareConfiguration.setRightImage(fInput.getRight().getImage());
        if (fContentPane != null)
            fContentPane.setInput(fInput);
        open();
    }

    /* (non Javadoc)
 	 * Creates SWT control tree.
 	 */
    @Override
    protected synchronized Control createDialogArea(Composite parent2) {
        Composite parent = (Composite) super.createDialogArea(parent2);
        //$NON-NLS-1$
        getShell().setText(JavaCompareUtilities.getString(fBundle, "title"));
        fContentPane = new ViewerSwitchingPane(parent, SWT.BORDER | SWT.FLAT);
        fContentPane.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));
        if (fInput != null)
            fContentPane.setInput(fInput);
        applyDialogFont(parent);
        return parent;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        //$NON-NLS-1$
        String buttonLabel = JavaCompareUtilities.getString(fBundle, "buttonLabel", IDialogConstants.OK_LABEL);
        createButton(parent, IDialogConstants.CANCEL_ID, buttonLabel, false);
    }

    /*
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IJavaHelpContextIds.COMPARE_DIALOG);
    }
}
