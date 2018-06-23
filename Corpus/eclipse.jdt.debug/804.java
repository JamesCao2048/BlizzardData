/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.display;

import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;

/**
 * An implementation of a data display for a text viewer
 */
public class DataDisplay implements IDataDisplay {

    /**
	 * The text viewer this data display works on
	 */
    private ITextViewer fTextViewer;

    /**
	 * Constructs a data display for the given text viewer.
	 * 
	 * @param viewer text viewer
	 */
    public  DataDisplay(ITextViewer viewer) {
        setTextViewer(viewer);
    }

    /**
	 * @see IDataDisplay#clear()
	 */
    @Override
    public void clear() {
        IDocument document = getTextViewer().getDocument();
        if (document != null) {
            //$NON-NLS-1$
            document.set("");
        }
    }

    /**
	 * @see IDataDisplay#displayExpression(String)
	 */
    @Override
    public void displayExpression(String expression) {
        IDocument document = fTextViewer.getDocument();
        int offset = document.getLength();
        try {
            // add a cariage return if needed.
            if (offset != document.getLineInformationOfOffset(offset).getOffset()) {
                expression = System.getProperty("line.separator") + //$NON-NLS-1$
                expression.trim();
            }
            document.replace(offset, 0, expression);
            fTextViewer.setSelectedRange(offset + expression.length(), 0);
            fTextViewer.revealRange(offset, expression.length());
        } catch (BadLocationException ble) {
            JDIDebugUIPlugin.log(ble);
        }
    }

    /**
	 * @see IDataDisplay#displayExpressionValue(String)
	 */
    @Override
    public void displayExpressionValue(String value) {
        //$NON-NLS-1$
        value = System.getProperty("line.separator") + '\t' + value;
        ITextSelection selection = (ITextSelection) fTextViewer.getSelectionProvider().getSelection();
        int offset = selection.getOffset() + selection.getLength();
        int length = value.length();
        try {
            fTextViewer.getDocument().replace(offset, 0, value);
        } catch (BadLocationException ble) {
            JDIDebugUIPlugin.log(ble);
        }
        fTextViewer.setSelectedRange(offset + length, 0);
        fTextViewer.revealRange(offset, length);
    }

    /**
	 * Sets the text viewer for this data display
	 * 
	 * @param viewer text viewer
	 */
    private void setTextViewer(ITextViewer viewer) {
        fTextViewer = viewer;
    }

    /**
	 * Returns the text viewer for this data display
	 * 
	 * @return text viewer
	 */
    protected ITextViewer getTextViewer() {
        return fTextViewer;
    }
}
