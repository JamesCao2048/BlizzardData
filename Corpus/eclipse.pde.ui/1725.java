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
package org.eclipse.pde.internal.ui.editor.site;

import org.eclipse.jface.viewers.*;
import org.eclipse.pde.internal.ui.editor.*;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class SiteSourcePage extends XMLSourcePage {

    public  SiteSourcePage(PDEFormEditor editor, String id, String title) {
        super(editor, id, title);
    }

    public IContentOutlinePage createContentOutlinePage() {
        return null;
    }

    @Override
    public ILabelProvider createOutlineLabelProvider() {
        return null;
    }

    @Override
    public ITreeContentProvider createOutlineContentProvider() {
        return null;
    }

    @Override
    public ViewerComparator createOutlineComparator() {
        return null;
    }

    @Override
    public void updateSelection(SelectionChangedEvent e) {
    // NO-OP
    }

    @Override
    protected ISortableContentOutlinePage createOutlinePage() {
        // are implemented
        return new SiteOutlinePage((PDEFormEditor) getEditor());
    }

    @Override
    public boolean isQuickOutlineEnabled() {
        return false;
    }

    @Override
    public void updateSelection(Object object) {
    // NO-OP
    }
}
