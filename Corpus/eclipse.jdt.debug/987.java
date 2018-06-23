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
package org.eclipse.jdt.internal.debug.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.EventRequest;

public class EventDispatcher implements Runnable {

    /**
	 * The debug target this event dispatcher belongs to.
	 */
    private JDIDebugTarget fTarget;

    /**
	 * Whether this dispatcher is shutdown.
	 */
    private boolean fShutdown;

    /**
	 * Table of event listeners. Table is a mapping of <code>EventRequest</code>
	 * to <code>IJDIEventListener</code>.
	 */
    private HashMap<EventRequest, IJDIEventListener> fEventHandlers;

    /**
	 * Queue of debug model events to fire, created when processing events on
	 * the target VM. Keyed by event sets, processed independently.
	 */
    private Map<EventSet, List<DebugEvent>> fSetToQueue = new HashMap<EventSet, List<DebugEvent>>();

    /**
	 * Constructs a new event dispatcher listening for events originating from
	 * the specified debug target's underlying VM.
	 * 
	 * @param target
	 *            the target this event dispatcher belongs to
	 */
    public  EventDispatcher(JDIDebugTarget target) {
        fEventHandlers = new HashMap<EventRequest, IJDIEventListener>(10);
        fTarget = target;
        fShutdown = false;
    }

    /**
	 * Dispatch the given event set.
	 * 
	 * @param eventSet
	 *            events to dispatch
	 */
    private void dispatch(EventSet eventSet) {
        if (isShutdown()) {
            return;
        }
        if (JDIDebugOptions.DEBUG_JDI_EVENTS) {
            EventIterator eventIter = eventSet.eventIterator();
            //$NON-NLS-1$
            StringBuffer buf = new StringBuffer("JDI Event Set: {\n");
            while (eventIter.hasNext()) {
                buf.append(eventIter.next());
                if (eventIter.hasNext()) {
                    //$NON-NLS-1$
                    buf.append(", ");
                }
            }
            //$NON-NLS-1$
            buf.append("}\n");
            JDIDebugOptions.trace(buf.toString());
        }
        EventIterator iter = eventSet.eventIterator();
        IJDIEventListener[] listeners = new IJDIEventListener[eventSet.size()];
        boolean vote = false;
        boolean resume = true;
        int index = -1;
        List<Event> deferredEvents = null;
        while (iter.hasNext()) {
            index++;
            if (isShutdown()) {
                return;
            }
            Event event = iter.nextEvent();
            if (event == null) {
                continue;
            }
            // Dispatch events to registered listeners, if any
            IJDIEventListener listener = fEventHandlers.get(event.request());
            listeners[index] = listener;
            if (listener != null) {
                if (listener instanceof IJavaLineBreakpoint) {
                    // other listeners vote.
                    try {
                        if (((IJavaLineBreakpoint) listener).isConditionEnabled()) {
                            if (deferredEvents == null) {
                                deferredEvents = new ArrayList<Event>(5);
                            }
                            deferredEvents.add(event);
                            continue;
                        }
                    } catch (CoreException exception) {
                        JDIDebugPlugin.log(exception);
                    }
                }
                vote = true;
                resume = listener.handleEvent(event, fTarget, !resume, eventSet) && resume;
                continue;
            }
            // Dispatch VM start/end events
            if (event instanceof VMDeathEvent) {
                fTarget.handleVMDeath((VMDeathEvent) event);
                // stop listening for events
                shutdown();
            } else if (event instanceof VMDisconnectEvent) {
                fTarget.handleVMDisconnect((VMDisconnectEvent) event);
                // stop listening for events
                shutdown();
            } else if (event instanceof VMStartEvent) {
                fTarget.handleVMStart((VMStartEvent) event);
            } else {
            // not handled
            }
        }
        // process deferred conditional breakpoint events
        if (deferredEvents != null) {
            Iterator<Event> deferredIter = deferredEvents.iterator();
            while (deferredIter.hasNext()) {
                if (isShutdown()) {
                    return;
                }
                Event event = deferredIter.next();
                if (event == null) {
                    continue;
                }
                // Dispatch events to registered listeners, if any
                IJDIEventListener listener = fEventHandlers.get(event.request());
                if (listener != null) {
                    vote = true;
                    resume = listener.handleEvent(event, fTarget, !resume, eventSet) && resume;
                    continue;
                }
            }
        }
        // notify handlers of the end result
        index = -1;
        iter = eventSet.eventIterator();
        while (iter.hasNext()) {
            index++;
            Event event = iter.nextEvent();
            // notify registered listener, if any
            IJDIEventListener listener = listeners[index];
            if (listener != null) {
                listener.eventSetComplete(event, fTarget, !resume, eventSet);
            }
        }
        // fire queued DEBUG events
        fireEvents(eventSet);
        if (vote && resume) {
            try {
                eventSet.resume();
            } catch (VMDisconnectedException e) {
            } catch (RuntimeException e) {
                try {
                    fTarget.targetRequestFailed(JDIDebugMessages.EventDispatcher_0, e);
                } catch (DebugException de) {
                    JDIDebugPlugin.log(de);
                }
            }
        }
    }

    /**
	 * Continuously reads events that are coming from the event queue, until
	 * this event dispatcher is shutdown. A debug target starts a thread on this
	 * method on startup.
	 * 
	 * @see #shutdown()
	 */
    @Override
    public void run() {
        VirtualMachine vm = fTarget.getVM();
        if (vm != null) {
            EventQueue q = vm.eventQueue();
            EventSet eventSet = null;
            while (!isShutdown()) {
                try {
                    try {
                        // Get the next event set.
                        eventSet = q.remove(1000);
                    } catch (VMDisconnectedException e) {
                        break;
                    }
                    if (!isShutdown() && eventSet != null) {
                        final EventSet set = eventSet;
                        Job job = new Job("JDI Event Dispatch") {

                            @Override
                            protected IStatus run(IProgressMonitor monitor) {
                                dispatch(set);
                                return Status.OK_STATUS;
                            }

                            @Override
                            public boolean belongsTo(Object family) {
                                if (family instanceof Class) {
                                    Class<?> clazz = (Class<?>) family;
                                    EventIterator iterator = set.eventIterator();
                                    while (iterator.hasNext()) {
                                        Event event = iterator.nextEvent();
                                        if (clazz.isInstance(event)) {
                                            return true;
                                        }
                                    }
                                }
                                return false;
                            }
                        };
                        job.setSystem(true);
                        job.schedule();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    /**
	 * Shutdown this event dispatcher - i.e. causes this event dispatcher to
	 * stop reading and dispatching events from the event queue. The thread
	 * associated with this runnable will exit.
	 */
    public void shutdown() {
        fShutdown = true;
    }

    /**
	 * Returns whether this event dispatcher has been shutdown.
	 * 
	 * @return whether this event dispatcher has been shutdown
	 */
    private boolean isShutdown() {
        return fShutdown;
    }

    /**
	 * Registers the given listener for with the given event request. When an
	 * event is received from the underlying VM, that is associated with the
	 * given event request, the listener will be notified.
	 * 
	 * @param listener
	 *            the listener to register
	 * @param request
	 *            the event request associated with events the listener is
	 *            interested in
	 */
    public void addJDIEventListener(IJDIEventListener listener, EventRequest request) {
        fEventHandlers.put(request, listener);
    }

    /**
	 * De-registers the given listener and event request. The listener will no
	 * longer be notified of events associated with the request. Listeners are
	 * responsible for deleting the associated event request if required.
	 * 
	 * @param listener
	 *            the listener to de-register
	 * @param request
	 *            the event request to de-register
	 */
    public void removeJDIEventListener(IJDIEventListener listener, EventRequest request) {
        fEventHandlers.remove(request);
    }

    /**
	 * Adds the given event to the queue of debug events to fire when done
	 * dispatching events from the given event set.
	 * 
	 * @param event
	 *            the event to queue
	 * @param set
	 *            event set the event is associated with
	 */
    public void queue(DebugEvent event, EventSet set) {
        synchronized (fSetToQueue) {
            List<DebugEvent> list = fSetToQueue.get(set);
            if (list == null) {
                list = new ArrayList<DebugEvent>(5);
                fSetToQueue.put(set, list);
            }
            list.add(event);
        }
    }

    /**
	 * Fires debug events in the event queue associated with the given event
	 * set, and clears the queue.
	 * @param set the set to fire events for
	 */
    private void fireEvents(EventSet set) {
        DebugPlugin plugin = DebugPlugin.getDefault();
        if (// check that not in the process of shutting down
        plugin != null) {
            List<DebugEvent> list = null;
            synchronized (fSetToQueue) {
                list = fSetToQueue.remove(set);
            }
            if (list != null) {
                DebugEvent[] events = list.toArray(new DebugEvent[list.size()]);
                plugin.fireDebugEventSet(events);
            }
        }
    }
}
