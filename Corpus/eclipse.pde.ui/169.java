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
/*
 * Created on Jan 30, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.pde.internal.ui.editor.plugin.rows;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.internal.core.ischema.*;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.editor.FormLayoutFactory;
import org.eclipse.pde.internal.ui.editor.IContextPart;
import org.eclipse.pde.internal.ui.parts.ComboPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class ChoiceAttributeRow extends ExtensionAttributeRow {

    protected ComboPart combo;

    /**
	 * @param att
	 */
    public  ChoiceAttributeRow(IContextPart part, ISchemaAttribute att) {
        super(part, att);
    }

    @Override
    public void createContents(Composite parent, FormToolkit toolkit, int span) {
        super.createContents(parent, toolkit, span);
        createLabel(parent, toolkit);
        combo = new ComboPart();
        combo.createControl(parent, toolkit, SWT.READ_ONLY);
        ISchemaSimpleType type = getAttribute().getType();
        ISchemaRestriction restriction = type.getRestriction();
        if (restriction != null) {
            Object rchildren[] = restriction.getChildren();
            if (getUse() != ISchemaAttribute.REQUIRED)
                //$NON-NLS-1$
                combo.add("");
            for (int i = 0; i < rchildren.length; i++) {
                Object rchild = rchildren[i];
                if (rchild instanceof ISchemaEnumeration)
                    combo.add(((ISchemaEnumeration) rchild).getName());
            }
        }
        GridData gd = new GridData(span == 2 ? GridData.FILL_HORIZONTAL : GridData.HORIZONTAL_ALIGN_FILL);
        gd.widthHint = 20;
        gd.horizontalSpan = span - 1;
        gd.horizontalIndent = FormLayoutFactory.CONTROL_HORIZONTAL_INDENT;
        combo.getControl().setLayoutData(gd);
        combo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!blockNotification)
                    markDirty();
            }
        });
        combo.getControl().setEnabled(part.isEditable());
    }

    @Override
    protected void update() {
        blockNotification = true;
        String value = getValue();
        if (value != null && isValid(value))
            combo.setText(value);
        else if (getUse() == ISchemaAttribute.REQUIRED)
            combo.setText(getValidValue());
        else
            //$NON-NLS-1$
            combo.setText("");
        blockNotification = false;
        dirty = false;
    }

    protected String getValidValue() {
        ISchemaAttribute attInfo = getAttribute();
        if (attInfo.getType().getRestriction() != null)
            return attInfo.getType().getRestriction().getChildren()[0].toString();
        //$NON-NLS-1$
        return "";
    }

    protected boolean isValid(String value) {
        if (//$NON-NLS-1$
        getAttribute().getUse() != ISchemaAttribute.REQUIRED && value.equals(""))
            return true;
        ISchemaRestriction restriction = getAttribute().getType().getRestriction();
        if (restriction == null)
            return true;
        Object[] children = restriction.getChildren();
        for (int i = 0; i < children.length; i++) {
            Object rchild = children[i];
            if (rchild instanceof ISchemaEnumeration && ((ISchemaEnumeration) rchild).getName().equals(value))
                return true;
        }
        return false;
    }

    @Override
    public void commit() {
        if (dirty && input != null) {
            try {
                String selection = combo.getSelection();
                if (selection.length() == 0)
                    selection = null;
                input.setAttribute(getName(), selection);
                dirty = false;
            } catch (CoreException e) {
                PDEPlugin.logException(e);
            }
        }
    }

    @Override
    public void setFocus() {
        combo.getControl().setFocus();
    }
}
