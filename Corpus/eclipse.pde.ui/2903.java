/*******************************************************************************
 * Copyright (c) Apr 2, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package x.y.z;

import i.INoRefDefaultInterface2;

/**
 * Tests an impl and interface ref to a restricted default method
 */
public class test4 implements INoRefDefaultInterface2 {

    public static void main(String[] args) {
        INoRefDefaultInterface2 four = new test4();
        four.m1();
    }
}
