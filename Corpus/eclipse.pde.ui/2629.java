/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Code 9 Corporation - ongoing enhancements
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 477527
 *******************************************************************************/
package org.eclipse.pde.internal.core.builders;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.core.*;
import org.eclipse.pde.internal.core.project.PDEProject;
import org.osgi.framework.Bundle;

public class ManifestConsistencyChecker extends IncrementalProjectBuilder {

    private int MANIFEST = 0x1;

    private int EXTENSIONS = 0x2;

    private int BUILD = 0x4;

    private int STRUCTURE = 0x8;

    private static IProject[] EMPTY_LIST = new IProject[0];

    private SelfVisitor fSelfVisitor = new SelfVisitor();

    private ClassChangeVisitor fClassFileVisitor = new ClassChangeVisitor();

    class ClassChangeVisitor implements IResourceDeltaVisitor {

        boolean hasChanged = false;

        boolean veto = false;

        @Override
        public boolean visit(IResourceDelta delta) throws CoreException {
            if (delta != null && !veto) {
                int kind = delta.getKind();
                if (kind == IResourceDelta.CHANGED) {
                    IResource resource = delta.getResource();
                    if (resource instanceof IFile) {
                        String extension = resource.getFileExtension();
                        // do nothing if a java file has changed.
                        if (//$NON-NLS-1$
                        "java".equals(//$NON-NLS-1$
                        extension)) {
                            veto = true;
                        } else if (//$NON-NLS-1$
                        "class".equals(extension) && //$NON-NLS-1$
                        !hasChanged) {
                            // only interested in .class file changes
                            hasChanged = true;
                        }
                    }
                    return !veto;
                }
            }
            return false;
        }

        public void reset() {
            veto = false;
            hasChanged = false;
        }

        public boolean hasChanged() {
            return hasChanged && !veto;
        }
    }

    class SelfVisitor implements IResourceDeltaVisitor {

        int type = 0;

        @Override
        public boolean visit(IResourceDelta delta) throws CoreException {
            if (delta != null && type != (MANIFEST | EXTENSIONS | BUILD | STRUCTURE)) {
                int kind = delta.getKind();
                if (kind == IResourceDelta.ADDED || kind == IResourceDelta.REMOVED) {
                    type = MANIFEST | EXTENSIONS | BUILD | STRUCTURE;
                    if (PDECore.DEBUG_VALIDATION) {
                        //$NON-NLS-1$ //$NON-NLS-2$
                        System.out.print("Needs to rebuild project [" + getProject().getName() + "]: ");
                        System.out.print(delta.getResource().getProjectRelativePath().toString());
                        //$NON-NLS-1$
                        System.out.print(//$NON-NLS-1$
                        " - ");
                        //$NON-NLS-1$ //$NON-NLS-2$
                        System.out.println(kind == IResourceDelta.ADDED ? "added" : "removed");
                    }
                    return false;
                }
                IResource resource = delta.getResource();
                // by ignoring derived resources we should scale a bit better.
                if (resource.isDerived())
                    return false;
                if (resource.getType() == IResource.FILE) {
                    IFile file = (IFile) resource;
                    IProject project = file.getProject();
                    String name = resource.getName();
                    if (isLocalizationFile(resource)) {
                        type |= MANIFEST | EXTENSIONS;
                        if (PDECore.DEBUG_VALIDATION) {
                            //$NON-NLS-1$ //$NON-NLS-2$
                            System.out.print("Needs to rebuild manifest and extensions in project [" + getProject().getName() + "]: ");
                            System.out.print(delta.getResource().getProjectRelativePath().toString());
                            //$NON-NLS-1$
                            System.out.println(//$NON-NLS-1$
                            " - changed");
                        }
                    } else if (file.equals(PDEProject.getManifest(project))) {
                        type |= MANIFEST | EXTENSIONS | BUILD;
                        if (PDECore.DEBUG_VALIDATION) {
                            //$NON-NLS-1$ //$NON-NLS-2$
                            System.out.print("Needs to rebuild project [" + getProject().getName() + "]: ");
                            System.out.print(delta.getResource().getProjectRelativePath().toString());
                            //$NON-NLS-1$
                            System.out.println(//$NON-NLS-1$
                            " - changed");
                        }
                    } else if (name.endsWith(".exsd") || file.equals(PDEProject.getPluginXml(project)) || file.equals(//$NON-NLS-1$
                    PDEProject.getFragmentXml(//$NON-NLS-1$
                    project))) {
                        type |= EXTENSIONS;
                        if (PDECore.DEBUG_VALIDATION) {
                            //$NON-NLS-1$ //$NON-NLS-2$
                            System.out.print("Needs to rebuild extensions in project [" + getProject().getName() + "]: ");
                            System.out.print(delta.getResource().getProjectRelativePath().toString());
                            //$NON-NLS-1$
                            System.out.println(//$NON-NLS-1$
                            " - changed");
                        }
                    } else if (file.equals(PDEProject.getBuildProperties(project))) {
                        type |= BUILD;
                        if (PDECore.DEBUG_VALIDATION) {
                            //$NON-NLS-1$ //$NON-NLS-2$
                            System.out.print("Needs to rebuild build.properties in project [" + getProject().getName() + "]: ");
                            System.out.print(delta.getResource().getProjectRelativePath().toString());
                            //$NON-NLS-1$
                            System.out.println(//$NON-NLS-1$
                            " - changed");
                        }
                    }
                }
            }
            return type != (MANIFEST | EXTENSIONS | BUILD | STRUCTURE);
        }

        public int getType() {
            return type;
        }

        public void reset() {
            type = 0;
        }
    }

    private boolean isLocalizationFile(IResource file) {
        return file.equals(PDEProject.getLocalizationFile(getProject()));
    }

    @Override
    protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
        if (PDECore.getDefault().getBundle().getState() != Bundle.ACTIVE || monitor.isCanceled())
            return EMPTY_LIST;
        IProject project = getProject();
        if (!WorkspaceModelManager.isBinaryProject(project)) {
            int type = getDeltaType(project);
            if (type != 0) {
                validateProject(type, monitor);
            }
        }
        return EMPTY_LIST;
    }

    private int getDeltaType(IProject project) throws CoreException {
        IResourceDelta delta = getDelta(project);
        // always do a build of the project if a full build or an unspecified change has occurred
        if (delta == null) {
            if (PDECore.DEBUG_VALIDATION) {
                //$NON-NLS-1$ //$NON-NLS-2$
                System.out.println("Project [" + getProject().getName() + "] - full build");
            }
            return MANIFEST | EXTENSIONS | BUILD | STRUCTURE;
        }
        // has changed and a StateDelta was fired
        if (Boolean.TRUE.equals(project.getSessionProperty(PDECore.TOUCH_PROJECT))) {
            project.setSessionProperty(PDECore.TOUCH_PROJECT, null);
            if (PDECore.DEBUG_VALIDATION) {
                //$NON-NLS-1$ //$NON-NLS-2$
                System.out.println("Dependencies Changed: Project [" + getProject().getName() + "] - full build");
            }
            return MANIFEST | EXTENSIONS | BUILD;
        }
        // check if any "significant" files have been changed/added/removed
        // and build a subset or all manifest files accordingly
        fSelfVisitor.reset();
        delta.accept(fSelfVisitor);
        int type = fSelfVisitor.getType();
        // its content and this does not warrant a rebuild of manifest files.
        if ((type & MANIFEST | EXTENSIONS) != (MANIFEST | EXTENSIONS)) {
            fClassFileVisitor.reset();
            delta.accept(fClassFileVisitor);
            if (fClassFileVisitor.hasChanged()) {
                type |= MANIFEST | EXTENSIONS | BUILD;
                if (PDECore.DEBUG_VALIDATION) {
                    //$NON-NLS-1$ //$NON-NLS-2$
                    System.out.println("Class files changed due to dependency changes: Project [" + getProject().getName() + "] - rebuild manifest and properties files");
                }
            }
        }
        return type;
    }

    private void validateProject(int type, IProgressMonitor monitor) {
        SubMonitor subMonitor = SubMonitor.convert(monitor, PDECoreMessages.ManifestConsistencyChecker_builderTaskName, getWorkAmount(type));
        if ((type & STRUCTURE) != 0) {
            validateProjectStructure(type, subMonitor.split(1));
        }
        if ((type & MANIFEST | EXTENSIONS) != 0) {
            IProject project = getProject();
            IFile file = PDEProject.getPluginXml(project);
            if (!file.exists())
                file = PDEProject.getFragmentXml(project);
            if (file.exists()) {
                validateFiles(file, type, monitor);
            } else if ((type & MANIFEST) != 0) {
                IFile manifestFile = PDEProject.getManifest(project);
                if (manifestFile.exists())
                    validateManifestFile(manifestFile, subMonitor.split(1));
            }
        }
        if ((type & BUILD) != 0) {
            validateBuildProperties(subMonitor.split(1));
        }
    }

    private int getWorkAmount(int type) {
        int work = 1;
        if ((type & MANIFEST | EXTENSIONS) != 0)
            ++work;
        if ((type & BUILD) != 0)
            ++work;
        return work;
    }

    private void validateProjectStructure(int type, IProgressMonitor monitor) {
        if (monitor.isCanceled())
            return;
        // clear markers from project
        IProject project = getProject();
        try {
            project.deleteMarkers(PDEMarkerFactory.MARKER_ID, false, IResource.DEPTH_ZERO);
        } catch (CoreException e) {
        }
        // make sure build.properties exists
        validateBuildPropertiesExists(project);
        // if META-INF exists, make sure MANIFEST.MF exists in correct casing
        validateManifestCasing(project);
    }

    private void validateManifestFile(IFile file, IProgressMonitor monitor) {
        if (monitor.isCanceled())
            return;
        String message = NLS.bind(PDECoreMessages.Builders_verifying, file.getFullPath().toString());
        monitor.subTask(message);
        BundleErrorReporter reporter = new BundleErrorReporter(file);
        reporter.validateContent(monitor);
        monitor.subTask(PDECoreMessages.Builders_updating);
        monitor.done();
    }

    private void validateFiles(IFile file, int type, IProgressMonitor monitor) {
        if (monitor.isCanceled())
            return;
        String message = NLS.bind(PDECoreMessages.Builders_verifying, file.getFullPath().toString());
        monitor.subTask(message);
        IFile bundleManifest = PDEProject.getManifest(getProject());
        XMLErrorReporter reporter = null;
        BundleErrorReporter bundleReporter = null;
        if (bundleManifest.exists()) {
            if ((type & EXTENSIONS) != 0)
                reporter = new ExtensionsErrorReporter(file);
            if ((type & MANIFEST) != 0)
                bundleReporter = new BundleErrorReporter(bundleManifest);
        } else if ((type & MANIFEST) != 0 || (type & EXTENSIONS) != 0) {
            if (file.equals(PDEProject.getPluginXml(getProject()))) {
                reporter = new PluginErrorReporter(file);
            } else if (file.equals(PDEProject.getFragmentXml(getProject()))) {
                reporter = new FragmentErrorReporter(file);
            }
        }
        if (reporter != null) {
            DefaultSAXParser.parse(file, reporter);
            reporter.validateContent(monitor);
            monitor.subTask(PDECoreMessages.Builders_updating);
        }
        if (bundleReporter != null) {
            bundleReporter.validateContent(monitor);
            monitor.subTask(PDECoreMessages.Builders_updating);
        }
        monitor.done();
    }

    private void validateBuildProperties(IProgressMonitor monitor) {
        if (monitor.isCanceled())
            return;
        IProject project = getProject();
        IFile file = PDEProject.getBuildProperties(project);
        if (file.exists()) {
            monitor.subTask(PDECoreMessages.ManifestConsistencyChecker_buildPropertiesSubtask);
            BuildErrorReporter ber = new BuildErrorReporter(file);
            ber.validateContent(monitor);
        }
    }

    // Will place a marker on the project if the build.properties does not exist
    private void validateBuildPropertiesExists(IProject project) {
        IFile file = PDEProject.getBuildProperties(project);
        if (!file.exists()) {
            int severity = CompilerFlags.getFlag(project, CompilerFlags.P_BUILD);
            if (severity == CompilerFlags.IGNORE)
                return;
            // if build.properties doesn't exist and build problems != IGNORE, create a marker on the project bug 172451
            try {
                IMarker marker = project.createMarker(PDEMarkerFactory.MARKER_ID);
                marker.setAttribute(IMarker.SEVERITY, CompilerFlags.ERROR == severity ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING);
                marker.setAttribute(IMarker.MESSAGE, PDECoreMessages.ManifestConsistencyChecker_buildDoesNotExist);
            } catch (CoreException e) {
            }
        }
    }

    // Will place a marker on either the project (if META-INF exist but not a MANIFEST.MF) or on the MANIFEST.MF file with incorrect casing.
    private void validateManifestCasing(IProject project) {
        IFolder manifestFolder = PDEProject.getMetaInf(project);
        if (manifestFolder.exists()) {
            try {
                manifestFolder.deleteMarkers(PDEMarkerFactory.MARKER_ID, false, IResource.DEPTH_ONE);
            } catch (CoreException e1) {
            }
            // exit if the proper casing exists (should be majority of the time)
            if (PDEProject.getManifest(project).exists())
                return;
            IPath location = manifestFolder.getLocation();
            if (location != null) {
                File metaFolder = location.toFile();
                String[] fileList = metaFolder.list(new ManifestFilter());
                if (fileList == null || fileList.length == 0) {
                    // no MANIFEST.MF at all -> flag the project
                    try {
                        IMarker marker = project.createMarker(PDEMarkerFactory.MARKER_ID);
                        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                        marker.setAttribute(IMarker.MESSAGE, PDECoreMessages.ManifestConsistencyChecker_manifestDoesNotExist);
                    } catch (CoreException e) {
                    }
                } else {
                    // check for misspelled MANIFEST.MF files
                    for (int i = 0; i < fileList.length; i++) {
                        String fileName = fileList[i];
                        IFile currentFile = manifestFolder.getFile(fileName);
                        try {
                            IMarker marker = currentFile.createMarker(PDEMarkerFactory.MARKER_ID);
                            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                            marker.setAttribute(IMarker.MESSAGE, PDECoreMessages.ManifestConsistencyChecker_manifestMisspelled);
                        } catch (CoreException e) {
                        }
                    }
                }
            }
        }
    }

    class ManifestFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return (name.equalsIgnoreCase(ICoreConstants.MANIFEST_FILENAME));
        }
    }

    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
        SubMonitor localmonitor = SubMonitor.convert(monitor, NLS.bind(PDECoreMessages.ManifestConsistencyChecker_0, getProject().getName()), 1);
        try {
            // clean problem markers on the project
            cleanProblems(getProject(), IResource.DEPTH_ZERO);
            // clean the manifest directory (since errors can be created on manifest files with incorrect casing)
            IFile manifestFile = PDEProject.getManifest(getProject());
            cleanProblems(manifestFile.getParent(), IResource.DEPTH_ONE);
            // clean plug-in XML file
            cleanProblems(PDEProject.getPluginXml(getProject()), IResource.DEPTH_ZERO);
            // clean fragment XML file
            cleanProblems(PDEProject.getFragmentXml(getProject()), IResource.DEPTH_ZERO);
            // clean build properties
            cleanProblems(PDEProject.getBuildProperties(getProject()), IResource.DEPTH_ZERO);
            localmonitor.worked(1);
        } finally {
            localmonitor.done();
        }
    }

    /**
	 * Cleans PDE problems from the given resource to the specified depth.
	 *
	 * @param resource resource to delete markers from
	 * @param depth one of the depth constants in {@link IResource}
	 * @throws CoreException
	 */
    private void cleanProblems(IResource resource, int depth) throws CoreException {
        if (resource.exists()) {
            resource.deleteMarkers(PDEMarkerFactory.MARKER_ID, true, depth);
        }
    }
}
