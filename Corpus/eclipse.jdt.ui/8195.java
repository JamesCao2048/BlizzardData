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
package org.eclipse.jdt.internal.corext.refactoring.changes;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;

public class WorkspaceTracker {

    public static final WorkspaceTracker INSTANCE = new WorkspaceTracker();

    public interface Listener {

        public void workspaceChanged();
    }

    private ListenerList<Listener> fListeners;

    private ResourceListener fResourceListener;

    private  WorkspaceTracker() {
        fListeners = new ListenerList();
    }

    private class ResourceListener implements IResourceChangeListener {

        @Override
        public void resourceChanged(IResourceChangeEvent event) {
            workspaceChanged();
        }
    }

    private void workspaceChanged() {
        for (Listener listener : fListeners) {
            listener.workspaceChanged();
        }
    }

    public void addListener(Listener l) {
        fListeners.add(l);
        if (fResourceListener == null) {
            fResourceListener = new ResourceListener();
            ResourcesPlugin.getWorkspace().addResourceChangeListener(fResourceListener);
        }
    }

    public void removeListener(Listener l) {
        if (fListeners.size() == 0)
            return;
        fListeners.remove(l);
        if (fListeners.size() == 0) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(fResourceListener);
            fResourceListener = null;
        }
    }
}
