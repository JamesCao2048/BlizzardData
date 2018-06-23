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
package org.eclipse.debug.jdi.tests;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import com.sun.jdi.Mirror;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;

/**
 * Tests for JDI com.sun.jdi.Mirror.
 */
public class MirrorTest extends AbstractJDITest {

    List<Mirror> fMirrors = new LinkedList();

    /**
	 * Creates a new test.
	 */
    public  MirrorTest() {
        super();
    }

    /**
	 * Init the fields that are used by this test only.
	 */
    @Override
    public void localSetUp() {
        // Get all kinds of concrete mirror that can be found in the VM
        // in alphabetical order.
        //TO DO: Add events too
        fMirrors = new LinkedList();
        if (fVM.canWatchFieldAccess())
            fMirrors.add(getAccessWatchpointRequest());
        // AccessWatchpointRequest
        // ArrayReference
        fMirrors.add(getObjectArrayReference());
        // ArrayType
        fMirrors.add(getArrayType());
        // BooleanValue
        fMirrors.add(fVM.mirrorOf(true));
        // BreakpointRequest
        fMirrors.add(getBreakpointRequest());
        // ByteValue
        fMirrors.add(fVM.mirrorOf((byte) 1));
        // CharValue
        fMirrors.add(fVM.mirrorOf('1'));
        // ClassLoaderReference
        fMirrors.add(getClassLoaderReference());
        // ClassType
        fMirrors.add(getMainClass());
        // DoubleValue
        fMirrors.add(fVM.mirrorOf(12345.6789));
        // EventRequestManager
        fMirrors.add(fVM.eventRequestManager());
        // EventQueue
        fMirrors.add(fVM.eventQueue());
        // Field
        fMirrors.add(getField());
        // FieldValue
        fMirrors.add(fVM.mirrorOf(123.45f));
        // IntegerValue
        fMirrors.add(fVM.mirrorOf(12345));
        // InterfaceType
        fMirrors.add(getInterfaceType());
        // LocalVariable
        fMirrors.add(getLocalVariable());
        // Location
        fMirrors.add(getLocation());
        // LongValue
        fMirrors.add(fVM.mirrorOf(123456789l));
        // Method
        fMirrors.add(getMethod());
        if (fVM.canWatchFieldModification())
            fMirrors.add(getModificationWatchpointRequest());
        // ModificationWatchpointRequest
        // ObjectReference
        fMirrors.add(getObjectReference());
        // ShortValue
        fMirrors.add(fVM.mirrorOf((short) 12345));
        // StackFrame
        fMirrors.add(getFrame(RUN_FRAME_OFFSET));
        // StringReference
        fMirrors.add(getStringReference());
        // ThreadGroupReference
        fMirrors.add(getThread().threadGroup());
        // ThreadReference
        fMirrors.add(getThread());
        // VirtualMachine
        fMirrors.add(fVM);
    }

    /**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
    @Override
    public void localTearDown() {
        ListIterator<Mirror> iterator = fMirrors.listIterator();
        while (iterator.hasNext()) {
            Object mirror = iterator.next();
            // Delete the access watchpoint request we created in this test
            if (mirror instanceof AccessWatchpointRequest)
                fVM.eventRequestManager().deleteEventRequest((AccessWatchpointRequest) mirror);
            // Delete the breakpoint request we created in this test
            if (mirror instanceof BreakpointRequest)
                fVM.eventRequestManager().deleteEventRequest((BreakpointRequest) mirror);
            // Delete the modification watchpoint request we created in this test
            if (mirror instanceof ModificationWatchpointRequest)
                fVM.eventRequestManager().deleteEventRequest((ModificationWatchpointRequest) mirror);
        }
    }

    /**
	 * Run all tests and output to standard output.
	 * @param args
	 */
    public static void main(java.lang.String[] args) {
        new MirrorTest().runSuite(args);
    }

    /**
	 * Gets the name of the test case.
	 * @see junit.framework.TestCase#getName()
	 */
    @Override
    public String getName() {
        return "com.sun.jdi.Mirror";
    }

    /**
	 * Test JDI toString().
	 */
    public void testJDIToString() {
        for (int i = 0; i < fMirrors.size(); i++) {
            Mirror mirror = fMirrors.get(i);
            assertNotNull(Integer.toString(i), mirror.toString());
        }
    }

    /**
	 * Test JDI virtualMachine().
	 */
    public void testJDIVirtualMachine() {
        for (int i = 0; i < fMirrors.size(); i++) {
            Mirror mirror = fMirrors.get(i);
            assertEquals(Integer.toString(i), fVM, mirror.virtualMachine());
        }
    }
}
