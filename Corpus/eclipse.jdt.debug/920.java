/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal.event;

import java.io.IOException;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdi.internal.MirrorImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.connect.PacketReceiveManager;
import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.request.RequestID;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class EventQueueImpl extends MirrorImpl implements EventQueue {

    /** Flag used to see if a VMDisconnectEvent has already been generated. */
    private boolean genereatedVMDisconnectEvent = false;

    /**
	 * Creates new EventQueueImpl.
	 */
    public  EventQueueImpl(VirtualMachineImpl vmImpl) {
        //$NON-NLS-1$
        super("EventQueue", vmImpl);
    }

    /*
	 * @return Returns next EventSet from Virtual Machine.
	 */
    @Override
    public EventSet remove() throws InterruptedException {
        return remove(PacketReceiveManager.TIMEOUT_INFINITE);
    }

    /*
	 * @return Returns next EventSet from Virtual Machine, returns null if times
	 * out.
	 */
    @Override
    public EventSet remove(long timeout) throws InterruptedException {
        // events only).
        try {
            // We remove elements from event sets that are generated from
            // inside, therefore the set may become empty.
            EventSetImpl set;
            do {
                JdwpCommandPacket packet = getCommandVM(JdwpCommandPacket.E_COMPOSITE, timeout);
                initJdwpEventSet(packet);
                set = EventSetImpl.read(this, packet.dataInStream());
                handledJdwpEventSet();
            } while (set.isEmpty());
            return set;
        } catch (TimeoutException e) {
            handledJdwpEventSet();
            return null;
        } catch (IOException e) {
            handledJdwpEventSet();
            defaultIOExceptionHandler(e);
            return null;
        } catch (VMDisconnectedException e) {
            handledJdwpEventSet();
            if (!genereatedVMDisconnectEvent) {
                genereatedVMDisconnectEvent = true;
                return new EventSetImpl(virtualMachineImpl(), new VMDisconnectEventImpl(virtualMachineImpl(), RequestID.nullID));
            }
            throw e;
        }
    }
}
