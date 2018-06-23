/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Manumitting Technologies Inc - initial API and implementation
 *******************************************************************************/
package a.b.c;

public class TestGenericMethod1<E, C extends Collection> {

    /**
	 * @nooverride
	 * @return
	 */
    public int m1(E object);

    /**
	 * @nooverride
	 * @noreference
	 * @return
	 */
    public int m2(C collection) {
        return 0;
    }
}
