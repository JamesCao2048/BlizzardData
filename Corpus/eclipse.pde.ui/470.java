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
package x.y.z;

import m.MethodUsageClass;

/**
 * 
 */
public class testM2 extends MethodUsageClass {

    /**
	 * @see x.y.z.MethodUsageClass#m2()
	 */
    public void m2() {
        super.m2();
    }

    public static class inner extends MethodUsageClass {

        /**
		 * @see x.y.z.MethodUsageClass#m2()
		 */
        public void m2() {
            super.m2();
        }
    }

    class inner2 extends MethodUsageClass {

        public void m2() {
            super.m2();
        }
    }
}

class outermu2 extends MethodUsageClass {

    public void m2() {
        super.m2();
    }
}
