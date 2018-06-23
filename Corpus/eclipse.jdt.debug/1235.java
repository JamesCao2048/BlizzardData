/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.targets;

public class ThreadStack {

    public static void main(String[] args) {
        Thread thread1 = new Thread(new Runnable() {

            public void run() {
                ClassOne one = new ClassOne();
                one.method1();
            }
        });
        thread1.start();
        Thread thread2 = new Thread(new Runnable() {

            public void run() {
                ClassOne one = new ClassOne();
                one.method1();
            }
        });
        thread2.start();
        Thread thread3 = new Thread(new Runnable() {

            public void run() {
                ClassTwo two = new ClassTwo();
                two.method1();
            }
        });
        thread3.start();
        System.out.println("Done running ThreadStack.java");
    }
}
