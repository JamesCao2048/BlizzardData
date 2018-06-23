/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package x.y.z;

import i.NoRefInterface;
import i.NoRefInterfaceImpl;

public class testI4 {

    void method1() {
        NoRefInterface clazz = new NoRefInterfaceImpl();
        clazz.noRefInterfaceMethod();
        //not a problem since the field is in-lined
        String str = NoRefInterface.fNoRefInterfaceField;
    }
}
