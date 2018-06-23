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
import org.eclipse.jdi.internal.event.MonitorWaitedEventImpl;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 * @since 3.3
 */
public class MonitorWaitedRequestImpl extends EventRequestImpl implements com.sun.jdi.request.MonitorWaitedRequest {

    /** constructor **/
    public  MonitorWaitedRequestImpl(VirtualMachineImpl vmImpl) {
        //$NON-NLS-1$
        super("MonitorWaitedRequestImpl", vmImpl);
    }

    /**
	 * @return Returns JDWP EventKind.
	 */
    @Override
    protected byte eventKind() {
        return MonitorWaitedEventImpl.EVENT_KIND;
    }
}
