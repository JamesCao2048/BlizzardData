/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444808
 *******************************************************************************/
package org.eclipse.pde.internal.core.feature;

import java.io.PrintWriter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.w3c.dom.Node;

public class FeaturePlugin extends FeatureData implements IFeaturePlugin {

    private static final long serialVersionUID = 1L;

    private boolean fFragment;

    private String fVersion;

    private boolean fUnpack = true;

    public  FeaturePlugin() {
    }

    @Override
    protected void reset() {
        super.reset();
        fVersion = null;
        fFragment = false;
    }

    @Override
    public boolean isFragment() {
        return fFragment;
    }

    public IPluginBase getPluginBase() {
        if (id == null) {
            return null;
        }
        String version = getVersion();
        IPluginModelBase model = null;
        if (version == null || version.equals(ICoreConstants.DEFAULT_VERSION))
            model = PluginRegistry.findModel(id);
        else {
            ModelEntry entry = PluginRegistry.findEntry(id);
            // if no plug-ins match the id, entry == null
            if (entry != null) {
                IPluginModelBase bases[] = entry.getActiveModels();
                for (int i = 0; i < bases.length; i++) {
                    if (bases[i].getPluginBase().getVersion().equals(version)) {
                        model = bases[i];
                        break;
                    }
                }
            }
        }
        if (fFragment && model instanceof IFragmentModel)
            return model.getPluginBase();
        if (!fFragment && model instanceof IPluginModel)
            return model.getPluginBase();
        return null;
    }

    @Override
    public String getVersion() {
        return fVersion;
    }

    @Override
    public boolean isUnpack() {
        return fUnpack;
    }

    @Override
    public void setVersion(String version) throws CoreException {
        ensureModelEditable();
        Object oldValue = this.fVersion;
        this.fVersion = version;
        firePropertyChanged(this, P_VERSION, oldValue, version);
    }

    @Override
    public void setUnpack(boolean unpack) throws CoreException {
        ensureModelEditable();
        boolean oldValue = fUnpack;
        this.fUnpack = unpack;
        firePropertyChanged(this, P_UNPACK, Boolean.valueOf(oldValue), Boolean.valueOf(unpack));
    }

    @Override
    public void restoreProperty(String name, Object oldValue, Object newValue) throws CoreException {
        if (name.equals(P_VERSION)) {
            setVersion(newValue != null ? newValue.toString() : null);
        } else
            super.restoreProperty(name, oldValue, newValue);
    }

    public void setFragment(boolean fragment) throws CoreException {
        ensureModelEditable();
        this.fFragment = fragment;
    }

    @Override
    protected void parse(Node node) {
        super.parse(node);
        //$NON-NLS-1$
        fVersion = getNodeAttribute(node, "version");
        //$NON-NLS-1$
        String f = getNodeAttribute(node, "fragment");
        if (//$NON-NLS-1$
        f != null && f.equalsIgnoreCase("true"))
            fFragment = true;
        //$NON-NLS-1$
        String unpack = getNodeAttribute(node, "unpack");
        if (//$NON-NLS-1$
        unpack != null && unpack.equalsIgnoreCase("false"))
            fUnpack = false;
    }

    public void loadFrom(IPluginBase plugin) {
        id = plugin.getId();
        label = plugin.getTranslatedName();
        fVersion = plugin.getVersion();
        fFragment = plugin instanceof IFragment;
    }

    @Override
    public void write(String indent, PrintWriter writer) {
        //$NON-NLS-1$
        writer.print(indent + "<plugin");
        String indent2 = indent + Feature.INDENT + Feature.INDENT;
        writeAttributes(indent2, writer);
        if (getVersion() != null) {
            writer.println();
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.print(indent2 + "version=\"" + getVersion() + "\"");
        }
        if (isFragment()) {
            writer.println();
            //$NON-NLS-1$
            writer.print(indent2 + "fragment=\"true\"");
        }
        if (!isUnpack()) {
            writer.println();
            //$NON-NLS-1$
            writer.print(indent2 + "unpack=\"false\"");
        }
        //$NON-NLS-1$
        writer.println("/>");
    //writer.println(indent + "</plugin>");
    }

    @Override
    public String getLabel() {
        IPluginBase pluginBase = getPluginBase();
        if (pluginBase != null) {
            return pluginBase.getTranslatedName();
        }
        String name = super.getLabel();
        if (name == null)
            name = getId();
        return name;
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
