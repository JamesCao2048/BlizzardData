/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.lang.reflect.Method;

public class MethodEntryRunner {

    // Why the reflection?  Because this class fires up a class that is meant to exist only as 
    // a .class file in the tests, and MethodEntryRunner wouldn't compile in
    // development if we directly invoked MethodEntryTest, since there's no source for it
    public static void main(java.lang.String[] args) {
        try {
            Class clazz = Class.forName("MethodEntryTest");
            Method method = clazz.getMethod("main", new Class[] { String[].class });
            method.invoke(null, new Object[] { new String[] {} });
        } catch (Exception ex) {
            System.err.println("Exception trying to invoke MethodEntryTest");
            ex.printStackTrace();
        }
    }
}
