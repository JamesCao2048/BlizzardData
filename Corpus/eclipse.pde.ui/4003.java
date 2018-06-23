/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mickael Istria (Red Hat Inc.) - 482905 Project Explorer
 *******************************************************************************/
package org.eclipse.pde.internal.ui;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.pde.internal.ui.views.target.TargetStateView;
import org.eclipse.ui.*;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

public class PDEPerspective implements IPerspectiveFactory {

    private IPageLayout factory;

    public  PDEPerspective() {
        super();
    }

    @Override
    public void createInitialLayout(IPageLayout factory) {
        this.factory = factory;
        addViews();
    }

    private void addViews() {
        IFolderLayout topLeft = //$NON-NLS-1$
        factory.createFolder(//$NON-NLS-1$
        "topLeft", IPageLayout.LEFT, 0.25f, factory.getEditorArea());
        topLeft.addView(ProjectExplorer.VIEW_ID);
        topLeft.addPlaceholder(IPageLayout.ID_PROJECT_EXPLORER);
        topLeft.addPlaceholder(JavaUI.ID_TYPE_HIERARCHY);
        topLeft.addView(IPDEUIConstants.PLUGINS_VIEW_ID);
        IFolderLayout bottom = //$NON-NLS-1$
        factory.createFolder(//$NON-NLS-1$
        "bottomRight", IPageLayout.BOTTOM, 0.75f, factory.getEditorArea());
        bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
        bottom.addView(TargetStateView.VIEW_ID);
        factory.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, 0.75f, factory.getEditorArea());
        //$NON-NLS-1$
        factory.addNewWizardShortcut("org.eclipse.pde.ui.NewProjectWizard");
        //$NON-NLS-1$
        factory.addNewWizardShortcut("org.eclipse.pde.ui.NewFeatureProjectWizard");
    }
}
