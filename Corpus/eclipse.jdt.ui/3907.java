/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.text.javadoc;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.BoldStylerProvider;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;

/**
 * Types directly completed to &#x7b;&#x40;link Type&#x7d;. See {@link CompletionProposal#JAVADOC_TYPE_REF}.
 *
 * @since 3.2
 */
public class JavadocLinkTypeCompletionProposal extends LazyJavaTypeCompletionProposal {

    //$NON-NLS-1$
    private static final String LINK_PREFIX = "{@link ";

    public  JavadocLinkTypeCompletionProposal(CompletionProposal proposal, JavaContentAssistInvocationContext context) {
        super(proposal, context);
        Assert.isTrue(isInJavadoc());
    }

    /*
	 * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal#computeReplacementString()
	 */
    @Override
    protected String computeReplacementString() {
        String typeReplacement = super.computeReplacementString();
        //$NON-NLS-1$
        return LINK_PREFIX + typeReplacement + "}";
    //		else
    //			return "{@link " + typeReplacement; //$NON-NLS-1$
    }

    /*
	 * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal#apply(org.eclipse.jface.text.IDocument, char, int)
	 */
    @Override
    public void apply(IDocument document, char trigger, int offset) {
        // convert . to #
        if (trigger == '.')
            trigger = '#';
        // XXX: respect the auto-close preference, but do so consistently with method completions
        // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=113544
        //		boolean continueWithMember= trigger == '#' && autocloseBrackets();
        boolean continueWithMember = trigger == '#';
        if (continueWithMember)
            // before the closing curly brace
            setCursorPosition(getCursorPosition() - 1);
        super.apply(document, trigger, offset);
        if (continueWithMember)
            setUpLinkedMode(document, '}');
    }

    @Override
    public StyledString getStyledDisplayString(IDocument document, int offset, BoldStylerProvider boldStylerProvider) {
        StyledString styledDisplayString = new StyledString();
        styledDisplayString.append(getStyledDisplayString());
        String pattern = getPatternToEmphasizeMatch(document, offset);
        if (pattern != null && pattern.length() > 0) {
            String displayString = styledDisplayString.getString();
            int index = displayString.indexOf('-');
            if (index != -1) {
                displayString = displayString.substring(0, index);
            }
            int prefixLength = LINK_PREFIX.length();
            displayString = displayString.substring(prefixLength);
            int patternMatchRule = getPatternMatchRule(pattern, displayString);
            int[] matchingRegions = SearchPattern.getMatchingRegions(pattern, displayString, patternMatchRule);
            if (matchingRegions != null) {
                for (int i = 0; i < matchingRegions.length; i += 2) {
                    matchingRegions[i] = matchingRegions[i] + prefixLength;
                }
            }
            Strings.markMatchingRegions(styledDisplayString, 0, matchingRegions, boldStylerProvider.getBoldStyler());
        }
        return styledDisplayString;
    }
}
