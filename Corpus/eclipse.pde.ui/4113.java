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
 * Test supported @noextend tag on an interface
 * @noextend
 */
public interface test6 {

    /**
	 * @noextend
	 */
    public interface inner {
    }

    public interface inner1 {

        /**
		 * @noextend
		 */
        public interface inner2 {
        }
    }

    interface inner2 {
    }
}
