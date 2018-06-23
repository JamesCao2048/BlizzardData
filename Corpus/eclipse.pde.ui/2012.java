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
import java.io.Serializable;
import org.eclipse.pde.core.ISourceObject;
import org.eclipse.pde.core.IWritable;
import org.eclipse.pde.core.plugin.*;

public class ImportObject extends PluginReference implements IWritable, Serializable, IWritableDelimiter {

    private static final long serialVersionUID = 1L;

    private IPluginImport iimport;

    public  ImportObject() {
        super();
    }

    public  ImportObject(IPluginImport iimport) {
        super(iimport.getId());
        this.iimport = iimport;
    }

    public  ImportObject(IPluginImport iimport, IPlugin plugin) {
        super(plugin);
        this.iimport = iimport;
    }

    public IPluginImport getImport() {
        return iimport;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ImportObject) {
            ImportObject io = (ImportObject) object;
            if (iimport.equals(io.getImport()))
                return true;
        }
        return false;
    }

    @Override
    public void write(String indent, PrintWriter writer) {
        iimport.write(indent, writer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Class<T> key) {
        if (key.equals(ISourceObject.class)) {
            if (iimport instanceof ISourceObject)
                return (T) iimport;
        }
        return super.getAdapter(key);
    }

    @Override
    public void reconnect(IPluginModelBase model) {
        super.reconnect(model);
        // Field that has transient fields:  Import
        IPluginBase parent = model.getPluginBase();
        // is usually done by the '*Node' classes; but, it is the opposite here
        if (iimport instanceof PluginImport) {
            ((PluginImport) iimport).reconnect(model, parent);
        }
    }

    @Override
    public void writeDelimeter(PrintWriter writer) {
        // is usually done by the '*Node' classes; but, it is the opposite here
        if (iimport instanceof PluginImport) {
            ((PluginImport) iimport).writeDelimeter(writer);
        }
    }
}
