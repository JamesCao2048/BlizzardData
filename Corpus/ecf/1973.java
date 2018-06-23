/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.security;

/**
 * Callback that handles String types
 * 
 */
public class NameCallback implements Callback, java.io.Serializable {

    private static final long serialVersionUID = -2506493444608585718L;

    private String prompt;

    private String defaultName;

    private String inputName;

    /**
	 * Construct a <code>NameCallback</code> with a prompt.
	 * 
	 * @param prompt
	 *            the prompt used to request the name.
	 * 
	 * @exception IllegalArgumentException
	 *                if <code>prompt</code> is null.
	 */
    public  NameCallback(String prompt) {
        if (prompt == null)
            //$NON-NLS-1$
            throw new IllegalArgumentException("Prompt cannot be null");
        this.prompt = prompt;
    }

    /**
	 * Construct a <code>NameCallback</code> with a prompt and default name.
	 * 
	 * <p>
	 * 
	 * @param prompt
	 *            the prompt used to request the information.
	 *            <p>
	 * 
	 * @param defaultName
	 *            the name to be used as the default name displayed with the
	 *            prompt.
	 * 
	 * @exception IllegalArgumentException
	 *                if <code>prompt</code> is null.
	 */
    public  NameCallback(String prompt, String defaultName) {
        if (prompt == null)
            //$NON-NLS-1$
            throw new IllegalArgumentException("Prompt cannot be null");
        this.prompt = prompt;
        this.defaultName = defaultName;
    }

    /**
	 * Get the prompt.
	 * 
	 * <p>
	 * 
	 * @return the prompt.
	 */
    public String getPrompt() {
        return prompt;
    }

    /**
	 * Get the default name.
	 * 
	 * <p>
	 * 
	 * @return the default name, or null if this <code>NameCallback</code> was
	 *         not instantiated with a <code>defaultName</code>.
	 */
    public String getDefaultName() {
        return defaultName;
    }

    /**
	 * Set the retrieved name.
	 * 
	 * <p>
	 * 
	 * @param name
	 *            the retrieved name (which may be null).
	 * 
	 * @see #getName
	 */
    public void setName(String name) {
        this.inputName = name;
    }

    /**
	 * Get the retrieved name.
	 * 
	 * <p>
	 * 
	 * @return the retrieved name (which may be null)
	 * 
	 * @see #setName
	 */
    public String getName() {
        return inputName;
    }
}
