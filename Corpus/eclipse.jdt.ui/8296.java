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
package org.eclipse.jdt.internal.corext.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.corext.util.SearchUtils;

public class SearchResultGroup {

    private final IResource fResouce;

    private final List<SearchMatch> fSearchMatches;

    public  SearchResultGroup(IResource res, SearchMatch[] matches) {
        Assert.isNotNull(matches);
        fResouce = res;
        fSearchMatches = new ArrayList(Arrays.asList(matches));
    }

    public void add(SearchMatch match) {
        Assert.isNotNull(match);
        fSearchMatches.add(match);
    }

    public IResource getResource() {
        return fResouce;
    }

    public SearchMatch[] getSearchResults() {
        return fSearchMatches.toArray(new SearchMatch[fSearchMatches.size()]);
    }

    public static IResource[] getResources(SearchResultGroup[] searchResultGroups) {
        Set<IResource> resourceSet = new HashSet(searchResultGroups.length);
        for (int i = 0; i < searchResultGroups.length; i++) {
            resourceSet.add(searchResultGroups[i].getResource());
        }
        return resourceSet.toArray(new IResource[resourceSet.size()]);
    }

    public ICompilationUnit getCompilationUnit() {
        if (getSearchResults() == null || getSearchResults().length == 0)
            return null;
        return SearchUtils.getCompilationUnit(getSearchResults()[0]);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(fResouce.getFullPath().toString());
        buf.append('\n');
        for (int i = 0; i < fSearchMatches.size(); i++) {
            SearchMatch match = fSearchMatches.get(i);
            //$NON-NLS-1$//$NON-NLS-2$
            buf.append("  ").append(match.getOffset()).append(", ").append(match.getLength());
            //$NON-NLS-1$//$NON-NLS-2$
            buf.append(match.getAccuracy() == SearchMatch.A_ACCURATE ? "; acc" : "; inacc");
            if (match.isInsideDocComment())
                //$NON-NLS-1$
                buf.append(//$NON-NLS-1$
                "; inDoc");
            if (match.getElement() instanceof IJavaElement)
                //$NON-NLS-1$
                buf.append("; in: ").append(//$NON-NLS-1$
                ((IJavaElement) match.getElement()).getElementName());
            buf.append('\n');
        }
        return buf.toString();
    }
}
