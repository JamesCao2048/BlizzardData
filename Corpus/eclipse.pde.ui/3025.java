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
package javadoc;

/**
 * Tests that a constructor is not processed as method needing a doc tag, since constructors have no return type
 * per se.See {@link MethodDeclaration#getReturnType2()} for the spec on returns types.
 * 
 * Addresses bug 210784 (https://bugs.eclipse.org/bugs/show_bug.cgi?id=210784)
 * 
 * @since 1.0.0
 */
public class JavadocTestClass8 {

    public  JavadocTestClass8() {
    }
}
