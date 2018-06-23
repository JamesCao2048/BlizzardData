/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.schema;

import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;
import org.eclipse.pde.internal.core.ischema.*;

public class ChoiceRestriction extends SchemaObject implements ISchemaRestriction {

    private static final long serialVersionUID = 1L;

    private ISchemaSimpleType baseType;

    private List<ISchemaEnumeration> children;

    //$NON-NLS-1$
    public static final String P_CHOICES = "choices";

    public  ChoiceRestriction(ISchema schema) {
        //$NON-NLS-1$
        super(schema, "__choice__");
    }

    public  ChoiceRestriction(ChoiceRestriction source) {
        this(source.getSchema());
        children = new Vector();
        Object[] choices = source.getChildren();
        for (int i = 0; i < choices.length; i++) {
            children.add(new SchemaEnumeration(this, ((ISchemaEnumeration) choices[i]).getName()));
        }
    }

    @Override
    public ISchemaSimpleType getBaseType() {
        return baseType;
    }

    @Override
    public ISchemaEnumeration[] getChildren() {
        return (children != null) ? children.toArray(new ISchemaEnumeration[children.size()]) : new ISchemaEnumeration[0];
    }

    public String[] getChoicesAsStrings() {
        if (children == null)
            return new String[0];
        Vector<String> result = new Vector();
        for (int i = 0; i < children.size(); i++) {
            ISchemaEnumeration enumeration = children.get(i);
            result.addElement(enumeration.getName());
        }
        String[] choices = new String[result.size()];
        result.copyInto(choices);
        return choices;
    }

    @Override
    public ISchemaObject getParent() {
        if (baseType != null)
            return baseType.getSchema();
        return super.getParent();
    }

    @Override
    public boolean isValueValid(java.lang.Object value) {
        if (children == null)
            return false;
        String svalue = value.toString();
        for (int i = 0; i < children.size(); i++) {
            ISchemaEnumeration enumeration = children.get(i);
            if (enumeration.getName().equals(svalue))
                return true;
        }
        return false;
    }

    @Override
    public void setBaseType(ISchemaSimpleType baseType) {
        this.baseType = baseType;
    }

    public void setChildren(List<ISchemaEnumeration> children) {
        List<ISchemaEnumeration> oldValue = this.children;
        this.children = children;
        if (getParent() != null)
            getSchema().fireModelObjectChanged(this, P_CHOICES, oldValue, children);
    }

    @Override
    public String toString() {
        if (children == null)
            //$NON-NLS-1$
            return "";
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < children.size(); i++) {
            Object child = children.get(i);
            if (child instanceof ISchemaEnumeration) {
                ISchemaEnumeration enumeration = (ISchemaEnumeration) child;
                if (i > 0)
                    //$NON-NLS-1$
                    buffer.append(//$NON-NLS-1$
                    ", ");
                buffer.append(enumeration.getName());
            }
        }
        return buffer.toString();
    }

    @Override
    public void write(String indent, PrintWriter writer) {
        //$NON-NLS-1$ //$NON-NLS-2$
        writer.println(indent + "<restriction base=\"" + baseType.getName() + "\">");
        for (int i = 0; i < children.size(); i++) {
            Object child = children.get(i);
            if (child instanceof ISchemaEnumeration) {
                ISchemaEnumeration enumeration = (ISchemaEnumeration) child;
                enumeration.write(indent + Schema.INDENT, writer);
            }
        }
        //$NON-NLS-1$
        writer.println(indent + "</restriction>");
    }
}
