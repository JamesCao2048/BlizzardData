/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ua.ui.editor.cheatsheet.simple;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.pde.internal.ua.ui.IConstants;
import org.eclipse.pde.internal.ua.ui.PDEUserAssistanceUIPlugin;
import org.eclipse.pde.internal.ua.ui.editor.cheatsheet.CSAbstractEditor;
import org.eclipse.pde.internal.ui.editor.ISortableContentOutlinePage;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.pde.internal.ui.editor.PDESourcePage;
import org.eclipse.pde.internal.ui.editor.context.InputContext;
import org.eclipse.pde.internal.ui.editor.context.InputContextManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.ide.FileStoreEditorInput;

public class SimpleCSEditor extends CSAbstractEditor {

    public  SimpleCSEditor() {
        super();
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormEditor#getEditorID()
	 */
    protected String getEditorID() {
        return IConstants.SIMPLE_CHEAT_SHEET_EDITOR_ID;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormEditor#isSaveAsAllowed()
	 */
    public boolean isSaveAsAllowed() {
        return true;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormEditor#getContextIDForSaveAs()
	 */
    public String getContextIDForSaveAs() {
        return SimpleCSInputContext.CONTEXT_ID;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormEditor#addEditorPages()
	 */
    protected void addEditorPages() {
        // Add form pages
        try {
            addPage(new SimpleCSDefinitionPage(this));
        } catch (PartInitException e) {
            PDEUserAssistanceUIPlugin.logException(e);
        }
        // Add source page
        addSourcePage(SimpleCSInputContext.CONTEXT_ID);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormEditor#createContentOutline()
	 */
    protected ISortableContentOutlinePage createContentOutline() {
        return new SimpleCSFormOutlinePage(this);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormEditor#createInputContextManager()
	 */
    protected InputContextManager createInputContextManager() {
        return new SimpleCSInputContextManager(this);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormEditor#createResourceContexts(org.eclipse.pde.internal.ui.editor.context.InputContextManager, org.eclipse.ui.IFileEditorInput)
	 */
    protected void createResourceContexts(InputContextManager contexts, IFileEditorInput input) {
        contexts.putContext(input, new SimpleCSInputContext(this, input, true));
        contexts.monitorFile(input.getFile());
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormEditor#createStorageContexts(org.eclipse.pde.internal.ui.editor.context.InputContextManager, org.eclipse.ui.IStorageEditorInput)
	 */
    protected void createStorageContexts(InputContextManager contexts, IStorageEditorInput input) {
        contexts.putContext(input, new SimpleCSInputContext(this, input, true));
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormEditor#createSystemFileContexts(org.eclipse.pde.internal.ui.editor.context.InputContextManager, org.eclipse.pde.internal.ui.editor.SystemFileEditorInput)
	 */
    protected void createSystemFileContexts(InputContextManager contexts, FileStoreEditorInput input) {
        try {
            IFileStore store = EFS.getStore(input.getURI());
            IEditorInput in = new FileStoreEditorInput(store);
            contexts.putContext(in, new SimpleCSInputContext(this, in, true));
        } catch (CoreException e) {
            PDEUserAssistanceUIPlugin.logException(e);
        }
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormEditor#editorContextAdded(org.eclipse.pde.internal.ui.editor.context.InputContext)
	 */
    public void editorContextAdded(InputContext context) {
        // Add the source page
        addSourcePage(context.getId());
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormEditor#getInputContext(java.lang.Object)
	 */
    protected InputContext getInputContext(Object object) {
        return fInputContextManager.findContext(SimpleCSInputContext.CONTEXT_ID);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.context.IInputContextListener#contextRemoved(org.eclipse.pde.internal.ui.editor.context.InputContext)
	 */
    public void contextRemoved(InputContext context) {
        close(false);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.context.IInputContextListener#monitoredFileAdded(org.eclipse.core.resources.IFile)
	 */
    public void monitoredFileAdded(IFile monitoredFile) {
    // NO-OP
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.context.IInputContextListener#monitoredFileRemoved(org.eclipse.core.resources.IFile)
	 */
    public boolean monitoredFileRemoved(IFile monitoredFile) {
        return true;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormEditor#getSelection()
	 */
    public ISelection getSelection() {
        // Override the parent getSelection because it doesn't work.
        // The selection provider operates at the form level and does not
        // track selections made in the master tree view.
        // The selection is required to synchronize the master tree view with
        // the outline view
        IFormPage formPage = getActivePageInstance();
        if ((formPage != null) && (formPage instanceof SimpleCSDefinitionPage)) {
            // is toggled on
            return ((SimpleCSDefinitionPage) formPage).getSelection();
        }
        return super.getSelection();
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormEditor#computeInitialPageId()
	 */
    protected String computeInitialPageId() {
        String firstPageId = super.computeInitialPageId();
        if (firstPageId == null) {
            firstPageId = SimpleCSDefinitionPage.PAGE_ID;
        }
        return firstPageId;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.MultiSourceEditor#createSourcePage(org.eclipse.pde.internal.ui.editor.PDEFormEditor, java.lang.String, java.lang.String, java.lang.String)
	 */
    protected PDESourcePage createSourcePage(PDEFormEditor editor, String title, String name, String contextId) {
        return new SimpleCSSourcePage(editor, title, name);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormEditor#canCut(org.eclipse.jface.viewers.ISelection)
	 */
    public boolean canCut(ISelection selection) {
        IFormPage page = getActivePageInstance();
        if (page instanceof PDEFormPage) {
            return ((PDEFormPage) page).canCut(selection);
        }
        return super.canCut(selection);
    }
}
