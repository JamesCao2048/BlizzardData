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

import java.util.List;

/**
 * Tests that methods with primitive return types are processed correctly
 * @since
 */
public class TestMethod15 {

    /**
	 * @noreference
	 * @param num
	 * @param dbls
	 * @param cs
	 * @param in
	 */
    public char one(int num, Double[][] dbls, char[] cs, Integer in) {
        return 'a';
    }

    /**
	 * @nooverride
	 * @param ls
	 * @param d
	 * @param c
	 * @param is
	 * @param r
	 */
    protected double two(List[][] ls, double d, char c, int[] is, Runnable[] r) {
        return 1.0;
    }
}
