/*******************************************************************************
 *  Copyright (c) 2007, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.search.dependencies;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.IProgressConstants;

public class CalculateUsesAction extends Action {

    private IProject fProject;

    private IBundlePluginModelBase fModel;

    public  CalculateUsesAction(IProject project, IBundlePluginModelBase model) {
        fProject = project;
        fModel = model;
    }

    @Override
    public void run() {
        Job job = createJob();
        job.setUser(true);
        job.setProperty(IProgressConstants.ICON_PROPERTY, PDEPluginImages.DESC_PSEARCH_OBJ.createImage());
        job.schedule();
    }

    protected Job createJob() {
        return new WorkspaceJob(PDEUIMessages.CalculateUsesAction_jobName) {

            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                CalculateUsesOperation op = getOperation();
                try {
                    op.run(monitor);
                } catch (InvocationTargetException e) {
                } catch (InterruptedException e) {
                } finally {
                    monitor.done();
                }
                return new //$NON-NLS-1$
                Status(//$NON-NLS-1$
                IStatus.OK, //$NON-NLS-1$
                PDEPlugin.getPluginId(), //$NON-NLS-1$
                IStatus.OK, //$NON-NLS-1$
                "", //$NON-NLS-1$
                null);
            }
        };
    }

    protected CalculateUsesOperation getOperation() {
        return new CalculateUsesOperation(fProject, fModel) {

            @Override
            protected void handleSetUsesDirectives(final Map<String, HashSet<String>> pkgsAndUses) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (pkgsAndUses.isEmpty())
                            return;
                        setUsesDirectives(pkgsAndUses);
                    }
                });
            }
        };
    }
}
