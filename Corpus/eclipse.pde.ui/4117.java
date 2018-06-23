/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package a.b.c;

/**
 * Test supported @nooverride tag on static methods
 */
public class test13 {

    /**
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 */
    public static void m1() {
    }

    static class inner {

        /**
		 * @nooverride This method is not intended to be re-implemented or extended by clients.
		 */
        public static final void m1() {
        }
    }
}

class outer {

    static void m3() {
    }
}
