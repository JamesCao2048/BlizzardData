/*******************************************************************************
 *  Copyright (c) 2006, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.variables;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.pde.core.plugin.TargetPlatform;

public class TargetHomeDynamicVariableResolver implements IDynamicVariableResolver {

    /**
	 * Resolver for ${target_home}
	 *
	 * @since 3.2
	 */
    @Override
    public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
        return TargetPlatform.getLocation();
    }
}
