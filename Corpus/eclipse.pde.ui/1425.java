/*******************************************************************************
 * Copyright (c) 2009, 2015 eXXcellent solutions gmbh and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Achim Demelt, eXXcellent solutions gmbh - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.pde.internal.ui.PDEPlugin;

public class GenericExceptionStatusHandler implements IStatusHandler {

    @Override
    public Object handleStatus(IStatus status, Object source) throws CoreException {
        PDEPlugin.logException(status.getException());
        return null;
    }
}
