/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.util;

import org.w3c.dom.Element;

public class QualifiedTypeNameHistory extends History<String, String> {

    //$NON-NLS-1$
    private static final String NODE_ROOT = "qualifiedTypeNameHistroy";

    //$NON-NLS-1$
    private static final String NODE_TYPE_INFO = "fullyQualifiedTypeName";

    //$NON-NLS-1$
    private static final String NODE_NAME = "name";

    private static QualifiedTypeNameHistory fgInstance;

    public static QualifiedTypeNameHistory getDefault() {
        if (fgInstance == null)
            //$NON-NLS-1$
            fgInstance = new QualifiedTypeNameHistory("QualifiedTypeNameHistory.xml");
        return fgInstance;
    }

    public  QualifiedTypeNameHistory(String fileName) {
        super(fileName, NODE_ROOT, NODE_TYPE_INFO);
        load();
    }

    @Override
    protected void setAttributes(Object object, Element element) {
        element.setAttribute(NODE_NAME, (String) object);
    }

    @Override
    protected String createFromElement(Element element) {
        return element.getAttribute(NODE_NAME);
    }

    @Override
    protected String getKey(String object) {
        return object;
    }

    public static int getBoost(String fullyQualifiedTypeName, int min, int max) {
        float position = getDefault().getNormalizedPosition(fullyQualifiedTypeName);
        int dist = max - min;
        return Math.round(position * dist) + min;
    }

    public static void remember(String fullyQualifiedTypeName) {
        getDefault().accessed(fullyQualifiedTypeName);
    }
}
