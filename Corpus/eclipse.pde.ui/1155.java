/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.text;

import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;
import java.util.*;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.pde.internal.ui.editor.context.XMLDocumentSetupParticpant;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.*;

/**
 * Reconcile strategy used for spell checking XML documents.
 *
 */
public class XMLReconcilingStrategy extends SpellingReconcileStrategy {

    /**
	 * Spelling problem collector.
	 */
    private class SpellingProblemCollector implements ISpellingProblemCollector {

        /** Annotation model. */
        private IAnnotationModel fAnnotationModel;

        /** Annotations to add. */
        private Map<SpellingAnnotation, Position> fAddAnnotations;

        /** Lock object for modifying the annotations. */
        private Object fLockObject;

        /**
		 * Initializes this collector with the given annotation model.
		 *
		 * @param annotationModel the annotation model
		 */
        public  SpellingProblemCollector(IAnnotationModel annotationModel) {
            Assert.isLegal(annotationModel != null);
            fAnnotationModel = annotationModel;
            if (fAnnotationModel instanceof ISynchronizable)
                fLockObject = ((ISynchronizable) fAnnotationModel).getLockObject();
            else
                fLockObject = fAnnotationModel;
        }

        /*
		 * @see org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector#accept(org.eclipse.ui.texteditor.spelling.SpellingProblem)
		 */
        @Override
        public void accept(SpellingProblem problem) {
            fAddAnnotations.put(new SpellingAnnotation(problem), new Position(problem.getOffset(), problem.getLength()));
        }

        /*
		 * @see org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector#beginCollecting()
		 */
        @Override
        public void beginCollecting() {
            fAddAnnotations = new HashMap();
        }

        /*
		 * @see org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector#endCollecting()
		 */
        @Override
        public void endCollecting() {
            synchronized (fLockObject) {
                for (Iterator<SpellingAnnotation> iter = fAddAnnotations.keySet().iterator(); iter.hasNext(); ) {
                    Annotation annotation = iter.next();
                    fAnnotationModel.addAnnotation(annotation, fAddAnnotations.get(annotation));
                }
                deleteNonstringSpellingAnnotations(fAddAnnotations.keySet().iterator());
            }
            fAddAnnotations = null;
        }
    }

    public  XMLReconcilingStrategy(ISourceViewer viewer) {
        super(viewer, EditorsUI.getSpellingService());
    }

    @Override
    public void reconcile(IRegion region) {
        deleteAllAnnotations(region);
        super.reconcile(region);
    }

    /**
	 * Deletes all the spelling annotations located inside the area marked by <code>region</code>
	 */
    private void deleteAllAnnotations(IRegion region) {
        IAnnotationModel model = getAnnotationModel();
        if (model == null)
            return;
        Iterator<?> iter = model.getAnnotationIterator();
        while (iter.hasNext()) {
            Annotation annotation = (Annotation) iter.next();
            if (annotation instanceof SpellingAnnotation) {
                SpellingAnnotation spellingAnnotation = (SpellingAnnotation) annotation;
                Position position = model.getPosition(spellingAnnotation);
                if (position.overlapsWith(region.getOffset(), region.getLength())) {
                    model.removeAnnotation(spellingAnnotation);
                }
            }
        }
    }

    /**
	 * Deletes the spelling annotations marked for XML Tags
	 */
    private void deleteNonstringSpellingAnnotations(Iterator<SpellingAnnotation> iter) {
        if (//can not proceed otherwise
        !(getDocument() instanceof IDocumentExtension3)) {
            return;
        }
        IDocumentExtension3 document = (IDocumentExtension3) getDocument();
        IDocumentPartitioner docPartitioner = document.getDocumentPartitioner(XMLStringPartitionScanner.XML_STRING);
        IDocumentPartitioner pdeXMLPartitioner = document.getDocumentPartitioner(XMLDocumentSetupParticpant.XML_PARTITIONING);
        IAnnotationModel model = getAnnotationModel();
        while (iter.hasNext()) {
            Object annotation = iter.next();
            if (annotation instanceof SpellingAnnotation) {
                SpellingAnnotation spellingAnnotation = (SpellingAnnotation) annotation;
                Position position = model.getPosition(spellingAnnotation);
                String docContentType = docPartitioner.getContentType(position.getOffset());
                String pdeXMLContentType = pdeXMLPartitioner.getContentType(position.getOffset());
                if (// delete the annotation if the marked position was not selected by XML_STRING document partitioner
                (!XMLStringPartitionScanner.XML_STRING.equalsIgnoreCase(docContentType) && !XMLStringPartitionScanner.CUSTOM_TAG.equalsIgnoreCase(docContentType)) || // also delete the annotations that are positioned at special XML Tags
                (XMLStringPartitionScanner.CUSTOM_TAG.equalsIgnoreCase(docContentType)) && XMLPartitionScanner.XML_TAG.equalsIgnoreCase(pdeXMLContentType)) {
                    model.removeAnnotation(spellingAnnotation);
                }
            }
        }
    }

    @Override
    protected ISpellingProblemCollector createSpellingProblemCollector() {
        IAnnotationModel model = getAnnotationModel();
        if (model == null)
            return null;
        return new SpellingProblemCollector(model);
    }
}
