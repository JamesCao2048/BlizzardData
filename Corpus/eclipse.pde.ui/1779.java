/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.correction;

import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.core.text.bundle.BundleModel;
import org.eclipse.pde.internal.ui.PDEUIMessages;

public class CreateJREBundleHeaderResolution extends AbstractManifestMarkerResolution {

    public  CreateJREBundleHeaderResolution(int type) {
        super(type);
    }

    @Override
    public String getDescription() {
        return PDEUIMessages.CreateJREBundleHeaderResolution_desc;
    }

    @Override
    public String getLabel() {
        return PDEUIMessages.CreateJREBundleHeaderResolution_label;
    }

    @Override
    protected void createChange(BundleModel model) {
        model.getBundle().setHeader(ICoreConstants.ECLIPSE_JREBUNDLE, Boolean.toString(true));
    }
}
