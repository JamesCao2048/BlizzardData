/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.template.java;

import org.eclipse.jdt.internal.corext.template.java.CompilationUnitCompletion.Variable;

/**
 * Resolves a template variable to a field that is assignment-compatible
 * with the variable instance' class parameter.
 *
 * @since 3.3
 */
public class FieldResolver extends AbstractVariableResolver {

    /**
	 * Default constructor for instantiation by the extension point.
	 */
    public  FieldResolver() {
        //$NON-NLS-1$
        this("java.lang.Object");
    }

     FieldResolver(String defaultType) {
        super(defaultType);
    }

    @Override
    protected Variable[] getVisibleVariables(String type, JavaContext context) {
        return context.getFields(type);
    }
}
