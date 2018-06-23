/*******************************************************************************
 *  Copyright (c) 2000, 2013 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core;

import java.io.File;
import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.osgi.service.resolver.*;
import org.eclipse.pde.core.IClasspathContributor;
import org.eclipse.pde.core.build.IBuild;
import org.eclipse.pde.core.build.IBuildEntry;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.build.IBuildPropertiesConstants;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;

public class RequiredPluginsClasspathContainer extends PDEClasspathContainer implements IClasspathContainer {

    private IPluginModelBase fModel;

    private IBuild fBuild;

    private IClasspathEntry[] fEntries = null;

    /**
	 * Cached list of {@link IClasspathContributor} from plug-in extensions
	 * @see #getClasspathContributors()
	 */
    private static List<IClasspathContributor> fClasspathContributors = null;

    /**
	 * Constructor for RequiredPluginsClasspathContainer.
	 */
    public  RequiredPluginsClasspathContainer(IPluginModelBase model) {
        this(model, null);
    }

    public  RequiredPluginsClasspathContainer(IPluginModelBase model, IBuild build) {
        fModel = model;
        fBuild = build;
    }

    @Override
    public int getKind() {
        return K_APPLICATION;
    }

    @Override
    public IPath getPath() {
        return PDECore.REQUIRED_PLUGINS_CONTAINER_PATH;
    }

    @Override
    public String getDescription() {
        return PDECoreMessages.RequiredPluginsClasspathContainer_description;
    }

    @Override
    public IClasspathEntry[] getClasspathEntries() {
        if (fModel == null) {
            if (PDECore.DEBUG_CLASSPATH) {
                //$NON-NLS-1$
                System.out.println(//$NON-NLS-1$
                "********Returned an empty container");
            }
            return new IClasspathEntry[0];
        }
        if (fEntries == null) {
            fEntries = computePluginEntries();
        }
        if (PDECore.DEBUG_CLASSPATH) {
            //$NON-NLS-1$ //$NON-NLS-2$
            System.out.println("Dependencies for plugin '" + fModel.getPluginBase().getId() + "':");
            for (int i = 0; i < fEntries.length; i++) {
                //$NON-NLS-1$
                System.out.println(//$NON-NLS-1$
                "\t" + fEntries[i]);
            }
        }
        return fEntries;
    }

    private IClasspathEntry[] computePluginEntries() {
        ArrayList<IClasspathEntry> entries = new ArrayList();
        try {
            BundleDescription desc = fModel.getBundleDescription();
            if (desc == null)
                return new IClasspathEntry[0];
            Map<BundleDescription, ArrayList<Rule>> map = retrieveVisiblePackagesFromState(desc);
            // Add any library entries contributed via classpath contributor extension (Bug 363733)
            for (IClasspathContributor cc : getClasspathContributors()) {
                List<IClasspathEntry> classpathEntries = cc.getInitialEntries(desc);
                if (classpathEntries == null || classpathEntries.isEmpty()) {
                    continue;
                }
                entries.addAll(classpathEntries);
            }
            HashSet<BundleDescription> added = new HashSet();
            // to avoid cycles, e.g. when a bundle imports a package it exports
            added.add(desc);
            HostSpecification host = desc.getHost();
            if (host != null) {
                addHostPlugin(host, added, map, entries);
            } else if (//$NON-NLS-1$ //$NON-NLS-2$
            "true".equals(System.getProperty("pde.allowCycles"))) {
                BundleDescription[] fragments = desc.getFragments();
                for (int i = 0; i < fragments.length; i++) {
                    if (fragments[i].isResolved()) {
                        addPlugin(fragments[i], false, map, entries);
                    }
                }
            }
            // add dependencies
            BundleSpecification[] required = desc.getRequiredBundles();
            for (int i = 0; i < required.length; i++) {
                addDependency((BundleDescription) required[i].getSupplier(), added, map, entries);
            }
            if (fBuild == null)
                fBuild = ClasspathUtilCore.getBuild(fModel);
            if (fBuild != null)
                addSecondaryDependencies(desc, added, entries);
            // add Import-Package
            // sort by symbolicName_version to get a consistent order
            Map<String, BundleDescription> sortedMap = new TreeMap();
            Iterator<BundleDescription> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                BundleDescription bundle = iter.next();
                sortedMap.put(bundle.toString(), bundle);
            }
            iter = sortedMap.values().iterator();
            while (iter.hasNext()) {
                BundleDescription bundle = iter.next();
                IPluginModelBase model = PluginRegistry.findModel(bundle);
                if (model != null && model.isEnabled())
                    addDependencyViaImportPackage(model.getBundleDescription(), added, map, entries);
            }
            if (fBuild != null)
                addExtraClasspathEntries(added, entries);
        } catch (CoreException e) {
        }
        return entries.toArray(new IClasspathEntry[entries.size()]);
    }

    /**
	 * Return the list of {@link IClasspathContributor}s provided by the
	 * <code>org.eclipse.pde.core.pluginClasspathContributors</code> extension point.
	 * @return list of classpath contributors from the extension point
	 */
    private static synchronized List<IClasspathContributor> getClasspathContributors() {
        if (fClasspathContributors == null) {
            fClasspathContributors = new ArrayList();
            IExtensionRegistry registry = Platform.getExtensionRegistry();
            //$NON-NLS-1$
            IConfigurationElement[] elements = registry.getConfigurationElementsFor("org.eclipse.pde.core.pluginClasspathContributors");
            for (int i = 0; i < elements.length; i++) {
                try {
                    fClasspathContributors.add((IClasspathContributor) //$NON-NLS-1$
                    elements[i].createExecutableExtension(//$NON-NLS-1$
                    "class"));
                } catch (CoreException e) {
                    PDECore.log(e.getStatus());
                }
            }
        }
        return fClasspathContributors;
    }

    private Map<BundleDescription, ArrayList<Rule>> retrieveVisiblePackagesFromState(BundleDescription desc) {
        Map<BundleDescription, ArrayList<Rule>> visiblePackages = new HashMap();
        StateHelper helper = Platform.getPlatformAdmin().getStateHelper();
        addVisiblePackagesFromState(helper, desc, visiblePackages);
        if (desc.getHost() != null)
            addVisiblePackagesFromState(helper, (BundleDescription) desc.getHost().getSupplier(), visiblePackages);
        return visiblePackages;
    }

    private void addVisiblePackagesFromState(StateHelper helper, BundleDescription desc, Map<BundleDescription, ArrayList<Rule>> visiblePackages) {
        if (desc == null)
            return;
        ExportPackageDescription[] exports = helper.getVisiblePackages(desc);
        for (int i = 0; i < exports.length; i++) {
            BundleDescription exporter = exports[i].getExporter();
            if (exporter == null)
                continue;
            ArrayList<Rule> list = visiblePackages.get(exporter);
            if (list == null) {
                list = new ArrayList();
                visiblePackages.put(exporter, list);
            }
            Rule rule = getRule(helper, desc, exports[i]);
            if (!list.contains(rule))
                list.add(rule);
        }
    }

    private Rule getRule(StateHelper helper, BundleDescription desc, ExportPackageDescription export) {
        Rule rule = new Rule();
        rule.discouraged = helper.getAccessCode(desc, export) == StateHelper.ACCESS_DISCOURAGED;
        String name = export.getName();
        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        rule.path = (name.equals(".")) ? new Path("*") : new Path(name.replaceAll("\\.", "/") + "/*");
        return rule;
    }

    protected void addDependencyViaImportPackage(BundleDescription desc, HashSet<BundleDescription> added, Map<BundleDescription, ArrayList<Rule>> map, ArrayList<IClasspathEntry> entries) throws CoreException {
        if (desc == null || !added.add(desc))
            return;
        addPlugin(desc, true, map, entries);
        if (hasExtensibleAPI(desc) && desc.getContainingState() != null) {
            BundleDescription[] fragments = desc.getFragments();
            for (int i = 0; i < fragments.length; i++) {
                if (fragments[i].isResolved())
                    addDependencyViaImportPackage(fragments[i], added, map, entries);
            }
        }
    }

    private void addDependency(BundleDescription desc, HashSet<BundleDescription> added, Map<BundleDescription, ArrayList<Rule>> map, ArrayList<IClasspathEntry> entries) throws CoreException {
        addDependency(desc, added, map, entries, true);
    }

    private void addDependency(BundleDescription desc, HashSet<BundleDescription> added, Map<BundleDescription, ArrayList<Rule>> map, ArrayList<IClasspathEntry> entries, boolean useInclusion) throws CoreException {
        if (desc == null || !added.add(desc))
            return;
        BundleDescription[] fragments = hasExtensibleAPI(desc) ? desc.getFragments() : new BundleDescription[0];
        // add fragment patches before host
        for (int i = 0; i < fragments.length; i++) {
            if (fragments[i].isResolved() && ClasspathUtilCore.isPatchFragment(fragments[i])) {
                addDependency(fragments[i], added, map, entries, useInclusion);
            }
        }
        addPlugin(desc, useInclusion, map, entries);
        // add fragments that are not patches after the host
        for (int i = 0; i < fragments.length; i++) {
            if (fragments[i].isResolved() && !ClasspathUtilCore.isPatchFragment(fragments[i])) {
                addDependency(fragments[i], added, map, entries, useInclusion);
            }
        }
        BundleSpecification[] required = desc.getRequiredBundles();
        for (int i = 0; i < required.length; i++) {
            if (required[i].isExported()) {
                addDependency((BundleDescription) required[i].getSupplier(), added, map, entries, useInclusion);
            }
        }
    }

    private boolean addPlugin(BundleDescription desc, boolean useInclusions, Map<BundleDescription, ArrayList<Rule>> map, ArrayList<IClasspathEntry> entries) throws CoreException {
        IPluginModelBase model = PluginRegistry.findModel(desc);
        if (model == null || !model.isEnabled())
            return false;
        IResource resource = model.getUnderlyingResource();
        Rule[] rules = useInclusions ? getInclusions(map, model) : null;
        BundleDescription hostBundle = fModel.getBundleDescription();
        if (desc == null)
            return false;
        // Add any library entries contributed via classpath contributor extension (Bug 363733)
        for (IClasspathContributor cc : getClasspathContributors()) {
            List<IClasspathEntry> classpathEntries = cc.getEntriesForDependency(hostBundle, desc);
            if (classpathEntries == null || classpathEntries.isEmpty()) {
                continue;
            }
            entries.addAll(classpathEntries);
        }
        if (resource != null) {
            addProjectEntry(resource.getProject(), rules, entries);
        } else {
            addExternalPlugin(model, rules, entries);
        }
        return true;
    }

    private Rule[] getInclusions(Map<BundleDescription, ArrayList<Rule>> map, IPluginModelBase model) {
        BundleDescription desc = model.getBundleDescription();
        if (//$NON-NLS-1$ //$NON-NLS-2$
        desc == null || "false".equals(System.getProperty("pde.restriction")) || !(fModel instanceof IBundlePluginModelBase) || TargetPlatformHelper.getTargetVersion() < 3.1)
            return null;
        Rule[] rules;
        if (desc.getHost() != null)
            rules = getInclusions(map, (BundleDescription) desc.getHost().getSupplier());
        else
            rules = getInclusions(map, desc);
        return (rules.length == 0 && !ClasspathUtilCore.hasBundleStructure(model)) ? null : rules;
    }

    private Rule[] getInclusions(Map<BundleDescription, ArrayList<Rule>> map, BundleDescription desc) {
        ArrayList<?> list = map.get(desc);
        return list != null ? (Rule[]) list.toArray(new Rule[list.size()]) : new Rule[0];
    }

    private void addHostPlugin(HostSpecification hostSpec, HashSet<BundleDescription> added, Map<BundleDescription, ArrayList<Rule>> map, ArrayList<IClasspathEntry> entries) throws CoreException {
        BaseDescription desc = hostSpec.getSupplier();
        if (desc instanceof BundleDescription) {
            BundleDescription host = (BundleDescription) desc;
            // add host plug-in
            if (added.add(host) && addPlugin(host, false, map, entries)) {
                BundleSpecification[] required = host.getRequiredBundles();
                for (int i = 0; i < required.length; i++) {
                    addDependency((BundleDescription) required[i].getSupplier(), added, map, entries);
                }
                // add Import-Package
                ImportPackageSpecification[] imports = host.getImportPackages();
                for (int i = 0; i < imports.length; i++) {
                    BaseDescription supplier = imports[i].getSupplier();
                    if (supplier instanceof ExportPackageDescription) {
                        addDependencyViaImportPackage(((ExportPackageDescription) supplier).getExporter(), added, map, entries);
                    }
                }
            }
        }
    }

    private boolean hasExtensibleAPI(BundleDescription desc) {
        IPluginModelBase model = PluginRegistry.findModel(desc);
        return model != null ? ClasspathUtilCore.hasExtensibleAPI(model) : false;
    }

    protected void addExtraClasspathEntries(HashSet<BundleDescription> added, ArrayList<IClasspathEntry> entries) {
        IBuildEntry[] buildEntries = fBuild.getBuildEntries();
        for (int i = 0; i < buildEntries.length; i++) {
            String name = buildEntries[i].getName();
            if (name.equals(IBuildPropertiesConstants.PROPERTY_JAR_EXTRA_CLASSPATH) || name.startsWith(IBuildPropertiesConstants.PROPERTY_EXTRAPATH_PREFIX)) {
                addExtraClasspathEntries(added, entries, buildEntries[i].getTokens());
            }
        }
    }

    protected void addExtraClasspathEntries(HashSet<BundleDescription> added, ArrayList<IClasspathEntry> entries, String[] tokens) {
        for (int i = 0; i < tokens.length; i++) {
            IPath path = Path.fromPortableString(tokens[i]);
            if (!path.isAbsolute()) {
                File file = new File(fModel.getInstallLocation(), path.toString());
                if (file.exists()) {
                    IFile resource = PDECore.getWorkspace().getRoot().getFileForLocation(new Path(file.getAbsolutePath()));
                    if (resource != null && resource.getProject().equals(fModel.getUnderlyingResource().getProject())) {
                        addExtraLibrary(resource.getFullPath(), null, entries);
                        continue;
                    }
                }
                if (//$NON-NLS-1$
                path.segmentCount() >= 3 && "..".equals(path.segment(0))) {
                    path = path.removeFirstSegments(1);
                    path = //$NON-NLS-1$
                    Path.fromPortableString("platform:/plugin/").append(//$NON-NLS-1$
                    path);
                } else {
                    continue;
                }
            }
            if (//$NON-NLS-1$
            !path.toPortableString().startsWith("platform:")) {
                addExtraLibrary(path, null, entries);
            } else {
                int count = path.getDevice() == null ? 4 : 3;
                if (path.segmentCount() >= count) {
                    String pluginID = path.segment(count - 2);
                    if (added.contains(pluginID))
                        continue;
                    IPluginModelBase model = PluginRegistry.findModel(pluginID);
                    if (model != null && model.isEnabled()) {
                        path = path.setDevice(null);
                        path = path.removeFirstSegments(count - 1);
                        if (model.getUnderlyingResource() == null) {
                            File file = new File(model.getInstallLocation(), path.toOSString());
                            if (file.exists()) {
                                addExtraLibrary(new Path(file.getAbsolutePath()), model, entries);
                            }
                        } else {
                            IProject project = model.getUnderlyingResource().getProject();
                            IFile file = project.getFile(path);
                            if (file.exists()) {
                                addExtraLibrary(file.getFullPath(), model, entries);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addSecondaryDependencies(BundleDescription desc, HashSet<BundleDescription> added, ArrayList<IClasspathEntry> entries) {
        try {
            IBuildEntry entry = fBuild.getEntry(IBuildEntry.SECONDARY_DEPENDENCIES);
            if (entry != null) {
                String[] tokens = entry.getTokens();
                for (int i = 0; i < tokens.length; i++) {
                    String pluginId = tokens[i];
                    // Get PluginModelBase first to resolve system.bundle entry if it exists
                    IPluginModelBase model = PluginRegistry.findModel(pluginId);
                    if (model != null) {
                        BundleDescription bundleDesc = model.getBundleDescription();
                        if (added.contains(bundleDesc))
                            continue;
                        Map<BundleDescription, ArrayList<Rule>> rules = new HashMap();
                        findExportedPackages(bundleDesc, desc, rules);
                        addDependency(bundleDesc, added, rules, entries, true);
                    }
                }
            }
        } catch (CoreException e) {
            return;
        }
    }

    protected final void findExportedPackages(BundleDescription desc, BundleDescription projectDesc, Map<BundleDescription, ArrayList<Rule>> map) {
        if (desc != null) {
            Stack<BaseDescription> stack = new Stack();
            stack.add(desc);
            while (!stack.isEmpty()) {
                BundleDescription bdesc = (BundleDescription) stack.pop();
                ExportPackageDescription[] expkgs = bdesc.getExportPackages();
                ArrayList<Rule> rules = new ArrayList();
                for (int i = 0; i < expkgs.length; i++) {
                    Rule rule = new Rule();
                    rule.discouraged = restrictPackage(projectDesc, expkgs[i]);
                    //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    rule.path = new Path(expkgs[i].getName().replaceAll("\\.", "/") + "/*");
                    rules.add(rule);
                }
                map.put(bdesc, rules);
                // Look at re-exported Require-Bundles for any other exported packages
                BundleSpecification[] requiredBundles = bdesc.getRequiredBundles();
                for (int i = 0; i < requiredBundles.length; i++) if (requiredBundles[i].isExported()) {
                    BaseDescription bd = requiredBundles[i].getSupplier();
                    if (bd != null && bd instanceof BundleDescription)
                        stack.add(bd);
                }
            }
        }
    }

    private boolean restrictPackage(BundleDescription desc, ExportPackageDescription pkg) {
        String[] friends = (String[]) pkg.getDirective(ICoreConstants.FRIENDS_DIRECTIVE);
        if (friends != null) {
            String symbolicName = desc.getSymbolicName();
            for (int i = 0; i < friends.length; i++) {
                if (symbolicName.equals(friends[i]))
                    return false;
            }
            return true;
        }
        return (((Boolean) pkg.getDirective(ICoreConstants.INTERNAL_DIRECTIVE)).booleanValue());
    }

    private void addExtraLibrary(IPath path, IPluginModelBase model, ArrayList<IClasspathEntry> entries) {
        if (path.segmentCount() > 1) {
            IPath srcPath = null;
            if (model != null) {
                IPath shortPath = path.removeFirstSegments(path.matchingFirstSegments(new Path(model.getInstallLocation())));
                srcPath = ClasspathUtilCore.getSourceAnnotation(model, shortPath.toString());
            } else {
                String filename = ClasspathUtilCore.getSourceZipName(path.lastSegment());
                IPath candidate = path.removeLastSegments(1).append(filename);
                if (PDECore.getWorkspace().getRoot().getFile(candidate).exists())
                    srcPath = candidate;
            }
            IClasspathEntry clsEntry = JavaCore.newLibraryEntry(path, srcPath, null);
            if (!entries.contains(clsEntry))
                entries.add(clsEntry);
        }
    }
}
