/*******************************************************************************
 * Copyright (c) May 16, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package x.y.z;

import i.INoOverrideInterface3;
import i.INoOverrideInterface4;

/**
 * Test no overriding restricted default methods
 */
public class test7 implements INoOverrideInterface4, INoOverrideInterface3 {

    @Override
    public void m1() {
        int a = 0;
    }
}
