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
package org.eclipse.pde.internal.ui.editor.schema;

import org.eclipse.jface.action.*;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.PDEFormTextEditorContributor;
import org.eclipse.pde.internal.ui.util.SWTUtil;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.Clipboard;

public class SchemaEditorContributor extends PDEFormTextEditorContributor {

    private PreviewAction fPreviewAction;

    class PreviewAction extends Action {

        public  PreviewAction() {
        }

        @Override
        public void run() {
            if (getEditor() != null) {
                final SchemaEditor schemaEditor = (SchemaEditor) getEditor();
                BusyIndicator.showWhile(SWTUtil.getStandardDisplay(), new Runnable() {

                    @Override
                    public void run() {
                        schemaEditor.previewReferenceDocument();
                    }
                });
            }
        }
    }

    public  SchemaEditorContributor() {
        //$NON-NLS-1$
        super("&Schema");
    }

    protected boolean hasKnownTypes(Clipboard clipboard) {
        return true;
    }

    @Override
    public void contextMenuAboutToShow(IMenuManager mm, boolean addClipboard) {
        super.contextMenuAboutToShow(mm, addClipboard);
        mm.add(new Separator());
        mm.add(fPreviewAction);
    }

    public Action getPreviewAction() {
        return fPreviewAction;
    }

    @Override
    protected void makeActions() {
        super.makeActions();
        fPreviewAction = new PreviewAction();
        fPreviewAction.setText(PDEUIMessages.SchemaEditorContributor_previewAction);
    }
}
