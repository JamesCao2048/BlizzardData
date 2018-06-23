/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal;

import org.eclipse.pde.api.tools.internal.provisional.IApiAccess;

/**
 * Default implementation of {@link IApiAccess}
 *
 * @since 1.0.1
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ApiAccess implements IApiAccess {

    public static final IApiAccess NORMAL_ACCESS = new NormalAccess();

    static class NormalAccess implements IApiAccess {

        @Override
        public int getAccessLevel() {
            return IApiAccess.NORMAL;
        }
    }

    private int access = IApiAccess.NORMAL;

    /**
	 * Constructor
	 *
	 * @param access
	 */
    public  ApiAccess(int access) {
        this.access = access;
    }

    @Override
    public int getAccessLevel() {
        return this.access;
    }

    @Override
    public int hashCode() {
        return this.access;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IApiAccess) {
            return this.access == ((IApiAccess) obj).getAccessLevel();
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        //$NON-NLS-1$
        buffer.append("Access Level: ");
        buffer.append(getAccessText(getAccessLevel()));
        return buffer.toString();
    }

    /**
	 * Returns a textual representation of an {@link IApiAccess}
	 *
	 * @param access
	 * @return the textual representation of an {@link IApiAccess}
	 */
    public static String getAccessText(int access) {
        switch(access) {
            case IApiAccess.NORMAL:
                return //$NON-NLS-1$
                "NORMAL";
            case IApiAccess.FRIEND:
                return //$NON-NLS-1$
                "FRIEND";
            default:
                break;
        }
        //$NON-NLS-1$
        return "<UNKNOWN ACCESS LEVEL>";
    }
}
