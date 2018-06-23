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
package org.eclipse.jdt.internal.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Specialization for a String variable classpath entry
 */
public class VariableClasspathEntry extends AbstractRuntimeClasspathEntry {

    //$NON-NLS-1$
    public static final String TYPE_ID = "org.eclipse.jdt.launching.classpathentry.variableClasspathEntry";

    private String variableString;

    /**
	 * Constructor
	 */
    public  VariableClasspathEntry() {
    }

    /**
	 * Constructor
	 * @param variableString the string value
	 */
    public  VariableClasspathEntry(String variableString) {
        this.variableString = variableString;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.launching.AbstractRuntimeClasspathEntry#buildMemento(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
    @Override
    protected void buildMemento(Document document, Element memento) throws CoreException {
        //$NON-NLS-1$
        memento.setAttribute("variableString", variableString);
        //$NON-NLS-1$
        memento.setAttribute("path", Integer.toString(getClasspathProperty()));
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#initializeFrom(org.w3c.dom.Element)
	 */
    @Override
    public void initializeFrom(Element memento) throws CoreException {
        //$NON-NLS-1$
        variableString = memento.getAttribute("variableString");
        //$NON-NLS-1$
        String property = memento.getAttribute("path");
        if (//$NON-NLS-1$
        property != null && !"".equals(property)) {
            try {
                setClasspathProperty(Integer.parseInt(property));
            } catch (NumberFormatException /*do nothing, but don't throw an exception*/
            nfe) {
            }
        }
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#getTypeId()
	 */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#getRuntimeClasspathEntries(org.eclipse.debug.core.ILaunchConfiguration)
	 */
    @Override
    public IRuntimeClasspathEntry[] getRuntimeClasspathEntries(ILaunchConfiguration configuration) throws CoreException {
        return new IRuntimeClasspathEntry[0];
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#getName()
	 */
    @Override
    public String getName() {
        return variableString;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getType()
	 */
    @Override
    public int getType() {
        return OTHER;
    }

    /**
	 * @return Returns the variableString.
	 */
    public String getVariableString() {
        return variableString;
    }

    /**
	 * @param variableString The variableString to set.
	 */
    public void setVariableString(String variableString) {
        this.variableString = variableString;
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
    @Override
    public int hashCode() {
        if (variableString != null) {
            return variableString.hashCode();
        }
        return 0;
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VariableClasspathEntry) {
            VariableClasspathEntry other = (VariableClasspathEntry) obj;
            if (variableString != null) {
                return variableString.equals(other.variableString);
            }
        }
        return false;
    }
}
