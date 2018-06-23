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
package org.eclipse.pde.ui.tests.model.xml;

import junit.framework.TestCase;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.pde.internal.core.text.IModelTextChangeListener;
import org.eclipse.pde.internal.core.text.plugin.PluginModel;
import org.eclipse.pde.internal.core.text.plugin.XMLTextChangeListener;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

public abstract class XMLModelTestCase extends TestCase {

    protected static final String LF = "\n";

    protected static final String CR = "\r";

    protected static final String CRLF = CR + LF;

    protected Document fDocument;

    protected PluginModel fModel;

    protected IModelTextChangeListener fListener;

    @Override
    protected void setUp() throws Exception {
        fDocument = new Document();
    }

    protected void load() {
        load(false);
    }

    protected void load(boolean addListener) {
        try {
            fModel = new PluginModel(fDocument, true);
            fModel.load();
            if (!fModel.isLoaded() || !fModel.isValid())
                fail("model cannot be loaded");
            if (addListener) {
                fListener = new XMLTextChangeListener(fModel.getDocument());
                fModel.addModelChangedListener(fListener);
            }
        } catch (CoreException e) {
            fail("model cannot be loaded");
        }
    }

    protected void setXMLContents(StringBuffer body, String newline) {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append(newline);
        sb.append("<?eclipse version=\"3.2\"?>");
        sb.append(newline);
        sb.append("<plugin>");
        sb.append(newline);
        if (body != null)
            sb.append(body.toString());
        sb.append(newline);
        sb.append("</plugin>");
        sb.append(newline);
        fDocument.set(sb.toString());
    }

    protected void reload() {
        TextEdit[] ops = fListener.getTextOperations();
        if (ops.length == 0)
            return;
        MultiTextEdit multi = new MultiTextEdit();
        multi.addChildren(ops);
        try {
            multi.apply(fDocument);
        } catch (MalformedTreeException e) {
            fail(e.getMessage());
        } catch (BadLocationException e) {
            fail(e.getMessage());
        }
        load();
    }
}
