/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - [misc] Allow custom token for WhitespaceRule - https://bugs.eclipse.org/bugs/show_bug.cgi?id=251224
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.text.javadoc;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jdt.internal.ui.text.CombinedWordRule;
import org.eclipse.jdt.internal.ui.text.CombinedWordRule.CharacterBuffer;
import org.eclipse.jdt.internal.ui.text.CombinedWordRule.WordMatcher;
import org.eclipse.jdt.internal.ui.text.JavaCommentScanner;
import org.eclipse.jdt.internal.ui.text.JavaWhitespaceDetector;

/**
 * A rule based JavaDoc scanner.
 */
public final class JavaDocScanner extends JavaCommentScanner {

    /**
	 * Detector for HTML comment delimiters.
	 */
    static class HTMLCommentDetector implements IWordDetector {

        /**
		 * @see IWordDetector#isWordStart(char)
		 */
        @Override
        public boolean isWordStart(char c) {
            return (c == '<' || c == '-');
        }

        /**
		 * @see IWordDetector#isWordPart(char)
		 */
        @Override
        public boolean isWordPart(char c) {
            return (c == '-' || c == '!' || c == '>');
        }
    }

    class TagRule extends SingleLineRule {

        /*
		 * @see SingleLineRule
		 */
        public  TagRule(IToken token) {
            //$NON-NLS-2$ //$NON-NLS-1$
            super("<", ">", token, (char) 0);
        }

        /*
		 * @see SingleLineRule
		 */
        public  TagRule(IToken token, char escapeCharacter) {
            //$NON-NLS-2$ //$NON-NLS-1$
            super("<", ">", token, escapeCharacter);
        }

        private IToken evaluateToken() {
            try {
                final String token = //$NON-NLS-1$
                getDocument().get(getTokenOffset(), getTokenLength()) + //$NON-NLS-1$
                ".";
                int offset = 0;
                char character = token.charAt(++offset);
                if (character == '/')
                    character = token.charAt(++offset);
                while (Character.isWhitespace(character)) character = token.charAt(++offset);
                while (Character.isLetterOrDigit(character)) character = token.charAt(++offset);
                while (Character.isWhitespace(character)) character = token.charAt(++offset);
                if (offset >= 2 && token.charAt(offset) == fEndSequence[0])
                    return fToken;
            } catch (BadLocationException exception) {
            }
            return getToken(IJavaColorConstants.JAVADOC_DEFAULT);
        }

        /*
		 * @see PatternRule#evaluate(ICharacterScanner)
		 */
        @Override
        public IToken evaluate(ICharacterScanner scanner) {
            IToken result = super.evaluate(scanner);
            if (result == fToken)
                return evaluateToken();
            return result;
        }
    }

    private static String[] fgTokenProperties = { IJavaColorConstants.JAVADOC_KEYWORD, IJavaColorConstants.JAVADOC_TAG, IJavaColorConstants.JAVADOC_LINK, IJavaColorConstants.JAVADOC_DEFAULT, TASK_TAG };

    public  JavaDocScanner(IColorManager manager, IPreferenceStore store, Preferences coreStore) {
        super(manager, store, coreStore, IJavaColorConstants.JAVADOC_DEFAULT, fgTokenProperties);
    }

    /**
	 * Initialize with the given arguments
	 * @param manager	Color manager
	 * @param store	Preference store
	 *
	 * @since 3.0
	 */
    public  JavaDocScanner(IColorManager manager, IPreferenceStore store) {
        this(manager, store, null);
    }

    public IDocument getDocument() {
        return fDocument;
    }

    /*
	 * @see AbstractJavaScanner#createRules()
	 */
    @Override
    protected List<IRule> createRules() {
        List<IRule> list = new ArrayList();
        // Add rule for tags
        Token token = getToken(IJavaColorConstants.JAVADOC_TAG);
        list.add(new TagRule(token));
        // Add rule for HTML comments
        WordRule wordRule = new WordRule(new HTMLCommentDetector(), token);
        //$NON-NLS-1$
        wordRule.addWord("<!--", token);
        //$NON-NLS-1$
        wordRule.addWord("--!>", token);
        list.add(wordRule);
        // Add rules for links
        token = getToken(IJavaColorConstants.JAVADOC_LINK);
        //$NON-NLS-2$ //$NON-NLS-1$
        list.add(new MultiLineRule("{@link", "}", token));
        //$NON-NLS-2$ //$NON-NLS-1$
        list.add(new MultiLineRule("{@value", "}", token));
        //$NON-NLS-2$ //$NON-NLS-1$
        list.add(new MultiLineRule("{@inheritDoc", "}", token));
        // Add rules for @code and @literals
        token = getToken(IJavaColorConstants.JAVADOC_DEFAULT);
        //$NON-NLS-2$ //$NON-NLS-1$
        list.add(new MultiLineRule("{@code", "}", token));
        //$NON-NLS-2$ //$NON-NLS-1$
        list.add(new MultiLineRule("{@literal", "}", token));
        // Add generic whitespace rule
        token = getToken(IJavaColorConstants.JAVADOC_DEFAULT);
        list.add(new WhitespaceRule(new JavaWhitespaceDetector(), token));
        list.addAll(super.createRules());
        return list;
    }

    /*
	 * @see org.eclipse.jdt.internal.ui.text.JavaCommentScanner#createMatchers()
	 */
    @Override
    protected List<WordMatcher> createMatchers() {
        List<WordMatcher> list = super.createMatchers();
        // Add word rule for keywords.
        final IToken token = getToken(IJavaColorConstants.JAVADOC_KEYWORD);
        WordMatcher matcher = new CombinedWordRule.WordMatcher() {

            @Override
            public IToken evaluate(ICharacterScanner scanner, CharacterBuffer word) {
                int length = word.length();
                if (length > 1 && word.charAt(0) == '@') {
                    int i = 0;
                    try {
                        for (; i <= length; i++) scanner.unread();
                        int c = scanner.read();
                        i--;
                        if (c == '*' || Character.isWhitespace((char) c)) {
                            scanner.unread();
                            i++;
                            return token;
                        }
                    } finally {
                        for (; i > 0; i--) scanner.read();
                    }
                }
                return Token.UNDEFINED;
            }
        };
        list.add(matcher);
        return list;
    }
}
