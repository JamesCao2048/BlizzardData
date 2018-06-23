/*******************************************************************************
 *  Copyright (c) 2005, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.search;

import org.eclipse.search.ui.text.Match;
import java.io.File;
import java.util.ArrayList;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.*;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

public class SearchResult extends AbstractTextSearchResult implements IEditorMatchAdapter {

    protected ISearchQuery fQuery;

    public  SearchResult(ISearchQuery query) {
        fQuery = query;
    }

    @Override
    public IEditorMatchAdapter getEditorMatchAdapter() {
        return this;
    }

    @Override
    public String getLabel() {
        int numMatches = getMatchCount();
        //$NON-NLS-1$ //$NON-NLS-2$
        return fQuery.getLabel() + " - " + numMatches + " " + (numMatches == 1 ? PDEUIMessages.SearchResult_match : PDEUIMessages.SearchResult_matches);
    }

    @Override
    public String getTooltip() {
        return null;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return PDEPluginImages.DESC_PSEARCH_OBJ;
    }

    @Override
    public ISearchQuery getQuery() {
        return fQuery;
    }

    @Override
    public boolean isShownInEditor(Match match, IEditorPart editor) {
        Object element = match.getElement();
        if (element instanceof IPluginObject)
            return isMatchContained(editor, (IPluginObject) element);
        return false;
    }

    @Override
    public Match[] computeContainedMatches(AbstractTextSearchResult result, IEditorPart editor) {
        ArrayList<Match> list = new ArrayList();
        Object[] objects = result.getElements();
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof IPluginObject) {
                IPluginObject object = (IPluginObject) objects[i];
                if (isMatchContained(editor, object)) {
                    Match[] matches = getMatches(object);
                    for (int j = 0; j < matches.length; j++) {
                        IDocument document = getDocument(editor, matches[j]);
                        if (document != null)
                            list.add(ManifestEditorOpener.findExactMatch(document, matches[j], editor));
                    }
                }
            }
        }
        return list.toArray(new Match[list.size()]);
    }

    @Override
    public IFileMatchAdapter getFileMatchAdapter() {
        return null;
    }

    protected boolean isMatchContained(IEditorPart editor, IPluginObject object) {
        IFile resource = (IFile) editor.getEditorInput().getAdapter(IFile.class);
        if (resource != null) {
            IResource objectResource = object.getModel().getUnderlyingResource();
            if (objectResource != null)
                return resource.getProject().equals(objectResource.getProject());
        }
        File file = (File) editor.getEditorInput().getAdapter(File.class);
        if (file != null) {
            IPath path = new Path(object.getModel().getInstallLocation());
            IPath filePath = null;
            if (ICoreConstants.MANIFEST_FILENAME.equals(file.getName()))
                filePath = new Path(file.getParentFile().getParent());
            else if (//$NON-NLS-1$
            file.getName().endsWith("jar")) {
                filePath = new Path(file.getPath());
            } else {
                filePath = new Path(file.getParent());
            }
            return path.equals(filePath);
        }
        return false;
    }

    protected IDocument getDocument(IEditorPart editor, Match match) {
        IDocument document = null;
        if (editor instanceof ISearchEditorAccess) {
            document = ((ISearchEditorAccess) editor).getDocument(match);
        } else if (editor instanceof ITextEditor) {
            document = ((ITextEditor) editor).getDocumentProvider().getDocument(editor.getEditorInput());
        }
        return document;
    }
}
