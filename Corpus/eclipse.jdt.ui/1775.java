/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.junit.buildpath;

import java.util.ArrayList;
import org.eclipse.jdt.junit.JUnitCore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.junit.JUnitCorePlugin;
import org.eclipse.jdt.internal.junit.JUnitMessages;
import org.eclipse.jdt.internal.junit.JUnitPreferencesConstants;

public class JUnitContainerInitializer extends ClasspathContainerInitializer {

    private static final IStatus NOT_SUPPORTED = new Status(IStatus.ERROR, JUnitCorePlugin.CORE_PLUGIN_ID, ClasspathContainerInitializer.ATTRIBUTE_NOT_SUPPORTED, new String(), null);

    private static final IStatus READ_ONLY = new Status(IStatus.ERROR, JUnitCorePlugin.CORE_PLUGIN_ID, ClasspathContainerInitializer.ATTRIBUTE_READ_ONLY, new String(), null);

    /**
	 * @deprecated just for compatibility
	 */
    @Deprecated
    private static final String //$NON-NLS-1$
    JUNIT3_8_1 = "3.8.1";

    //$NON-NLS-1$
    private static final String JUNIT3 = "3";

    //$NON-NLS-1$
    private static final String JUNIT4 = "4";

    private static class JUnitContainer implements IClasspathContainer {

        private final IClasspathEntry[] fEntries;

        private final IPath fPath;

        public  JUnitContainer(IPath path, IClasspathEntry[] entries) {
            fPath = path;
            fEntries = entries;
        }

        @Override
        public IClasspathEntry[] getClasspathEntries() {
            return fEntries;
        }

        @Override
        public String getDescription() {
            if (JUnitCore.JUNIT4_CONTAINER_PATH.equals(fPath)) {
                return JUnitMessages.JUnitContainerInitializer_description_junit4;
            }
            return JUnitMessages.JUnitContainerInitializer_description_junit3;
        }

        @Override
        public int getKind() {
            return IClasspathContainer.K_APPLICATION;
        }

        @Override
        public IPath getPath() {
            return fPath;
        }
    }

    public  JUnitContainerInitializer() {
    }

    @Override
    public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
        if (isValidJUnitContainerPath(containerPath)) {
            JUnitContainer container = getNewContainer(containerPath);
            JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { project }, new IClasspathContainer[] { container }, null);
        }
    }

    private static JUnitContainer getNewContainer(IPath containerPath) {
        IClasspathEntry entry = null;
        IClasspathEntry entry2 = null;
        String version = containerPath.segment(1);
        if (JUNIT3_8_1.equals(version) || JUNIT3.equals(version)) {
            entry = BuildPathSupport.getJUnit3LibraryEntry();
            if (// JUnit 4 includes most of JUnit 3, so let's cheat
            entry == null) {
                entry = BuildPathSupport.getJUnit4as3LibraryEntry();
            }
        } else if (JUNIT4.equals(version)) {
            entry = BuildPathSupport.getJUnit4LibraryEntry();
            entry2 = BuildPathSupport.getHamcrestCoreLibraryEntry();
        }
        IClasspathEntry[] entries;
        if (entry == null) {
            entries = new IClasspathEntry[] {};
        } else if (entry2 == null) {
            entries = new IClasspathEntry[] { entry };
        } else {
            entries = new IClasspathEntry[] { entry, entry2 };
        }
        return new JUnitContainer(containerPath, entries);
    }

    private static boolean isValidJUnitContainerPath(IPath path) {
        return path != null && path.segmentCount() == 2 && JUnitCore.JUNIT_CONTAINER_ID.equals(path.segment(0));
    }

    @Override
    public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project) {
        return true;
    }

    @Override
    public IStatus getAccessRulesStatus(IPath containerPath, IJavaProject project) {
        return READ_ONLY;
    }

    @Override
    public IStatus getSourceAttachmentStatus(IPath containerPath, IJavaProject project) {
        return READ_ONLY;
    }

    @Override
    public IStatus getAttributeStatus(IPath containerPath, IJavaProject project, String attributeKey) {
        if (attributeKey.equals(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME)) {
            return Status.OK_STATUS;
        }
        return NOT_SUPPORTED;
    }

    @Override
    public void requestClasspathContainerUpdate(IPath containerPath, IJavaProject project, IClasspathContainer containerSuggestion) throws CoreException {
        IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(JUnitCorePlugin.CORE_PLUGIN_ID);
        IClasspathEntry[] entries = containerSuggestion.getClasspathEntries();
        if (entries.length >= 1 && isValidJUnitContainerPath(containerPath)) {
            String version = containerPath.segment(1);
            // only modifiable entry is Javadoc location
            for (int i = 0; i < entries.length; i++) {
                IClasspathEntry entry = entries[i];
                String preferenceKey = getPreferenceKey(entry, version);
                IClasspathAttribute[] extraAttributes = entry.getExtraAttributes();
                if (extraAttributes.length == 0) {
                    // Revert to default
                    String defaultValue = //$NON-NLS-1$
                    DefaultScope.INSTANCE.getNode(JUnitCorePlugin.CORE_PLUGIN_ID).get(//$NON-NLS-1$
                    preferenceKey, //$NON-NLS-1$
                    "");
                    if (!defaultValue.equals(preferences.get(preferenceKey, defaultValue))) {
                        preferences.put(preferenceKey, defaultValue);
                    }
                /* 
					 * The following would be correct, but would not allow to revert to the default.
					 * There's no concept of "default value" for a classpath attribute, see
					 * org.eclipse.jdt.internal.ui.preferences.JavadocConfigurationBlock.performDefaults()
					 */
                // preferenceStore.setValue(preferenceKey, "");
                } else {
                    for (int j = 0; j < extraAttributes.length; j++) {
                        IClasspathAttribute attrib = extraAttributes[j];
                        if (attrib.getName().equals(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME)) {
                            if (preferenceKey != null) {
                                preferences.put(preferenceKey, attrib.getValue());
                            }
                            break;
                        }
                    }
                }
            }
            rebindClasspathEntries(project.getJavaModel(), containerPath);
        }
    }

    private String getPreferenceKey(IClasspathEntry entry, String version) {
        if (JUNIT3.equals(version)) {
            return JUnitPreferencesConstants.JUNIT3_JAVADOC;
        } else if (JUNIT4.equals(version)) {
            if (//$NON-NLS-1$
            entry.getPath().lastSegment().indexOf("junit") != -1) {
                return JUnitPreferencesConstants.JUNIT4_JAVADOC;
            } else {
                return JUnitPreferencesConstants.HAMCREST_CORE_JAVADOC;
            }
        }
        return null;
    }

    private static void rebindClasspathEntries(IJavaModel model, IPath containerPath) throws JavaModelException {
        ArrayList<IJavaProject> affectedProjects = new ArrayList();
        IJavaProject[] projects = model.getJavaProjects();
        for (int i = 0; i < projects.length; i++) {
            IJavaProject project = projects[i];
            IClasspathEntry[] entries = project.getRawClasspath();
            for (int k = 0; k < entries.length; k++) {
                IClasspathEntry curr = entries[k];
                if (curr.getEntryKind() == IClasspathEntry.CPE_CONTAINER && containerPath.equals(curr.getPath())) {
                    affectedProjects.add(project);
                }
            }
        }
        if (!affectedProjects.isEmpty()) {
            IJavaProject[] affected = affectedProjects.toArray(new IJavaProject[affectedProjects.size()]);
            IClasspathContainer[] containers = new IClasspathContainer[affected.length];
            for (int i = 0; i < containers.length; i++) {
                containers[i] = getNewContainer(containerPath);
            }
            JavaCore.setClasspathContainer(containerPath, affected, containers, null);
        }
    }

    /**
	 * @see org.eclipse.jdt.core.ClasspathContainerInitializer#getDescription(org.eclipse.core.runtime.IPath, org.eclipse.jdt.core.IJavaProject)
	 */
    @Override
    public String getDescription(IPath containerPath, IJavaProject project) {
        if (isValidJUnitContainerPath(containerPath)) {
            String version = containerPath.segment(1);
            if (JUNIT3_8_1.equals(version) || JUNIT3.equals(version)) {
                return JUnitMessages.JUnitContainerInitializer_description_initializer_junit3;
            } else if (JUNIT4.equals(version)) {
                return JUnitMessages.JUnitContainerInitializer_description_initializer_junit4;
            }
        }
        return JUnitMessages.JUnitContainerInitializer_description_initializer_unresolved;
    }

    @Override
    public Object getComparisonID(IPath containerPath, IJavaProject project) {
        return containerPath;
    }
}
