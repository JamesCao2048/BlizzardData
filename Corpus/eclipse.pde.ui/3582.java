/*******************************************************************************
* Copyright (c) 2009, 2015 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.pde.internal.ui.editor.category;

import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.context.InputContext;
import org.eclipse.pde.internal.ui.editor.context.InputContextManager;

public class CategoryInputContextManager extends InputContextManager {

    /**
	 *
	 */
    public  CategoryInputContextManager(PDEFormEditor editor) {
        super(editor);
    }

    @Override
    public IBaseModel getAggregateModel() {
        return findSiteModel();
    }

    private IBaseModel findSiteModel() {
        InputContext scontext = findContext(CategoryInputContext.CONTEXT_ID);
        return (scontext != null) ? scontext.getModel() : null;
    }
}
