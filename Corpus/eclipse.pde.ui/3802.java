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
package org.eclipse.pde.api.tools.internal.builder;

import java.util.Set;
import org.eclipse.pde.api.tools.internal.provisional.builder.IReference;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblemTypes;

/**
 * Detects leaks in method return types
 *
 * @since 1.1
 */
public class LeakReturnTypeDetector extends MethodLeakDetector {

    /**
	 * @param nonApiPackageNames
	 */
    public  LeakReturnTypeDetector(Set<String> nonApiPackageNames) {
        super(nonApiPackageNames);
    }

    @Override
    protected String getSeverityKey() {
        return IApiProblemTypes.LEAK_METHOD_RETURN_TYPE;
    }

    @Override
    public int getReferenceKinds() {
        return IReference.REF_RETURNTYPE;
    }

    @Override
    protected int getProblemFlags(IReference reference) {
        return IApiProblem.LEAK_RETURN_TYPE;
    }
}
