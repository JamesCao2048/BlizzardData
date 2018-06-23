/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat Inc. - Support for <bundle...> tag
 *******************************************************************************/
package org.eclipse.pde.internal.core.site;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.Vector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IWritable;
import org.eclipse.pde.internal.core.isite.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Site extends SiteObject implements ISite {

    private static final long serialVersionUID = 1L;

    //$NON-NLS-1$
    static final String INDENT = "   ";

    private Vector<ISiteObject> features = new Vector();

    private Vector<ISiteObject> bundles = new Vector();

    private Vector<ISiteObject> archives = new Vector();

    private Vector<ISiteObject> categoryDefs = new Vector();

    private Vector<ISiteObject> repositoryReferences = new Vector();

    private String type;

    private String url;

    private String mirrorsUrl;

    private String digestUrl;

    private String associateSitesUrl;

    private ISiteDescription description;

    private IStatsInfo statsInfo;

    @Override
    public void setType(String type) throws CoreException {
        ensureModelEditable();
        Object oldValue = this.type;
        this.type = type;
        firePropertyChanged(P_TYPE, oldValue, type);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setURL(String url) throws CoreException {
        ensureModelEditable();
        Object oldValue = this.url;
        this.url = url;
        firePropertyChanged(P_URL, oldValue, url);
    }

    @Override
    public String getURL() {
        return url;
    }

    @Override
    public void setDigestURL(String url) throws CoreException {
        ensureModelEditable();
        Object oldValue = this.digestUrl;
        this.digestUrl = url;
        firePropertyChanged(P_DIGEST_URL, oldValue, url);
    }

    @Override
    public String getDigestURL() {
        return digestUrl;
    }

    @Override
    public void setAssociateSitesURL(String url) throws CoreException {
        ensureModelEditable();
        Object oldValue = this.associateSitesUrl;
        this.associateSitesUrl = url;
        firePropertyChanged(P_ASSOCIATE_SITES_URL, oldValue, url);
    }

    @Override
    public String getAssociateSitesURL() {
        return associateSitesUrl;
    }

    @Override
    public void setMirrorsURL(String url) throws CoreException {
        ensureModelEditable();
        Object oldValue = this.mirrorsUrl;
        this.mirrorsUrl = url;
        firePropertyChanged(P_MIRRORS_URL, oldValue, url);
    }

    @Override
    public String getMirrorsURL() {
        return mirrorsUrl;
    }

    @Override
    public ISiteDescription getDescription() {
        return description;
    }

    @Override
    public void setDescription(ISiteDescription description) throws CoreException {
        ensureModelEditable();
        Object oldValue = this.description;
        this.description = description;
        firePropertyChanged(P_DESCRIPTION, oldValue, description);
    }

    @Override
    public void addFeatures(ISiteFeature[] newFeatures) throws CoreException {
        ensureModelEditable();
        for (int i = 0; i < newFeatures.length; i++) {
            ISiteFeature feature = newFeatures[i];
            ((SiteFeature) feature).setInTheModel(true);
            features.add(newFeatures[i]);
        }
        fireStructureChanged(newFeatures, IModelChangedEvent.INSERT);
    }

    @Override
    public void addBundles(ISiteBundle[] newBundles) throws CoreException {
        ensureModelEditable();
        for (int i = 0; i < newBundles.length; i++) {
            ISiteBundle bundle = newBundles[i];
            ((SiteBundle) bundle).setInTheModel(true);
            bundles.add(bundle);
        }
        fireStructureChanged(newBundles, IModelChangedEvent.INSERT);
    }

    @Override
    public void addArchives(ISiteArchive[] archs) throws CoreException {
        ensureModelEditable();
        for (int i = 0; i < archs.length; i++) {
            ISiteArchive archive = archs[i];
            ((SiteArchive) archive).setInTheModel(true);
            archives.add(archs[i]);
        }
        fireStructureChanged(archs, IModelChangedEvent.INSERT);
    }

    @Override
    public void addCategoryDefinitions(ISiteCategoryDefinition[] defs) throws CoreException {
        ensureModelEditable();
        for (int i = 0; i < defs.length; i++) {
            ISiteCategoryDefinition def = defs[i];
            ((SiteCategoryDefinition) def).setInTheModel(true);
            categoryDefs.add(defs[i]);
        }
        fireStructureChanged(defs, IModelChangedEvent.INSERT);
    }

    @Override
    public void addRepositoryReferences(IRepositoryReference[] repos) throws CoreException {
        ensureModelEditable();
        for (int i = 0; i < repos.length; i++) {
            IRepositoryReference repo = repos[i];
            ((RepositoryReference) repo).setInTheModel(true);
            repositoryReferences.add(repos[i]);
        }
        fireStructureChanged(repos, IModelChangedEvent.INSERT);
    }

    @Override
    public void removeFeatures(ISiteFeature[] newFeatures) throws CoreException {
        ensureModelEditable();
        for (int i = 0; i < newFeatures.length; i++) {
            ISiteFeature feature = newFeatures[i];
            ((SiteFeature) feature).setInTheModel(false);
            features.remove(newFeatures[i]);
        }
        fireStructureChanged(newFeatures, IModelChangedEvent.REMOVE);
    }

    @Override
    public void removeBundles(ISiteBundle[] newBundles) throws CoreException {
        ensureModelEditable();
        for (int i = 0; i < newBundles.length; i++) {
            ISiteBundle bundle = newBundles[i];
            ((SiteBundle) bundle).setInTheModel(false);
            bundles.remove(bundle);
        }
        fireStructureChanged(newBundles, IModelChangedEvent.REMOVE);
    }

    @Override
    public void removeArchives(ISiteArchive[] archs) throws CoreException {
        ensureModelEditable();
        for (int i = 0; i < archs.length; i++) {
            ISiteArchive archive = archs[i];
            ((SiteArchive) archive).setInTheModel(false);
            archives.remove(archs[i]);
        }
        fireStructureChanged(archs, IModelChangedEvent.REMOVE);
    }

    @Override
    public void removeCategoryDefinitions(ISiteCategoryDefinition[] defs) throws CoreException {
        ensureModelEditable();
        for (int i = 0; i < defs.length; i++) {
            ISiteCategoryDefinition def = defs[i];
            ((SiteCategoryDefinition) def).setInTheModel(false);
            categoryDefs.remove(defs[i]);
        }
        fireStructureChanged(defs, IModelChangedEvent.REMOVE);
    }

    @Override
    public void removeRepositoryReferences(IRepositoryReference[] repos) throws CoreException {
        ensureModelEditable();
        for (int i = 0; i < repos.length; i++) {
            IRepositoryReference repo = repos[i];
            ((RepositoryReference) repo).setInTheModel(false);
            repositoryReferences.remove(repos[i]);
        }
        fireStructureChanged(repos, IModelChangedEvent.REMOVE);
    }

    @Override
    public ISiteFeature[] getFeatures() {
        return features.toArray(new ISiteFeature[features.size()]);
    }

    @Override
    public ISiteBundle[] getBundles() {
        return bundles.toArray(new ISiteBundle[bundles.size()]);
    }

    @Override
    public ISiteArchive[] getArchives() {
        return archives.toArray(new ISiteArchive[archives.size()]);
    }

    @Override
    public ISiteCategoryDefinition[] getCategoryDefinitions() {
        return categoryDefs.toArray(new ISiteCategoryDefinition[categoryDefs.size()]);
    }

    @Override
    public IRepositoryReference[] getRepositoryReferences() {
        return repositoryReferences.toArray(new IRepositoryReference[repositoryReferences.size()]);
    }

    @Override
    public IStatsInfo getStatsInfo() {
        return statsInfo;
    }

    @Override
    public void setStatsInfo(IStatsInfo info) throws CoreException {
        ensureModelEditable();
        Object oldValue = this.statsInfo;
        this.statsInfo = info;
        firePropertyChanged(P_STATS, oldValue, info);
    }

    @Override
    protected void reset() {
        archives.clear();
        categoryDefs.clear();
        repositoryReferences.clear();
        features.clear();
        bundles.clear();
        description = null;
        type = null;
        url = null;
        mirrorsUrl = null;
        digestUrl = null;
        associateSitesUrl = null;
        statsInfo = null;
    }

    @Override
    protected void parse(Node node) {
        type = getNodeAttribute(node, P_TYPE);
        url = getNodeAttribute(node, P_URL);
        mirrorsUrl = getNodeAttribute(node, P_MIRRORS_URL);
        digestUrl = getNodeAttribute(node, P_DIGEST_URL);
        associateSitesUrl = getNodeAttribute(node, P_ASSOCIATE_SITES_URL);
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                parseChild(child);
            }
        }
    }

    protected void parseChild(Node child) {
        String tag = child.getNodeName().toLowerCase(Locale.ENGLISH);
        if (//$NON-NLS-1$
        tag.equals("feature")) {
            ISiteFeature feature = getModel().getFactory().createFeature();
            ((SiteFeature) feature).parse(child);
            ((SiteFeature) feature).setInTheModel(true);
            features.add(feature);
        } else if (//$NON-NLS-1$
        tag.equals("bundle")) {
            ISiteBundle bundle = getModel().getFactory().createBundle();
            ((SiteBundle) bundle).parse(child);
            ((SiteBundle) bundle).setInTheModel(true);
            bundles.add(bundle);
        } else if (//$NON-NLS-1$
        tag.equals("archive")) {
            ISiteArchive archive = getModel().getFactory().createArchive();
            ((SiteArchive) archive).parse(child);
            ((SiteArchive) archive).setInTheModel(true);
            archives.add(archive);
        } else if (//$NON-NLS-1$
        tag.equals("category-def")) {
            ISiteCategoryDefinition def = getModel().getFactory().createCategoryDefinition();
            ((SiteCategoryDefinition) def).parse(child);
            ((SiteCategoryDefinition) def).setInTheModel(true);
            categoryDefs.add(def);
        } else if (//$NON-NLS-1$
        tag.equals("repository-reference")) {
            IRepositoryReference ref = getModel().getFactory().createRepositoryReference();
            ((RepositoryReference) ref).parse(child);
            ((RepositoryReference) ref).setInTheModel(true);
            repositoryReferences.add(ref);
        } else if (//$NON-NLS-1$
        tag.equals("stats")) {
            IStatsInfo info = getModel().getFactory().createStatsInfo();
            ((StatsInfo) info).parse(child);
            ((StatsInfo) info).setInTheModel(true);
            statsInfo = info;
        } else if (tag.equals(P_DESCRIPTION)) {
            if (description != null)
                return;
            description = getModel().getFactory().createDescription(this);
            ((SiteDescription) description).parse(child);
            ((SiteDescription) description).setInTheModel(true);
        }
    }

    @Override
    public void restoreProperty(String name, Object oldValue, Object newValue) throws CoreException {
        if (name.equals(P_TYPE)) {
            setType(newValue != null ? newValue.toString() : null);
        } else if (name.equals(P_URL)) {
            setURL(newValue != null ? newValue.toString() : null);
        } else if (name.equals(P_MIRRORS_URL)) {
            setMirrorsURL(newValue != null ? newValue.toString() : null);
        } else if (name.equals(P_DIGEST_URL)) {
            setDigestURL(newValue != null ? newValue.toString() : null);
        } else if (name.equals(P_ASSOCIATE_SITES_URL)) {
            setAssociateSitesURL(newValue != null ? newValue.toString() : null);
        } else if (name.equals(P_DESCRIPTION) && newValue instanceof ISiteDescription) {
            setDescription((ISiteDescription) newValue);
        } else
            super.restoreProperty(name, oldValue, newValue);
    }

    @Override
    public void write(String indent, PrintWriter writer) {
        //$NON-NLS-1$
        writer.print(indent + "<site");
        String indent2 = indent + INDENT;
        String indenta = indent + INDENT + INDENT;
        writeIfDefined(indenta, writer, P_TYPE, getType());
        writeIfDefined(indenta, writer, P_URL, getURL());
        writeIfDefined(indenta, writer, P_MIRRORS_URL, getMirrorsURL());
        writeIfDefined(indenta, writer, P_DIGEST_URL, getDigestURL());
        writeIfDefined(indenta, writer, P_ASSOCIATE_SITES_URL, getAssociateSitesURL());
        //$NON-NLS-1$
        writer.println(">");
        if (description != null) {
            description.write(indent2, writer);
        }
        if (statsInfo != null) {
            statsInfo.write(indent2, writer);
        }
        writeChildren(indent2, features, writer);
        writeChildren(indent2, bundles, writer);
        writeChildren(indent2, archives, writer);
        writeChildren(indent2, categoryDefs, writer);
        writeChildren(indent2, repositoryReferences, writer);
        //$NON-NLS-1$
        writer.println(indent + "</site>");
    }

    @Override
    public boolean isValid() {
        for (int i = 0; i < features.size(); i++) {
            ISiteFeature feature = (ISiteFeature) features.get(i);
            if (!feature.isValid())
                return false;
        }
        for (int i = 0; i < bundles.size(); i++) {
            ISiteBundle bundle = (ISiteBundle) bundles.get(i);
            if (!bundle.isValid())
                return false;
        }
        for (int i = 0; i < archives.size(); i++) {
            ISiteArchive archive = (ISiteArchive) archives.get(i);
            if (!archive.isValid())
                return false;
        }
        for (int i = 0; i < categoryDefs.size(); i++) {
            ISiteCategoryDefinition def = (ISiteCategoryDefinition) categoryDefs.get(i);
            if (!def.isValid())
                return false;
        }
        for (int i = 0; i < repositoryReferences.size(); i++) {
            IRepositoryReference repo = (IRepositoryReference) repositoryReferences.get(i);
            if (!repo.isValid())
                return false;
        }
        return true;
    }

    private void writeChildren(String indent, Vector<ISiteObject> children, PrintWriter writer) {
        for (int i = 0; i < children.size(); i++) {
            IWritable writable = children.get(i);
            writable.write(indent, writer);
        }
    }

    private void writeIfDefined(String indent, PrintWriter writer, String attName, String attValue) {
        if (attValue == null || attValue.length() <= 0)
            return;
        writer.println();
        //$NON-NLS-1$ //$NON-NLS-2$
        writer.print(indent + attName + "=\"" + SiteObject.getWritableString(attValue) + "\"");
    }
}
