/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.core.runtime.Assert;

/**
 * Project of a refactoring history.
 *
 * @since 3.2
 */
public final class RefactoringHistoryProject extends RefactoringHistoryNode {

    /** The project */
    private final String fProject;

    /**
	 * Creates a new refactoring history project.
	 * 
	 * @param project the project
	 */
    public  RefactoringHistoryProject(final String project) {
        Assert.isNotNull(project);
        //$NON-NLS-1$
        Assert.isTrue(!"".equals(project));
        fProject = project;
    }

    @Override
    public boolean equals(final Object object) {
        if (object instanceof RefactoringHistoryProject) {
            final RefactoringHistoryProject node = (RefactoringHistoryProject) object;
            return super.equals(object) && getProject().equals(node.getProject()) && getKind() == node.getKind();
        }
        return false;
    }

    @Override
    public int getKind() {
        return PROJECT;
    }

    @Override
    public RefactoringHistoryNode getParent() {
        return null;
    }

    /**
	 * Returns the project.
	 *
	 * @return the project
	 */
    public String getProject() {
        return fProject;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + 17 * getKind() + 31 * getProject().hashCode();
    }
}
