/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *    Andy Clement
 * ******************************************************************/
package org.aspectj.weaver.patterns.bcel;

import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.patterns.TypePatternListTestCase;

public class BcelTypePatternListTestCase extends TypePatternListTestCase {

	public World getWorld() {
		return new BcelWorld();
	}
}
