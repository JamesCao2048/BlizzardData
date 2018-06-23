/*******************************************************************************
 *  Copyright (c) 2003, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.plugin;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.PDEFormTextEditorContributor;
import org.eclipse.pde.internal.ui.nls.GetNonExternalizedStringsAction;
import org.eclipse.pde.internal.ui.util.SWTUtil;
import org.eclipse.swt.custom.BusyIndicator;

public class ManifestEditorContributor extends PDEFormTextEditorContributor {

    private ExternalizeAction fExternalizeAction;

    class ExternalizeAction extends Action {

        public  ExternalizeAction() {
        }

        @Override
        public void run() {
            if (getEditor() != null) {
                BusyIndicator.showWhile(SWTUtil.getStandardDisplay(), new Runnable() {

                    @Override
                    public void run() {
                        GetNonExternalizedStringsAction externalizeAction = new GetNonExternalizedStringsAction();
                        IStructuredSelection selection = new StructuredSelection(getEditor().getCommonProject());
                        externalizeAction.runGetNonExternalizedStringsAction(selection);
                    }
                });
            }
        }
    }

    public  ManifestEditorContributor() {
        //$NON-NLS-1$
        super("&Plugin");
    }

    @Override
    public void contextMenuAboutToShow(IMenuManager mm, boolean addClipboard) {
        super.contextMenuAboutToShow(mm, addClipboard);
        IBaseModel model = getEditor().getAggregateModel();
        if (model != null && model.isEditable()) {
            mm.add(new Separator());
            mm.add(fExternalizeAction);
        }
    }

    @Override
    protected void makeActions() {
        super.makeActions();
        fExternalizeAction = new ExternalizeAction();
        fExternalizeAction.setText(PDEUIMessages.ManifestEditorContributor_externStringsActionName);
    }

    @Override
    public boolean supportsContentAssist() {
        return true;
    }

    @Override
    public boolean supportsFormatAction() {
        return true;
    }

    @Override
    public boolean supportsCorrectionAssist() {
        return true;
    }

    @Override
    public boolean supportsHyperlinking() {
        return true;
    }
}
