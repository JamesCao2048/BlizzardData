/*******************************************************************************
 *  Copyright (c) 2006, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     David Carver - STAR - bug 212355
 *******************************************************************************/
package org.eclipse.pde.internal.core.schema;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * SchemaAnnotationHandler
 *
 */
public class SchemaAnnotationHandler extends BaseSchemaHandler {

    private static final String[] DESC_NESTED_ELEM = { //$NON-NLS-1$
    "documentation", "annotation", //$NON-NLS-1$ //$NON-NLS-2$
    "schema" };

    //$NON-NLS-1$
    private static final String META_SCHEMA_ELEM = "meta.schema";

    //$NON-NLS-1$
    private static final String APP_INFO_ELEM = "appinfo";

    //$NON-NLS-1$
    private static final String APP_INFO_ELEM_OLD = "appInfo";

    //$NON-NLS-1$
    private static final String NAME_ATTR = "name";

    private StringBuffer fDescription;

    private String fName;

    private boolean fMetaSchemaElemFlag;

    private boolean fAppInfoElemFlag;

    /**
	 *
	 */
    public  SchemaAnnotationHandler() {
        super();
    }

    @Override
    protected void reset() {
        super.reset();
        fName = null;
        fDescription = new StringBuffer();
        fMetaSchemaElemFlag = false;
        fAppInfoElemFlag = false;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if ((fElementList.size() >= 2) && ((fElementList.get(1).compareTo(APP_INFO_ELEM) == 0) || (fElementList.get(1).compareTo(APP_INFO_ELEM_OLD) == 0))) {
            fAppInfoElemFlag = true;
            if (qName.compareTo(META_SCHEMA_ELEM) == 0) {
                // Case:  <appInfo><meta.schema>
                fMetaSchemaElemFlag = true;
                if (attributes != null) {
                    fName = attributes.getValue(NAME_ATTR);
                }
            } else {
                // Case:  <appInfo><xxxxx>
                fMetaSchemaElemFlag = false;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
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
            if (fMetaSchemaElemFlag || !fAppInfoElemFlag) {
                return true;
            }
        }
        return false;
    }

    public String getDescription() {
        return fDescription.toString();
    }

    public String getName() {
        return fName;
    }
}
