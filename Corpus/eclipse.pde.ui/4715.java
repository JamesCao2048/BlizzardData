/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor;

import java.util.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.pde.internal.core.project.PDEProject;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.editor.actions.ActionMenu;

public abstract class PDELauncherFormEditor extends MultiSourceEditor {

    protected static final int RUN_LAUNCHER_INDEX = 0;

    protected static final int DEBUG_LAUNCHER_INDEX = 1;

    protected static final int PROFILE_LAUNCHER_INDEX = 2;

    /**
	 * Stores the toolbar contributions that contain all the actions.  Entries may
	 * be null if the toolbar hasn't been created or if there are no actions for that
	 * index.  Uses {@link #RUN_LAUNCHER_INDEX}, {@link #DEBUG_LAUNCHER_INDEX} and
	 * {@link #PROFILE_LAUNCHER_INDEX}.
	 */
    ActionMenu[] fToolbarActions = new ActionMenu[3];

    LauncherAction[][] fActions = null;

    protected abstract ILauncherFormPageHelper getLauncherHelper();

    protected void contributeLaunchersToToolbar(IToolBarManager manager) {
        // but we'll guard against it anyway
        if (getLauncherHelper() != null) {
            List<String> recentLaunches = PDEEditorLaunchManager.getDefault().getRecentLaunches();
            LauncherAction[][] actions = getActions();
            if (actions[RUN_LAUNCHER_INDEX].length > 0) {
                fToolbarActions[RUN_LAUNCHER_INDEX] = new ActionMenu(actions[RUN_LAUNCHER_INDEX]);
                fToolbarActions[RUN_LAUNCHER_INDEX].updateActionOrder(recentLaunches);
                manager.add(fToolbarActions[RUN_LAUNCHER_INDEX]);
            }
            if (actions[DEBUG_LAUNCHER_INDEX].length > 0) {
                fToolbarActions[DEBUG_LAUNCHER_INDEX] = new ActionMenu(actions[DEBUG_LAUNCHER_INDEX]);
                fToolbarActions[DEBUG_LAUNCHER_INDEX].updateActionOrder(recentLaunches);
                manager.add(fToolbarActions[DEBUG_LAUNCHER_INDEX]);
            }
            if (actions[PROFILE_LAUNCHER_INDEX].length > 0) {
                fToolbarActions[PROFILE_LAUNCHER_INDEX] = new ActionMenu(actions[PROFILE_LAUNCHER_INDEX]);
                fToolbarActions[PROFILE_LAUNCHER_INDEX].updateActionOrder(recentLaunches);
                manager.add(fToolbarActions[PROFILE_LAUNCHER_INDEX]);
            }
        }
    }

    private LauncherAction[][] getActions() {
        if (fActions == null) {
            fActions = new LauncherAction[3][];
            IConfigurationElement[][] elements = getLaunchers(getLauncherHelper().isOSGi());
            fActions[RUN_LAUNCHER_INDEX] = getLauncherActions(elements[RUN_LAUNCHER_INDEX], RUN_LAUNCHER_INDEX);
            fActions[DEBUG_LAUNCHER_INDEX] = getLauncherActions(elements[DEBUG_LAUNCHER_INDEX], DEBUG_LAUNCHER_INDEX);
            fActions[PROFILE_LAUNCHER_INDEX] = getLauncherActions(elements[PROFILE_LAUNCHER_INDEX], PROFILE_LAUNCHER_INDEX);
        }
        return fActions;
    }

    private LauncherAction[] getLauncherActions(IConfigurationElement[] elements, final int toolbarIndex) {
        LauncherAction[] result = new LauncherAction[elements.length];
        for (int i = 0; i < elements.length; i++) {
            LauncherAction thisAction = new LauncherAction(elements[i]) {

                @Override
                public void run() {
                    doSave(null);
                    String id = //$NON-NLS-1$
                    getConfigurationElement().getAttribute(//$NON-NLS-1$
                    "id");
                    String mode = //$NON-NLS-1$
                    getConfigurationElement().getAttribute(//$NON-NLS-1$
                    "mode");
                    launch(id, mode, getPreLaunchRunnable(), getLauncherHelper().getLaunchObject());
                    // Have all toolbar items update their order
                    PDEEditorLaunchManager.getDefault().setRecentLaunch(//$NON-NLS-1$
                    getConfigurationElement().getAttribute(//$NON-NLS-1$
                    "id"));
                    List<String> updatedActionOrder = PDEEditorLaunchManager.getDefault().getRecentLaunches();
                    for (int j = 0; j < fToolbarActions.length; j++) {
                        if (fToolbarActions[j] != null) {
                            fToolbarActions[j].updateActionOrder(updatedActionOrder);
                        }
                    }
                }
            };
            result[i] = thisAction;
        }
        return result;
    }

    protected Runnable getPreLaunchRunnable() {
        return new Runnable() {

            @Override
            public void run() {
                getLauncherHelper().preLaunch();
            }
        };
    }

    public void launch(String launcherID, String mode, Runnable preLaunch, Object launchObject) {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        //$NON-NLS-1$
        IConfigurationElement[] elements = registry.getConfigurationElementsFor("org.eclipse.debug.ui.launchShortcuts");
        for (int i = 0; i < elements.length; i++) {
            if (//$NON-NLS-1$
            launcherID.equals(elements[i].getAttribute("id"))) {
                try {
                    ILaunchShortcut shortcut = (ILaunchShortcut) //$NON-NLS-1$
                    elements[i].createExecutableExtension(//$NON-NLS-1$
                    "class");
                    preLaunch.run();
                    StructuredSelection selection = launchObject != null ? new StructuredSelection(launchObject) : StructuredSelection.EMPTY;
                    shortcut.launch(selection, mode);
                } catch (CoreException e1) {
                }
            }
        }
    }

    protected IConfigurationElement[][] getLaunchers(boolean osgi) {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        //$NON-NLS-1$
        IConfigurationElement[] elements = registry.getConfigurationElementsFor("org.eclipse.pde.ui.launchShortcuts");
        // validate elements
        ArrayList<IConfigurationElement> runList = new ArrayList();
        ArrayList<IConfigurationElement> debugList = new ArrayList();
        ArrayList<IConfigurationElement> profileList = new ArrayList();
        // limit to specific shortcuts based on project settings (if specified)
        IResource resource = (IResource) getEditorInput().getAdapter(IResource.class);
        Set<String> specificIds = null;
        if (resource != null) {
            IProject project = resource.getProject();
            if (project != null) {
                String[] values = PDEProject.getLaunchShortcuts(project);
                if (values != null) {
                    specificIds = new HashSet();
                    for (int i = 0; i < values.length; i++) {
                        specificIds.add(values[i]);
                    }
                }
            }
        }
        for (int i = 0; i < elements.length; i++) {
            //$NON-NLS-1$
            String mode = elements[i].getAttribute("mode");
            //$NON-NLS-1$
            String id = elements[i].getAttribute("id");
            //$NON-NLS-1$
            String projectSpecific = elements[i].getAttribute("projectSpecific");
            if (//$NON-NLS-1$
            mode != null && elements[i].getAttribute("label") != null && id != null) {
                boolean include = false;
                if (specificIds != null) {
                    include = specificIds.contains(id);
                } else {
                    //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                    include = osgi == "true".equals(elements[i].getAttribute("osgi")) && !"true".equals(projectSpecific);
                }
                if (include) {
                    if (mode.equals(ILaunchManager.RUN_MODE))
                        runList.add(elements[i]);
                    else if (mode.equals(ILaunchManager.DEBUG_MODE))
                        debugList.add(elements[i]);
                    else if (mode.equals(ILaunchManager.PROFILE_MODE))
                        profileList.add(elements[i]);
                }
            }
        }
        // sort elements based on criteria specified in bug 172703
        IConfigurationElement[] runElements = runList.toArray(new IConfigurationElement[runList.size()]);
        IConfigurationElement[] debugElements = debugList.toArray(new IConfigurationElement[debugList.size()]);
        IConfigurationElement[] profileElements = profileList.toArray(new IConfigurationElement[profileList.size()]);
        return new IConfigurationElement[][] { runElements, debugElements, profileElements };
    }

    /**
	 * Represents an action that will launch a PDE launch shortcut extension
	 */
    public abstract static class LauncherAction extends Action {

        private IConfigurationElement configElement;

        public  LauncherAction(IConfigurationElement configurationElement) {
            super();
            configElement = configurationElement;
            //$NON-NLS-1$
            String label = configElement.getAttribute("label");
            setText(label);
            setToolTipText(label);
            //$NON-NLS-1$
            setImageDescriptor(getImageDescriptor(configurationElement.getAttribute("mode")));
        }

        public IConfigurationElement getConfigurationElement() {
            return configElement;
        }

        private ImageDescriptor getImageDescriptor(String mode) {
            if (mode == null)
                return null;
            else if (mode.equals(ILaunchManager.RUN_MODE))
                return PDEPluginImages.DESC_RUN_EXC;
            else if (mode.equals(ILaunchManager.DEBUG_MODE))
                return PDEPluginImages.DESC_DEBUG_EXC;
            else if (mode.equals(ILaunchManager.PROFILE_MODE))
                return PDEPluginImages.DESC_PROFILE_EXC;
            return null;
        }
    }
}
