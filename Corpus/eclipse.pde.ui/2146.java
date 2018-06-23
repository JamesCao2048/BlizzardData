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
 */
public class test3 {

    /**
	 * @noreference This field is not intended to be referenced by clients.
	 * @noreference This field is not intended to be referenced by clients.
	 */
    public String f1 = null;

    /**
	 */
    public class Inner {

        /**
		 * @noreference This field is not intended to be referenced by clients.
		 * @noreference This field is not intended to be referenced by clients.
		 */
        protected Object o = null;
    }
}

/**
 */
class outer {

    protected int i = -1;
}
