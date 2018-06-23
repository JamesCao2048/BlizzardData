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
 * That methods that return a primitive type array are processed correctly
 * @since
 */
public class TestMethod17 {

    /**
	 * @noreference
	 * @param num
	 * @param dbls
	 * @param cs
	 * @param in
	 */
    public char[][] one(int num, Double[][] dbls, char[] cs, Integer in) {
        return null;
    }

    /**
	 * @nooverride
	 * @param ls
	 * @param d
	 * @param c
	 * @param is
	 * @param r
	 */
    protected double[] two(List[][] ls, double d, char c, int[] is, Runnable[] r) {
        return null;
    }
}
