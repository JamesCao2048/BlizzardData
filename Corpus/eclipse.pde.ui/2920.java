/*******************************************************************************
 *  Copyright (c) 2005, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.builders;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

public class PluginErrorReporter extends PluginBaseErrorReporter {

    public  PluginErrorReporter(IFile file) {
        super(file);
    }

    @Override
    protected void validateTopLevelAttributes(Element element) {
        super.validateTopLevelAttributes(element);
        //$NON-NLS-1$
        Attr attr = element.getAttributeNode("class");
        if (attr != null)
            validateJavaAttribute(element, attr);
    }

    @Override
    protected String getRootElementName() {
        //$NON-NLS-1$
        return "plugin";
    }
}
