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

import i.INoOverrideInterface2;

/**
 * Test no overriding (pde annotation)  restricted default methods
 */
public class test5 implements INoOverrideInterface2 {

    @Override
    public void m1() {
        int a = 0;
    }
}
