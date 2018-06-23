/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package a.b.c;

import org.eclipse.pde.api.tools.annotations.NoOverride;
import org.eclipse.pde.api.tools.annotations.NoImplement;
import org.eclipse.pde.api.tools.annotations.NoInstantiate;
import org.eclipse.pde.api.tools.annotations.NoExtend;
import org.eclipse.pde.api.tools.annotations.NoReference;

/**
 * Test unsupported tags on fields in outer / inner annotation
 */
public @interface test9 {

    @interface inner {

        @NoOverride
        @NoImplement
        @NoInstantiate
        @NoExtend
        @NoReference
        public int f2 = 0;

        @interface inner2 {

            @NoOverride
            @NoImplement
            @NoInstantiate
            @NoExtend
            @NoReference
            public char[] f3 = {};
        }
    }
}

@interface outer {

    @NoOverride
    @NoImplement
    @NoInstantiate
    @NoExtend
    @NoReference
    public static Object f1 = null;
}
