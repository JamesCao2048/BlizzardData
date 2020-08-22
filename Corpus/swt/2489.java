/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.internal.win32;

/** @jniclass flags=no_gen */
public class LRESULT {
	public long /*int*/ value;
	public static final LRESULT ONE = new LRESULT (1);
	public static final LRESULT ZERO = new LRESULT (0);
	
public LRESULT (long /*int*/ value) {
	this.value = value;
}

}
