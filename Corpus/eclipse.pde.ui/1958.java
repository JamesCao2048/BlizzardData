/*******************************************************************************
 * Copyright (c) 2009, 2015 Zend Technologies Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Zend Technologies Ltd. - initial API and implementation
 *     IBM Corporation - ongoing enhancements
 *******************************************************************************/
package org.eclipse.pde.ui.tests.views.log;

import org.eclipse.ui.internal.views.log.LogEntry;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import junit.framework.TestCase;

public class LogEntryTest extends TestCase {

    public void testProcessEntry() throws ParseException {
        LogEntry entry = new LogEntry();
        entry.processEntry("!ENTRY org.eclipse.pde.ui 1 100 2009-01-03 11:15:30.123");
        assertEquals("org.eclipse.pde.ui", entry.getPluginId());
        assertEquals(1, entry.getSeverity());
        assertEquals(100, entry.getCode());
        Calendar expectedDate = new GregorianCalendar(2009, Calendar.JANUARY, 3, 11, 15, 30);
        expectedDate.set(Calendar.MILLISECOND, 123);
        assertEquals(expectedDate.getTime(), entry.getDate());
    }

    public void testProcessFrameworkEntry() throws ParseException {
        LogEntry entry = new LogEntry();
        entry.processEntry("!ENTRY org.eclipse.osgi 2009-01-07 11:15:30.123");
        assertEquals("org.eclipse.osgi", entry.getPluginId());
        assertEquals(0, entry.getSeverity());
        assertEquals(0, entry.getCode());
        Calendar expectedDate = new GregorianCalendar(2009, Calendar.JANUARY, 7, 11, 15, 30);
        expectedDate.set(Calendar.MILLISECOND, 123);
        assertEquals(expectedDate.getTime(), entry.getDate());
    }

    public void testProcessSubEntry() throws ParseException {
        LogEntry entry = new LogEntry();
        int depth = entry.processSubEntry("!SUBENTRY 1 org.eclipse.osgi 1 101 2009-01-08 11:15:30.123");
        assertEquals("org.eclipse.osgi", entry.getPluginId());
        assertEquals(1, entry.getSeverity());
        assertEquals(101, entry.getCode());
        Calendar expectedDate = new GregorianCalendar(2009, Calendar.JANUARY, 8, 11, 15, 30);
        expectedDate.set(Calendar.MILLISECOND, 123);
        assertEquals(expectedDate.getTime(), entry.getDate());
        assertEquals(1, depth);
    }

    public void testProcessFrameworkSubEntry() throws ParseException {
        LogEntry entry = new LogEntry();
        int depth = entry.processSubEntry("!SUBENTRY 1 org.eclipse.osgi 2009-01-01 11:15:30.123");
        assertEquals("org.eclipse.osgi", entry.getPluginId());
        assertEquals(0, entry.getSeverity());
        assertEquals(0, entry.getCode());
        Calendar expectedDate = new GregorianCalendar(2009, Calendar.JANUARY, 1, 11, 15, 30);
        expectedDate.set(Calendar.MILLISECOND, 123);
        assertEquals(expectedDate.getTime(), entry.getDate());
        assertEquals(1, depth);
    }

    public void testInvalidEntry() {
        try {
            LogEntry entry = new LogEntry();
            entry.processEntry("!ENTRY org.eclipse.core.contenttype 4 0");
            fail("Should throw ParseException for invalid entry");
        } catch (ParseException e) {
        }
        try {
            LogEntry entry = new LogEntry();
            entry.processEntry("!ENTRY org.eclipse.ui 4 0");
            fail("Should throw ParseException for invalid entry");
        } catch (ParseException e) {
        }
        try {
            LogEntry entry = new LogEntry();
            entry.processEntry("!ENTRY org.eclipse.ui 4 0");
            fail("Should throw ParseException for invalid entry");
        } catch (ParseException e) {
        }
    }
}
