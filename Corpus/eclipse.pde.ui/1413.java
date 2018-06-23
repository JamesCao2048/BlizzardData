/*******************************************************************************
 * Copyright (c) 2014 Rapicorp Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rapicorp Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.product;

import java.io.PrintWriter;
import org.eclipse.pde.internal.core.iproduct.ICSSInfo;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.w3c.dom.*;

public class CSSInfo extends ProductObject implements ICSSInfo {

    private static final long serialVersionUID = 1L;

    private String fFilePath;

    public  CSSInfo(IProductModel model) {
        super(model);
    }

    @Override
    public void setFilePath(String text) {
        String old = fFilePath;
        fFilePath = text;
        if (isEditable()) {
            firePropertyChanged(P_CSSFILEPATH, old, fFilePath);
        }
    }

    @Override
    public String getFilePath() {
        return fFilePath;
    }

    @Override
    public void write(String indent, PrintWriter writer) {
        //$NON-NLS-1$
        writer.println(indent + "<cssInfo>");
        if (fFilePath != null && fFilePath.length() > 0) {
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.println(indent + "   <file path=\"" + getWritableString(fFilePath.trim()) + "\"/>");
        }
        //$NON-NLS-1$
        writer.println(indent + "</cssInfo>");
    }

    @Override
    public void parse(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (//$NON-NLS-1$
                child.getNodeName().equals("file")) {
                    fFilePath = //$NON-NLS-1$
                    ((Element) child).getAttribute(//$NON-NLS-1$
                    "path");
                }
            }
        }
    }
}
