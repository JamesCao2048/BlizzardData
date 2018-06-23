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

import m.GenericMethodUsageClass;

/**
 * 
 */
public class testM6 {

    public static class inner {

        /**
		 * Constructor
		 */
        public  inner() {
            GenericMethodUsageClass<String> o = new GenericMethodUsageClass<String>();
            String s = o.m1();
            System.out.println(s);
            o.m2(s);
        }
    }

    class inner2 {

        public  inner2() {
            GenericMethodUsageClass<String> o = new GenericMethodUsageClass<String>();
            String s = o.m1();
            System.out.println(s);
            o.m2(s);
        }
    }
}

class outer {

    /**
	 * Constructor
	 */
    public  outer() {
        GenericMethodUsageClass<String> o = new GenericMethodUsageClass<String>();
        String s = o.m1();
        System.out.println(s);
        o.m2(s);
    }
}
