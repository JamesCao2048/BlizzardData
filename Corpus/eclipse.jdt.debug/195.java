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
package org.eclipse.jdi.internal.request;

import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.event.ClassUnloadEventImpl;
import com.sun.jdi.request.ClassUnloadRequest;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class ClassUnloadRequestImpl extends EventRequestImpl implements ClassUnloadRequest {

    /**
	 * Creates new ClassUnloadRequest.
	 */
    public  ClassUnloadRequestImpl(VirtualMachineImpl vmImpl) {
        //$NON-NLS-1$
        super("ClassUnloadRequest", vmImpl);
    }

    /**
	 * @return Returns JDWP EventKind.
	 */
    @Override
    protected final byte eventKind() {
        return ClassUnloadEventImpl.EVENT_KIND;
    }
}
