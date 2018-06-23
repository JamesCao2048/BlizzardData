/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching.environments;

import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.LibraryLocation;

/**
 * Contributes access rules for an execution environment. Contributed with
 * an execution environments extension. 
 * <p>
 * Clients contributing an access rule participant may implement this interface.
 * </p>
 * @since 3.3
 */
public interface IAccessRuleParticipant {

    /**
	 * Returns a collection of access rules to be applied to the specified VM
	 * libraries and execution environment in the context of the given project.
	 * An array of access rules is returned for each library specified by
	 * <code>libraries</code>, possibly empty.
	 * 
	 * @param environment the environment that access rules are requested for
	 * @param vm the vm that access rules are requested for
	 * @param libraries the libraries that access rules are requested for 
	 * @param project the project the access rules are requested for or <code>null</code> if none
	 * @return a collection of arrays of access rules - one array per library, possibly empty
	 * @since 3.3
	 */
    public IAccessRule[][] getAccessRules(IExecutionEnvironment environment, IVMInstall vm, LibraryLocation[] libraries, IJavaProject project);
}
