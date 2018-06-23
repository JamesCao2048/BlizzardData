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
package org.eclipse.pde.internal.ui.elements;

import org.eclipse.swt.graphics.Image;

public abstract class DefaultElement implements IPDEElement {

    @Override
    public Object[] getChildren() {
        return null;
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public String getLabel() {
        //$NON-NLS-1$
        return "";
    }

    @Override
    public Object getParent() {
        return null;
    }
}
