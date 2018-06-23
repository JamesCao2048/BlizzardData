/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package m;

/**
 * 
 */
public class ConstructorUsageClass {

    /**
	 * Constructor
	 * @noreference This constructor is not intended to be referenced by clients.
	 */
    public  ConstructorUsageClass() {
    }

    /**
	 * Constructor
	 * @param i
	 * @param o
	 * @param chars
	 * @noreference This constructor is not intended to be referenced by clients.
	 */
    public  ConstructorUsageClass(int i, Object o, char[] chars) {
    }

    public static class inner {

        /**
		 * Constructor
		 * @noreference This constructor is not intended to be referenced by clients.
		 */
        public  inner() {
        }

        /**
		 * Constructor
		 * @param i
		 * @param o
		 * @param chars
		 * @noreference This constructor is not intended to be referenced by clients.
		 */
        public  inner(int i, Object o, char[] chars) {
        }
    }

    class inner2 {

        public  inner2() {
        }

        public  inner2(int i, Object o, char[] chars) {
        }
    }
}

class outer2 {

    public  outer2() {
    }

    public  outer2(int i, Object o, char[] chars) {
    }
}
