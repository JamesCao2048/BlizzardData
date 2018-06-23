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
 * Test supported @noextend tag on methods in outer / inner classes
 */
public class test3 {

    /**
	 * @noextend
	 * @return
	 */
    public int m1() {
        return 0;
    }

    /**
	 * @noextend
	 * @return
	 */
    public final char m2() {
        return 's';
    }

    /**
	 * @noextend
	 */
    protected void m3() {
    }

    /**
	 * @noextend
	 * @return
	 */
    protected static Object m4() {
        return null;
    }

    static class inner {

        /**
		 * @noextend
		 * @return
		 */
        public int m1() {
            return 0;
        }

        /**
		 * @noextend
		 * @return
		 */
        public final char m2() {
            return 's';
        }

        /**
		 * @noextend
		 */
        protected void m3() {
        }

        /**
		 * @noextend
		 * @return
		 */
        protected static Object m4() {
            return null;
        }

        static class inner2 {

            /**
			 * @noextend
			 * @return
			 */
            public int m1() {
                return 0;
            }

            /**
			 * @noextend
			 * @return
			 */
            public final char m2() {
                return 's';
            }

            /**
			 * @noextend
			 */
            protected void m3() {
            }

            /**
			 * @noextend
			 * @return
			 */
            protected static Object m4() {
                return null;
            }
        }
    }
}

class outer {

    public int m1() {
        return 0;
    }

    public final char m2() {
        return 's';
    }

    protected void m3() {
    }

    protected static Object m4() {
        return null;
    }
}
