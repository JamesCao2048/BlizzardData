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
package org.eclipse.pde.internal.ui.editor.feature;

import org.eclipse.jface.action.*;
import org.eclipse.pde.internal.ui.editor.PDEFormTextEditorContributor;
import org.eclipse.pde.internal.ui.wizards.site.OpenProjectWizardAction;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.IEditorPart;

public class FeatureEditorContributor extends PDEFormTextEditorContributor {

    private EditorBuildFeatureAction fBuildAction;

    private SynchronizeVersionsAction fSynchronizeAction;

    private OpenProjectWizardAction fNewSiteAction;

    public  FeatureEditorContributor() {
        //$NON-NLS-1$
        super("&Feature");
    }

    @Override
    public void contextMenuAboutToShow(IMenuManager mng) {
        super.contextMenuAboutToShow(mng);
        mng.add(new Separator());
        mng.add(fSynchronizeAction);
        mng.add(fBuildAction);
    }

    public Action getBuildAction() {
        return fBuildAction;
    }

    public Action getSynchronizeAction() {
        return fSynchronizeAction;
    }

    public Action getNewSiteAction() {
        return fNewSiteAction;
    }

    @Override
    protected void makeActions() {
        super.makeActions();
        fBuildAction = new EditorBuildFeatureAction();
        fSynchronizeAction = new SynchronizeVersionsAction();
        fNewSiteAction = new OpenProjectWizardAction();
    }

    @Override
    public void setActiveEditor(IEditorPart targetEditor) {
        super.setActiveEditor(targetEditor);
        if (targetEditor instanceof FeatureEditor) {
            fBuildAction.setActiveEditor((FeatureEditor) targetEditor);
            fSynchronizeAction.setActiveEditor((FeatureEditor) targetEditor);
        }
    }

    protected boolean hasKnownTypes(Clipboard clipboard) {
        return true;
    }
}
