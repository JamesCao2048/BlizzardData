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

import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.pde.internal.core.ischema.*;
import org.eclipse.pde.internal.ui.editor.ModelDataTransfer;
import org.eclipse.swt.dnd.TransferData;

public class ElementSectionDropAdapter extends ViewerDropAdapter {

    private TransferData fCurrentTransfer;

    private ElementSection fSsection;

    private ElementSectionDragAdapter fDragAdapter;

    public  ElementSectionDropAdapter(ElementSectionDragAdapter dragAdapter, ElementSection section) {
        super(section.getTreeViewer());
        fSsection = section;
        fDragAdapter = dragAdapter;
    }

    /**
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
	 */
    @Override
    public boolean performDrop(Object data) {
        fSsection.handleOp(getCurrentTarget(), fDragAdapter.getDragData(), getCurrentOperation());
        return true;
    }

    /**
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 */
    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        fCurrentTransfer = transferType;
        if (!ModelDataTransfer.getInstance().isSupportedType(fCurrentTransfer))
            return false;
        Object cargo = getSelectedObject();
        if (// dropping an element reference
        cargo instanceof ISchemaObjectReference) {
            // onto a compositor or reference
            if ((target instanceof ISchemaCompositor || target instanceof ISchemaObjectReference))
                return true;
        } else if (// dropping an element
        cargo instanceof ISchemaElement) {
            // onto a non referenced element
            if (target instanceof ISchemaCompositor || target instanceof ISchemaObjectReference || isNonRefElement(target) || target == null)
                return true;
        } else if (// dropping a compositor
        cargo instanceof ISchemaCompositor) {
            // onto a non referenced element
            if (isNonRefElement(target) || target instanceof ISchemaCompositor || target instanceof ISchemaObjectReference)
                return true;
        } else if (// dropping an attribute
        cargo instanceof ISchemaAttribute) {
            // onto a non referenced element or attribute
            if (isNonRefElement(target) || target instanceof ISchemaAttribute)
                return true;
        }
        return false;
    }

    private boolean isNonRefElement(Object obj) {
        return (obj instanceof ISchemaElement && !(obj instanceof ISchemaObjectReference));
    }
}
