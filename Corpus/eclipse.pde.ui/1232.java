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
package org.eclipse.pde.internal.ui.editor.plugin;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.filebuffers.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.*;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.ClasspathHelper;
import org.eclipse.pde.internal.core.TargetPlatformHelper;
import org.eclipse.pde.internal.core.project.PDEProject;
import org.eclipse.pde.internal.core.util.ManifestUtils;
import org.eclipse.text.edits.*;

public class CreateManifestOperation implements IRunnableWithProgress {

    private IPluginModelBase fModel;

    public  CreateManifestOperation(IPluginModelBase model) {
        fModel = model;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            handleConvert();
            trimOldManifest();
        } catch (BadLocationException e) {
            throw new InvocationTargetException(e);
        } catch (CoreException e) {
            throw new InvocationTargetException(e);
        }
    }

    private void handleConvert() throws CoreException {
        IProject project = fModel.getUnderlyingResource().getProject();
        String target = TargetPlatformHelper.getTargetVersionString();
        ManifestUtils.convertToOSGIFormat(project, target, ClasspathHelper.getDevDictionary(fModel));
    }

    private void trimOldManifest() throws BadLocationException, CoreException {
        ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
        IProject project = fModel.getUnderlyingResource().getProject();
        IFile file = fModel.isFragmentModel() ? PDEProject.getFragmentXml(project) : PDEProject.getPluginXml(project);
        try {
            manager.connect(file.getFullPath(), LocationKind.NORMALIZE, null);
            ITextFileBuffer buffer = manager.getTextFileBuffer(file.getFullPath(), LocationKind.NORMALIZE);
            IDocument doc = buffer.getDocument();
            FindReplaceDocumentAdapter adapter = new FindReplaceDocumentAdapter(doc);
            MultiTextEdit multiEdit = new MultiTextEdit();
            //$NON-NLS-1$ //$NON-NLS-2$
            TextEdit edit = editRootElement(fModel.isFragmentModel() ? "fragment" : "plugin", adapter, doc, 0);
            if (edit != null)
                multiEdit.addChild(edit);
            //$NON-NLS-1$
            edit = removeElement("requires", adapter, doc, 0);
            if (edit != null)
                multiEdit.addChild(edit);
            //$NON-NLS-1$
            edit = removeElement("runtime", adapter, doc, 0);
            if (edit != null)
                multiEdit.addChild(edit);
            if (multiEdit.hasChildren()) {
                multiEdit.apply(doc);
                buffer.commit(null, true);
            }
        } finally {
            manager.disconnect(file.getFullPath(), LocationKind.NORMALIZE, null);
        }
    }

    private TextEdit editRootElement(String elementName, FindReplaceDocumentAdapter adapter, IDocument doc, int offset) throws BadLocationException {
        //$NON-NLS-1$ //$NON-NLS-2$
        IRegion region = adapter.find(0, "<" + elementName + "[^>]*", true, true, false, true);
        if (region != null) {
            //$NON-NLS-1$
            String replacementString = "<" + elementName;
            if (doc.getChar(region.getOffset() + region.getLength()) == '/')
                //$NON-NLS-1$
                replacementString += //$NON-NLS-1$
                "/";
            return new ReplaceEdit(region.getOffset(), region.getLength(), replacementString);
        }
        return null;
    }

    private TextEdit removeElement(String elementName, FindReplaceDocumentAdapter adapter, IDocument doc, int offset) throws BadLocationException {
        //$NON-NLS-1$ //$NON-NLS-2$
        IRegion region = adapter.find(0, "<" + elementName + "[^>]*", true, true, false, true);
        if (region != null) {
            if (doc.getChar(region.getOffset() + region.getLength()) == '/')
                return new DeleteEdit(region.getOffset(), region.getLength() + 1);
            //$NON-NLS-1$ //$NON-NLS-2$
            IRegion endRegion = adapter.find(0, "</" + elementName + ">", true, true, false, true);
            if (endRegion != null) {
                int lastPos = endRegion.getOffset() + endRegion.getLength() + 1;
                while (Character.isWhitespace(doc.getChar(lastPos))) {
                    lastPos += 1;
                }
                lastPos -= 1;
                return new DeleteEdit(region.getOffset(), lastPos - region.getOffset());
            }
        }
        return null;
    }
}
