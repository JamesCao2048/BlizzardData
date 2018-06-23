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
 * Test unsupported @noreference tag on final fields in inner / outer classes
 */
public class test1 {

    /**
	 * @noreference
	 */
    public final Object f1 = null;

    /**
	 * @noreference
	 */
    protected final int f2 = 0;

    /**
	 * @noreference
	 */
    private final char[] f3 = {};

    /**
	 * @noreference
	 */
    final long f4 = 0L;

    static class inner {

        /**
		 * @noreference
		 */
        public final Object f1 = null;

        /**
		 * @noreference
		 */
        protected final int f2 = 0;

        /**
		 * @noreference
		 */
        private final char[] f3 = {};

        /**
		 * @noreference
		 */
        final long f4 = 0L;

        class inner2 {

            /**
			 * @noreference
			 */
            public final Object f1 = null;

            /**
			 * @noreference
			 */
            protected final int f2 = 0;

            /**
			 * @noreference
			 */
            private final char[] f3 = {};

            /**
			 * @noreference
			 */
            final long f4 = 0L;
        }
    }
}

class outer {

    public final Object f1 = null;

    protected final int f2 = 0;

    private final char[] f3 = {};

    final long f4 = 0L;
}
