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
package org.eclipse.pde.internal.ui.editor.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.pde.internal.ui.PDEPluginImages;

public class SortAction extends Action {

    private boolean fSorted;

    private StructuredViewer fViewer;

    private ViewerComparator fComparator;

    private ViewerComparator fDefaultComparator;

    /**
	 * @param viewer
	 * @param tooltipText
	 * @param sorter
	 * @param defaultSorter
	 * @param listener
	 * @param useMiniImage
	 */
    public  SortAction(StructuredViewer viewer, String tooltipText, ViewerComparator sorter, ViewerComparator defaultSorter, IPropertyChangeListener listener) {
        super(tooltipText, IAction.AS_CHECK_BOX);
        // Set the tooltip
        setToolTipText(tooltipText);
        // Set the image
        setImageDescriptor(PDEPluginImages.DESC_ALPHAB_SORT_CO);
        // Set the default comparator
        fDefaultComparator = defaultSorter;
        // Set the viewer
        fViewer = viewer;
        // If one was not specified, use the default
        if (sorter == null) {
            fComparator = new ViewerComparator();
        } else {
            fComparator = sorter;
        }
        // Note: Most likely the default comparator is null
        if (viewer.getComparator() == fDefaultComparator) {
            fSorted = false;
        } else {
            fSorted = true;
        }
        // Set the status of this action depending on whether it is sorted or
        // not
        setChecked(fSorted);
        // If a listener was specified, use it
        if (listener != null) {
            addListenerObject(listener);
        }
    }

    @Override
    public void run() {
        // Toggle sorting on/off
        if (fSorted) {
            // Sorting is on
            // Turn it off
            fViewer.setComparator(fDefaultComparator);
            fSorted = false;
        } else {
            // Sorting is off
            // Turn it on
            fViewer.setComparator(fComparator);
            fSorted = true;
        }
        notifyResult(true);
    }
}
