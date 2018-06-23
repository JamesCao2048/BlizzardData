/*******************************************************************************
 * Copyright (c) May 28, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.targets;

public class HcrClass9 {

    class Inner {

        public void run() {
            class Local {

                public void run() {
                    String s = new String("Inner$Local#run()");
                }
            }
            new Local().run();
        }
    }

    public void run() {
        new Inner().run();
    }

    public static void main(String[] args) {
        HcrClass9 clazz = new HcrClass9();
        clazz.run();
    }
}
