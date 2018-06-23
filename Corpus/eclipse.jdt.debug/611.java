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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdi.internal.MirrorImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.EventRequestImpl;
import com.sun.jdi.InternalException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.request.EventRequest;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class EventSetImpl extends MirrorImpl implements EventSet {

    /** Set that is used to store events. */
    private List<Event> fEvents;

    /** Which threads were suspended by this composite event. */
    private byte fSuspendPolicy;

    /**
	 * Creates new EventSetImpl.
	 */
    private  EventSetImpl(VirtualMachineImpl vmImpl) {
        //$NON-NLS-1$
        super("EventSet", vmImpl);
    }

    /**
	 * Creates new EventSetImpl with events in a given array.
	 */
    public  EventSetImpl(VirtualMachineImpl vmImpl, EventImpl[] events) {
        this(vmImpl);
        fEvents = new ArrayList<Event>(events.length);
        for (EventImpl event : events) fEvents.add(event);
    }

    /**
	 * Creates new EventSetImpl with given event.
	 */
    public  EventSetImpl(VirtualMachineImpl vmImpl, EventImpl event) {
        this(vmImpl);
        fEvents = new ArrayList<Event>(1);
        fEvents.add(event);
    }

    /**
	 * @return Returns iterator over events.
	 */
    @Override
    public EventIterator eventIterator() {
        return new EventIteratorImpl(fEvents.listIterator());
    }

    /**
	 * @return Returns which threads were suspended by this composite event.
	 */
    @Override
    public int suspendPolicy() {
        switch(fSuspendPolicy) {
            case EventRequestImpl.SUSPENDPOL_NONE_JDWP:
                return EventRequest.SUSPEND_NONE;
            case EventRequestImpl.SUSPENDPOL_EVENT_THREAD_JDWP:
                return EventRequest.SUSPEND_EVENT_THREAD;
            case EventRequestImpl.SUSPENDPOL_ALL_JDWP:
                return EventRequest.SUSPEND_ALL;
            default:
                throw new InternalException(EventMessages.EventSetImpl_Invalid_suspend_policy_encountered___1 + fSuspendPolicy);
        }
    }

    /**
	 * Resumes threads that were suspended by this event set.
	 */
    @Override
    public void resume() {
        switch(fSuspendPolicy) {
            case EventRequestImpl.SUSPENDPOL_NONE_JDWP:
                break;
            case EventRequestImpl.SUSPENDPOL_EVENT_THREAD_JDWP:
                resumeThreads();
                break;
            case EventRequestImpl.SUSPENDPOL_ALL_JDWP:
                virtualMachineImpl().resume();
                break;
            default:
                throw new InternalException(EventMessages.EventSetImpl_Invalid_suspend_policy_encountered___1 + fSuspendPolicy);
        }
    }

    /**
	 * Resumes threads that were suspended by this event set.
	 */
    private void resumeThreads() {
        if (fEvents.size() == 1) {
            // Most event sets have only one event.
            // Avoid expensive object creation.
            ThreadReference ref = ((EventImpl) fEvents.get(0)).thread();
            if (ref != null) {
                ref.resume();
            } else {
                fEvents.get(0).virtualMachine().resume();
            }
            return;
        }
        Iterator<Event> iter = fEvents.iterator();
        List<ThreadReference> resumedThreads = new ArrayList<ThreadReference>(fEvents.size());
        while (iter.hasNext()) {
            EventImpl event = (EventImpl) iter.next();
            ThreadReference thread = event.thread();
            if (thread == null) {
                event.virtualMachine().resume();
                return;
            }
            if (!resumedThreads.contains(thread)) {
                resumedThreads.add(thread);
            }
        }
        Iterator<ThreadReference> resumeIter = resumedThreads.iterator();
        while (resumeIter.hasNext()) {
            resumeIter.next().resume();
        }
    }

    /**
	 * @return Returns EventSetImpl that was read from InputStream.
	 */
    public static EventSetImpl read(MirrorImpl target, DataInputStream in) throws IOException {
        VirtualMachineImpl vmImpl = target.virtualMachineImpl();
        EventSetImpl eventSet = new EventSetImpl(vmImpl);
        // Read suspend policy.
        eventSet.fSuspendPolicy = target.readByte(//$NON-NLS-1$
        "suspendPolicy", //$NON-NLS-1$
        EventRequestImpl.suspendPolicyMap(), //$NON-NLS-1$
        in);
        // Read size.
        //$NON-NLS-1$
        int size = target.readInt("size", in);
        // Create event list.
        eventSet.fEvents = new ArrayList<Event>(size);
        while (size-- > 0) {
            EventImpl event = EventImpl.read(target, in);
            // See ClassPrepareEvent.
            if (event == null)
                continue;
            EventRequestImpl request = (EventRequestImpl) event.request();
            // from inside this JDI implementation.
            if (request == null || !request.isGeneratedInside())
                eventSet.fEvents.add(event);
        }
        return eventSet;
    }

    /**
	 * @see java.util.Collection
	 */
    @Override
    public boolean contains(Object event) {
        return fEvents.contains(event);
    }

    /* (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
    @Override
    public boolean containsAll(Collection<?> events) {
        return fEvents.containsAll(events);
    }

    /**
	 * @see java.util.Collection
	 */
    @Override
    public boolean equals(Object object) {
        return object != null && object.getClass().equals(this.getClass()) && fEvents.equals(((EventSetImpl) object).fEvents);
    }

    /**
	 * @see java.util.Collection
	 */
    @Override
    public int hashCode() {
        return fEvents.hashCode();
    }

    /**
	 * @see java.util.Collection
	 */
    @Override
    public boolean isEmpty() {
        return fEvents.isEmpty();
    }

    /**
	 * @see java.util.Collection#iterator()
	 */
    @Override
    public Iterator<Event> iterator() {
        return fEvents.iterator();
    }

    /**
	 * @see java.util.Collection#size()
	 */
    @Override
    public int size() {
        return fEvents.size();
    }

    /**
	 * @see java.util.Collection#toArray()
	 */
    @Override
    public Object[] toArray() {
        return fEvents.toArray();
    }

    /**
	 * @see java.util.Collection#clear()
	 * @exception UnsupportedOperationException
	 *                always thrown since EventSets are unmodifiable.
	 */
    @Override
    public void clear() {
        throw new UnsupportedOperationException(EventMessages.EventSetImpl_EventSets_are_unmodifiable_3);
    }

    /**
	 * @see java.util.Collection#remove(Object)
	 * @exception UnsupportedOperationException
	 *                always thrown since EventSets are unmodifiable.
	 */
    @Override
    public boolean remove(Object arg1) {
        throw new UnsupportedOperationException(EventMessages.EventSetImpl_EventSets_are_unmodifiable_3);
    }

    /**
	 * @see java.util.Collection#removeAll(Collection)
	 * @exception UnsupportedOperationException
	 *                always thrown since EventSets are unmodifiable.
	 */
    @Override
    public boolean removeAll(Collection<?> arg1) {
        throw new UnsupportedOperationException(EventMessages.EventSetImpl_EventSets_are_unmodifiable_3);
    }

    /**
	 * @see java.util.Collection#retainAll(Collection)
	 * @exception UnsupportedOperationException
	 *                always thrown since EventSets are unmodifiable.
	 */
    @Override
    public boolean retainAll(Collection<?> arg1) {
        throw new UnsupportedOperationException(EventMessages.EventSetImpl_EventSets_are_unmodifiable_3);
    }

    /* (non-Javadoc)
	 * @see java.util.Collection#toArray(T[])
	 */
    @Override
    public <T> T[] toArray(T[] a) {
        return fEvents.toArray(a);
    }

    /* (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
    @Override
    public boolean add(Event o) {
        throw new UnsupportedOperationException(EventMessages.EventSetImpl_EventSets_are_unmodifiable_3);
    }

    /* (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
    @Override
    public boolean addAll(Collection<? extends Event> c) {
        throw new UnsupportedOperationException(EventMessages.EventSetImpl_EventSets_are_unmodifiable_3);
    }
}
