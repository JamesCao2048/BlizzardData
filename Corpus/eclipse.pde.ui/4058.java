/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     EclipseSource Corporation - ongoing enhancements
 *******************************************************************************/
package org.eclipse.pde.launching;

import java.io.File;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.TargetPlatform;
import org.eclipse.pde.internal.core.*;
import org.eclipse.pde.internal.core.util.CoreUtility;
import org.eclipse.pde.internal.core.util.VersionUtil;
import org.eclipse.pde.internal.launching.launcher.*;
import org.osgi.framework.Version;

/**
 * A launch delegate for launching Eclipse applications
 * <p>
 * Clients may subclass and instantiate this class.
 * </p>
 * <p>
 * This class originally existed in 3.2 as
 * <code>org.eclipse.pde.ui.launcher.EclipseApplicationLaunchConfiguration</code>.
 * </p>
 * @since 3.6
 */
public class EclipseApplicationLaunchConfiguration extends AbstractPDELaunchConfiguration {

    // used to generate the dev classpath entries
    // key is bundle ID, value is a model
    private Map<String, IPluginModelBase> fAllBundles;

    // key is a model, value is startLevel:autoStart
    private Map<IPluginModelBase, String> fModels;

    /**
	 * To avoid duplicating variable substitution (and duplicate prompts)
	 * this variable will store the substituted workspace location.
	 */
    private String fWorkspaceLocation;

    @Override
    public String[] getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
        ArrayList<String> programArgs = new ArrayList();
        // If a product is specified, then add it to the program args
        if (configuration.getAttribute(IPDELauncherConstants.USE_PRODUCT, false)) {
            //$NON-NLS-1$
            String product = configuration.getAttribute(IPDELauncherConstants.PRODUCT, "");
            if (product.length() > 0) {
                //$NON-NLS-1$
                programArgs.add(//$NON-NLS-1$
                "-product");
                programArgs.add(product);
            } else // TODO product w/o an application and product... how to handle gracefully?
            {
                //$NON-NLS-1$
                programArgs.add(//$NON-NLS-1$
                "-application");
                //$NON-NLS-1$
                programArgs.add(//$NON-NLS-1$
                configuration.getAttribute(IPDELauncherConstants.APPLICATION, ""));
            }
        } else {
            // specify the application to launch
            //$NON-NLS-1$
            programArgs.add("-application");
            programArgs.add(configuration.getAttribute(IPDELauncherConstants.APPLICATION, TargetPlatform.getDefaultApplication()));
        }
        // specify the workspace location for the runtime workbench
        if (fWorkspaceLocation == null) {
            fWorkspaceLocation = LaunchArgumentsHelper.getWorkspaceLocation(configuration);
        }
        if (fWorkspaceLocation.length() > 0) {
            //$NON-NLS-1$
            programArgs.add("-data");
            programArgs.add(fWorkspaceLocation);
        }
        boolean showSplash = true;
        String productID = LaunchConfigurationHelper.getProductID(configuration);
        Properties prop = LaunchConfigurationHelper.createConfigIniFile(configuration, productID, fAllBundles, fModels, getConfigDir(configuration));
        //$NON-NLS-1$ //$NON-NLS-2$
        showSplash = prop.containsKey("osgi.splashPath") || prop.containsKey("splashLocation");
        TargetPlatformHelper.checkPluginPropertiesConsistency(fAllBundles, getConfigDir(configuration));
        //$NON-NLS-1$
        programArgs.add("-configuration");
        //$NON-NLS-1$
        programArgs.add("file:" + new Path(getConfigDir(configuration).getPath()).addTrailingSeparator().toString());
        // add the output folder names
        //$NON-NLS-1$
        programArgs.add("-dev");
        //$NON-NLS-1$
        programArgs.add(ClasspathHelper.getDevEntriesProperties(getConfigDir(configuration).toString() + "/dev.properties", fAllBundles));
        // necessary for PDE to know how to load plugins when target platform = host platform
        // see PluginPathFinder.getPluginPaths() and PluginPathFinder.isDevLaunchMode()
        IPluginModelBase base = fAllBundles.get(PDECore.PLUGIN_ID);
        if (//$NON-NLS-1$
        base != null && VersionUtil.compareMacroMinorMicro(base.getBundleDescription().getVersion(), new Version("3.3.1")) < 0)
            //$NON-NLS-1$
            programArgs.add("-pdelaunch");
        String[] args = super.getProgramArguments(configuration);
        for (String arg : args) {
            programArgs.add(arg);
        }
        if (//$NON-NLS-1$
        !programArgs.contains("-nosplash") && showSplash) {
            if (TargetPlatformHelper.getTargetVersion() >= 3.1) {
                //$NON-NLS-1$
                programArgs.add(//$NON-NLS-1$
                0, //$NON-NLS-1$
                "-launcher");
                IPath path = null;
                if (//$NON-NLS-1$
                TargetPlatform.getOS().equals("macosx")) {
                    path = new Path(TargetPlatform.getLocation()).append("Eclipse.app/Contents/MacOS/eclipse");
                } else {
                    path = //$NON-NLS-1$
                    new Path(TargetPlatform.getLocation()).append(//$NON-NLS-1$
                    "eclipse");
                    if (//$NON-NLS-1$
                    TargetPlatform.getOS().equals(//$NON-NLS-1$
                    "win32")) {
                        path = //$NON-NLS-1$
                        path.addFileExtension(//$NON-NLS-1$
                        "exe");
                    }
                }
                //This could be the branded launcher if we want (also this does not bring much)
                programArgs.add(1, path.toOSString());
                //$NON-NLS-1$
                programArgs.add(//$NON-NLS-1$
                2, //$NON-NLS-1$
                "-name");
                //This should be the name of the product //$NON-NLS-1$
                programArgs.add(3, "Eclipse");
                //$NON-NLS-1$
                programArgs.add(//$NON-NLS-1$
                4, //$NON-NLS-1$
                "-showsplash");
                //$NON-NLS-1$
                programArgs.add(//$NON-NLS-1$
                5, //$NON-NLS-1$
                "600");
            } else {
                //$NON-NLS-1$
                programArgs.add(//$NON-NLS-1$
                0, //$NON-NLS-1$
                "-showsplash");
                programArgs.add(1, computeShowsplashArgument());
            }
        }
        return programArgs.toArray(new String[programArgs.size()]);
    }

    private String computeShowsplashArgument() {
        IPath eclipseHome = new Path(TargetPlatform.getLocation());
        //$NON-NLS-1$
        IPath fullPath = eclipseHome.append("eclipse");
        //$NON-NLS-1$
        return fullPath.toOSString() + " -showsplash 600";
    }

    @Override
    protected File getConfigDir(ILaunchConfiguration config) {
        if (fConfigDir == null) {
            fConfigDir = LaunchConfigurationHelper.getConfigurationArea(config);
        }
        return fConfigDir;
    }

    /**
	 * Clears the workspace prior to launching if the workspace exists and the option to
	 * clear it is turned on.  Also clears the configuration area if that option is chosen.
	 *
	 * @param configuration
	 * 			the launch configuration
	 * @param monitor
	 * 			the progress monitor
	 * @throws CoreException
	 * 			if unable to retrieve launch attribute values
	 * @since 3.3
	 */
    @Override
    protected void clear(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
        if (fWorkspaceLocation == null) {
            fWorkspaceLocation = LaunchArgumentsHelper.getWorkspaceLocation(configuration);
        }
        SubMonitor subMon = SubMonitor.convert(monitor, 50);
        // Clear workspace and prompt, if necessary
        if (!LauncherUtils.clearWorkspace(configuration, fWorkspaceLocation, subMon.split(25)))
            throw new CoreException(Status.CANCEL_STATUS);
        subMon.setWorkRemaining(25);
        if (subMon.isCanceled()) {
            throw new CoreException(Status.CANCEL_STATUS);
        }
        // clear config area, if necessary
        if (configuration.getAttribute(IPDELauncherConstants.CONFIG_CLEAR_AREA, false)) {
            CoreUtility.deleteContent(getConfigDir(configuration), subMon.split(25));
        }
        subMon.setWorkRemaining(0);
    }

    @Override
    protected void preLaunchCheck(ILaunchConfiguration configuration, ILaunch launch, IProgressMonitor monitor) throws CoreException {
        fWorkspaceLocation = null;
        fModels = BundleLauncherHelper.getMergedBundleMap(configuration, false);
        fAllBundles = new HashMap(fModels.size());
        Iterator<IPluginModelBase> iter = fModels.keySet().iterator();
        while (iter.hasNext()) {
            IPluginModelBase model = iter.next();
            fAllBundles.put(model.getPluginBase().getId(), model);
        }
        validateConfigIni(configuration);
        super.preLaunchCheck(configuration, launch, monitor);
    }

    private void validateConfigIni(ILaunchConfiguration configuration) throws CoreException {
        if (!configuration.getAttribute(IPDELauncherConstants.CONFIG_GENERATE_DEFAULT, true)) {
            //$NON-NLS-1$
            String templateLoc = configuration.getAttribute(IPDELauncherConstants.CONFIG_TEMPLATE_LOCATION, "");
            IStringVariableManager mgr = VariablesPlugin.getDefault().getStringVariableManager();
            templateLoc = mgr.performStringSubstitution(templateLoc);
            File templateFile = new File(templateLoc);
            if (!templateFile.exists()) {
                if (!LauncherUtils.generateConfigIni())
                    throw new CoreException(Status.CANCEL_STATUS);
            // with the way the launcher works, if a config.ini file is not found one will be generated automatically.
            // This check was to warn the user a config.ini needs to be generated. - bug 161265, comment #7
            }
        }
    }

    @Override
    public String[] getVMArguments(ILaunchConfiguration configuration) throws CoreException {
        String[] vmArgs = super.getVMArguments(configuration);
        IPluginModelBase base = fAllBundles.get(PDECore.PLUGIN_ID);
        if (//$NON-NLS-1$
        base != null && VersionUtil.compareMacroMinorMicro(base.getBundleDescription().getVersion(), new Version("3.3.1")) >= 0) {
            // necessary for PDE to know how to load plugins when target platform = host platform
            // see PluginPathFinder.getPluginPaths() and PluginPathFinder.isDevLaunchMode()
            String[] result = new String[vmArgs.length + 1];
            System.arraycopy(vmArgs, 0, result, 0, vmArgs.length);
            //$NON-NLS-1$
            result[vmArgs.length] = "-Declipse.pde.launch=true";
            return result;
        }
        return vmArgs;
    }
}
