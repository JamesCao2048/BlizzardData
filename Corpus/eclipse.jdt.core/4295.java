/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package targets.model.pc;

public class Overriding {

    public class A {

        // overrides OverB.f() in context of OverC, but not in context of OverD
        public void f() {
        }

        // does not override OverB.g() in any context
        private void g() {
        }

        // overrides OverB.h() in context of OverC and OverD
        public void h() {
        }

        public void j() {
        }
    }

    public interface B {

        public void f();

        public void g();

        public void h();
    }

    public abstract class C extends A implements B {

        public void h() {
        }

        public void j() {
        }
    }

    public class D extends C {

        public void f() {
        }

        public void g() {
        }

        public void j() {
        }
    }
}
