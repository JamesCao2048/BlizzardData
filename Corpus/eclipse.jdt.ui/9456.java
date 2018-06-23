/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Offer new command "Annotate" on ClassFileEditor - https://bugs.eclipse.org/458201
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.text.correction;

import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor;

public class JavaCorrectionAssistant extends QuickAssistAssistant {

    private ITextViewer fViewer;

    private ITextEditor fEditor;

    private Position fPosition;

    private Annotation[] fCurrentAnnotations;

    private QuickAssistLightBulbUpdater fLightBulbUpdater;

    private boolean fIsCompletionActive;

    private boolean fIsProblemLocationAvailable;

    /**
	 * Constructor for JavaCorrectionAssistant.
	 * @param editor the editor
	 */
    public  JavaCorrectionAssistant(ITextEditor editor) {
        super();
        Assert.isNotNull(editor);
        fEditor = editor;
        IQuickAssistProcessor processor;
        if (editor instanceof ClassFileEditor)
            processor = new ExternalNullAnnotationQuickAssistProcessor(this);
        else
            processor = new JavaCorrectionProcessor(this);
        setQuickAssistProcessor(processor);
        enableColoredLabels(PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS));
        setInformationControlCreator(getInformationControlCreator());
        addCompletionListener(new ICompletionListener() {

            @Override
            public void assistSessionEnded(ContentAssistEvent event) {
                fIsCompletionActive = false;
            }

            @Override
            public void assistSessionStarted(ContentAssistEvent event) {
                fIsCompletionActive = true;
            }

            @Override
            public void selectionChanged(ICompletionProposal proposal, boolean smartToggle) {
            }
        });
    }

    public IEditorPart getEditor() {
        return fEditor;
    }

    private IInformationControlCreator getInformationControlCreator() {
        return new IInformationControlCreator() {

            @Override
            public IInformationControl createInformationControl(Shell parent) {
                return new DefaultInformationControl(parent, JavaPlugin.getAdditionalInfoAffordanceString());
            }
        };
    }

    @Override
    public void install(ISourceViewer sourceViewer) {
        super.install(sourceViewer);
        fViewer = sourceViewer;
        fLightBulbUpdater = new QuickAssistLightBulbUpdater(fEditor, sourceViewer);
        fLightBulbUpdater.install();
    }

    @Override
    public void uninstall() {
        if (fLightBulbUpdater != null) {
            fLightBulbUpdater.uninstall();
            fLightBulbUpdater = null;
        }
        super.uninstall();
    }

    /*
	 * @see org.eclipse.jface.text.quickassist.QuickAssistAssistant#showPossibleQuickAssists()
	 * @since 3.2
	 */
    /**
	 * Show completions at caret position. If current
	 * position does not contain quick fixes look for
	 * next quick fix on same line by moving from left
	 * to right and restarting at end of line if the
	 * beginning of the line is reached.
	 *
	 * @see IQuickAssistAssistant#showPossibleQuickAssists()
	 */
    @Override
    public String showPossibleQuickAssists() {
        boolean isReinvoked = false;
        fIsProblemLocationAvailable = false;
        if (fIsCompletionActive) {
            if (isUpdatedOffset()) {
                isReinvoked = true;
                restorePosition();
                hide();
                fIsProblemLocationAvailable = true;
            }
        }
        fPosition = null;
        fCurrentAnnotations = null;
        if (fViewer == null || fViewer.getDocument() == null)
            // Let superclass deal with this
            return super.showPossibleQuickAssists();
        ArrayList<Annotation> resultingAnnotations = new ArrayList(20);
        try {
            Point selectedRange = fViewer.getSelectedRange();
            int currOffset = selectedRange.x;
            int currLength = selectedRange.y;
            boolean goToClosest = (currLength == 0) && !isReinvoked;
            int newOffset = collectQuickFixableAnnotations(fEditor, currOffset, goToClosest, resultingAnnotations);
            if (newOffset != currOffset) {
                storePosition(currOffset, currLength);
                fViewer.setSelectedRange(newOffset, 0);
                fViewer.revealRange(newOffset, 0);
                fIsProblemLocationAvailable = true;
                if (fIsCompletionActive) {
                    hide();
                }
            }
        } catch (BadLocationException e) {
            JavaPlugin.log(e);
        }
        fCurrentAnnotations = resultingAnnotations.toArray(new Annotation[resultingAnnotations.size()]);
        return super.showPossibleQuickAssists();
    }

    private static IRegion getRegionOfInterest(ITextEditor editor, int invocationLocation) throws BadLocationException {
        IDocumentProvider documentProvider = editor.getDocumentProvider();
        if (documentProvider == null) {
            return null;
        }
        IDocument document = documentProvider.getDocument(editor.getEditorInput());
        if (document == null) {
            return null;
        }
        return document.getLineInformationOfOffset(invocationLocation);
    }

    public static int collectQuickFixableAnnotations(ITextEditor editor, int invocationLocation, boolean goToClosest, ArrayList<Annotation> resultingAnnotations) throws BadLocationException {
        IAnnotationModel model = JavaUI.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
        if (model == null) {
            return invocationLocation;
        }
        ensureUpdatedAnnotations(editor);
        Iterator<Annotation> iter = model.getAnnotationIterator();
        if (goToClosest) {
            IRegion lineInfo = getRegionOfInterest(editor, invocationLocation);
            if (lineInfo == null) {
                return invocationLocation;
            }
            int rangeStart = lineInfo.getOffset();
            int rangeEnd = rangeStart + lineInfo.getLength();
            ArrayList<Annotation> allAnnotations = new ArrayList();
            ArrayList<Position> allPositions = new ArrayList();
            int bestOffset = Integer.MAX_VALUE;
            while (iter.hasNext()) {
                Annotation annot = iter.next();
                if (JavaCorrectionProcessor.isQuickFixableType(annot)) {
                    Position pos = model.getPosition(annot);
                    if (// inside our range?
                    pos != null && isInside(pos.offset, rangeStart, rangeEnd)) {
                        allAnnotations.add(annot);
                        allPositions.add(pos);
                        bestOffset = processAnnotation(annot, pos, invocationLocation, bestOffset);
                    }
                }
            }
            if (bestOffset == Integer.MAX_VALUE) {
                bestOffset = invocationLocation;
            }
            for (int i = 0; i < allPositions.size(); i++) {
                Position pos = allPositions.get(i);
                if (isInside(bestOffset, pos.offset, pos.offset + pos.length)) {
                    resultingAnnotations.add(allAnnotations.get(i));
                }
            }
            return bestOffset;
        } else {
            while (iter.hasNext()) {
                Annotation annot = iter.next();
                if (JavaCorrectionProcessor.isQuickFixableType(annot)) {
                    Position pos = model.getPosition(annot);
                    if (pos != null && isInside(invocationLocation, pos.offset, pos.offset + pos.length)) {
                        resultingAnnotations.add(annot);
                    }
                }
            }
            return invocationLocation;
        }
    }

    private static void ensureUpdatedAnnotations(ITextEditor editor) {
        Object inputElement = editor.getEditorInput().getAdapter(IJavaElement.class);
        if (inputElement instanceof ICompilationUnit) {
            SharedASTProvider.getAST((ICompilationUnit) inputElement, SharedASTProvider.WAIT_ACTIVE_ONLY, null);
        }
    }

    private static int processAnnotation(Annotation annot, Position pos, int invocationLocation, int bestOffset) {
        int posBegin = pos.offset;
        int posEnd = posBegin + pos.length;
        if (// covers invocation location?
        isInside(invocationLocation, posBegin, posEnd) && JavaCorrectionProcessor.hasCorrections(annot)) {
            return invocationLocation;
        } else if (bestOffset != invocationLocation) {
            int newClosestPosition = computeBestOffset(posBegin, invocationLocation, bestOffset);
            if (newClosestPosition != -1) {
                if (// new best
                newClosestPosition != // new best
                bestOffset) {
                    if (// only jump to it if there are proposals
                    JavaCorrectionProcessor.hasCorrections(annot)) {
                        return newClosestPosition;
                    }
                }
            }
        }
        return bestOffset;
    }

    private static boolean isInside(int offset, int start, int end) {
        // make sure to handle 0-length ranges
        return offset == start || offset == end || (offset > start && offset < end);
    }

    /**
	 * Computes and returns the invocation offset given a new
	 * position, the initial offset and the best invocation offset
	 * found so far.
	 * <p>
	 * The closest offset to the left of the initial offset is the
	 * best. If there is no offset on the left, the closest on the
	 * right is the best.</p>
	 * @param newOffset the offset to llok at
	 * @param invocationLocation the invocation location
	 * @param bestOffset the current best offset
	 * @return -1 is returned if the given offset is not closer or the new best offset
	 */
    private static int computeBestOffset(int newOffset, int invocationLocation, int bestOffset) {
        if (newOffset <= invocationLocation) {
            if (bestOffset > invocationLocation) {
                // closest was on the right, prefer on the left
                return newOffset;
            } else if (bestOffset <= newOffset) {
                // we are closer or equal
                return newOffset;
            }
            // further away
            return -1;
        }
        if (newOffset <= bestOffset)
            // we are closer or equal
            return newOffset;
        // further away
        return -1;
    }

    /*
	 * @see org.eclipse.jface.text.contentassist.ContentAssistant#possibleCompletionsClosed()
	 */
    @Override
    protected void possibleCompletionsClosed() {
        super.possibleCompletionsClosed();
        restorePosition();
    }

    private void storePosition(int currOffset, int currLength) {
        fPosition = new Position(currOffset, currLength);
    }

    private void restorePosition() {
        if (fPosition != null && !fPosition.isDeleted() && fViewer.getDocument() != null) {
            fViewer.setSelectedRange(fPosition.offset, fPosition.length);
            fViewer.revealRange(fPosition.offset, fPosition.length);
        }
        fPosition = null;
    }

    /**
	 * Returns true if the last invoked completion was called with an updated offset.
	 * @return <code> true</code> if the last invoked completion was called with an updated offset.
	 */
    public boolean isUpdatedOffset() {
        return fPosition != null;
    }

    /**
	 * @return <code>true</code> if a problem exist on the current line and the completion was not invoked at the problem location
	 * @since 3.4
	 */
    public boolean isProblemLocationAvailable() {
        return fIsProblemLocationAvailable;
    }

    /**
	 * Returns the annotations at the current offset
	 * @return the annotations at the offset
	 */
    public Annotation[] getAnnotationsAtOffset() {
        return fCurrentAnnotations;
    }
}
