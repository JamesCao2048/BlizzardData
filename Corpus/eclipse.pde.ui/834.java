/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     EclipseSource Corporation - ongoing enhancements
 *******************************************************************************/
package org.eclipse.pde.internal.launching.launcher;

import java.util.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.build.IPDEBuildConstants;
import org.eclipse.pde.internal.core.*;
import org.eclipse.pde.internal.core.ifeature.*;
import org.eclipse.pde.internal.launching.IPDEConstants;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.osgi.framework.Version;

public class BundleLauncherHelper {

    /**
	 * When creating a mapping of bundles to their start levels, update configurator is set
	 * to auto start at level three.  However, if at launch time we are launching with both
	 * simple configurator and update configurator, we change the start level as they
	 * shouldn't be started together.
	 */
    //$NON-NLS-1$
    public static final String DEFAULT_UPDATE_CONFIGURATOR_START_LEVEL_TEXT = "3";

    //$NON-NLS-1$
    public static final String DEFAULT_UPDATE_CONFIGURATOR_AUTO_START_TEXT = "true";

    //$NON-NLS-1$
    public static final String DEFAULT_UPDATE_CONFIGURATOR_START_LEVEL = DEFAULT_UPDATE_CONFIGURATOR_START_LEVEL_TEXT + ":" + DEFAULT_UPDATE_CONFIGURATOR_AUTO_START_TEXT;

    public static final char VERSION_SEPARATOR = '*';

    public static Map<IPluginModelBase, String> getWorkspaceBundleMap(ILaunchConfiguration configuration) throws CoreException {
        return getWorkspaceBundleMap(configuration, null, IPDELauncherConstants.WORKSPACE_BUNDLES);
    }

    public static Map<IPluginModelBase, String> getTargetBundleMap(ILaunchConfiguration configuration) throws CoreException {
        return getTargetBundleMap(configuration, null, IPDELauncherConstants.TARGET_BUNDLES);
    }

    public static Map<IPluginModelBase, String> getMergedBundleMap(ILaunchConfiguration configuration, boolean osgi) throws CoreException {
        Set<String> set = new HashSet();
        Map<IPluginModelBase, String> map = new HashMap();
        // if we are using the eclipse-based launcher, we need special checks
        if (!osgi) {
            checkBackwardCompatibility(configuration, true);
            if (configuration.getAttribute(IPDELauncherConstants.USE_DEFAULT, true)) {
                IPluginModelBase[] models = PluginRegistry.getActiveModels();
                for (IPluginModelBase model : models) {
                    addBundleToMap(map, model, "default:default");
                }
                return map;
            }
        }
        if (configuration.getAttribute(IPDELauncherConstants.USE_CUSTOM_FEATURES, false)) {
            // Get the default location settings
            String defaultLocation = configuration.getAttribute(IPDELauncherConstants.FEATURE_DEFAULT_LOCATION, IPDELauncherConstants.LOCATION_WORKSPACE);
            String defaultPluginResolution = configuration.getAttribute(IPDELauncherConstants.FEATURE_PLUGIN_RESOLUTION, IPDELauncherConstants.LOCATION_WORKSPACE);
            // Get all available features
            HashMap<String, IFeatureModel> workspaceFeatureMap = new HashMap();
            HashMap<String, IFeatureModel> externalFeatureMap = new HashMap();
            FeatureModelManager fmm = PDECore.getDefault().getFeatureModelManager();
            IFeatureModel[] workspaceFeatureModels = fmm.getWorkspaceModels();
            for (IFeatureModel workspaceFeatureModel : workspaceFeatureModels) {
                String id = workspaceFeatureModel.getFeature().getId();
                workspaceFeatureMap.put(id, workspaceFeatureModel);
            }
            IFeatureModel[] externalFeatureModels = fmm.getExternalModels();
            for (IFeatureModel externalFeatureModel : externalFeatureModels) {
                String id = externalFeatureModel.getFeature().getId();
                externalFeatureMap.put(id, externalFeatureModel);
            }
            // Get the selected features and their plugin resolution
            Map<String, String> featureResolutionMap = new HashMap();
            Set<String> selectedFeatures = configuration.getAttribute(IPDELauncherConstants.SELECTED_FEATURES, (Set<String>) null);
            if (selectedFeatures != null) {
                for (String currentSelected : selectedFeatures) {
                    String[] attributes = //$NON-NLS-1$
                    currentSelected.split(//$NON-NLS-1$
                    ":");
                    if (attributes.length > 1) {
                        featureResolutionMap.put(attributes[0], attributes[1]);
                    }
                }
            }
            // Get the feature model for each selected feature id and resolve its plugins
            Set<IPluginModelBase> launchPlugins = new HashSet();
            for (String id : featureResolutionMap.keySet()) {
                IFeatureModel featureModel = null;
                if (IPDELauncherConstants.LOCATION_WORKSPACE.equalsIgnoreCase(defaultLocation)) {
                    featureModel = workspaceFeatureMap.get(id);
                }
                if (featureModel == null || IPDELauncherConstants.LOCATION_EXTERNAL.equalsIgnoreCase(defaultLocation)) {
                    if (externalFeatureMap.containsKey(id)) {
                        featureModel = externalFeatureMap.get(id);
                    }
                }
                if (featureModel == null) {
                    continue;
                }
                IFeaturePlugin[] featurePlugins = featureModel.getFeature().getPlugins();
                String pluginResolution = featureResolutionMap.get(id);
                if (IPDELauncherConstants.LOCATION_DEFAULT.equalsIgnoreCase(pluginResolution)) {
                    pluginResolution = defaultPluginResolution;
                }
                for (IFeaturePlugin featurePlugin : featurePlugins) {
                    ModelEntry modelEntry = PluginRegistry.findEntry(featurePlugin.getId());
                    if (modelEntry != null) {
                        IPluginModelBase model = findModel(modelEntry, featurePlugin.getVersion(), pluginResolution);
                        if (model != null)
                            launchPlugins.add(model);
                    }
                }
                IFeatureImport[] featureImports = featureModel.getFeature().getImports();
                for (IFeatureImport featureImport : featureImports) {
                    if (featureImport.getType() == IFeatureImport.PLUGIN) {
                        ModelEntry modelEntry = PluginRegistry.findEntry(featureImport.getId());
                        if (modelEntry != null) {
                            IPluginModelBase model = findModel(modelEntry, featureImport.getVersion(), pluginResolution);
                            if (model != null)
                                launchPlugins.add(model);
                        }
                    }
                }
            }
            HashMap<IPluginModelBase, String> additionalPlugins = getAdditionalPlugins(configuration, true);
            launchPlugins.addAll(additionalPlugins.keySet());
            // Get any plug-ins required by the application/product set on the config
            if (!osgi) {
                String[] applicationIds = RequirementHelper.getApplicationRequirements(configuration);
                for (String applicationId : applicationIds) {
                    ModelEntry modelEntry = PluginRegistry.findEntry(applicationId);
                    if (modelEntry != null) {
                        IPluginModelBase model = findModel(modelEntry, null, defaultPluginResolution);
                        if (model != null)
                            launchPlugins.add(model);
                    }
                }
            }
            // Get all required plugins
            Set<String> additionalIds = DependencyManager.getDependencies(launchPlugins.toArray(), false, null);
            Iterator<String> it = additionalIds.iterator();
            while (it.hasNext()) {
                String id = it.next();
                ModelEntry modelEntry = PluginRegistry.findEntry(id);
                if (modelEntry != null) {
                    IPluginModelBase model = findModel(modelEntry, null, defaultPluginResolution);
                    if (model != null)
                        launchPlugins.add(model);
                }
            }
            //remove conflicting duplicates - if they have same version or both are singleton
            HashMap<String, IPluginModelBase> pluginMap = new HashMap();
            Set<IPluginModelBase> pluginSet = new HashSet();
            List<IPluginModelBase> workspaceModels = null;
            for (IPluginModelBase model : launchPlugins) {
                String id = model.getPluginBase().getId();
                if (pluginMap.containsKey(id)) {
                    IPluginModelBase existing = pluginMap.get(id);
                    if (model.getPluginBase().getVersion().equalsIgnoreCase(existing.getPluginBase().getVersion()) || (isSingleton(model) && isSingleton(existing))) {
                        if (workspaceModels == null)
                            workspaceModels = Arrays.asList(PluginRegistry.getWorkspaceModels());
                        if (//if existing model is external
                        !workspaceModels.contains(existing)) {
                            // launch the workspace model
                            pluginSet.add(model);
                            continue;
                        }
                    }
                }
                pluginSet.add(model);
            }
            pluginMap.clear();
            // Create the start levels for the selected plugins and add them to the map
            for (IPluginModelBase model : pluginSet) {
                addBundleToMap(//$NON-NLS-1$
                map, //$NON-NLS-1$
                model, //$NON-NLS-1$
                "default:default");
            }
            return map;
        }
        String workspace = osgi == false ? IPDELauncherConstants.SELECTED_WORKSPACE_PLUGINS : IPDELauncherConstants.WORKSPACE_BUNDLES;
        String target = osgi == false ? IPDELauncherConstants.SELECTED_TARGET_PLUGINS : IPDELauncherConstants.TARGET_BUNDLES;
        map = getWorkspaceBundleMap(configuration, set, workspace);
        map.putAll(getTargetBundleMap(configuration, set, target));
        return map;
    }

    /**
	 * Finds the best candidate model from the <code>resolution</code> location. If the model is not found there,
	 * alternate location is explored before returning <code>null</code>.
	 * @param modelEntry
	 * @param version
	 * @param location
	 * @return model
	 */
    private static IPluginModelBase findModel(ModelEntry modelEntry, String version, String location) {
        IPluginModelBase model = null;
        if (IPDELauncherConstants.LOCATION_WORKSPACE.equalsIgnoreCase(location)) {
            model = getBestCandidateModel(modelEntry.getWorkspaceModels(), version);
        }
        if (model == null) {
            model = getBestCandidateModel(modelEntry.getExternalModels(), version);
        }
        if (model == null && IPDELauncherConstants.LOCATION_EXTERNAL.equalsIgnoreCase(location)) {
            model = getBestCandidateModel(modelEntry.getWorkspaceModels(), version);
        }
        return model;
    }

    private static boolean isSingleton(IPluginModelBase model) {
        if (model.getBundleDescription() == null || model.getBundleDescription().isSingleton()) {
            return true;
        }
        return false;
    }

    /**
	 * Returns model from the given list that is a 'best match' to the given bundle version or
	 * <code>null</code> if no enabled models were in the provided list.  The best match will
	 * be an exact version match if one is found.  Otherwise a model that is resolved in the
	 * OSGi state with the highest version is returned.
	 *
	 * @param models list of candidate models to choose from
	 * @param version the bundle version to find a match for
	 * @return best candidate model from the list of models or <code>null</code> if no there were no acceptable models in the list
	 */
    private static IPluginModelBase getBestCandidateModel(IPluginModelBase[] models, String version) {
        Version requiredVersion = version != null ? Version.parseVersion(version) : Version.emptyVersion;
        IPluginModelBase model = null;
        for (int i = 0; i < models.length; i++) {
            if (models[i].getBundleDescription() == null || !models[i].isEnabled())
                continue;
            if (model == null) {
                model = models[i];
                if (requiredVersion.compareTo(model.getBundleDescription().getVersion()) == 0) {
                    break;
                }
                continue;
            }
            if (!model.isEnabled() && models[i].isEnabled()) {
                model = models[i];
                continue;
            }
            BundleDescription current = model.getBundleDescription();
            BundleDescription candidate = models[i].getBundleDescription();
            if (current == null || candidate == null) {
                continue;
            }
            if (!current.isResolved() && candidate.isResolved()) {
                model = models[i];
                continue;
            }
            if (requiredVersion.compareTo(candidate.getVersion()) == 0) {
                model = models[i];
                break;
            }
            if (current.getVersion().compareTo(candidate.getVersion()) < 0) {
                model = models[i];
            }
        }
        return model;
    }

    public static IPluginModelBase[] getMergedBundles(ILaunchConfiguration configuration, boolean osgi) throws CoreException {
        Map<IPluginModelBase, String> map = getMergedBundleMap(configuration, osgi);
        return map.keySet().toArray(new IPluginModelBase[map.size()]);
    }

    public static Map<IPluginModelBase, String> getWorkspaceBundleMap(ILaunchConfiguration configuration, Set<String> set, String attribute) throws CoreException {
        //$NON-NLS-1$
        String selected = configuration.getAttribute(attribute, "");
        Map<IPluginModelBase, String> map = new HashMap();
        //$NON-NLS-1$
        StringTokenizer tok = new StringTokenizer(selected, ",");
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            int index = token.indexOf('@');
            if (// if no start levels, assume default
            index < 0) {
                token = //$NON-NLS-1$
                token.concat(//$NON-NLS-1$
                "@default:default");
                index = token.indexOf('@');
            }
            String idVersion = token.substring(0, index);
            int versionIndex = idVersion.indexOf(VERSION_SEPARATOR);
            String id = (versionIndex > 0) ? idVersion.substring(0, versionIndex) : idVersion;
            String version = (versionIndex > 0) ? idVersion.substring(versionIndex + 1) : null;
            if (set != null)
                set.add(id);
            ModelEntry entry = PluginRegistry.findEntry(id);
            if (entry != null) {
                IPluginModelBase[] models = entry.getWorkspaceModels();
                Set<String> versions = new HashSet();
                for (IPluginModelBase model : models) {
                    IPluginBase base = model.getPluginBase();
                    String v = base.getVersion();
                    if (// don't add exact same version more than once
                    versions.add(v)) {
                        // c) all else fails, if there's just one bundle available, use it
                        if (base.getVersion().equals(version) || version == null || models.length == 1)
                            addBundleToMap(map, model, token.substring(index + 1));
                    }
                }
            }
        }
        if (configuration.getAttribute(IPDELauncherConstants.AUTOMATIC_ADD, true)) {
            Set<IPluginModelBase> deselectedPlugins = LaunchPluginValidator.parsePlugins(configuration, IPDELauncherConstants.DESELECTED_WORKSPACE_PLUGINS);
            IPluginModelBase[] models = PluginRegistry.getWorkspaceModels();
            for (int i = 0; i < models.length; i++) {
                String id = models[i].getPluginBase().getId();
                if (id == null)
                    continue;
                if (!deselectedPlugins.contains(models[i])) {
                    if (set != null)
                        set.add(id);
                    if (!map.containsKey(models[i]))
                        addBundleToMap(//$NON-NLS-1$
                        map, //$NON-NLS-1$
                        models[i], //$NON-NLS-1$
                        "default:default");
                }
            }
        }
        return map;
    }

    public static String resolveSystemRunLevelText(IPluginModelBase model) {
        BundleDescription description = model.getBundleDescription();
        String modelName = description.getSymbolicName();
        if (IPDEBuildConstants.BUNDLE_DS.equals(modelName)) {
            //$NON-NLS-1$
            return "1";
        } else if (IPDEBuildConstants.BUNDLE_SIMPLE_CONFIGURATOR.equals(modelName)) {
            //$NON-NLS-1$
            return "1";
        } else if (IPDEBuildConstants.BUNDLE_EQUINOX_COMMON.equals(modelName)) {
            //$NON-NLS-1$
            return "2";
        } else if (IPDEBuildConstants.BUNDLE_OSGI.equals(modelName)) {
            //$NON-NLS-1$
            return "-1";
        } else if (IPDEBuildConstants.BUNDLE_UPDATE_CONFIGURATOR.equals(modelName)) {
            return DEFAULT_UPDATE_CONFIGURATOR_START_LEVEL_TEXT;
        } else if (IPDEBuildConstants.BUNDLE_CORE_RUNTIME.equals(modelName)) {
            if (TargetPlatformHelper.getTargetVersion() > 3.1) {
                return //$NON-NLS-1$
                "default";
            }
            //$NON-NLS-1$
            return "2";
        } else {
            return null;
        }
    }

    public static String resolveSystemAutoText(IPluginModelBase model) {
        BundleDescription description = model.getBundleDescription();
        String modelName = description.getSymbolicName();
        if (IPDEBuildConstants.BUNDLE_DS.equals(modelName)) {
            //$NON-NLS-1$
            return "true";
        } else if (IPDEBuildConstants.BUNDLE_SIMPLE_CONFIGURATOR.equals(modelName)) {
            //$NON-NLS-1$
            return "true";
        } else if (IPDEBuildConstants.BUNDLE_EQUINOX_COMMON.equals(modelName)) {
            //$NON-NLS-1$
            return "true";
        } else if (IPDEBuildConstants.BUNDLE_OSGI.equals(modelName)) {
            //$NON-NLS-1$
            return "true";
        } else if (IPDEBuildConstants.BUNDLE_UPDATE_CONFIGURATOR.equals(modelName)) {
            return DEFAULT_UPDATE_CONFIGURATOR_AUTO_START_TEXT;
        } else if (IPDEBuildConstants.BUNDLE_CORE_RUNTIME.equals(modelName)) {
            if (TargetPlatformHelper.getTargetVersion() > 3.1) {
                //$NON-NLS-1$
                return "true";
            }
            //$NON-NLS-1$
            return "true";
        } else {
            return null;
        }
    }

    /**
	 * Adds the given bundle and start information to the map.  This will override anything set
	 * for system bundles, and set their start level to the appropriate level
	 * @param map The map to add the bundles too
	 * @param bundle The bundle to add
	 * @param substring the start information in the form level:autostart
	 */
    private static void addBundleToMap(Map<IPluginModelBase, String> map, IPluginModelBase bundle, String sl) {
        BundleDescription desc = bundle.getBundleDescription();
        //$NON-NLS-1$
        boolean defaultsl = (sl == null || sl.equals("default:default"));
        if (desc != null && defaultsl) {
            String runLevelText = resolveSystemRunLevelText(bundle);
            String autoText = resolveSystemAutoText(bundle);
            if (runLevelText != null && autoText != null) {
                //$NON-NLS-1$
                map.put(//$NON-NLS-1$
                bundle, //$NON-NLS-1$
                runLevelText + ":" + autoText);
            } else {
                map.put(bundle, sl);
            }
        } else {
            map.put(bundle, sl);
        }
    }

    public static Map<IPluginModelBase, String> getTargetBundleMap(ILaunchConfiguration configuration, Set<String> set, String attribute) throws CoreException {
        //$NON-NLS-1$
        String selected = configuration.getAttribute(attribute, "");
        Map<IPluginModelBase, String> map = new HashMap();
        //$NON-NLS-1$
        StringTokenizer tok = new StringTokenizer(selected, ",");
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            int index = token.indexOf('@');
            if (// if no start levels, assume default
            index < 0) {
                token = //$NON-NLS-1$
                token.concat(//$NON-NLS-1$
                "@default:default");
                index = token.indexOf('@');
            }
            String idVersion = token.substring(0, index);
            int versionIndex = idVersion.indexOf(VERSION_SEPARATOR);
            String id = (versionIndex > 0) ? idVersion.substring(0, versionIndex) : idVersion;
            String version = (versionIndex > 0) ? idVersion.substring(versionIndex + 1) : null;
            if (set != null && set.contains(id))
                continue;
            ModelEntry entry = PluginRegistry.findEntry(id);
            if (entry != null) {
                IPluginModelBase[] models = entry.getExternalModels();
                for (IPluginModelBase model : models) {
                    if (model.isEnabled()) {
                        IPluginBase base = model.getPluginBase();
                        // c) all else fails, if there's just one bundle available, use it
                        if (base.getVersion().equals(version) || version == null || models.length == 1)
                            addBundleToMap(map, model, token.substring(index + 1));
                    }
                }
            }
        }
        return map;
    }

    public static String writeBundleEntry(IPluginModelBase model, String startLevel, String autoStart) {
        IPluginBase base = model.getPluginBase();
        String id = base.getId();
        StringBuffer buffer = new StringBuffer(id);
        ModelEntry entry = PluginRegistry.findEntry(id);
        if (entry != null && entry.getActiveModels().length > 1) {
            buffer.append(VERSION_SEPARATOR);
            buffer.append(model.getPluginBase().getVersion());
        }
        boolean hasStartLevel = (startLevel != null && startLevel.length() > 0);
        boolean hasAutoStart = (autoStart != null && autoStart.length() > 0);
        if (hasStartLevel || hasAutoStart)
            buffer.append('@');
        if (hasStartLevel)
            buffer.append(startLevel);
        if (hasStartLevel || hasAutoStart)
            buffer.append(':');
        if (hasAutoStart)
            buffer.append(autoStart);
        return buffer.toString();
    }

    public static void checkBackwardCompatibility(ILaunchConfiguration configuration, boolean save) throws CoreException {
        ILaunchConfigurationWorkingCopy wc = null;
        if (configuration.isWorkingCopy()) {
            wc = (ILaunchConfigurationWorkingCopy) configuration;
        } else {
            wc = configuration.getWorkingCopy();
        }
        //$NON-NLS-1$
        String value = configuration.getAttribute("wsproject", (String) null);
        if (value != null) {
            //$NON-NLS-1$
            wc.setAttribute("wsproject", (String) null);
            if (value.indexOf(';') != -1) {
                value = value.replace(';', ',');
            } else if (value.indexOf(':') != -1) {
                value = value.replace(':', ',');
            }
            value = //$NON-NLS-1$
            (value.length() == 0 || value.equals(",")) ? null : value.substring(0, value.length() - 1);
            boolean automatic = configuration.getAttribute(IPDELauncherConstants.AUTOMATIC_ADD, true);
            String attr = automatic ? IPDELauncherConstants.DESELECTED_WORKSPACE_PLUGINS : IPDELauncherConstants.SELECTED_WORKSPACE_PLUGINS;
            wc.setAttribute(attr, value);
        }
        //$NON-NLS-1$
        String value2 = configuration.getAttribute("extplugins", (String) null);
        if (value2 != null) {
            //$NON-NLS-1$
            wc.setAttribute("extplugins", (String) null);
            if (value2.indexOf(';') != -1) {
                value2 = value2.replace(';', ',');
            } else if (value2.indexOf(':') != -1) {
                value2 = value2.replace(':', ',');
            }
            //$NON-NLS-1$
            value2 = (value2.length() == 0 || value2.equals(",")) ? null : value2.substring(0, value2.length() - 1);
            wc.setAttribute(IPDELauncherConstants.SELECTED_TARGET_PLUGINS, value2);
        }
        String version = configuration.getAttribute(IPDEConstants.LAUNCHER_PDE_VERSION, (String) null);
        boolean newApp = TargetPlatformHelper.usesNewApplicationModel();
        //$NON-NLS-1$
        boolean upgrade = !"3.3".equals(version) && newApp;
        if (!upgrade)
            upgrade = TargetPlatformHelper.getTargetVersion() >= 3.2 && version == null;
        if (upgrade) {
            //$NON-NLS-1$ //$NON-NLS-2$
            wc.setAttribute(IPDEConstants.LAUNCHER_PDE_VERSION, newApp ? "3.3" : "3.2a");
            boolean usedefault = configuration.getAttribute(IPDELauncherConstants.USE_DEFAULT, true);
            boolean automaticAdd = configuration.getAttribute(IPDELauncherConstants.AUTOMATIC_ADD, true);
            if (!usedefault) {
                ArrayList<String> list = new ArrayList();
                if (version == null) {
                    list.add("org.eclipse.core.contenttype");
                    list.add("org.eclipse.core.jobs");
                    list.add(IPDEBuildConstants.BUNDLE_EQUINOX_COMMON);
                    list.add("org.eclipse.equinox.preferences");
                    list.add("org.eclipse.equinox.registry");
                }
                if (//$NON-NLS-1$
                !"3.3".equals(version) && //$NON-NLS-1$
                newApp)
                    list.add("org.eclipse.equinox.app");
                StringBuffer extensions = new //$NON-NLS-1$
                StringBuffer(//$NON-NLS-1$
                configuration.getAttribute(IPDELauncherConstants.SELECTED_WORKSPACE_PLUGINS, ""));
                StringBuffer target = new //$NON-NLS-1$
                StringBuffer(//$NON-NLS-1$
                configuration.getAttribute(IPDELauncherConstants.SELECTED_TARGET_PLUGINS, ""));
                for (int i = 0; i < list.size(); i++) {
                    String plugin = list.get(i).toString();
                    IPluginModelBase model = PluginRegistry.findModel(plugin);
                    if (model == null)
                        continue;
                    if (model.getUnderlyingResource() != null) {
                        if (automaticAdd)
                            continue;
                        if (extensions.length() > 0)
                            //$NON-NLS-1$
                            extensions.append(",");
                        extensions.append(plugin);
                    } else {
                        if (target.length() > 0)
                            //$NON-NLS-1$
                            target.append(",");
                        target.append(plugin);
                    }
                }
                if (extensions.length() > 0)
                    wc.setAttribute(IPDELauncherConstants.SELECTED_WORKSPACE_PLUGINS, extensions.toString());
                if (target.length() > 0)
                    wc.setAttribute(IPDELauncherConstants.SELECTED_TARGET_PLUGINS, target.toString());
            }
        }
        if (save && (value != null || value2 != null || upgrade))
            wc.doSave();
    }

    public static String writeAdditionalPluginsEntry(IPluginModelBase model, String pluginResolution, boolean checked) {
        IPluginBase base = model.getPluginBase();
        String id = base.getId();
        StringBuffer buffer = new StringBuffer(id);
        buffer.append(':');
        buffer.append(base.getVersion());
        buffer.append(':');
        buffer.append(pluginResolution);
        buffer.append(':');
        buffer.append(checked);
        return buffer.toString();
    }

    /**
	 * Returns a map of IPluginModelBase to their associated String resolution setting. Reads the
	 * additional plug-ins attribute of the given launch config and returns a map of plug-in models
	 * to their resolution.  The attribute stores the id, version, enablement and resolution of each plug-in.
	 * The models to be returned are determined by trying to find a model with a matching name, matching version
	 * (or highest) in the resolution location (falling back on other locations if the chosen option is unavailable).
	 * The includeDisabled option allows the returned list to contain only plug-ins that are enabled (checked) in
	 * the config.
	 *
	 * @param config launch config to read attribute from
	 * @param onlyEnabled whether all plug-ins in the attribute should be returned or just the ones marked as enabled/checked
	 * @return map of IPluginModelBase to String resolution setting
	 * @throws CoreException if there is a problem reading the launch config
	 */
    public static HashMap<IPluginModelBase, String> getAdditionalPlugins(ILaunchConfiguration config, boolean onlyEnabled) throws CoreException {
        HashMap<IPluginModelBase, String> resolvedAdditionalPlugins = new HashMap();
        Set<String> userAddedPlugins = config.getAttribute(IPDELauncherConstants.ADDITIONAL_PLUGINS, (Set<String>) null);
        String defaultPluginResolution = config.getAttribute(IPDELauncherConstants.FEATURE_PLUGIN_RESOLUTION, IPDELauncherConstants.LOCATION_WORKSPACE);
        if (userAddedPlugins != null) {
            for (String addedPlugin : userAddedPlugins) {
                String[] pluginData = //$NON-NLS-1$
                addedPlugin.split(//$NON-NLS-1$
                ":");
                boolean checked = Boolean.valueOf(pluginData[3]).booleanValue();
                if (!onlyEnabled || checked) {
                    String id = pluginData[0];
                    String version = pluginData[1];
                    String pluginResolution = pluginData[2];
                    ModelEntry pluginModelEntry = PluginRegistry.findEntry(id);
                    if (pluginModelEntry != null) {
                        if (IPDELauncherConstants.LOCATION_DEFAULT.equalsIgnoreCase(pluginResolution)) {
                            pluginResolution = defaultPluginResolution;
                        }
                        IPluginModelBase model = findModel(pluginModelEntry, version, pluginResolution);
                        if (model != null) {
                            resolvedAdditionalPlugins.put(model, pluginData[2]);
                        }
                    }
                }
            }
        }
        return resolvedAdditionalPlugins;
    }
}
