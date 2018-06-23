/*******************************************************************************
 *  Copyright (c) 2012, 2015 Christian Pontesegger and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     Christian Pontesegger - initial API and implementation
 *     IBM Corporation - bugs fixing
 *******************************************************************************/
package org.eclipse.pde.internal.ui.views.imagebrowser.filter;

import java.util.regex.Pattern;
import org.eclipse.pde.internal.ui.views.imagebrowser.ImageElement;

public class PatternFilter implements IFilter {

    private final Pattern mPattern;

    public  PatternFilter(final String pattern) {
        mPattern = Pattern.compile(pattern);
    }

    @Override
    public boolean accept(final ImageElement element) {
        //$NON-NLS-1$
        return mPattern.matcher(element.getPlugin() + "/" + element.getPath()).matches();
    }

    @Override
    public String toString() {
        //$NON-NLS-1$
        return "match " + mPattern.pattern();
    }
}
