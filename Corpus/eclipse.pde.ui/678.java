/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.pde.internal.core.PDECoreMessages;

public abstract class BaseExportTask extends Task {

    protected String fDestination;

    protected String fZipFilename;

    protected boolean fToDirectory;

    protected boolean fUseJarFormat;

    protected boolean fExportSource;

    /**
	 * Whether to create a source bundle if creating source.  By default the UI
	 * and this task should create a source bundle if {@link #fExportSource} is
	 * <code>true</code>
	 */
    protected boolean fExportSourceBundle = true;

    protected String fQualifier;

    protected boolean fAllowBinaryCycles;

    protected boolean fUseWorkspaceCompiledClasses;

    public  BaseExportTask() {
    }

    @Override
    public void execute() throws BuildException {
        if (fDestination == null)
            //$NON-NLS-1$
            throw new BuildException("No destination is specified");
        if (!fToDirectory && fZipFilename == null)
            //$NON-NLS-1$
            throw new BuildException("No zip file is specified");
        Job job = getExportJob(PDECoreMessages.BaseExportTask_pdeExport);
        // blocking will cause errors if done when running in regular runtime.
        if (isAntRunner()) {
            try {
                job.schedule();
                job.join();
            } catch (InterruptedException e) {
            }
        } else
            job.schedule(2000);
    }

    public void setExportType(String type) {
        //$NON-NLS-1$
        fToDirectory = !"zip".equals(type);
    }

    public void setUseJARFormat(String useJarFormat) {
        fUseJarFormat = Boolean.parseBoolean(useJarFormat);
    }

    /**
	 * Whether to include source when exporting the bundle.  By default the source will
	 * be exported as a separate source bundle, but source can be embedded in the binary
	 * output by setting <code>exportSourceBundle=false</code> in the task.
	 *
	 * @see #setExportSourceBundle(String)
	 * @param doExportSource whether to include source in the export
	 */
    public void setExportSource(String doExportSource) {
        fExportSource = Boolean.parseBoolean(doExportSource);
    }

    /**
	 * Whether a separate source bundle should be created when exporting source.  This
	 * is <code>true</code> by default.  If <code>false</code> the source will be embedded
	 * inside the binary output.
	 *
	 * @see #setExportSource(String)
	 * @param doExportSourceBundle whether to create a source bundle when exporting source
	 */
    public void setExportSourceBundle(String doExportSourceBundle) {
        fExportSourceBundle = Boolean.parseBoolean(doExportSourceBundle);
    }

    public void setDestination(String destination) {
        fDestination = destination;
    }

    public void setFilename(String filename) {
        fZipFilename = filename;
    }

    public void setQualifier(String qualifier) {
        fQualifier = qualifier;
    }

    public void setAllowBinaryCycles(String allowBinaryCycles) {
        fAllowBinaryCycles = Boolean.parseBoolean(allowBinaryCycles);
    }

    public void setUseWorkspaceCompiledClasses(String useWorkspaceCompiledClasses) {
        fUseWorkspaceCompiledClasses = Boolean.parseBoolean(useWorkspaceCompiledClasses);
    }

    public boolean isAntRunner() {
        String args[] = Platform.getCommandLineArgs();
        for (int i = 0; i < args.length; i++) {
            if (//$NON-NLS-1$
            args[i].equals("-application"))
                return //$NON-NLS-1$
                args[i + 1].equals(//$NON-NLS-1$
                "org.eclipse.ant.core.antRunner");
        }
        return false;
    }

    protected abstract Job getExportJob(String jobName);
}
