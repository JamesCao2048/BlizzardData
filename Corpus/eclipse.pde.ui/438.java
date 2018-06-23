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
package org.eclipse.pde.internal.ui.util;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.*;

/**
 * An OverlayIcon consists of a main icon and several adornments.
 */
public class ImageOverlayIcon extends AbstractOverlayIcon {

    private Image base;

    public  ImageOverlayIcon(Image base, ImageDescriptor[][] overlays) {
        this(base, overlays, null);
    }

    public  ImageOverlayIcon(Image base, ImageDescriptor[][] overlays, Point size) {
        super(overlays, size);
        this.base = base;
    }

    @Override
    protected ImageData getBaseImageData() {
        return base.getImageData();
    }
}
