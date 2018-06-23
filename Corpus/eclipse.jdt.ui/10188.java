/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.history;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

/**
 * Default implementation of a refactoring descriptor proxy.
 *
 * @since 3.2
 */
final class DefaultRefactoringDescriptorProxy extends RefactoringDescriptorProxy {

    /** The description of the refactoring */
    private final String fDescription;

    /** The non-empty name of the project, or <code>null</code> */
    private final String fProject;

    /** The time stamp of the refactoring */
    private final long fTimeStamp;

    /**
	 * Creates a new default refactoring descriptor proxy.
	 *
	 * @param description
	 *            the description
	 * @param project
	 *            the project name, or <code>null</code>
	 * @param stamp
	 *            the time stamp
	 */
    public  DefaultRefactoringDescriptorProxy(final String description, final String project, final long stamp) {
        //$NON-NLS-1$
        Assert.isTrue(project == null || !"".equals(project));
        //$NON-NLS-1$
        Assert.isTrue(description != null && !"".equals(description));
        fDescription = description.intern();
        fProject = project != null ? project.intern() : null;
        fTimeStamp = stamp;
    }

    @Override
    public String getDescription() {
        return fDescription;
    }

    @Override
    public String getProject() {
        return fProject;
    }

    @Override
    public long getTimeStamp() {
        return fTimeStamp;
    }

    @Override
    public String toString() {
        final StringBuffer buffer = new StringBuffer(128);
        buffer.append(getClass().getName());
        //$NON-NLS-1$
        buffer.append("[stamp=");
        buffer.append(fTimeStamp);
        //$NON-NLS-1$
        buffer.append(",project=");
        buffer.append(fProject);
        //$NON-NLS-1$
        buffer.append(",description=");
        buffer.append(fDescription);
        //$NON-NLS-1$
        buffer.append("]");
        return buffer.toString();
    }
}
