/*******************************************************************************
 *  Copyright (c) 2005, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.product;

import java.io.PrintWriter;
import org.eclipse.pde.internal.core.iproduct.IAboutInfo;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class AboutInfo extends ProductObject implements IAboutInfo {

    private static final long serialVersionUID = 1L;

    private String fImagePath;

    private String fAboutText;

    public  AboutInfo(IProductModel model) {
        super(model);
    }

    @Override
    public void setText(String text) {
        String old = fAboutText;
        fAboutText = text;
        if (isEditable())
            firePropertyChanged(P_TEXT, old, fAboutText);
    }

    @Override
    public String getText() {
        return fAboutText;
    }

    @Override
    public void setImagePath(String path) {
        String old = fImagePath;
        fImagePath = path;
        if (isEditable())
            firePropertyChanged(P_IMAGE, old, fImagePath);
    }

    @Override
    public String getImagePath() {
        return fImagePath;
    }

    @Override
    public void write(String indent, PrintWriter writer) {
        if (isAboutImageDefined() || isAboutTextDefined()) {
            //$NON-NLS-1$
            writer.println(indent + "<aboutInfo>");
            if (isAboutImageDefined())
                //$NON-NLS-1$ //$NON-NLS-2$
                writer.println(indent + "   <image path=\"" + getWritableString(fImagePath.trim()) + "\"/>");
            if (isAboutTextDefined()) {
                //$NON-NLS-1$
                writer.println(//$NON-NLS-1$
                indent + "   <text>");
                //$NON-NLS-1$
                writer.println(//$NON-NLS-1$
                indent + "      " + getWritableString(fAboutText.trim()));
                //$NON-NLS-1$
                writer.println(//$NON-NLS-1$
                indent + "   </text>");
            }
            //$NON-NLS-1$
            writer.println(indent + "</aboutInfo>");
        }
    }

    private boolean isAboutTextDefined() {
        return fAboutText != null && fAboutText.length() > 0;
    }

    private boolean isAboutImageDefined() {
        return fImagePath != null && fImagePath.length() > 0;
    }

    @Override
    public void parse(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (//$NON-NLS-1$
                child.getNodeName().equals("image")) {
                    fImagePath = //$NON-NLS-1$
                    ((Element) child).getAttribute(//$NON-NLS-1$
                    "path");
                } else if (//$NON-NLS-1$
                child.getNodeName().equals("text")) {
                    child.normalize();
                    if (child.getChildNodes().getLength() > 0) {
                        Node text = child.getFirstChild();
                        if (text.getNodeType() == Node.TEXT_NODE)
                            fAboutText = ((Text) text).getData().trim();
                    }
                }
            }
        }
    }
}
