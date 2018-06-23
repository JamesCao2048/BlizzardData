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
 * Callback that handles passphrases
 * 
 */
public class PassphraseCallback implements Callback, java.io.Serializable {

    private static final long serialVersionUID = -6036907502015127266L;

    private String prompt;

    private String defaultPassphrase;

    private String inputPassphrase;

    /**
	 * Construct a <code>PassphraseCallback</code> with a prompt.
	 * 
	 * @param prompt
	 *            the prompt used to request the passphrase.
	 * 
	 * @exception IllegalArgumentException
	 *                if <code>prompt</code> is null.
	 */
    public  PassphraseCallback(String prompt) {
        if (prompt == null)
            //$NON-NLS-1$
            throw new IllegalArgumentException("Prompt cannot be null");
        this.prompt = prompt;
    }

    /**
	 * Construct a <code>PassphraseCallback</code> with a prompt and default passphrase.
	 * 
	 * <p>
	 * 
	 * @param prompt
	 *            the prompt used to request the information.
	 *            <p>
	 * 
	 * @param defaultPassphrase
	 *            the name to be used as the default name displayed with the
	 *            prompt.
	 * 
	 * @exception IllegalArgumentException
	 *                if <code>prompt</code> is null.
	 */
    public  PassphraseCallback(String prompt, String defaultPassphrase) {
        if (prompt == null)
            //$NON-NLS-1$
            throw new IllegalArgumentException("Prompt cannot be null");
        this.prompt = prompt;
        this.defaultPassphrase = defaultPassphrase;
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
	 * Get the default passphrase.
	 * 
	 * <p>
	 * 
	 * @return the default passphrase, or <code>null</code> if this <code>PassphraseCallback</code> was
	 *         not instantiated with a <code>defaultPassphrase</code>.
	 */
    public String getDefaultPassphrase() {
        return defaultPassphrase;
    }

    /**
	 * Set the retrieved passphrase.
	 * 
	 * <p>
	 * 
	 * @param pw
	 *            the passphrase (which may be null).
	 * 
	 * @see #getPassphrase
	 */
    public void setPassphrase(String pw) {
        this.inputPassphrase = pw;
    }

    /**
	 * Get the retrieved passphrase.
	 * 
	 * <p>
	 * 
	 * @return the retrieved passphrase (which may be null)
	 * 
	 * @see #setPassphrase
	 */
    public String getPassphrase() {
        return inputPassphrase;
    }
}
