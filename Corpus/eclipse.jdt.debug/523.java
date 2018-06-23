/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.jdi.tests;

import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.request.EventRequest;

/**
 * Listen for ClassPrepareEvent for a specific class.
 */
public class ClassPrepareEventWaiter extends EventWaiter {

    protected String fClassName;

    /**
	 * Constructor
	 * @param request
	 * @param shouldGo
	 * @param className
	 */
    public  ClassPrepareEventWaiter(EventRequest request, boolean shouldGo, String className) {
        super(request, shouldGo);
        fClassName = className;
    }

    /**
	 * @see org.eclipse.debug.jdi.tests.EventWaiter#classPrepare(com.sun.jdi.event.ClassPrepareEvent)
	 */
    @Override
    public boolean classPrepare(ClassPrepareEvent event) {
        if (event.referenceType().name().equals(fClassName)) {
            notifyEvent(event);
            return fShouldGo;
        }
        return true;
    }
}
