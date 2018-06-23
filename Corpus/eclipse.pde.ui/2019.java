/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor;

import java.io.*;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

public class ModelDataTransfer extends ByteArrayTransfer {

    /**
	 * Singleton instance.
	 */
    private static final ModelDataTransfer instance = new ModelDataTransfer();

    // Create a unique ID to make sure that different Eclipse
    // applications use different "types" of <code>ModelDataTransfer</code>
    //$NON-NLS-1$
    public static final String TYPE_PREFIX = "pde-model-transfer-format";

    private static final String TYPE_NAME = //$NON-NLS-1$
    TYPE_PREFIX + ":" + System.currentTimeMillis() + //$NON-NLS-1$
    ":" + instance.hashCode();

    private static final int TYPEID = registerType(TYPE_NAME);

    public static ModelDataTransfer getInstance() {
        return instance;
    }

    /**
	 * Constructor for ModelDataTransfer.
	 */
    public  ModelDataTransfer() {
        super();
    }

    @Override
    protected int[] getTypeIds() {
        return new int[] { TYPEID };
    }

    @Override
    protected String[] getTypeNames() {
        return new String[] { TYPE_NAME };
    }

    @Override
    protected void javaToNative(Object data, TransferData transferData) {
        if (!(data instanceof Object[])) {
            return;
        }
        Object[] objects = (Object[]) data;
        int count = objects.length;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(out);
            //write the number of resources
            objectOut.writeInt(count);
            //write each object
            for (int i = 0; i < objects.length; i++) {
                objectOut.writeObject(objects[i]);
            }
            //cleanup
            objectOut.close();
            out.close();
            byte[] bytes = out.toByteArray();
            super.javaToNative(bytes, transferData);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    @Override
    protected Object nativeToJava(TransferData transferData) {
        byte[] bytes = (byte[]) super.nativeToJava(transferData);
        if (bytes == null)
            return null;
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
            int count = in.readInt();
            Object[] objects = new Object[count];
            for (int i = 0; i < count; i++) {
                objects[i] = in.readObject();
            }
            in.close();
            return objects;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
