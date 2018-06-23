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

import java.io.StringReader;
import java.util.LinkedList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * BaseDescriptionHandler
 *
 */
public class BaseSchemaHandler extends DefaultHandler {

    protected LinkedList<String> fElementList;

    public  BaseSchemaHandler() {
        reset();
    }

    protected void reset() {
        fElementList = new LinkedList();
    }

    @Override
    public void startDocument() throws SAXException {
        reset();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // Track where we are in the XML document
        // Note:  XML namespaces not utilized, safe to use qualified name
        fElementList.addFirst(qName);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // Track where we are in the XML document
        if (fElementList.size() != 0) {
            fElementList.removeFirst();
        } else {
            //$NON-NLS-1$
            throw new SAXException("Serious error.  XML document is not well-formed");
        }
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
        //$NON-NLS-1$
        return new InputSource(new StringReader(""));
    }
}
