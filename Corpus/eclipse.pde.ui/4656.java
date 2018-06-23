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
package org.eclipse.pde.internal.ui.wizards.tools;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.ClasspathComputer;
import org.eclipse.pde.internal.core.builders.PDEMarkerFactory;
import org.eclipse.pde.internal.ui.*;

public class UpdateClasspathJob extends Job {

    IPluginModelBase[] fModels;

    /**
	 * @param name
	 */
    public  UpdateClasspathJob(IPluginModelBase[] models) {
        super(PDEUIMessages.UpdateClasspathJob_title);
        setPriority(Job.LONG);
        fModels = models;
    }

    /*
	 * return canceled
	 */
    public boolean doUpdateClasspath(IProgressMonitor monitor, IPluginModelBase[] models) throws CoreException {
        monitor.beginTask(PDEUIMessages.UpdateClasspathJob_task, models.length);
        try {
            for (int i = 0; i < models.length; i++) {
                IPluginModelBase model = models[i];
                monitor.subTask(models[i].getPluginBase().getId());
                // no reason to compile classpath for a non-Java model
                IProject project = model.getUnderlyingResource().getProject();
                if (!project.hasNature(JavaCore.NATURE_ID)) {
                    monitor.worked(1);
                    continue;
                }
                IProjectDescription projDesc = project.getDescription();
                if (projDesc == null)
                    continue;
                projDesc.setReferencedProjects(new IProject[0]);
                project.setDescription(projDesc, null);
                IFile file = //$NON-NLS-1$
                project.getFile(//$NON-NLS-1$
                ".project");
                if (file.exists())
                    file.deleteMarkers(PDEMarkerFactory.MARKER_ID, true, IResource.DEPTH_ZERO);
                ClasspathComputer.setClasspath(project, model);
                monitor.worked(1);
                if (monitor.isCanceled())
                    return false;
            }
        } finally {
            monitor.done();
        }
        return true;
    }

    class UpdateClasspathWorkspaceRunnable implements IWorkspaceRunnable {

        boolean fCanceled = false;

        @Override
        public void run(IProgressMonitor monitor) throws CoreException {
            fCanceled = doUpdateClasspath(monitor, fModels);
        }

        public boolean isCanceled() {
            return fCanceled;
        }
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        try {
            UpdateClasspathWorkspaceRunnable runnable = new UpdateClasspathWorkspaceRunnable();
            PDEPlugin.getWorkspace().run(runnable, monitor);
            if (runnable.isCanceled()) {
                return new //$NON-NLS-1$
                Status(//$NON-NLS-1$
                IStatus.CANCEL, //$NON-NLS-1$
                IPDEUIConstants.PLUGIN_ID, //$NON-NLS-1$
                IStatus.CANCEL, //$NON-NLS-1$
                "", //$NON-NLS-1$
                null);
            }
        } catch (CoreException e) {
            String title = PDEUIMessages.UpdateClasspathJob_error_title;
            String message = PDEUIMessages.UpdateClasspathJob_error_message;
            PDEPlugin.logException(e, title, message);
            return new Status(IStatus.ERROR, IPDEUIConstants.PLUGIN_ID, IStatus.OK, message, e);
        }
        //$NON-NLS-1$
        return new Status(IStatus.OK, IPDEUIConstants.PLUGIN_ID, IStatus.OK, "", null);
    }
}
