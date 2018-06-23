/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
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
 * Tests multiple annotations on class
 * Tests type annotation at class, interface, class field, meth param,method,local var
 *
 */
@Annotation1(id = 0)
@Annotation2
@Annotation3(string3 = "abc")
public class test7 {

    @Annotation1(id = 3)
    public int fMember = 0;

    @Annotation1(id = 1)
    @Annotation2
    interface I {

        public void m1();

        public default void m2(@Annotation1(id = 4) int m) {
        }
    }

    @Annotation2(string2 = "abc", string1 = "")
    public int classMethod() {
        @Annotation1(id = 5) int a = 9;
        return (a == 9) ? 1 : 0;
    }
}

@interface Annotation1 {

    int id();
}

@interface Annotation2 {

    String string1() default "[unassigned]";

    String string2() default "[unimplemented]";
}

@interface Annotation3 {

    String string3();
}
