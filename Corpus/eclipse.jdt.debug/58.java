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
package org.eclipse.jdi.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.eclipse.jdi.internal.jdwp.JdwpID;
import com.sun.jdi.LongValue;
import com.sun.jdi.Type;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class LongValueImpl extends PrimitiveValueImpl implements LongValue {

    /** JDWP Tag. */
    public static final byte tag = JdwpID.LONG_TAG;

    /**
	 * Creates new instance.
	 */
    public  LongValueImpl(VirtualMachineImpl vmImpl, Long value) {
        //$NON-NLS-1$
        super("LongValue", vmImpl, value);
    }

    /**
	 * @returns tag.
	 */
    @Override
    public byte getTag() {
        return tag;
    }

    /**
	 * @returns type of value.
	 */
    @Override
    public Type type() {
        return virtualMachineImpl().getLongType();
    }

    /* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
    @Override
    public int compareTo(LongValue o) {
        return ((Long) longValue()).compareTo(o.longValue());
    }

    /**
	 * @returns Value.
	 */
    @Override
    public long value() {
        return longValue();
    }

    /**
	 * @return Reads and returns new instance.
	 */
    public static LongValueImpl read(MirrorImpl target, DataInputStream in) throws IOException {
        VirtualMachineImpl vmImpl = target.virtualMachineImpl();
        //$NON-NLS-1$
        long value = target.readLong("longValue", in);
        return new LongValueImpl(vmImpl, new Long(value));
    }

    /**
	 * Writes value without value tag.
	 */
    @Override
    public void write(MirrorImpl target, DataOutputStream out) throws IOException {
        //$NON-NLS-1$
        target.writeLong(((Long) fValue).longValue(), "longValue", out);
    }
}
