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
package x.y.z;

import f.FieldUsageClass;

/**
 * Tests field usage, where the accessed field is tagged with a noreference tag
 */
public class testF4 {

    /**
	 * Constructor
	 */
    public  testF4() {
        FieldUsageClass.inner c = new FieldUsageClass.inner();
        int f3 = FieldUsageClass.inner.f3;
        int f4 = c.f4;
    }

    public static class inner {

        /**
		 * Constructor
		 */
        public  inner() {
            FieldUsageClass.inner c = new FieldUsageClass.inner();
            int f3 = FieldUsageClass.inner.f3;
            int f4 = c.f4;
        }
    }

    class inner2 {

        public  inner2() {
            FieldUsageClass.inner c = new FieldUsageClass.inner();
            int f3 = FieldUsageClass.inner.f3;
            int f4 = c.f4;
        }
    }
}

class outer {

    public  outer() {
        FieldUsageClass.inner c = new FieldUsageClass.inner();
        int f3 = FieldUsageClass.inner.f3;
        int f4 = c.f4;
    }
}
