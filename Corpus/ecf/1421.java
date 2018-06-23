/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.tests.sharedobject;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.ReplicaSharedObjectDescription;
import org.eclipse.ecf.core.sharedobject.SharedObjectInitException;
import org.eclipse.ecf.core.sharedobject.SharedObjectMsg;
import org.eclipse.ecf.core.sharedobject.TransactionSharedObject;

/**
 *
 */
public class TestTransactionSharedObject extends TransactionSharedObject {

    public static final String NAME_PROPERTY = "name";

    String name;

    /**
	 * Primary constructor
	 * @param name the name to say hello to
	 */
    public  TestTransactionSharedObject(String name, IMessageReceiver receiver) {
        this.name = name;
    }

    /**
	 * Replica constructor (null constructor)
	 */
    public  TestTransactionSharedObject() {
        super();
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ecf.core.sharedobject.BaseSharedObject#initialize()
	 */
    protected void initialize() throws SharedObjectInitException {
        super.initialize();
        if (isPrimary()) {
            System.out.println("Primary(" + getContext().getLocalContainerID() + ") says Hello " + name);
        } else {
            // This is a replica, so initialize the name from property
            name = (String) getConfig().getProperties().get(NAME_PROPERTY);
            System.out.println("Replica(" + getContext().getLocalContainerID() + ") says Hello " + name);
        }
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ecf.core.sharedobject.BaseSharedObject#getReplicaDescription(org.eclipse.ecf.core.identity.ID)
	 */
    protected ReplicaSharedObjectDescription getReplicaDescription(ID receiver) {
        // Put primary state into properties and include in replica description
        final Map properties = new HashMap();
        properties.put(NAME_PROPERTY, name);
        return new ReplicaSharedObjectDescription(this.getClass(), getConfig().getSharedObjectID(), getConfig().getHomeContainerID(), properties);
    }

    protected boolean handleSharedObjectMsg(SharedObjectMsg msg) {
        try {
            msg.invoke(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
