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
 * Test unsupported @noreference tag on enum constants in inner / outer enums
 */
public enum test13 implements  {

    /**
	 * @noreference
	 */
    A() {
    }
    ;

    static enum inner implements  {

        /**
		 * @noreference
		 */
        A() {
        }
        ;

        enum inner2 implements  {

            /**
			 * @noreference
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
