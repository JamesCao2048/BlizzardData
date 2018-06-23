/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444808
 *******************************************************************************/
package org.eclipse.pde.internal.core.product;

import java.io.PrintWriter;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.core.iproduct.IProductFeature;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ProductFeature extends ProductObject implements IProductFeature {

    private static final long serialVersionUID = 1L;

    private String fId;

    private String fVersion;

    /*
	 * Support for preserving tycho's installMode="root" feature
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=361722
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=429902
	 */
    private String fInstallMode;

    public  ProductFeature(IProductModel model) {
        super(model);
    }

    @Override
    public void parse(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            //$NON-NLS-1$
            fId = element.getAttribute("id");
            //$NON-NLS-1$
            fVersion = element.getAttribute("version");
            //$NON-NLS-1$
            fInstallMode = element.getAttribute("installMode");
        }
    }

    @Override
    public void write(String indent, PrintWriter writer) {
        //$NON-NLS-1$ //$NON-NLS-2$
        writer.print(indent + "<feature id=\"" + fId + "\"");
        if (fVersion != null && fVersion.length() > 0 && !fVersion.equals(ICoreConstants.DEFAULT_VERSION)) {
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.print(" version=\"" + fVersion + "\"");
        }
        if (//$NON-NLS-1$
        fInstallMode != null && !fInstallMode.equals("")) {
            //$NON-NLS-1$ //$NON-NLS-2$
            writer.print(" installMode=\"" + fInstallMode + "\"");
        }
        //$NON-NLS-1$
        writer.println("/>");
    }

    @Override
    public String getId() {
        return fId;
    }

    @Override
    public void setId(String id) {
        fId = id;
    }

    @Override
    public String getVersion() {
        return fVersion;
    }

    @Override
    public void setVersion(String version) {
        String old = fVersion;
        fVersion = version;
        if (isEditable())
            //$NON-NLS-1$
            firePropertyChanged("version", old, fVersion);
    }
}
