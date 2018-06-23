/*******************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.schema;

import org.eclipse.pde.internal.core.ischema.ISchemaObject;
import org.eclipse.pde.internal.core.ischema.ISchemaRepeatable;

public abstract class RepeatableSchemaObject extends SchemaObject implements ISchemaRepeatable {

    private static final long serialVersionUID = 1L;

    //$NON-NLS-1$
    public static final String P_MIN_OCCURS = "min_occurs";

    //$NON-NLS-1$
    public static final String P_MAX_OCCURS = "max_occurs";

    private int minOccurs = 1;

    private int maxOccurs = 1;

    public  RepeatableSchemaObject(ISchemaObject parent, String name) {
        super(parent, name);
    }

    @Override
    public int getMaxOccurs() {
        return maxOccurs;
    }

    @Override
    public int getMinOccurs() {
        return minOccurs;
    }

    public boolean isRequired() {
        return minOccurs > 0;
    }

    public boolean isUnbounded() {
        return maxOccurs == Integer.MAX_VALUE;
    }

    public void setMaxOccurs(int newMaxOccurs) {
        Integer oldValue = Integer.valueOf(maxOccurs);
        maxOccurs = newMaxOccurs;
        getSchema().fireModelObjectChanged(this, P_MAX_OCCURS, oldValue, Integer.valueOf(maxOccurs));
    }

    public void setMinOccurs(int newMinOccurs) {
        Integer oldValue = Integer.valueOf(minOccurs);
        minOccurs = newMinOccurs;
        getSchema().fireModelObjectChanged(this, P_MIN_OCCURS, oldValue, Integer.valueOf(minOccurs));
    }
}
