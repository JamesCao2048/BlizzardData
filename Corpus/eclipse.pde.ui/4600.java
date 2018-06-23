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
package org.eclipse.pde.internal.core.plugin;

import java.io.PrintWriter;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginExtensionPoint;

public class Extensions extends AbstractExtensions {

    private static final long serialVersionUID = 1L;

    private Boolean fValid;

    private boolean fIsFragment;

    public  Extensions(boolean readOnly) {
        super(readOnly);
    }

    void load(Extensions srcPluginBase) {
        super.load(srcPluginBase);
    }

    void load(String schemaVersion) {
        fSchemaVersion = schemaVersion;
    }

    @Override
    public void reset() {
        super.reset();
        fValid = null;
    }

    @Override
    public boolean isValid() {
        if (fValid == null) {
            fValid = Boolean.valueOf(hasRequiredAttributes());
        }
        return fValid.booleanValue();
    }

    @Override
    public void write(String indent, PrintWriter writer) {
        //$NON-NLS-1$
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        if (fSchemaVersion != null)
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.println("<?eclipse version=\"" + fSchemaVersion + "\"?>");
        //$NON-NLS-1$ //$NON-NLS-2$
        writer.println(fIsFragment ? "<fragment>" : "<plugin>");
        //$NON-NLS-1$
        String firstIndent = "   ";
        Object[] children = getExtensionPoints();
        if (children.length > 0)
            writer.println();
        for (int i = 0; i < children.length; i++) {
            ((IPluginExtensionPoint) children[i]).write(firstIndent, writer);
        }
        // add extensions
        children = getExtensions();
        if (children.length > 0)
            writer.println();
        for (int i = 0; i < children.length; i++) {
            ((IPluginExtension) children[i]).write(firstIndent, writer);
        }
        writer.println();
        //$NON-NLS-1$ //$NON-NLS-2$
        writer.println(fIsFragment ? "</fragment>" : "</plugin>");
    }

    public void setIsFragment(boolean isFragment) {
        fIsFragment = isFragment;
    }
}
