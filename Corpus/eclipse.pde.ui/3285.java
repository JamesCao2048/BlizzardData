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
 * Test unsupported @noextend tag on fields in inner / outer classes
 */
public class test5 {

    /**
	 * @noextend
	 */
    public Object f1 = null;

    /**
	 * @noextend
	 */
    protected int f2 = 0;

    /**
	 * @noextend
	 */
    private char[] f3 = {};

    /**
	 * @noextend
	 */
    long f4 = 0L;

    static class inner {

        /**
		 * @noextend
		 */
        public static Object f1 = null;

        /**
		 * @noextend
		 */
        protected int f2 = 0;

        /**
		 * @noextend
		 */
        private static char[] f3 = {};

        /**
		 * @noextend
		 */
        long f4 = 0L;

        class inner2 {

            /**
			 * @noextend
			 */
            public Object f1 = null;

            /**
			 * @noextend
			 */
            protected int f2 = 0;

            /**
			 * @noextend
			 */
            private char[] f3 = {};

            /**
			 * @noextend
			 */
            long f4 = 0L;
        }
    }
}

class outer {

    public Object f1 = null;

    protected int f2 = 0;

    private static char[] f3 = {};

    long f4 = 0L;
}
