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
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.iproduct.IRepositoryInfo;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class RepositoryInfo extends ProductObject implements IRepositoryInfo {

    private static final long serialVersionUID = 1L;

    //$NON-NLS-1$
    public static final String P_LOCATION = "location";

    //$NON-NLS-1$
    public static final String P_ENABLED = "enabled";

    private String fURL;

    // enabled unless specified otherwise
    private boolean fEnabled = true;

    public  RepositoryInfo(IProductModel model) {
        super(model);
    }

    @Override
    public void setURL(String url) {
        String old = fURL;
        fURL = url;
        if (isEditable())
            firePropertyChanged(P_LOCATION, old, fURL);
    }

    @Override
    public String getURL() {
        return fURL;
    }

    @Override
    public boolean getEnabled() {
        return fEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        boolean old = fEnabled;
        fEnabled = enabled;
        if (isEditable())
            firePropertyChanged(P_ENABLED, old, fEnabled);
    }

    @Override
    public void parse(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            //$NON-NLS-1$
            fURL = element.getAttribute("location");
            fEnabled = Boolean.valueOf(element.getAttribute(P_ENABLED)).booleanValue();
        }
    }

    @Override
    public void write(String indent, PrintWriter writer) {
        if (isURLDefined()) {
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.print(indent + "<repository location=\"" + fURL + "\"");
            //$NON-NLS-1$//$NON-NLS-2$
            writer.print(" enabled=\"" + fEnabled + "\"");
            //$NON-NLS-1$
            writer.println(" />");
        }
    }

    private boolean isURLDefined() {
        return fURL != null && fURL.length() > 0;
    }
}
