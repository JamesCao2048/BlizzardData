/*******************************************************************************
 *  Copyright (c) 2003, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.context;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.*;
import org.eclipse.pde.internal.core.text.IEditingModel;
import org.eclipse.pde.internal.core.util.PropertiesUtil;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.PDEStorageDocumentProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;

/**
 * This class maintains objects associated with a single editor input.
 */
public abstract class InputContext {

    private PDEFormEditor fEditor;

    private IEditorInput fEditorInput;

    private IBaseModel fModel;

    private IModelChangedListener fModelListener;

    private IDocumentProvider fDocumentProvider;

    private IElementStateListener fElementListener;

    protected ArrayList<TextEdit> fEditOperations = new ArrayList();

    private boolean fValidated;

    private boolean fPrimary;

    private boolean fIsSourceMode;

    private boolean fMustSynchronize;

    class ElementListener implements IElementStateListener {

        @Override
        public void elementContentAboutToBeReplaced(Object element) {
        }

        @Override
        public void elementContentReplaced(Object element) {
            if (element != null && element.equals(fEditorInput))
                doRevert();
        }

        @Override
        public void elementDeleted(Object element) {
            if (element != null && element.equals(fEditorInput))
                dispose();
        }

        @Override
        public void elementDirtyStateChanged(Object element, boolean isDirty) {
            if (element != null && element.equals(fEditorInput))
                fMustSynchronize = true;
        }

        @Override
        public void elementMoved(Object originalElement, Object movedElement) {
            if (originalElement != null && originalElement.equals(fEditorInput)) {
                dispose();
                fEditor.close(true);
            }
        }
    }

    public  InputContext(PDEFormEditor editor, IEditorInput input, boolean primary) {
        this.fEditor = editor;
        this.fEditorInput = input;
        setPrimary(primary);
    }

    public abstract String getId();

    public IEditorInput getInput() {
        return fEditorInput;
    }

    public PDEFormEditor getEditor() {
        return fEditor;
    }

    public IBaseModel getModel() {
        return fModel;
    }

    public IDocumentProvider getDocumentProvider() {
        return fDocumentProvider;
    }

    private IDocumentProvider createDocumentProvider(IEditorInput input) {
        if (input instanceof IFileEditorInput) {
            return new ForwardingDocumentProvider(getPartitionName(), getDocumentSetupParticipant(), PDEPlugin.getDefault().getTextFileDocumentProvider());
        }
        return new PDEStorageDocumentProvider(getDocumentSetupParticipant());
    }

    protected IDocumentSetupParticipant getDocumentSetupParticipant() {
        return new IDocumentSetupParticipant() {

            @Override
            public void setup(IDocument document) {
            }
        };
    }

    protected abstract String getPartitionName();

    protected abstract String getDefaultCharset();

    protected abstract IBaseModel createModel(IEditorInput input) throws CoreException;

    protected void create() {
        fDocumentProvider = createDocumentProvider(fEditorInput);
        try {
            fDocumentProvider.connect(fEditorInput);
            fModel = createModel(fEditorInput);
            if (fModel instanceof IModelChangeProvider) {
                fModelListener = new IModelChangedListener() {

                    @Override
                    public void modelChanged(IModelChangedEvent e) {
                        if (e.getChangeType() != IModelChangedEvent.WORLD_CHANGED) {
                            if (!fEditor.getLastDirtyState())
                                fEditor.fireSaveNeeded(fEditorInput, true);
                            IModelChangeProvider provider = e.getChangeProvider();
                            if (provider instanceof IEditingModel) {
                                // and a text edit operation is falsely requested
                                if (((IEditingModel) provider).isDirty())
                                    addTextEditOperation(fEditOperations, e);
                            }
                        }
                    }
                };
                ((IModelChangeProvider) fModel).addModelChangedListener(fModelListener);
            }
            IAnnotationModel amodel = fDocumentProvider.getAnnotationModel(fEditorInput);
            if (amodel != null)
                amodel.connect(fDocumentProvider.getDocument(fEditorInput));
            fElementListener = new ElementListener();
            fDocumentProvider.addElementStateListener(fElementListener);
        } catch (CoreException e) {
            PDEPlugin.logException(e);
        }
    }

    public synchronized boolean validateEdit() {
        if (!fValidated) {
            if (fEditorInput instanceof IFileEditorInput) {
                IFile file = ((IFileEditorInput) fEditorInput).getFile();
                if (file.isReadOnly()) {
                    Shell shell = fEditor.getEditorSite().getShell();
                    IStatus validateStatus = PDEPlugin.getWorkspace().validateEdit(new IFile[] { file }, shell);
                    // to prevent loops
                    fValidated = true;
                    if (validateStatus.getSeverity() != IStatus.OK)
                        ErrorDialog.openError(shell, fEditor.getTitle(), null, validateStatus);
                    return validateStatus.getSeverity() == IStatus.OK;
                }
            }
        }
        return true;
    }

    public void doSave(IProgressMonitor monitor) {
        try {
            IDocument doc = fDocumentProvider.getDocument(fEditorInput);
            fDocumentProvider.aboutToChange(fEditorInput);
            flushModel(doc);
            fDocumentProvider.saveDocument(monitor, fEditorInput, doc, true);
            fDocumentProvider.changed(fEditorInput);
            fValidated = false;
        } catch (CoreException e) {
            PDEPlugin.logException(e);
        }
    }

    protected abstract void addTextEditOperation(ArrayList<TextEdit> ops, IModelChangedEvent event);

    public void flushEditorInput() {
        if (fEditOperations.size() > 0) {
            IDocument doc = fDocumentProvider.getDocument(fEditorInput);
            fDocumentProvider.aboutToChange(fEditorInput);
            flushModel(doc);
            fDocumentProvider.changed(fEditorInput);
            fValidated = false;
        } else if ((fModel instanceof IEditable) && ((IEditable) fModel).isDirty()) {
            // When text edit operations are made that cancel each other out,
            // the editor is not undirtied
            // e.g. Extensions page:  Move an element up and then move it down
            // back in the same position:  Bug # 197831
            ((IEditable) fModel).setDirty(false);
        }
    }

    protected void flushModel(IDocument doc) {
        boolean flushed = true;
        if (fEditOperations.size() > 0) {
            try {
                MultiTextEdit edit = new MultiTextEdit();
                if (isNewlineNeeded(doc))
                    insert(edit, new InsertEdit(doc.getLength(), TextUtilities.getDefaultLineDelimiter(doc)));
                for (int i = 0; i < fEditOperations.size(); i++) {
                    insert(edit, fEditOperations.get(i));
                }
                if (fModel instanceof IEditingModel)
                    ((IEditingModel) fModel).setStale(true);
                edit.apply(doc);
                fEditOperations.clear();
            } catch (MalformedTreeException e) {
                PDEPlugin.logException(e);
                flushed = false;
            } catch (BadLocationException e) {
                PDEPlugin.logException(e);
                flushed = false;
            }
        }
        // updated and the editor needs to be undirtied
        if (flushed && (fModel instanceof IEditable)) {
            ((IEditable) fModel).setDirty(false);
        }
    }

    protected boolean isNewlineNeeded(IDocument doc) throws BadLocationException {
        return PropertiesUtil.isNewlineNeeded(doc);
    }

    protected static void insert(TextEdit parent, TextEdit edit) {
        if (!parent.hasChildren()) {
            parent.addChild(edit);
            if (edit instanceof MoveSourceEdit) {
                parent.addChild(((MoveSourceEdit) edit).getTargetEdit());
            }
            return;
        }
        TextEdit[] children = parent.getChildren();
        // First dive down to find the right parent.
        for (int i = 0; i < children.length; i++) {
            TextEdit child = children[i];
            if (covers(child, edit)) {
                insert(child, edit);
                return;
            }
        }
        // be moved under the new edit since it is covering it.
        for (int i = children.length - 1; i >= 0; i--) {
            TextEdit child = children[i];
            if (covers(edit, child)) {
                parent.removeChild(i);
                edit.addChild(child);
            }
        }
        parent.addChild(edit);
        if (edit instanceof MoveSourceEdit) {
            parent.addChild(((MoveSourceEdit) edit).getTargetEdit());
        }
    }

    protected static boolean covers(TextEdit thisEdit, TextEdit otherEdit) {
        if (// an insertion point can't cover anything
        thisEdit.getLength() == 0)
            return false;
        int thisOffset = thisEdit.getOffset();
        int thisEnd = thisEdit.getExclusiveEnd();
        if (otherEdit.getLength() == 0) {
            int otherOffset = otherEdit.getOffset();
            return thisOffset < otherOffset && otherOffset < thisEnd;
        }
        int otherOffset = otherEdit.getOffset();
        int otherEnd = otherEdit.getExclusiveEnd();
        return thisOffset <= otherOffset && otherEnd <= thisEnd;
    }

    public boolean mustSave() {
        if (!fIsSourceMode) {
            if (fModel instanceof IEditable) {
                if (((IEditable) fModel).isDirty()) {
                    return true;
                }
            }
        }
        return fEditOperations.size() > 0 || fDocumentProvider.canSaveDocument(fEditorInput);
    }

    public void dispose() {
        IAnnotationModel amodel = fDocumentProvider.getAnnotationModel(fEditorInput);
        if (amodel != null)
            amodel.disconnect(fDocumentProvider.getDocument(fEditorInput));
        fDocumentProvider.removeElementStateListener(fElementListener);
        fDocumentProvider.disconnect(fEditorInput);
        if (fModelListener != null && fModel instanceof IModelChangeProvider) {
            ((IModelChangeProvider) fModel).removeModelChangedListener(fModelListener);
        //if (undoManager != null)
        //undoManager.disconnect((IModelChangeProvider) model);
        }
        if (fModel != null)
            fModel.dispose();
    }

    /**
	 * @return Returns the primary.
	 */
    public boolean isPrimary() {
        return fPrimary;
    }

    /**
	 * @param primary The primary to set.
	 */
    public void setPrimary(boolean primary) {
        this.fPrimary = primary;
    }

    public boolean setSourceEditingMode(boolean sourceMode) {
        fIsSourceMode = sourceMode;
        if (sourceMode) {
            // entered source editing mode; in this mode,
            // this context's document will be edited directly
            // in the source editor. All changes in the model
            // are caused by reconciliation and should not be
            // fired to the world.
            flushModel(fDocumentProvider.getDocument(fEditorInput));
            fMustSynchronize = true;
            return true;
        }
        // to cause all the model listeners to become stale.
        return synchronizeModelIfNeeded();
    }

    private boolean synchronizeModelIfNeeded() {
        if (fMustSynchronize) {
            boolean result = synchronizeModel(fDocumentProvider.getDocument(fEditorInput));
            fMustSynchronize = false;
            return result;
        }
        return true;
    }

    public void doRevert() {
        fMustSynchronize = true;
        synchronizeModelIfNeeded();
    /*
		if (model instanceof IEditable) {
			((IEditable)model).setDirty(false);
		}
		*/
    }

    public boolean isInSourceMode() {
        return fIsSourceMode;
    }

    public boolean isModelCorrect() {
        synchronizeModelIfNeeded();
        return fModel != null ? fModel.isValid() : false;
    }

    protected boolean synchronizeModel(IDocument doc) {
        return true;
    }

    public boolean matches(IResource resource) {
        if (fEditorInput instanceof IFileEditorInput) {
            IFileEditorInput finput = (IFileEditorInput) fEditorInput;
            IFile file = finput.getFile();
            if (file.equals(resource))
                return true;
        }
        return false;
    }

    /**
	 * @return Returns the validated.
	 */
    public boolean isValidated() {
        return fValidated;
    }

    /**
	 * @param validated The validated to set.
	 */
    public void setValidated(boolean validated) {
        this.fValidated = validated;
    }

    public String getLineDelimiter() {
        if (fDocumentProvider != null) {
            IDocument document = fDocumentProvider.getDocument(fEditorInput);
            if (document != null) {
                return TextUtilities.getDefaultLineDelimiter(document);
            }
        }
        //$NON-NLS-1$
        return System.getProperty("line.separator");
    }

    /**
	 * @param input
	 * @throws CoreException
	 */
    private void updateInput(IEditorInput newInput) throws CoreException {
        deinitializeDocumentProvider();
        fEditorInput = newInput;
        initializeDocumentProvider();
    }

    /**
	 *
	 */
    private void deinitializeDocumentProvider() {
        IAnnotationModel amodel = fDocumentProvider.getAnnotationModel(fEditorInput);
        if (amodel != null) {
            amodel.disconnect(fDocumentProvider.getDocument(fEditorInput));
        }
        fDocumentProvider.removeElementStateListener(fElementListener);
        fDocumentProvider.disconnect(fEditorInput);
    }

    /**
	 * @throws CoreException
	 */
    private void initializeDocumentProvider() throws CoreException {
        fDocumentProvider.connect(fEditorInput);
        IAnnotationModel amodel = fDocumentProvider.getAnnotationModel(fEditorInput);
        if (amodel != null) {
            amodel.connect(fDocumentProvider.getDocument(fEditorInput));
        }
        fDocumentProvider.addElementStateListener(fElementListener);
    }

    /**
	 * @param monitor
	 */
    public void doSaveAs(IProgressMonitor monitor) throws Exception {
        // Get the editor shell
        Shell shell = getEditor().getSite().getShell();
        // Create the save as dialog
        SaveAsDialog dialog = new SaveAsDialog(shell);
        // Set the initial file name to the original file name
        IFile file = null;
        if (fEditorInput instanceof IFileEditorInput) {
            file = ((IFileEditorInput) fEditorInput).getFile();
            dialog.setOriginalFile(file);
        }
        // Create the dialog
        dialog.create();
        // Warn the user if the underlying file does not exist
        if (fDocumentProvider.isDeleted(fEditorInput) && (file != null)) {
            String message = NLS.bind(PDEUIMessages.InputContext_errorMessageFileDoesNotExist, file.getName());
            dialog.setErrorMessage(null);
            dialog.setMessage(message, IMessageProvider.WARNING);
        }
        // Open the dialog
        if (dialog.open() == Window.OK) {
            // Get the path to where the new file will be stored
            IPath path = dialog.getResult();
            handleSaveAs(monitor, path);
        }
    }

    /**
	 * @param monitor
	 * @param path
	 * @throws Exception
	 * @throws CoreException
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 */
    private void handleSaveAs(IProgressMonitor monitor, IPath path) throws Exception, CoreException, InterruptedException, InvocationTargetException {
        // Ensure a new location was selected
        if (path == null) {
            monitor.setCanceled(true);
            throw new Exception(PDEUIMessages.InputContext_errorMessageLocationNotSet);
        }
        // Resolve the new file location
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IFile newFile = workspace.getRoot().getFile(path);
        // Create the new editor input
        final IEditorInput newInput = new FileEditorInput(newFile);
        // Send notice of editor input changes
        fDocumentProvider.aboutToChange(newInput);
        // Flush any unsaved changes
        flushModel(fDocumentProvider.getDocument(fEditorInput));
        try {
            // Execute the workspace modification in a separate thread
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(createWorkspaceModifyOperation(newInput));
            monitor.setCanceled(false);
            // Store the new editor input in this context
            updateInput(newInput);
        } catch (InterruptedException e) {
            monitor.setCanceled(true);
            throw e;
        } catch (InvocationTargetException e) {
            monitor.setCanceled(true);
            throw e;
        } finally {
            fDocumentProvider.changed(newInput);
        }
    }

    private WorkspaceModifyOperation createWorkspaceModifyOperation(final IEditorInput newInput) {
        WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

            @Override
            public void execute(final IProgressMonitor monitor) throws CoreException {
                // Save the old editor input content to the new editor input
                // location
                fDocumentProvider.saveDocument(monitor, // New editor input location
                newInput, // Old editor input content
                fDocumentProvider.getDocument(fEditorInput), true);
            }
        };
        return operation;
    }
}
