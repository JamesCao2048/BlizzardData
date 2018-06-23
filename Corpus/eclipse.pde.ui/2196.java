/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
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
 * Test unsupported @noimplement tag on fields in inner / outer enums
 */
public enum test7 implements  {

    A() {
    }
    ;

    /**
	 * @noimplement
	 */
    public Object f1 = null;

    /**
	 * @noimplement
	 */
    protected int f2 = 0;

    /**
	 * @noimplement
	 */
    private char[] f3 = {};

    static enum inner implements  {

        A() {
        }
        ;

        /**
		 * @noimplement
		 */
        public static Object f1 = null;

        /**
		 * @noimplement
		 */
        protected int f2 = 0;

        /**
		 * @noimplement
		 */
        private static char[] f3 = {};

        enum inner2 implements  {

            A() {
            }
            ;

            /**
			 * @noimplement
			 */
            public Object f1 = null;

            /**
			 * @noimplement
			 */
            protected int f2 = 0;

            /**
			 * @noimplement
			 */
            private char[] f3 = {};
        }
    }
}

enum outer implements  {

    A() {
    }
    ;

    public Object f1 = null;

    protected int f2 = 0;

    private static char[] f3 = {};
}
