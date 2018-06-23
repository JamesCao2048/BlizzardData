/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.text.java;

import org.eclipse.jdt.core.CompletionProposal;

public class RelevanceComputer {

    /**
	 * Computes the relevance for a given <code>CompletionProposal</code>.
	 * 
	 * @param proposal the proposal to compute the relevance for
	 * @return the relevance for <code>proposal</code>
	 */
    public static int computeRelevance(CompletionProposal proposal) {
        final int baseRelevance = proposal.getRelevance() * 16;
        switch(proposal.getKind()) {
            case CompletionProposal.PACKAGE_REF:
                return baseRelevance + 0;
            case CompletionProposal.LABEL_REF:
                return baseRelevance + 1;
            case CompletionProposal.KEYWORD:
                return baseRelevance + 2;
            case CompletionProposal.TYPE_REF:
            case CompletionProposal.ANONYMOUS_CLASS_DECLARATION:
            case CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION:
                return baseRelevance + 3;
            case CompletionProposal.METHOD_REF:
            case CompletionProposal.CONSTRUCTOR_INVOCATION:
            case CompletionProposal.METHOD_NAME_REFERENCE:
            case CompletionProposal.METHOD_DECLARATION:
            case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
                return baseRelevance + 4;
            case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
                return baseRelevance/* + 99 */
                 + 4;
            case CompletionProposal.FIELD_REF:
                return baseRelevance + 5;
            case CompletionProposal.LOCAL_VARIABLE_REF:
            case CompletionProposal.VARIABLE_DECLARATION:
                return baseRelevance + 6;
            default:
                return baseRelevance;
        }
    }
}
