/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Johannes Ahlers <Johannes.Ahlers@gmx.de> - bug 477677
 *******************************************************************************/
package org.eclipse.pde.internal.ui.samples;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Properties;
import java.util.zip.ZipFile;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;
import org.osgi.framework.Bundle;

public class SampleOperation implements IRunnableWithProgress {

    //$NON-NLS-1$
    private static final String SAMPLE_PROPERTIES = "sample.properties";

    private IConfigurationElement sample;

    private String[] projectNames;

    private IFile sampleManifest;

    private IOverwriteQuery query;

    private boolean yesToAll;

    private boolean cancel;

    private IProject[] createdProjects;

    /**
	 *
	 */
    public  SampleOperation(IConfigurationElement sample, String[] projectNames, IOverwriteQuery query) {
        this.sample = sample;
        this.query = query;
        this.projectNames = projectNames;
    }

    public IFile getSampleManifest() {
        return sampleManifest;
    }

    public IProject[] getCreatedProjects() {
        return createdProjects;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            ICoreRunnable op = new ICoreRunnable() {

                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    IConfigurationElement[] projects = //$NON-NLS-1$
                    sample.getChildren(//$NON-NLS-1$
                    "project");
                    SubMonitor subMonitor = SubMonitor.convert(monitor, PDEUIMessages.SampleOperation_creating, projects.length);
                    createdProjects = new IProject[projects.length];
                    try {
                        for (int i = 0; i < projects.length; i++) {
                            IFile file = importProject(projectNames[i], projects[i], subMonitor.split(1));
                            if (file != null && sampleManifest == null)
                                sampleManifest = file;
                            if (file != null) {
                                createdProjects[i] = file.getProject();
                            }
                            if (cancel)
                                // if user has cancelled operation, exit.
                                break;
                        }
                    } catch (InterruptedException e) {
                        throw new OperationCanceledException();
                    } catch (InvocationTargetException e) {
                        throwCoreException(e);
                    }
                }
            };
            PDEPlugin.getWorkspace().run(op, monitor);
        } catch (CoreException e) {
            throw new InvocationTargetException(e);
        } catch (OperationCanceledException e) {
            throw e;
        }
    }

    private void throwCoreException(InvocationTargetException e) throws CoreException {
        Throwable t = e.getCause();
        Status status = new Status(IStatus.ERROR, IPDEUIConstants.PLUGIN_ID, IStatus.OK, e.getMessage(), t);
        throw new CoreException(status);
    }

    private IFile importProject(String name, IConfigurationElement config, IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
        //$NON-NLS-1$
        String path = config.getAttribute("archive");
        if (name == null || path == null)
            return null;
        IWorkspace workspace = PDEPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IProject project = root.getProject(name);
        boolean skip = false;
        SubMonitor subMonitor = SubMonitor.convert(monitor, name, 5);
        if (project.exists()) {
            if (!yesToAll) {
                String returnId = query.queryOverwrite(project.getFullPath().toString());
                if (returnId.equals(IOverwriteQuery.ALL)) {
                    yesToAll = true;
                    skip = false;
                } else if (returnId.equals(IOverwriteQuery.YES)) {
                    skip = false;
                } else if (returnId.equals(IOverwriteQuery.NO)) {
                    skip = true;
                } else if (returnId.equals(IOverwriteQuery.CANCEL)) {
                    skip = true;
                    cancel = true;
                }
            }
            if (!skip) {
                project.delete(true, true, subMonitor.split(1));
                project = root.getProject(name);
            } else {
                subMonitor.worked(1);
            }
        }
        if (skip) {
            subMonitor.worked(4);
            IFile manifest = project.getFile(SAMPLE_PROPERTIES);
            return manifest;
        }
        project.create(subMonitor.split(1));
        project.open(subMonitor.split(1));
        Bundle bundle = Platform.getBundle(sample.getNamespaceIdentifier());
        ZipFile zipFile = getZipFileFromPluginDir(path, bundle);
        importFilesFromZip(zipFile, project.getFullPath(), subMonitor.split(1));
        return createSampleManifest(project, config, subMonitor.split(1));
    }

    private IFile createSampleManifest(IProject project, IConfigurationElement config, IProgressMonitor monitor) throws CoreException {
        IFile file = project.getFile(SAMPLE_PROPERTIES);
        if (!file.exists()) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Properties properties = new Properties();
                createSampleManifestContent(//$NON-NLS-1$
                config.getAttribute("name"), //$NON-NLS-1$
                properties);
                //$NON-NLS-1$
                properties.store(//$NON-NLS-1$
                out, //$NON-NLS-1$
                "");
                out.flush();
                String contents = out.toString();
                out.close();
                ByteArrayInputStream stream = new //$NON-NLS-1$
                ByteArrayInputStream(//$NON-NLS-1$
                contents.getBytes("UTF8"));
                file.create(stream, true, monitor);
                stream.close();
            } catch (UnsupportedEncodingException e) {
            } catch (IOException e) {
            }
        }
        return file;
    }

    private void createSampleManifestContent(String projectName, Properties properties) {
        //$NON-NLS-1$ //$NON-NLS-2$
        writeProperty(properties, "id", sample.getAttribute("id"));
        //$NON-NLS-1$ //$NON-NLS-2$
        writeProperty(properties, "name", sample.getAttribute("name"));
        //$NON-NLS-1$
        writeProperty(properties, "projectName", projectName);
        //$NON-NLS-1$ //$NON-NLS-2$
        writeProperty(properties, "launcher", sample.getAttribute("launcher"));
        //$NON-NLS-1$
        IConfigurationElement desc[] = sample.getChildren("description");
        if (desc.length == 1) {
            writeProperty(//$NON-NLS-1$
            properties, //$NON-NLS-1$
            "helpHref", desc[0].getAttribute(//$NON-NLS-1$
            "helpHref"));
            //$NON-NLS-1$
            writeProperty(properties, "description", desc[0].getValue());
        }
    }

    private void writeProperty(Properties properties, String name, String value) {
        if (value == null)
            return;
        properties.setProperty(name, value);
    }

    private ZipFile getZipFileFromPluginDir(String pluginRelativePath, Bundle bundle) throws CoreException {
        try {
            URL starterURL = FileLocator.resolve(bundle.getEntry(pluginRelativePath));
            return new ZipFile(FileLocator.toFileURL(starterURL).getFile());
        } catch (IOException e) {
            String message = pluginRelativePath + ": " + e.getMessage();
            Status status = new Status(IStatus.ERROR, PDEPlugin.getPluginId(), IStatus.ERROR, message, e);
            throw new CoreException(status);
        }
    }

    private void importFilesFromZip(ZipFile srcZipFile, IPath destPath, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        ZipFileStructureProvider structureProvider = new ZipFileStructureProvider(srcZipFile);
        ImportOperation op = new ImportOperation(destPath, structureProvider.getRoot(), structureProvider, query);
        op.run(monitor);
    }
}
