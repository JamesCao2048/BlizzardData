/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.launching.launcher;

import java.io.File;
import java.util.ArrayList;
import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.pde.internal.launching.*;
import org.eclipse.pde.launching.IPDELauncherConstants;

public class LaunchListener implements ILaunchListener, IDebugEventSetListener {

    private ArrayList<ILaunch> managedLaunches;

    public  LaunchListener() {
        managedLaunches = new ArrayList();
    }

    public void manage(ILaunch launch) {
        if (managedLaunches.size() == 0)
            hookListener(true);
        if (!managedLaunches.contains(launch))
            managedLaunches.add(launch);
    }

    /**
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch)
	 */
    @Override
    public void launchRemoved(ILaunch launch) {
        update(launch, true);
    }

    /**
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch)
	 */
    @Override
    public void launchAdded(ILaunch launch) {
    }

    /**
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch)
	 */
    @Override
    public void launchChanged(ILaunch launch) {
    }

    private void update(ILaunch launch, boolean remove) {
        if (managedLaunches.contains(launch)) {
            if (remove || launch.isTerminated()) {
                managedLaunches.remove(launch);
                if (managedLaunches.size() == 0) {
                    hookListener(false);
                }
            }
        }
    }

    private void hookListener(boolean add) {
        DebugPlugin debugPlugin = DebugPlugin.getDefault();
        ILaunchManager launchManager = debugPlugin.getLaunchManager();
        if (add) {
            launchManager.addLaunchListener(this);
            debugPlugin.addDebugEventListener(this);
        } else {
            launchManager.removeLaunchListener(this);
            debugPlugin.removeDebugEventListener(this);
        }
    }

    private void doRestart(ILaunch launch) {
        ILaunchConfiguration config = launch.getLaunchConfiguration();
        try {
            ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
            copy.setAttribute(IPDEConstants.RESTART, true);
            copy.launch(launch.getLaunchMode(), new NullProgressMonitor());
        } catch (CoreException e) {
            Status status = new Status(IStatus.ERROR, IPDEConstants.PLUGIN_ID, 42, null, e);
            IStatusHandler statusHandler = DebugPlugin.getDefault().getStatusHandler(status);
            if (statusHandler == null)
                PDELaunchingPlugin.log(e);
            else {
                try {
                    statusHandler.handleStatus(status, null);
                } catch (CoreException e1) {
                    PDELaunchingPlugin.log(e);
                }
            }
        }
    }

    public void shutdown() {
        hookListener(false);
    }

    /**
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent)
	 */
    @Override
    public void handleDebugEvents(DebugEvent[] events) {
        for (DebugEvent event : events) {
            Object source = event.getSource();
            if (source instanceof IProcess && event.getKind() == DebugEvent.TERMINATE) {
                IProcess process = (IProcess) source;
                ILaunch launch = process.getLaunch();
                if (launch != null) {
                    try {
                        launchTerminated(launch, process.getExitValue());
                    } catch (DebugException e) {
                    } catch (CoreException e) {
                        PDELaunchingPlugin.log(e);
                    }
                }
            }
        }
    }

    private void launchTerminated(final ILaunch launch, int returnValue) throws CoreException {
        if (managedLaunches.contains(launch)) {
            update(launch, true);
            if (returnValue == 23) {
                doRestart(launch);
                return;
            }
            // launch failed because the associated workspace is in use
            if (returnValue == 15) {
                Status status = new Status(IStatus.ERROR, IPDEConstants.PLUGIN_ID, LauncherUtils.WORKSPACE_LOCKED, null, null);
                IStatusHandler statusHandler = DebugPlugin.getDefault().getStatusHandler(status);
                if (statusHandler == null)
                    PDELaunchingPlugin.log(status);
                else {
                    ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
                    String workspace = //$NON-NLS-1$
                    launchConfiguration.getAttribute(//$NON-NLS-1$
                    IPDELauncherConstants.LOCATION, //$NON-NLS-1$
                    "");
                    statusHandler.handleStatus(status, new Object[] { workspace, launchConfiguration, launch.getLaunchMode() });
                }
                return;
            }
            // launch failed for reasons printed to the log.
            if (returnValue == 13) {
                Status status = new Status(IStatus.ERROR, IPDEConstants.PLUGIN_ID, returnValue, PDEMessages.Launcher_error_code13, null);
                IStatusHandler statusHandler = DebugPlugin.getDefault().getStatusHandler(status);
                if (statusHandler == null)
                    PDELaunchingPlugin.log(status);
                else
                    statusHandler.handleStatus(status, launch);
                return;
            }
        }
    }

    /**
	 * Returns latest log file for Launch Configuration.
	 * It's ".metadala/.log", file with most recent timestamp ending with ".log"
	 * in configuration location or null if none found.
	 *
	 * @returns log file or null
	 * @throws CoreException
	 * @since 3.4
	 */
    public static File getMostRecentLogFile(ILaunchConfiguration configuration) throws CoreException {
        File latest = null;
        String workspace = LaunchArgumentsHelper.getWorkspaceLocation(configuration);
        if (workspace.length() > 0) {
            //$NON-NLS-1$
            latest = new File(workspace, ".metadata/.log");
            if (!latest.exists())
                latest = null;
        }
        File configDir = LaunchConfigurationHelper.getConfigurationLocation(configuration);
        File[] children = configDir.listFiles();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                if (//$NON-NLS-1$
                !children[i].isDirectory() && children[i].getName().endsWith(".log")) {
                    if (latest == null || latest.lastModified() < children[i].lastModified())
                        latest = children[i];
                }
            }
        }
        return latest;
    }
}
