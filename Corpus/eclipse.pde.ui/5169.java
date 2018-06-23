/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Rafael Oliveira Nóbrega <rafael.oliveira@gmail.com> - bug 223738
 *******************************************************************************/
package org.eclipse.pde.internal.ds.core.text;

import java.io.Serializable;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.internal.core.text.DocumentObject;
import org.eclipse.pde.internal.ds.core.IDSComponent;
import org.eclipse.pde.internal.ds.core.IDSConstants;
import org.eclipse.pde.internal.ds.core.IDSModel;
import org.eclipse.pde.internal.ds.core.IDSObject;

/**
 * All modeled objects of a declarative services xml file must extend from this
 * abstract class.
 *
 * @since 3.4
 * @see DSModel
 * @see DSDocumentFactory
 */
public abstract class DSObject extends DocumentObject implements IDSConstants, Serializable, IDSObject {

    private static final long serialVersionUID = 1L;

    /**
	 * Constructs the DSObject and initializes its attributes.
	 *
	 * @param model
	 *            The model to associate with this DSObject
	 * @param tagName
	 *            The xml tag name for this object
	 */
    public  DSObject(DSModel model, String tagName) {
        super(model, tagName);
    }

    @Override
    protected String getAttributeIndent() {
        //$NON-NLS-1$
        return " ";
    }

    @Override
    protected String getContentIndent() {
        //$NON-NLS-1$
        return "";
    }

    @Override
    public abstract boolean canBeParent();

    public abstract boolean canAddChild(int objectType);

    @Override
    public IDSModel getModel() {
        final IModel sharedModel = getSharedModel();
        if (sharedModel instanceof DSModel) {
            return (DSModel) sharedModel;
        }
        return null;
    }

    @Override
    public IDSComponent getComponent() {
        final IDSModel model = getModel();
        if (model != null) {
            return model.getDSComponent();
        }
        return null;
    }

    @Override
    public abstract String getName();

    @Override
    public abstract int getType();
}
