/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.ant;

import org.apache.tools.ant.*;
import org.eclipse.pde.internal.core.exports.FeatureExportOperation;
import org.eclipse.pde.internal.core.exports.ProductExportOperation;

public class ExportBuildListener implements BuildListener {

    @Override
    public void buildStarted(BuildEvent event) {
    }

    @Override
    public void buildFinished(BuildEvent event) {
    }

    @Override
    public void targetStarted(BuildEvent event) {
    }

    @Override
    public void targetFinished(BuildEvent event) {
    }

    @Override
    public void taskStarted(BuildEvent event) {
    }

    //$NON-NLS-1$
    private static final String RUN_DIRECTOR = "runDirector";

    //$NON-NLS-1$
    private static final String DIRECTOR_OUTPUT = "p2.director.java.output";

    @Override
    public void taskFinished(BuildEvent event) {
        if (event.getException() != null && event.getTarget().getName().equals(RUN_DIRECTOR)) {
            String directorOutput = event.getProject().getProperty(DIRECTOR_OUTPUT);
            if (directorOutput != null) {
                int idx = //$NON-NLS-1$
                directorOutput.indexOf(//$NON-NLS-1$
                "Installation failed.");
                if (idx > -1) {
                    String part2 = directorOutput.substring(idx);
                    ProductExportOperation.setErrorMessage(part2);
                }
            }
        }
    }

    @Override
    public void messageLogged(BuildEvent event) {
        if (event.getPriority() == Project.MSG_ERR) {
            FeatureExportOperation.errorFound();
        }
    }
}
