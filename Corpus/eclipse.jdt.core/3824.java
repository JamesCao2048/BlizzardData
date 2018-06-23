/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.impl;

public class LongConstant extends Constant {

    long value;

    public  LongConstant(long value) {
        this.value = value;
    }

    public byte byteValue() {
        return (byte) value;
    }

    public char charValue() {
        return (char) value;
    }

    public double doubleValue() {
        // implicit cast to return type
        return value;
    }

    public float floatValue() {
        // implicit cast to return type
        return value;
    }

    public int intValue() {
        return (int) value;
    }

    public long longValue() {
        return value;
    }

    public short shortValue() {
        return (short) value;
    }

    public String stringValue() {
        //spec 15.17.11
        String s = new Long(value).toString();
        //$NON-NLS-1$
        if (s == null)
            return "null";
        return s;
    }

    public String toString() {
        //$NON-NLS-1$
        return "(long)" + value;
    }

    public int typeID() {
        return T_long;
    }
}
