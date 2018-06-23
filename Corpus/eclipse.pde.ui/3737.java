/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Cabe <benjamin.cabe@anyware-tech.com> - bug 97149, bug 268363
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.product;

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
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.product.ProductModel;
import org.eclipse.pde.internal.core.product.WorkspaceProductModel;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.context.UTF8InputContext;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.*;

public class ProductInputContext extends UTF8InputContext {

    //$NON-NLS-1$
    public static final String CONTEXT_ID = "product-context";

    public  ProductInputContext(PDEFormEditor editor, IEditorInput input, boolean primary) {
        super(editor, input, primary);
        create();
    }

    @Override
    public String getId() {
        return CONTEXT_ID;
    }

    @Override
    protected IBaseModel createModel(IEditorInput input) throws CoreException {
        IProductModel model = null;
        if (input instanceof IStorageEditorInput) {
            try {
                if (input instanceof IFileEditorInput) {
                    IFile file = ((IFileEditorInput) input).getFile();
                    model = new WorkspaceProductModel(file, true);
                    model.load();
                } else if (input instanceof IStorageEditorInput) {
                    InputStream is = new BufferedInputStream(((IStorageEditorInput) input).getStorage().getContents());
                    model = new ProductModel();
                    model.load(is, false);
                }
            } catch (CoreException e) {
                PDEPlugin.logException(e);
                return null;
            }
        } else if (input instanceof IURIEditorInput) {
            IFileStore store = EFS.getStore(((IURIEditorInput) input).getURI());
            InputStream is = store.openInputStream(EFS.CACHE, new NullProgressMonitor());
            model = new ProductModel();
            model.load(is, false);
        }
        return model;
    }

    @Override
    protected void addTextEditOperation(ArrayList<TextEdit> ops, IModelChangedEvent event) {
    }

    @Override
    protected void flushModel(IDocument doc) {
        if (!(getModel() instanceof IEditable))
            return;
        IEditable editableModel = (IEditable) getModel();
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
    public void doRevert() {
        fEditOperations.clear();
        IProductModel model = (IProductModel) getModel();
        try {
            InputStream is = ((IFile) model.getUnderlyingResource()).getContents();
            model.reload(is, true);
            getEditor().doRevert();
        } catch (CoreException e) {
        }
    }

    @Override
    protected String getPartitionName() {
        //$NON-NLS-1$
        return "___prod_partition";
    }
}
