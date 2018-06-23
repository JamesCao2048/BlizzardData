/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nicolaj Hoess <nicohoess@gmail.com> - Make some internal methods accessible to help Postfix Code Completion plug-in - https://bugs.eclipse.org/433500
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 486899, 486903
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.text.template.contentassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.template.java.CompilationUnitContext;
import org.eclipse.jdt.internal.corext.template.java.CompilationUnitContextType;
import org.eclipse.jdt.internal.corext.template.java.SWTContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;

public class TemplateEngine {

    //$NON-NLS-1$ //$NON-NLS-2$
    private static final Pattern $_LINE_SELECTION_PATTERN = Pattern.compile("\\$\\{(.*:)?" + GlobalTemplateVariables.LineSelection.NAME + "(\\(.*\\))?\\}");

    //$NON-NLS-1$ //$NON-NLS-2$
    private static final Pattern $_WORD_SELECTION_PATTERN = Pattern.compile("\\$\\{(.*:)?" + GlobalTemplateVariables.WordSelection.NAME + "(\\(.*\\))?\\}");

    /** The context type. */
    private TemplateContextType fContextType;

    /** The result proposals. */
    private ArrayList<TemplateProposal> fProposals = new ArrayList();

    /** Positions created on the key documents to remove in reset. */
    private final Map<IDocument, Position> fPositions = new HashMap();

    /**
	 * Creates the template engine for the given <code>contextType</code>.
	 * <p>
	 * The <code>JavaPlugin.getDefault().getTemplateContextRegistry()</code>
	 * defines the supported context types.</p>
	 *
	 * @param contextType the context type
	 */
    public  TemplateEngine(TemplateContextType contextType) {
        Assert.isNotNull(contextType);
        fContextType = contextType;
    }

    /**
	 * Empties the collector.
	 */
    public void reset() {
        fProposals.clear();
        for (Iterator<Entry<IDocument, Position>> it = fPositions.entrySet().iterator(); it.hasNext(); ) {
            Entry<IDocument, Position> entry = it.next();
            IDocument doc = entry.getKey();
            Position position = entry.getValue();
            doc.removePosition(position);
        }
        fPositions.clear();
    }

    /**
	 * Returns the array of matching templates.
	 *
	 * @return the template proposals
	 */
    public TemplateProposal[] getResults() {
        return fProposals.toArray(new TemplateProposal[fProposals.size()]);
    }

    /**
	 * Inspects the context of the compilation unit around <code>completionPosition</code>
	 * and feeds the collector with proposals.
	 * @param viewer the text viewer
	 * @param completionPosition the context position in the document of the text viewer
	 * @param compilationUnit the compilation unit (may be <code>null</code>)
	 */
    public void complete(ITextViewer viewer, int completionPosition, ICompilationUnit compilationUnit) {
        IDocument document = viewer.getDocument();
        if (!(fContextType instanceof CompilationUnitContextType))
            return;
        Point selection = viewer.getSelectedRange();
        Position position = new Position(completionPosition, selection.y);
        // remember selected text
        String selectedText = null;
        if (selection.y != 0) {
            try {
                selectedText = document.get(selection.x, selection.y);
                document.addPosition(position);
                fPositions.put(document, position);
            } catch (BadLocationException e) {
            }
        }
        CompilationUnitContext context = ((CompilationUnitContextType) fContextType).createContext(document, position, compilationUnit);
        //$NON-NLS-1$
        context.setVariable("selection", selectedText);
        int start = context.getStart();
        int end = context.getEnd();
        IRegion region = new Region(start, end - start);
        Template[] templates = JavaPlugin.getDefault().getTemplateStore().getTemplates();
        if (selection.y == 0) {
            for (int i = 0; i != templates.length; i++) {
                Template template = templates[i];
                if (context.canEvaluate(template)) {
                    fProposals.add(new TemplateProposal(template, context, region, getImage()));
                }
            }
        } else {
            if (context.getKey().length() == 0)
                context.setForceEvaluation(true);
            boolean multipleLinesSelected = areMultipleLinesSelected(viewer);
            for (int i = 0; i != templates.length; i++) {
                Template template = templates[i];
                if (context.canEvaluate(template)) {
                    Matcher wordSelectionMatcher = $_WORD_SELECTION_PATTERN.matcher(template.getPattern());
                    Matcher lineSelectionMatcher = $_LINE_SELECTION_PATTERN.matcher(template.getPattern());
                    if ((!multipleLinesSelected && wordSelectionMatcher.find()) || (multipleLinesSelected && lineSelectionMatcher.find())) {
                        fProposals.add(new TemplateProposal(templates[i], context, region, getImage()));
                    }
                }
            }
        }
    }

    protected TemplateContextType getContextType() {
        return fContextType;
    }

    protected ArrayList<TemplateProposal> getProposals() {
        return fProposals;
    }

    protected Image getImage() {
        if (fContextType instanceof SWTContextType)
            return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_SWT_TEMPLATE);
        else
            return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_TEMPLATE);
    }

    /**
	 * Returns <code>true</code> if one line is completely selected or if multiple lines are selected.
	 * Being completely selected means that all characters except the new line characters are
	 * selected.
	 *
	 * @param viewer the text viewer
	 * @return <code>true</code> if one or multiple lines are selected
	 * @since 2.1
	 */
    private boolean areMultipleLinesSelected(ITextViewer viewer) {
        if (viewer == null)
            return false;
        Point s = viewer.getSelectedRange();
        if (s.y == 0)
            return false;
        try {
            IDocument document = viewer.getDocument();
            int startLine = document.getLineOfOffset(s.x);
            int endLine = document.getLineOfOffset(s.x + s.y);
            IRegion line = document.getLineInformation(startLine);
            return startLine != endLine || (s.x == line.getOffset() && s.y == line.getLength());
        } catch (BadLocationException x) {
            return false;
        }
    }
}
