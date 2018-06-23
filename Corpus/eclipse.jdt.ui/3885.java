/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.propertiesfileeditor;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WordPatternRule;

/**
 * A leading white space predicate rule.
 *
 * @since 3.1
 */
public final class LeadingWhitespacePredicateRule extends WordPatternRule {

    private static class DummyDetector implements IWordDetector {

        /*
		 * @see IWordDetector#isWordStart
		 */
        @Override
        public boolean isWordStart(char c) {
            return false;
        }

        /*
		 * @see IWordDetector#isWordPart
		 */
        @Override
        public boolean isWordPart(char c) {
            return false;
        }
    }

    /**
	 * Creates a white space rule for the given <code>token</code>.
	 *
	 * @param token the token to be returned on success
	 */
    public  LeadingWhitespacePredicateRule(IToken token, String whitespace) {
        //$NON-NLS-1$
        super(new DummyDetector(), whitespace, "dummy", token);
        setColumnConstraint(0);
    }

    /*
	 * @see org.eclipse.jface.text.rules.WordPatternRule#endSequenceDetected(org.eclipse.jface.text.rules.ICharacterScanner)
	 */
    @Override
    protected boolean endSequenceDetected(ICharacterScanner scanner) {
        int c;
        do {
            c = scanner.read();
        } while (Character.isWhitespace((char) c));
        scanner.unread();
        return true;
    }
}
