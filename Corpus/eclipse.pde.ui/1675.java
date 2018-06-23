/*******************************************************************************
 *  Copyright (c) 2006, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.outline;

import org.eclipse.jface.viewers.*;
import org.eclipse.pde.internal.ui.util.StringMatcher;

public class QuickOutlineNamePatternFilter extends ViewerFilter {

    private StringMatcher fStringMatcher;

    public  QuickOutlineNamePatternFilter() {
        fStringMatcher = null;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        // viewer is not a tree viewer
        if ((fStringMatcher == null) || ((viewer instanceof TreeViewer) == false)) {
            return true;
        }
        TreeViewer treeViewer = (TreeViewer) viewer;
        // Match the pattern against the label of the given element
        String matchName = ((ILabelProvider) treeViewer.getLabelProvider()).getText(element);
        // Element passes the filter if it matches the pattern
        if ((matchName != null) && fStringMatcher.match(matchName)) {
            return true;
        }
        // Determine whether the element has children that pass the filter
        return hasUnfilteredChild(treeViewer, element);
    }

    private boolean hasUnfilteredChild(TreeViewer viewer, Object element) {
        // No point calling hasChildren() because the operation is the same cost
        // as getting the children
        // If the element has a child that passes the filter, then we want to
        // keep the parent around - even if it does not pass the filter itself
        Object[] children = ((ITreeContentProvider) viewer.getContentProvider()).getChildren(element);
        for (int i = 0; i < children.length; i++) {
            if (select(viewer, element, children[i])) {
                return true;
            }
        }
        // Element does not pass the filter
        return false;
    }

    public void setStringMatcher(StringMatcher stringMatcher) {
        fStringMatcher = stringMatcher;
    }
}
