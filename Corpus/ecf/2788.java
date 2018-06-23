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
 * Callback that handles passwords
 * 
 */
public class PasswordCallback implements Callback, java.io.Serializable {

    private static final long serialVersionUID = 6940002988125290335L;

    private String prompt;

    private String defaultPassword;

    private String inputPassword;

    /**
	 * Construct a <code>PasswordCallback</code> with a prompt.
	 * 
	 * @param prompt
	 *            the prompt used to request the name.
	 * 
	 * @exception IllegalArgumentException
	 *                if <code>prompt</code> is null.
	 */
    public  PasswordCallback(String prompt) {
        if (prompt == null)
            //$NON-NLS-1$
            throw new IllegalArgumentException("Prompt cannot be null");
        this.prompt = prompt;
    }

    /**
	 * Construct a <code>PasswordCallback</code> with a prompt and default password.
	 * 
	 * <p>
	 * 
	 * @param prompt
	 *            the prompt used to request the information.
	 *            <p>
	 * 
	 * @param defaultPassword
	 *            the name to be used as the default name displayed with the
	 *            prompt.
	 * 
	 * @exception IllegalArgumentException
	 *                if <code>prompt</code> is null.
	 */
    public  PasswordCallback(String prompt, String defaultPassword) {
        if (prompt == null)
            //$NON-NLS-1$
            throw new IllegalArgumentException("Prompt cannot be null");
        this.prompt = prompt;
        this.defaultPassword = defaultPassword;
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
	 * Get the default password.
	 * 
	 * <p>
	 * 
	 * @return the default password, or <code>null</code> if this <code>PasswordCallback</code> was
	 *         not instantiated with a <code>defaultPassword</code>.
	 */
    public String getDefaultPassword() {
        return defaultPassword;
    }

    /**
	 * Set the retrieved password.
	 * 
	 * <p>
	 * 
	 * @param pw
	 *            the password (which may be null).
	 * 
	 * @see #getPassword
	 */
    public void setPassword(String pw) {
        this.inputPassword = pw;
    }

    /**
	 * Get the retrieved password.
	 * 
	 * <p>
	 * 
	 * @return the retrieved password (which may be null)
	 * 
	 * @see #setPassword
	 */
    public String getPassword() {
        return inputPassword;
    }
}
