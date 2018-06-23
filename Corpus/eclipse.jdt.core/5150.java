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
package org.eclipse.jdt.internal.compiler.util;

public final class HashtableOfLong {

    // to avoid using Enumerations, walk the individual tables skipping nulls
    public long[] keyTable;

    public Object[] valueTable;

    // number of elements in the table
    public int elementSize;

    int threshold;

    public  HashtableOfLong() {
        this(13);
    }

    public  HashtableOfLong(int size) {
        this.elementSize = 0;
        // size represents the expected number of elements
        this.threshold = size;
        int extraRoom = (int) (size * 1.75f);
        if (this.threshold == extraRoom)
            extraRoom++;
        this.keyTable = new long[extraRoom];
        this.valueTable = new Object[extraRoom];
    }

    public boolean containsKey(long key) {
        int index = ((int) (key >>> 32)) % valueTable.length;
        long currentKey;
        while ((currentKey = keyTable[index]) != 0) {
            if (currentKey == key)
                return true;
            index = (index + 1) % keyTable.length;
        }
        return false;
    }

    public Object get(long key) {
        int index = ((int) (key >>> 32)) % valueTable.length;
        long currentKey;
        while ((currentKey = keyTable[index]) != 0) {
            if (currentKey == key)
                return valueTable[index];
            index = (index + 1) % keyTable.length;
        }
        return null;
    }

    public Object put(long key, Object value) {
        int index = ((int) (key >>> 32)) % valueTable.length;
        long currentKey;
        while ((currentKey = keyTable[index]) != 0) {
            if (currentKey == key)
                return valueTable[index] = value;
            index = (index + 1) % keyTable.length;
        }
        keyTable[index] = key;
        valueTable[index] = value;
        // assumes the threshold is never equal to the size of the table
        if (++elementSize > threshold)
            rehash();
        return value;
    }

    private void rehash() {
        // double the number of expected elements
        HashtableOfLong newHashtable = new HashtableOfLong(elementSize * 2);
        long currentKey;
        for (int i = keyTable.length; --i >= 0; ) if ((currentKey = keyTable[i]) != 0)
            newHashtable.put(currentKey, valueTable[i]);
        this.keyTable = newHashtable.keyTable;
        this.valueTable = newHashtable.valueTable;
        this.threshold = newHashtable.threshold;
    }

    public int size() {
        return elementSize;
    }

    public String toString() {
        //$NON-NLS-1$
        String s = "";
        Object object;
        for (int i = 0, length = valueTable.length; i < length; i++) if ((object = valueTable[i]) != null)
            //$NON-NLS-2$ //$NON-NLS-1$
            s += keyTable[i] + " -> " + object.toString() + "\n";
        return s;
    }
}
