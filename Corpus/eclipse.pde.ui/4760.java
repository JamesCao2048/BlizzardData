/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
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
 * Used to test that a field with inherited restrictions will not inherit the class restrictions
 * as fields do not support 'no extend'
 * 
 * @noextend
 * @since
 */
public class TestField7 {

    /**
	 * @noreference
	 */
    public Object field1 = null;
}
