/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Cabe <benjamin.cabe@anyware-tech.com> - bug 262977
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.site;

import java.io.*;
import java.util.ArrayList;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.pde.core.*;
import org.eclipse.pde.internal.core.AbstractModel;
import org.eclipse.pde.internal.core.isite.ISiteModel;
import org.eclipse.pde.internal.core.site.ExternalSiteModel;
import org.eclipse.pde.internal.core.site.WorkspaceSiteModel;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.context.XMLInputContext;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.*;

public class SiteInputContext extends XMLInputContext {

    //$NON-NLS-1$
    public static final String CONTEXT_ID = "site-context";

    private boolean storageModel = false;

    /**
	 * @param editor
	 * @param input
	 */
    public  SiteInputContext(PDEFormEditor editor, IEditorInput input, boolean primary) {
        super(editor, input, primary);
        create();
    }

    @Override
    protected IBaseModel createModel(IEditorInput input) {
        IBaseModel model = null;
        InputStream is = null;
        try {
            if (input instanceof IFileEditorInput) {
                IFile file = ((IFileEditorInput) input).getFile();
                is = new BufferedInputStream(file.getContents());
                model = createWorkspaceModel(file, is, true);
            } else if (input instanceof IStorageEditorInput) {
                is = new BufferedInputStream(((IStorageEditorInput) input).getStorage().getContents());
                model = createStorageModel(is);
            } else if (input instanceof IURIEditorInput) {
                IFileStore store = EFS.getStore(((IURIEditorInput) input).getURI());
                is = store.openInputStream(EFS.CACHE, new NullProgressMonitor());
                model = createStorageModel(is);
            }
        } catch (CoreException e) {
            PDEPlugin.logException(e);
            return null;
        }
        return model;
    }

    private IBaseModel createWorkspaceModel(IFile file, InputStream stream, boolean editable) {
        WorkspaceSiteModel model = new WorkspaceSiteModel(file);
        try {
            model.setEditable(editable);
            model.load(stream, false);
        } catch (CoreException e) {
        }
        try {
            stream.close();
        } catch (IOException e) {
            PDEPlugin.logException(e);
        }
        return model;
    }

    private IBaseModel createStorageModel(InputStream stream) {
        ExternalSiteModel model = new ExternalSiteModel();
        try {
            model.load(stream, true);
        } catch (CoreException e) {
        } finally {
            try {
                stream.close();
            } catch (IOException e1) {
            }
        }
        return model;
    }

    @Override
    public void dispose() {
        ISiteModel model = (ISiteModel) getModel();
        if (storageModel) {
            model.dispose();
        }
        super.dispose();
    }

    @Override
    protected void flushModel(IDocument doc) {
        // pick up the changes.
        if (!(getModel() instanceof IEditable))
            return;
        IEditable editableModel = (IEditable) getModel();
        if (editableModel.isEditable() == false)
            return;
        if (editableModel.isDirty() == false)
            return;
        try {
            StringWriter swriter = new StringWriter();
            PrintWriter writer = new PrintWriter(swriter);
            editableModel.save(writer);
            writer.flush();
            swriter.close();
            String content = swriter.toString();
            content = AbstractModel.fixLineDelimiter(content, (IFile) ((IModel) getModel()).getUnderlyingResource());
            doc.set(content);
        } catch (IOException e) {
            PDEPlugin.logException(e);
        }
    }

    @Override
    protected boolean synchronizeModel(IDocument doc) {
        ISiteModel model = (ISiteModel) getModel();
        boolean cleanModel = true;
        String text = doc.get();
        try {
            //$NON-NLS-1$
            InputStream stream = new ByteArrayInputStream(text.getBytes("UTF8"));
            try {
                model.reload(stream, false);
            } catch (CoreException e) {
                cleanModel = false;
            }
            try {
                stream.close();
            } catch (IOException e) {
            }
        } catch (UnsupportedEncodingException e) {
            PDEPlugin.logException(e);
        }
        return cleanModel;
    }

    @Override
    public String getId() {
        return CONTEXT_ID;
    }

    @Override
    protected void addTextEditOperation(ArrayList<TextEdit> ops, IModelChangedEvent event) {
    }

    @Override
    protected void reorderInsertEdits(ArrayList<TextEdit> ops) {
    }

    @Override
    protected String getPartitionName() {
        //$NON-NLS-1$
        return "___site_partition";
    }
}
