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
 * Test supported @noreference tag on methods in outer / inner classes
 */
public class test4 {

    public static class inner {

        /**
		 * @noreference This method is not intended to be referenced by clients.
		 * @nooverride This method is not intended to be re-implemented or extended by clients.
		 * @return
		 */
        public int m1() {
            return 0;
        }

        /**
		 * @noreference This method is not intended to be referenced by clients.
		 * @return
		 */
        public final char m2() {
            return 's';
        }

        /**
		 * @nooverride This method is not intended to be re-implemented or extended by clients.
		 * @noreference This method is not intended to be referenced by clients.
		 */
        protected void m3() {
        }

        /**
		 * @noreference This method is not intended to be referenced by clients.
		 * @return
		 */
        protected static Object m4() {
            return null;
        }

        public static class inner2 {

            /**
			 * @noreference This method is not intended to be referenced by clients.
			 * @nooverride This method is not intended to be re-implemented or extended by clients.
			 * @return
			 */
            public int m1() {
                return 0;
            }

            /**
			 * @noreference This method is not intended to be referenced by clients.
			 * @return
			 */
            public final char m2() {
                return 's';
            }

            /**
			 * @nooverride This method is not intended to be re-implemented or extended by clients.
			 * @noreference This method is not intended to be referenced by clients.
			 */
            protected void m3() {
            }

            /**
			 * @noreference This method is not intended to be referenced by clients.
			 * @return
			 */
            protected static Object m4() {
                return null;
            }
        }
    }
}
