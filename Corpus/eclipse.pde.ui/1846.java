/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Rafael Oliveira Nóbrega <rafael.oliveira@gmail.com> - bug 223738
 *******************************************************************************/
package org.eclipse.pde.internal.ds.core.text;

import org.eclipse.pde.internal.ds.core.IDSConstants;
import org.eclipse.pde.internal.ds.core.IDSProperty;

public class DSProperty extends DSObject implements IDSProperty {

    private static final long serialVersionUID = 1L;

    public  DSProperty(DSModel model) {
        super(model, ELEMENT_PROPERTY);
        // set Default Values
        this.setPropertyType(IDSConstants.VALUE_PROPERTY_TYPE_STRING);
        int property_count = model.getDSComponent().getPropertyElements().length + 1;
        this.setPropertyName(IDSConstants.ELEMENT_PROPERTY + property_count);
        this.setPropertyValue(IDSConstants.ATTRIBUTE_PROPERTY_VALUE);
    }

    @Override
    public boolean canAddChild(int objectType) {
        return false;
    }

    @Override
    public boolean canBeParent() {
        return false;
    }

    @Override
    public String getName() {
        return getPropertyName();
    }

    @Override
    public int getType() {
        return TYPE_PROPERTY;
    }

    @Override
    public String getPropertyName() {
        return getXMLAttributeValue(ATTRIBUTE_PROPERTY_NAME);
    }

    @Override
    public void setPropertyName(String name) {
        setXMLAttribute(ATTRIBUTE_PROPERTY_NAME, name);
    }

    @Override
    public String getPropertyValue() {
        return getXMLAttributeValue(ATTRIBUTE_PROPERTY_VALUE);
    }

    @Override
    public void setPropertyValue(String value) {
        setXMLAttribute(ATTRIBUTE_PROPERTY_VALUE, value);
    }

    @Override
    public String getPropertyType() {
        return getXMLAttributeValue(ATTRIBUTE_PROPERTY_TYPE);
    }

    @Override
    public void setPropertyType(String type) {
        setXMLAttribute(ATTRIBUTE_PROPERTY_TYPE, type);
    }

    @Override
    public String getPropertyElemBody() {
        return getXMLContent();
    }

    @Override
    public void setPropertyElemBody(String body) {
        setXMLContent(body);
    }

    @Override
    public String[] getAttributesNames() {
        return new String[] { IDSConstants.ATTRIBUTE_PROPERTY_NAME, IDSConstants.ATTRIBUTE_PROPERTY_TYPE, IDSConstants.ATTRIBUTE_PROPERTY_VALUE };
    }

    @Override
    public boolean isLeafNode() {
        return true;
    }
}
