/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.refactoring.reorg;

import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Null implementation of reorg queries.
 *
 * @since 3.3
 */
public final class NullReorgQueries implements IReorgQueries {

    /** Null implementation of confirm query */
    private static final class NullConfirmQuery implements IConfirmQuery {

        @Override
        public boolean confirm(String question) throws OperationCanceledException {
            return true;
        }

        @Override
        public boolean confirm(String question, Object[] elements) throws OperationCanceledException {
            return true;
        }
    }

    /** The null query */
    private static final IConfirmQuery NULL_QUERY = new NullConfirmQuery();

    @Override
    public IConfirmQuery createSkipQuery(String queryTitle, int queryID) {
        return NULL_QUERY;
    }

    @Override
    public IConfirmQuery createYesNoQuery(String queryTitle, boolean allowCancel, int queryID) {
        return NULL_QUERY;
    }

    @Override
    public IConfirmQuery createYesYesToAllNoNoToAllQuery(String queryTitle, boolean allowCancel, int queryID) {
        return NULL_QUERY;
    }
}
