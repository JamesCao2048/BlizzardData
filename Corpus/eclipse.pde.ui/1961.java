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
public class test9 {

    protected static class inner {

        /**
		 * Constructor
		 * @noreference This constructor is not intended to be referenced by clients.
		 */
        public  inner() {
        }

        /**
		 * Constructor
		 * @noreference This constructor is not intended to be referenced by clients.
		 */
        protected  inner(int i) {
        }

        protected static class inner2 {

            /**
			 * Constructor
			 * @noreference This constructor is not intended to be referenced by clients.
			 */
            public  inner2() {
            }

            /**
			 * Constructor
			 * @noreference This constructor is not intended to be referenced by clients.
			 */
            protected  inner2(int i) {
            }
        }
    }
}
