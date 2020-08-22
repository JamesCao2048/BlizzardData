/*******************************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc166;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsAspectJ166 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.6.6 tests");
		// $JUnit-BEGIN$
		suite.addTest(Ajc166Tests.suite());
		// $JUnit-END$
		return suite;
	}
}
