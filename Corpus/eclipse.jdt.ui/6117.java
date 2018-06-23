/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.astview.views;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.swt.graphics.Image;

public abstract class DynamicBindingProperty extends ExceptionAttribute {

    //$NON-NLS-1$
    protected static final String N_A = "N/A";

    private final Binding fParent;

    private Binding fViewerElement;

    private String fLabel = "<unknown>";

    public  DynamicBindingProperty(Binding parent) {
        fParent = parent;
    }

    @Override
    public Object getParent() {
        return fParent;
    }

    @Override
    public Object[] getChildren() {
        return EMPTY;
    }

    public void setViewerElement(Binding viewerElement) {
        if (fViewerElement == viewerElement)
            return;
        fViewerElement = viewerElement;
        fException = null;
        IBinding trayBinding = fParent.getBinding();
        StringBuffer buf = new StringBuffer(getName());
        if (viewerElement != null) {
            IBinding viewerBinding = viewerElement.getBinding();
            try {
                String queryResult = executeQuery(viewerBinding, trayBinding);
                buf.append(queryResult);
            } catch (RuntimeException e) {
                fException = e;
                buf.append(e.getClass().getName());
                buf.append(" for \"");
                if (viewerBinding == null)
                    buf.append("null");
                else
                    buf.append('"').append(viewerBinding.getKey());
                buf.append("\" and ");
                buf.append(trayBinding.getKey()).append('"');
            }
        } else {
            buf.append(N_A);
        }
        fLabel = buf.toString();
    }

    /**
	 * Executes this dynamic binding property's query in a protected environment.
	 * A {@link RuntimeException} thrown by this method is made available via
	 * {@link #getException()}. 
	 * 
	 * @param viewerBinding the binding of the element selected in the AST viewer, or <code>null</code> iff none
	 * @param trayBinding the binding of the element selected in the comparison tray, or <code>null</code> iff none
	 * @return this property's result
	 */
    protected abstract String executeQuery(IBinding viewerBinding, IBinding trayBinding);

    /**
	 * @return a description of the dynamic property
	 */
    protected abstract String getName();

    @Override
    public String getLabel() {
        return fLabel;
    }

    @Override
    public Image getImage() {
        return null;
    }
}
