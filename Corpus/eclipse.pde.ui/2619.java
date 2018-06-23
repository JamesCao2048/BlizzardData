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
 * Test unsupported @noreference tag on methods in outer / inner annotations
 */
public @interface test12 {

    @interface inner {

        /**
		 * @nooverride
		 * @noimplement
		 * @noinstantiate
		 * @noextend
		 * @noreference
		 * @return
		 */
        public String m1() default "one";

        @interface inner2 {

            /**
			 * @nooverride
			 * @noimplement
			 * @noinstantiate
			 * @noextend
			 * @noreference
			 * @return
			 */
            public String m1() default "one";
        }
    }
}

@interface outer {

    /**
	 * @nooverride
	 * @noimplement
	 * @noinstantiate
	 * @noextend
	 * @noreference
	 * @return
	 */
    public String m1() default "one";
}
