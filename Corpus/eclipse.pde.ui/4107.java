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
 * Test unsupported @noreference tag on fields in outer / inner interface
 */
public interface test9 {

    interface inner {

        /**
		 * @noreference
		 */
        public final int f2 = 0;

        class C {

            interface inner2 {

                /**
				 * @noreference
				 */
                public final char[] f3 = {};
            }
        }
    }
}

interface outer {

    /**
	 * @noreference
	 */
    public final Object f1 = null;
}
