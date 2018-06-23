/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
 * Tests that tags are scanned correctly off an anonymous inner type field
 * @since
 */
public class TestField6 {

    public Runnable runner = new Runnable() {

        /**
		 * @noreference
		 */
        protected int number = -1;

        public void run() {
        }
    };
}
