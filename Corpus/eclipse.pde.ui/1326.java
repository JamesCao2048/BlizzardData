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
 * Test unsupported @noimplement tag on enum constants in inner / outer enums
 */
public enum test17 implements  {

    /**
	 * @noimplement
	 */
    A() {
    }
    ;

    static enum inner implements  {

        /**
		 * @noimplement
		 */
        A() {
        }
        ;

        enum inner2 implements  {

            /**
			 * @noimplement
			 */
            A() {
            }
            ;
        }
    }
}

enum outer implements  {

    A() {
    }
    ;
}
