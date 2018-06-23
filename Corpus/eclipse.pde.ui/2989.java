/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.feature;

import java.io.PrintWriter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ifeature.*;
import org.eclipse.pde.internal.core.util.VersionUtil;
import org.w3c.dom.Node;

public class FeatureImport extends VersionableObject implements IFeatureImport {

    private static final long serialVersionUID = 1L;

    private int fMatch = NONE;

    private int fIdMatch = PERFECT;

    private int fType = PLUGIN;

    private boolean fPatch = false;

    private String fFilter = null;

    public  FeatureImport() {
    }

    public IPlugin getPlugin() {
        if (id != null && fType == PLUGIN) {
            IPluginModelBase model = PluginRegistry.findModel(id);
            return model instanceof IPluginModel ? ((IPluginModel) model).getPlugin() : null;
        }
        return null;
    }

    @Override
    public IFeature getFeature() {
        if (id != null && fType == FEATURE) {
            return findFeature(id, getVersion(), fMatch);
        }
        return null;
    }

    private IFeature findFeature(IFeatureModel[] models, String id, String version, int match) {
        for (int i = 0; i < models.length; i++) {
            IFeatureModel model = models[i];
            IFeature feature = model.getFeature();
            String pid = feature.getId();
            String pversion = feature.getVersion();
            if (VersionUtil.compare(pid, pversion, id, version, match))
                return feature;
        }
        return null;
    }

    /**
	 * Finds a feature with the given ID and satisfying constraints
	 * of the version and the match.
	 * @param id
	 * @param version
	 * @param match
	 * @return IFeature or null
	 */
    public IFeature findFeature(String id, String version, int match) {
        IFeatureModel[] models = PDECore.getDefault().getFeatureModelManager().findFeatureModels(id);
        return findFeature(models, id, version, match);
    }

    @Override
    public int getIdMatch() {
        return fIdMatch;
    }

    @Override
    protected void reset() {
        super.reset();
        fPatch = false;
        fType = PLUGIN;
        fMatch = NONE;
        fIdMatch = PERFECT;
        fFilter = null;
    }

    @Override
    protected void parse(Node node) {
        super.parse(node);
        //$NON-NLS-1$
        this.id = getNodeAttribute(node, "plugin");
        if (id != null)
            fType = PLUGIN;
        else {
            //$NON-NLS-1$
            this.id = getNodeAttribute(node, "feature");
            if (id != null)
                fType = FEATURE;
        }
        //$NON-NLS-1$
        String mvalue = getNodeAttribute(node, "match");
        if (mvalue != null && mvalue.length() > 0) {
            String[] choices = RULE_NAME_TABLE;
            for (int i = 0; i < choices.length; i++) {
                if (mvalue.equalsIgnoreCase(choices[i])) {
                    fMatch = i;
                    break;
                }
            }
        }
        //$NON-NLS-1$
        mvalue = getNodeAttribute(node, "id-match");
        if (mvalue != null && mvalue.length() > 0) {
            if (mvalue.equalsIgnoreCase(RULE_PREFIX))
                fIdMatch = PREFIX;
        }
        //$NON-NLS-1$
        fPatch = getBooleanAttribute(node, "patch");
        //$NON-NLS-1$
        fFilter = getNodeAttribute(node, "filter");
    }

    public void loadFrom(IFeature feature) {
        reset();
        fType = FEATURE;
        id = feature.getId();
        version = feature.getVersion();
    }

    @Override
    public int getMatch() {
        return fMatch;
    }

    @Override
    public void setMatch(int match) throws CoreException {
        ensureModelEditable();
        Integer oldValue = Integer.valueOf(this.fMatch);
        this.fMatch = match;
        firePropertyChanged(P_MATCH, oldValue, Integer.valueOf(match));
    }

    @Override
    public void setIdMatch(int idMatch) throws CoreException {
        ensureModelEditable();
        Integer oldValue = Integer.valueOf(this.fIdMatch);
        this.fIdMatch = idMatch;
        firePropertyChanged(P_ID_MATCH, oldValue, Integer.valueOf(idMatch));
    }

    @Override
    public int getType() {
        return fType;
    }

    @Override
    public void setType(int type) throws CoreException {
        ensureModelEditable();
        Integer oldValue = Integer.valueOf(this.fType);
        this.fType = type;
        firePropertyChanged(P_TYPE, oldValue, Integer.valueOf(type));
    }

    @Override
    public boolean isPatch() {
        return fPatch;
    }

    @Override
    public void setPatch(boolean patch) throws CoreException {
        ensureModelEditable();
        Boolean oldValue = Boolean.valueOf(this.fPatch);
        this.fPatch = patch;
        firePropertyChanged(P_PATCH, oldValue, Boolean.valueOf(patch));
    }

    @Override
    public void restoreProperty(String name, Object oldValue, Object newValue) throws CoreException {
        if (name.equals(P_MATCH)) {
            setMatch(newValue != null ? ((Integer) newValue).intValue() : 0);
        } else if (name.equals(P_ID_MATCH)) {
            setIdMatch(newValue != null ? ((Integer) newValue).intValue() : 0);
        } else if (name.equals(P_TYPE)) {
            setType(newValue != null ? ((Integer) newValue).intValue() : PLUGIN);
        } else if (name.equals(P_PATCH)) {
            setPatch(newValue != null ? ((Boolean) newValue).booleanValue() : false);
        } else
            super.restoreProperty(name, oldValue, newValue);
    }

    @Override
    public void write(String indent, PrintWriter writer) {
        //$NON-NLS-1$ //$NON-NLS-2$
        String typeAtt = fType == FEATURE ? "feature" : "plugin";
        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        writer.print(indent + "<import " + typeAtt + "=\"" + getId() + "\"");
        String version = getVersion();
        if (version != null && version.length() > 0) {
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.print(" version=\"" + version + "\"");
        }
        if (!fPatch && fMatch != NONE) {
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.print(" match=\"" + RULE_NAME_TABLE[fMatch] + "\"");
        }
        if (fIdMatch == PREFIX) {
            //$NON-NLS-1$
            writer.print(" id-match=\"prefix\"");
        }
        if (fPatch) {
            //$NON-NLS-1$
            writer.print(" patch=\"true\"");
        }
        if (fFilter != null) {
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.print(" filter=\"" + fFilter + "\"");
        }
        //$NON-NLS-1$
        writer.println("/>");
    }

    @Override
    public String toString() {
        IPlugin plugin = getPlugin();
        if (plugin != null)
            return plugin.getTranslatedName();
        IFeature feature = getFeature();
        if (feature != null)
            return feature.getLabel();
        return getId();
    }

    @Override
    public String getFilter() {
        return fFilter;
    }

    @Override
    public void setFilter(String filter) throws CoreException {
        this.fFilter = filter;
    }
}
