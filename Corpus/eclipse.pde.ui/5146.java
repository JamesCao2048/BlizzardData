/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Peter Friese <peter.friese@gentleware.com> - bug 194694
 *******************************************************************************/
package org.eclipse.pde.internal.ui.wizards.imports;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.pde.internal.core.SearchablePluginsManager;

public class ExternalPluginLibrariesFilter extends ViewerFilter {

    /**
	 * Returns <code>false</code> if the given element is the External Plugin Libraries project,
	 * and <code>true</code> otherwise.
	 *
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        IProject project = null;
        if (element instanceof IJavaProject) {
            project = ((IJavaProject) element).getProject();
        } else if (element instanceof IProject) {
            project = (IProject) element;
        }
        if (project != null) {
            String projectName = project.getName();
            if (projectName.equals(SearchablePluginsManager.PROXY_PROJECT_NAME)) {
                return false;
            }
        }
        return true;
    }
}
