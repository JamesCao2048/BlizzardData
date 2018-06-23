/*******************************************************************************
 *  Copyright (c) 2006, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.schema;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * ElementDescriptionHandler
 *
 */
public class SchemaElementHandler extends BaseSchemaHandler {

    private String fElementName;

    private String fTargetElementName;

    private StringBuffer fDescription;

    private static final String[] DESC_NESTED_ELEM = { //$NON-NLS-1$
    "documentation", "annotation", //$NON-NLS-1$ //$NON-NLS-2$
    "element" };

    //$NON-NLS-1$
    private static final String NAME_ATTR = "name";

    /**
	 *
	 */
    public  SchemaElementHandler(String targetElementName) {
        super();
        setTargetElementName(targetElementName);
    }

    public void setTargetElementName(String targetElement) {
        fTargetElementName = targetElement;
    }

    @Override
    protected void reset() {
        super.reset();
        fDescription = new StringBuffer();
        fElementName = null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (qName.compareTo(DESC_NESTED_ELEM[2]) == 0) {
            // Element
            // Note: No need to check if attributes object is null
            // Parser returns empty attributes object if no attributes
            // Remember the last element
            fElementName = attributes.getValue(NAME_ATTR);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (onTarget()) {
            for (int i = 0; i < length; i++) {
                fDescription.append(ch[start + i]);
            }
        }
    }

    protected boolean onTarget() {
        if (fElementList.size() >= DESC_NESTED_ELEM.length) {
            for (int i = 0; i < DESC_NESTED_ELEM.length; i++) {
                String currentElement = fElementList.get(i);
                if (currentElement.compareTo(DESC_NESTED_ELEM[i]) != 0) {
                    return false;
                }
            }
            if ((fElementName == null) || (fElementName.compareTo(fTargetElementName) != 0)) {
                return false;
            }
            return true;
        }
        return false;
    }

    public String getDescription() {
        return fDescription.toString();
    }
}
