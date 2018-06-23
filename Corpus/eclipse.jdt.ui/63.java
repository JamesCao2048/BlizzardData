/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frits Jalvingh <jal@etc.to> - Contribution for Bug 459831 - [launching] Support attaching external annotations to a JRE container
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.wizards.buildpaths;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.javadoc.JavaDocLocations;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.jdt.ui.wizards.ClasspathAttributeConfiguration;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;

public class ExternalAnnotationsAttributeConfiguration extends ClasspathAttributeConfiguration {

    public  ExternalAnnotationsAttributeConfiguration() {
    }

    @Override
    public ImageDescriptor getImageDescriptor(ClasspathAttributeAccess attribute) {
        // FIXME Need image
        return JavaPluginImages.DESC_OBJS_EXTERNAL_ANNOTATION_LOCATION_ATTRIB;
    }

    @Override
    public String getNameLabel(ClasspathAttributeAccess attribute) {
        return NewWizardMessages.CPListLabelProvider_external_annotations_location_label;
    }

    @Override
    public String getValueLabel(ClasspathAttributeAccess access) {
        String arg = null;
        String str = access.getClasspathAttribute().getValue();
        if (str != null) {
            String prefix = JavaDocLocations.ARCHIVE_PREFIX;
            if (str.startsWith(prefix)) {
                int sepIndex = //$NON-NLS-1$
                str.lastIndexOf(//$NON-NLS-1$
                "!/");
                if (sepIndex == -1) {
                    arg = str.substring(prefix.length());
                } else {
                    String archive = str.substring(prefix.length(), sepIndex);
                    String root = str.substring(sepIndex + 2);
                    if (root.length() > 0) {
                        arg = Messages.format(NewWizardMessages.CPListLabelProvider_twopart, new String[] { BasicElementLabels.getURLPart(archive), BasicElementLabels.getURLPart(root) });
                    } else {
                        arg = BasicElementLabels.getURLPart(archive);
                    }
                }
            } else {
                arg = BasicElementLabels.getURLPart(str);
            }
        } else {
            arg = NewWizardMessages.CPListLabelProvider_none;
        }
        return arg;
    }

    @Override
    public boolean canEdit(ClasspathAttributeAccess attribute) {
        return true;
    }

    @Override
    public boolean canRemove(ClasspathAttributeAccess attribute) {
        return attribute.getClasspathAttribute().getValue() != null;
    }

    @Override
    public IClasspathAttribute performEdit(Shell shell, ClasspathAttributeAccess attribute) {
        String initialLocation = attribute.getClasspathAttribute().getValue();
        IPath locationPath = initialLocation == null ? null : new Path(initialLocation);
        IPath newPath = BuildPathDialogAccess.configureExternalAnnotationsAttachment(shell, locationPath);
        if (// Was the dialog cancelled?
        null == newPath)
            return null;
        return JavaCore.newClasspathAttribute(IClasspathAttribute.EXTERNAL_ANNOTATION_PATH, newPath.segmentCount() == 0 ? null : newPath.toPortableString());
    }

    @Override
    public IClasspathAttribute performRemove(ClasspathAttributeAccess attribute) {
        return JavaCore.newClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, null);
    }
}
