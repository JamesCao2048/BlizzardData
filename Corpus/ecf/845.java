/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.sharedobject.events;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.ISharedObjectManager;

/**
 * Shared object manager event sent/triggered when a shared object is added to
 * container via
 * {@link ISharedObjectManager#addSharedObject(ID, org.eclipse.ecf.core.sharedobject.ISharedObject, java.util.Map)}
 * is called
 */
public class SharedObjectManagerAddEvent implements ISharedObjectManagerEvent {

    ID localContainerID = null;

    ID sharedObjectID = null;

    public  SharedObjectManagerAddEvent(ID localContainerID, ID sharedObjectID) {
        this.localContainerID = localContainerID;
        this.sharedObjectID = sharedObjectID;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.events.IContainerEvent#getLocalContainerID()
	 */
    public ID getLocalContainerID() {
        return localContainerID;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.sharedobject.events.ISharedObjectManagerEvent#getSharedObjectID()
	 */
    public ID getSharedObjectID() {
        return sharedObjectID;
    }

    public String toString() {
        //$NON-NLS-1$
        StringBuffer buf = new StringBuffer("SharedObjectManagerAddEvent[");
        //$NON-NLS-1$
        buf.append(getLocalContainerID()).append(";");
        //$NON-NLS-1$
        buf.append(getSharedObjectID()).append("]");
        return buf.toString();
    }
}
