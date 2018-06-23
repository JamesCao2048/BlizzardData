/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.examples.provider.trivial.identity;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;

public class TrivialNamespace extends Namespace {

    private static final long serialVersionUID = 1235788855435011811L;

    public static final String SCHEME = "trivial";

    public static final String NAME = "ecf.namespace.trivial";

    public  TrivialNamespace(String name) {
        super(name, null);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.Namespace#createInstance(java.lang.Object[])
	 */
    public ID createInstance(Object[] parameters) throws IDCreateException {
        // e.g. IDFactory.getDefault().createID("myid");
        if (parameters == null || parameters.length < 1)
            throw new IDCreateException("parameters not of correct size");
        if (!(parameters[0] instanceof String))
            throw new IDCreateException("parameter not of String type");
        return new TrivialID(this, (String) parameters[0]);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.Namespace#getScheme()
	 */
    public String getScheme() {
        return SCHEME;
    }
}
