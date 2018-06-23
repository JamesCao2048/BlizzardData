/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Code 9 Corporation - ongoing enhancements
 *     Les Jones <lesojones@gamil.com> - bug 205361
 *******************************************************************************/
package org.eclipse.pde.internal.ui.wizards.tools;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.core.build.IBuild;
import org.eclipse.pde.core.build.IBuildEntry;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.core.*;
import org.eclipse.pde.internal.core.build.WorkspaceBuildModel;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundlePluginModel;
import org.eclipse.pde.internal.core.ibundle.IBundle;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.pde.internal.core.project.PDEProject;
import org.eclipse.pde.internal.core.util.CoreUtility;
import org.eclipse.pde.internal.core.util.IdUtil;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.util.ModelModification;
import org.eclipse.pde.internal.ui.util.PDEModelUtility;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.osgi.framework.Constants;

/**
 * Operation to convert a normal workspace project into a plug-in project. This
 * code has, in the main, been refactored (copied with little or no amendment)
 * from org.eclipse.pde.internal.ui.wizards.tool.ConvertedProjectsPage.
 */
public class ConvertProjectToPluginOperation extends WorkspaceModifyOperation {

    private IProject[] projectsToConvert;

    private boolean enableApiAnalysis;

    private String fLibraryName;

    private String[] fSrcEntries;

    private String[] fLibEntries;

    /**
	 * Workspace operation to convert the specified project into a plug-in
	 * project.
	 *
	 * @param theProjectsToConvert The project to be converted.
	 * @param enableApiAnalysis Whether to set up API Tooling analysis on the projects
	 */
    public  ConvertProjectToPluginOperation(IProject[] theProjectsToConvert, boolean enableApiAnalysis) {
        this.projectsToConvert = theProjectsToConvert;
        this.enableApiAnalysis = enableApiAnalysis;
    }

    /**
	 * Convert a normal java project into a plug-in project.
	 *
	 * @param monitor
	 *            Progress monitor
	 */
    @Override
    protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
        try {
            monitor.beginTask(PDEUIMessages.ConvertedProjectWizard_converting, projectsToConvert.length);
            for (int i = 0; i < projectsToConvert.length; i++) {
                IProject projectToConvert = projectsToConvert[i];
                convertProject(projectToConvert, monitor);
                monitor.worked(1);
            }
        } catch (CoreException e) {
            PDEPlugin.logException(e);
        } finally {
            monitor.done();
        }
    }

    private void convertProject(IProject projectToConvert, IProgressMonitor monitor) throws CoreException {
        // properly
        if (projectToConvert == null || !projectToConvert.exists()) {
            return;
        }
        // Nature check - do we need to do anything at all?
        if (projectToConvert.hasNature(PDE.PLUGIN_NATURE)) {
            return;
        }
        CoreUtility.addNatureToProject(projectToConvert, PDE.PLUGIN_NATURE, monitor);
        // Setup API Tooling, which requires the java nature
        if (enableApiAnalysis) {
            if (!projectToConvert.hasNature(JavaCore.NATURE_ID)) {
                CoreUtility.addNatureToProject(projectToConvert, JavaCore.NATURE_ID, monitor);
            }
            //$NON-NLS-1$
            CoreUtility.addNatureToProject(projectToConvert, "org.eclipse.pde.api.tools.apiAnalysisNature", monitor);
        }
        loadClasspathEntries(projectToConvert, monitor);
        loadLibraryName(projectToConvert);
        createManifestFile(PDEProject.getManifest(projectToConvert), monitor);
        IFile buildFile = PDEProject.getBuildProperties(projectToConvert);
        if (!buildFile.exists()) {
            WorkspaceBuildModel model = new WorkspaceBuildModel(buildFile);
            IBuild build = model.getBuild(true);
            IBuildEntry entry = model.getFactory().createEntry(IBuildEntry.BIN_INCLUDES);
            if (PDEProject.getPluginXml(projectToConvert).exists())
                entry.addToken(ICoreConstants.PLUGIN_FILENAME_DESCRIPTOR);
            if (PDEProject.getManifest(projectToConvert).exists())
                entry.addToken(ICoreConstants.MANIFEST_FOLDER_NAME);
            for (int i = 0; i < fLibEntries.length; i++) {
                entry.addToken(fLibEntries[i]);
            }
            if (fSrcEntries.length > 0) {
                entry.addToken(fLibraryName);
                IBuildEntry source = model.getFactory().createEntry(IBuildEntry.JAR_PREFIX + fLibraryName);
                for (int i = 0; i < fSrcEntries.length; i++) {
                    source.addToken(fSrcEntries[i]);
                }
                build.add(source);
            }
            if (entry.getTokens().length > 0)
                build.add(entry);
            model.save();
        }
    }

    private void loadClasspathEntries(IProject project, IProgressMonitor monitor) {
        IJavaProject javaProject = JavaCore.create(project);
        IClasspathEntry[] currentClassPath = new IClasspathEntry[0];
        ArrayList<String> sources = new ArrayList();
        ArrayList<String> libraries = new ArrayList();
        try {
            currentClassPath = javaProject.getRawClasspath();
        } catch (JavaModelException e) {
        }
        for (int i = 0; i < currentClassPath.length; i++) {
            int contentType = currentClassPath[i].getEntryKind();
            if (contentType == IClasspathEntry.CPE_SOURCE) {
                String relativePath = getRelativePath(currentClassPath[i], project);
                if (//$NON-NLS-1$
                relativePath.equals("")) {
                    //$NON-NLS-1$
                    sources.add(".");
                } else {
                    sources.add(//$NON-NLS-1$
                    relativePath + //$NON-NLS-1$
                    "/");
                }
            } else if (contentType == IClasspathEntry.CPE_LIBRARY) {
                String path = getRelativePath(currentClassPath[i], project);
                if (path.length() > 0)
                    libraries.add(path);
                else
                    //$NON-NLS-1$
                    libraries.add(//$NON-NLS-1$
                    ".");
            }
        }
        fSrcEntries = sources.toArray(new String[sources.size()]);
        fLibEntries = libraries.toArray(new String[libraries.size()]);
        IClasspathEntry[] classPath = new IClasspathEntry[currentClassPath.length + 1];
        System.arraycopy(currentClassPath, 0, classPath, 0, currentClassPath.length);
        classPath[classPath.length - 1] = ClasspathComputer.createContainerEntry();
        try {
            javaProject.setRawClasspath(classPath, monitor);
        } catch (JavaModelException e) {
        }
    }

    private String getRelativePath(IClasspathEntry cpe, IProject project) {
        IPath path = project.getFile(cpe.getPath()).getProjectRelativePath();
        return path.removeFirstSegments(1).toString();
    }

    private void loadLibraryName(IProject project) {
        if (isOldTarget() || (fLibEntries.length > 0 && fSrcEntries.length > 0)) {
            String libName = project.getName();
            //$NON-NLS-1$
            int i = libName.lastIndexOf(".");
            if (i != -1)
                libName = libName.substring(i + 1);
            //$NON-NLS-1$
            fLibraryName = libName + ".jar";
        } else {
            //$NON-NLS-1$
            fLibraryName = ".";
        }
    }

    private void organizeExports(final IProject project) {
        PDEModelUtility.modifyModel(new ModelModification(PDEProject.getManifest(project)) {

            @Override
            protected void modifyModel(IBaseModel model, IProgressMonitor monitor) throws CoreException {
                if (!(model instanceof IBundlePluginModelBase))
                    return;
                OrganizeManifest.organizeExportPackages(((IBundlePluginModelBase) model).getBundleModel().getBundle(), project, true, true);
            }
        }, null);
    }

    private String createInitialName(String id) {
        int loc = id.lastIndexOf('.');
        if (loc == -1)
            return id;
        StringBuffer buf = new StringBuffer(id.substring(loc + 1));
        buf.setCharAt(0, Character.toUpperCase(buf.charAt(0)));
        return buf.toString();
    }

    private void createManifestFile(IFile file, IProgressMonitor monitor) throws CoreException {
        WorkspaceBundlePluginModel model = new WorkspaceBundlePluginModel(file, null);
        model.load();
        IBundle pluginBundle = model.getBundleModel().getBundle();
        String pluginId = pluginBundle.getHeader(Constants.BUNDLE_SYMBOLICNAME);
        String pluginName = pluginBundle.getHeader(Constants.BUNDLE_NAME);
        String pluginVersion = pluginBundle.getHeader(Constants.BUNDLE_VERSION);
        boolean missingInfo = (pluginId == null || pluginName == null || pluginVersion == null);
        // If no ID exists, create one
        if (pluginId == null) {
            pluginId = IdUtil.getValidId(file.getProject().getName());
        }
        // If no version number exists, create one
        if (pluginVersion == null) {
            //$NON-NLS-1$
            pluginVersion = "1.0.0.qualifier";
        }
        // If no name exists, create one using the non-null pluginID
        if (pluginName == null) {
            pluginName = createInitialName(pluginId);
        }
        pluginBundle.setHeader(Constants.BUNDLE_SYMBOLICNAME, pluginId);
        pluginBundle.setHeader(Constants.BUNDLE_VERSION, pluginVersion);
        pluginBundle.setHeader(Constants.BUNDLE_NAME, pluginName);
        if (missingInfo) {
            IPluginModelFactory factory = model.getPluginFactory();
            IPluginBase base = model.getPluginBase();
            if (//$NON-NLS-1$
            fLibraryName != null && !fLibraryName.equals(".")) {
                IPluginLibrary library = factory.createLibrary();
                library.setName(fLibraryName);
                library.setExported(true);
                base.add(library);
            }
            for (int i = 0; i < fLibEntries.length; i++) {
                IPluginLibrary library = factory.createLibrary();
                library.setName(fLibEntries[i]);
                library.setExported(true);
                base.add(library);
            }
            if (TargetPlatformHelper.getTargetVersion() >= 3.1)
                //$NON-NLS-1$
                pluginBundle.setHeader(//$NON-NLS-1$
                Constants.BUNDLE_MANIFESTVERSION, //$NON-NLS-1$
                "2");
        }
        model.save();
        monitor.done();
        organizeExports(file.getProject());
    }

    private boolean isOldTarget() {
        return TargetPlatformHelper.getTargetVersion() < 3.1;
    }
}
