/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.search;

import org.eclipse.core.runtime.*;
import org.eclipse.pde.core.ISourceObject;
import org.eclipse.pde.internal.core.search.*;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;

public class PluginSearchQuery implements ISearchQuery {

    private SearchResult fSearchResult;

    private PluginSearchInput fSearchInput;

    public  PluginSearchQuery(PluginSearchInput input) {
        fSearchInput = input;
    }

    @Override
    public IStatus run(IProgressMonitor monitor) {
        final AbstractTextSearchResult result = (AbstractTextSearchResult) getSearchResult();
        result.removeAll();
        ISearchResultCollector collector = new ISearchResultCollector() {

            @Override
            public void accept(Object match) {
                if (match instanceof ISourceObject) {
                    ISourceObject object = (ISourceObject) match;
                    result.addMatch(new Match(match, Match.UNIT_LINE, object.getStartLine() - 1, 1));
                }
            }
        };
        PluginSearchOperation op = new PluginSearchOperation(fSearchInput, collector);
        op.execute(monitor);
        monitor.done();
        return Status.OK_STATUS;
    }

    @Override
    public String getLabel() {
        return fSearchInput.getSearchString();
    }

    @Override
    public boolean canRerun() {
        return true;
    }

    @Override
    public boolean canRunInBackground() {
        return true;
    }

    @Override
    public ISearchResult getSearchResult() {
        if (fSearchResult == null)
            fSearchResult = new SearchResult(this);
        return fSearchResult;
    }
}
