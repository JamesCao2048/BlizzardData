/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.targetdefinition;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.target.*;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.target.WorkspaceFileTargetHandle;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.shared.target.*;
import org.eclipse.pde.internal.ui.wizards.exports.TargetDefinitionExportWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.UIJob;

/**
 * Editor for target definition (*.target) files.  Interacts with the ITargetDefinition model
 * to modify target attributes.  Uses the target platform service to persist the modified model
 * to the backing file.
 *
 * @see ITargetDefinition
 * @see ITargetPlatformService
 */
public class TargetEditor extends FormEditor {

    private List<IManagedForm> fManagedFormPages = new ArrayList(2);

    private InputHandler fInputHandler = new InputHandler();

    private TargetChangedListener fTargetChangedListener;

    private boolean fDirty;

    @Override
    protected FormToolkit createToolkit(Display display) {
        return new FormToolkit(PDEPlugin.getDefault().getFormColors(display));
    }

    @Override
    protected void addPages() {
        try {
            setActiveEditor(this);
            addPage(new DefinitionPage(this));
            addPage(new ContentPage(this));
            addPage(new EnvironmentPage(this));
        } catch (PartInitException e) {
            PDEPlugin.log(e);
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        commitPages(true);
        try {
            fInputHandler.saveTargetDefinition();
        } catch (CoreException e) {
            PDEPlugin.log(e);
            showError(PDEUIMessages.TargetEditor_3, e);
        }
    }

    @Override
    public void doSaveAs() {
        commitPages(true);
        ITargetDefinition target = getTarget();
        SaveAsDialog dialog = new SaveAsDialog(getSite().getShell());
        dialog.create();
        dialog.setMessage(PDEUIMessages.TargetEditor_0, IMessageProvider.NONE);
        if (target.getHandle() instanceof WorkspaceFileTargetHandle) {
            WorkspaceFileTargetHandle currentTargetHandle = (WorkspaceFileTargetHandle) target.getHandle();
            dialog.setOriginalFile(currentTargetHandle.getTargetFile());
        }
        dialog.open();
        IPath path = dialog.getResult();
        if (path == null) {
            return;
        }
        if (//$NON-NLS-1$
        !"target".equalsIgnoreCase(path.getFileExtension())) {
            //$NON-NLS-1$
            path = path.addFileExtension("target");
        }
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IFile file = workspace.getRoot().getFile(path);
        if (workspace.validateEdit(new IFile[] { file }, getSite().getShell()).isOK()) {
            try {
                WorkspaceFileTargetHandle newFileTarget = new WorkspaceFileTargetHandle(file);
                newFileTarget.save(target);
                setInput(new FileEditorInput(file));
            } catch (CoreException e) {
                PDEPlugin.log(e);
                showError(PDEUIMessages.TargetEditor_3, e);
            }
        }
    }

    @Override
    public boolean isSaveAsAllowed() {
        return true;
    }

    protected void setDirty(boolean dirty) {
        fDirty = fDirty || dirty;
        editorDirtyStateChanged();
    }

    @Override
    public boolean isDirty() {
        return fDirty || super.isDirty();
    }

    /*
	 * @see org.eclipse.ui.forms.editor.FormEditor#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 * @since 3.7
	 */
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        if (!(input instanceof IFileEditorInput) && !(input instanceof IURIEditorInput))
            throw new PartInitException(NLS.bind(PDEUIMessages.TargetEditor_6, input.getClass().getName()));
        super.init(site, input);
    }

    @Override
    protected void setInput(IEditorInput input) {
        super.setInput(input);
        setPartName(getEditorInput().getName());
        fInputHandler.setInput(input);
    }

    @Override
    public void dispose() {
        // Cancel any resolution jobs that are runnning
        Job.getJobManager().cancel(getTargetChangedListener().getJobFamily());
        getTargetChangedListener().setContentTree(null);
        getTargetChangedListener().setLocationTree(null);
        fInputHandler.dispose();
        super.dispose();
    }

    /**
	 * Returns the target model backing this editor
	 * @return target model
	 */
    public ITargetDefinition getTarget() {
        return fInputHandler.getTarget();
    }

    /**
	 * @return a shared listener that will refresh UI components when the target is modified
	 */
    public TargetChangedListener getTargetChangedListener() {
        if (fTargetChangedListener == null) {
            fTargetChangedListener = new TargetChangedListener();
        }
        return fTargetChangedListener;
    }

    /**
	 * Handles the revert action
	 */
    public void doRevert() {
        fInputHandler.reset();
        for (Iterator<IManagedForm> iterator = fManagedFormPages.iterator(); iterator.hasNext(); ) {
            IFormPart[] parts = iterator.next().getParts();
            for (int i = 0; i < parts.length; i++) {
                if (parts[i] instanceof AbstractFormPart) {
                    ((AbstractFormPart) parts[i]).markStale();
                }
            }
        }
        setActivePage(getActivePage());
        editorDirtyStateChanged();
    }

    public void contributeToToolbar(final ScrolledForm form, String contextID) {
        ControlContribution setAsTarget = new //$NON-NLS-1$
        ControlContribution(//$NON-NLS-1$
        "Set") {

            @Override
            protected Control createControl(Composite parent) {
                final ImageHyperlink hyperlink = new ImageHyperlink(parent, SWT.NONE | SWT.NO_FOCUS);
                hyperlink.setText(PDEUIMessages.AbstractTargetPage_setTarget);
                hyperlink.setUnderlined(true);
                hyperlink.setForeground(getToolkit().getHyperlinkGroup().getForeground());
                hyperlink.addHyperlinkListener(new IHyperlinkListener() {

                    @Override
                    public void linkActivated(HyperlinkEvent e) {
                        LoadTargetDefinitionJob.load(getTarget());
                    }

                    @Override
                    public void linkEntered(HyperlinkEvent e) {
                        HyperlinkGroup hyperlinkGroup = getHyperlinkGroup();
                        if (hyperlinkGroup != null) {
                            hyperlink.setForeground(hyperlinkGroup.getActiveForeground());
                        }
                    }

                    @Override
                    public void linkExited(HyperlinkEvent e) {
                        HyperlinkGroup hyperlinkGroup = getHyperlinkGroup();
                        if (hyperlinkGroup != null) {
                            hyperlink.setForeground(hyperlinkGroup.getForeground());
                        }
                    }

                    private HyperlinkGroup getHyperlinkGroup() {
                        FormToolkit toolkit = getToolkit();
                        HyperlinkGroup hyperlinkGroup = null;
                        if (toolkit != null) {
                            hyperlinkGroup = toolkit.getHyperlinkGroup();
                        }
                        return hyperlinkGroup;
                    }
                });
                return hyperlink;
            }
        };
        final String helpContextID = contextID;
        Action help = new //$NON-NLS-1$
        Action(//$NON-NLS-1$
        "help") {

            @Override
            public void run() {
                BusyIndicator.showWhile(form.getForm().getDisplay(), new Runnable() {

                    @Override
                    public void run() {
                        PlatformUI.getWorkbench().getHelpSystem().displayHelp(helpContextID);
                    }
                });
            }
        };
        help.setToolTipText(PDEUIMessages.PDEFormPage_help);
        help.setImageDescriptor(PDEPluginImages.DESC_HELP);
        Action export = new //$NON-NLS-1$
        Action(//$NON-NLS-1$
        "export") {

            @Override
            public void run() {
                TargetDefinitionExportWizard wizard = new TargetDefinitionExportWizard(getTarget());
                wizard.setWindowTitle(PDEUIMessages.ExportActiveTargetDefinition);
                WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
                dialog.open();
            }
        };
        //$NON-NLS-1$
        export.setToolTipText("Export");
        export.setImageDescriptor(PDEPluginImages.DESC_EXPORT_TARGET_TOOL);
        form.getToolBarManager().add(setAsTarget);
        form.getToolBarManager().add(export);
        form.getToolBarManager().add(help);
        form.updateToolBar();
    }

    /**
	 * Adds the given form to the list of forms to be refreshed when reverting
	 * @param managedForm
	 */
    public void addForm(IManagedForm managedForm) {
        fManagedFormPages.add(managedForm);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(managedForm.getForm().getBody(), IHelpContextIds.TARGET_EDITOR);
    }

    /**
	 * Opens an error dialog using the editor's shell.
	 * @param message error message to display
	 * @param exception exception to display in details section
	 */
    public void showError(final String message, final CoreException exception) {
        Display display = getSite().getShell().getDisplay();
        display.asyncExec(new Runnable() {

            @Override
            public void run() {
                ErrorDialog.openError(getSite().getShell(), PDEUIMessages.TargetEditor_4, message, exception.getStatus());
            }
        });
    }

    /**
	 * The InputHandler serves as a bridge between the input file and the TargetDefinition model.
	 */
    private class InputHandler implements IResourceChangeListener {

        private IEditorInput fInput;

        private ITargetDefinition fTarget;

        private IFile fTargetFileInWorkspace;

        private boolean fSaving = false;

        public void dispose() {
            PDEPlugin.getWorkspace().removeResourceChangeListener(this);
        }

        public void reset() {
            setInput(fInput);
        }

        public void setInput(IEditorInput input) {
            fInput = input;
            fTargetFileInWorkspace = null;
            fTarget = null;
            File targetFile = null;
            if (input instanceof IFileEditorInput) {
                fTargetFileInWorkspace = ((IFileEditorInput) input).getFile();
                targetFile = fTargetFileInWorkspace.getLocation().toFile();
            } else if (input instanceof IURIEditorInput) {
                String part = ((IURIEditorInput) input).getURI().getSchemeSpecificPart();
                Path path = new Path(part);
                fTargetFileInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
                targetFile = path.toFile();
            }
            //If the file is not available or invalid, close the target editor
            if (fTargetFileInWorkspace == null || targetFile == null || !targetFile.exists()) {
                TargetEditor.this.close(false);
            }
            PDEPlugin.getWorkspace().addResourceChangeListener(this);
        }

        /**
		 * @return the target definition that is the input to the editor
		 */
        public ITargetDefinition getTarget() {
            if (fTarget == null) {
                try {
                    loadTargetDefinition();
                    TargetEditor.this.fDirty = false;
                    TargetEditor.this.editorDirtyStateChanged();
                } catch (CoreException e) {
                    TargetEditor.this.showError(PDEUIMessages.TargetEditor_5, e);
                    TargetEditor.this.close(false);
                }
            }
            return fTarget;
        }

        private ITargetDefinition loadTargetDefinition() throws CoreException {
            ITargetPlatformService service = getTargetPlatformService();
            try {
                if (fInput instanceof IFileEditorInput) {
                    ITargetHandle fileHandle = service.getTarget(((IFileEditorInput) fInput).getFile());
                    fTarget = fileHandle.getTargetDefinition();
                } else if (fInput instanceof IURIEditorInput) {
                    ITargetHandle externalTarget = service.getTarget(((IURIEditorInput) fInput).getURI());
                    fTarget = externalTarget.getTargetDefinition();
                }
            } catch (CoreException e) {
                fTarget = service.newTarget();
                throw e;
            }
            TargetEditor.this.getTargetChangedListener().contentsChanged(fTarget, this, true, false);
            return fTarget;
        }

        public void saveTargetDefinition() throws CoreException {
            ITargetPlatformService service = getTargetPlatformService();
            fSaving = true;
            try {
                service.saveTargetDefinition(fTarget);
                TargetEditor.this.fDirty = false;
                TargetEditor.this.editorDirtyStateChanged();
            } finally {
                fSaving = false;
            }
        }

        private ITargetPlatformService getTargetPlatformService() throws CoreException {
            ITargetPlatformService service = (ITargetPlatformService) PDECore.getDefault().acquireService(ITargetPlatformService.class.getName());
            if (service == null) {
                throw new //$NON-NLS-1$
                CoreException(//$NON-NLS-1$
                new Status(IStatus.ERROR, PDECore.PLUGIN_ID, "ITargetPlatformService not available"));
            }
            return service;
        }

        @Override
        public void resourceChanged(IResourceChangeEvent event) {
            if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
                IResourceDelta delta = event.getDelta().findMember(fTargetFileInWorkspace.getFullPath());
                if (delta != null) {
                    if (delta.getKind() == IResourceDelta.REMOVED) {
                        TargetEditor.this.close(false);
                    } else if (delta.getKind() == IResourceDelta.CHANGED || delta.getKind() == IResourceDelta.REPLACED) {
                        if (!fSaving) {
                            Display display = getSite().getShell().getDisplay();
                            display.asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    TargetEditor.this.doRevert();
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    /**
	 * When changes are noticed in the target, this listener will resolve the
	 * target and update the necessary pages in the editor.
	 */
    class TargetChangedListener implements ITargetChangedListener {

        private TargetLocationsGroup fLocationTree;

        private TargetContentsGroup fContentTree;

        private Object fJobFamily = new Object();

        public void setLocationTree(TargetLocationsGroup locationTree) {
            fLocationTree = locationTree;
        }

        public void setContentTree(TargetContentsGroup contentTree) {
            fContentTree = contentTree;
        }

        /**
		 * @return non-null object identifier for any jobs created by this listener
		 */
        public Object getJobFamily() {
            return fJobFamily;
        }

        @Override
        public void contentsChanged(ITargetDefinition definition, Object source, boolean resolve, boolean forceResolve) {
            if (!forceResolve && (!resolve || definition.isResolved())) {
                if (fContentTree != null && source != fContentTree) {
                    ITargetDefinition target = getTarget();
                    // Check to see if we are resolved, resolving, or cancelled
                    if (target != null && target.isResolved()) {
                        fContentTree.setInput(getTarget());
                    } else if (Job.getJobManager().find(getJobFamily()).length > 0) {
                        fContentTree.setInput(null);
                    } else {
                        fContentTree.setCancelled();
                    }
                }
                if (fLocationTree != null && source != fLocationTree) {
                    fLocationTree.setInput(getTarget());
                }
            } else {
                if (fContentTree != null) {
                    fContentTree.setInput(null);
                }
                if (fLocationTree != null) {
                    fLocationTree.setInput(getTarget());
                }
                Job.getJobManager().cancel(getJobFamily());
                Job resolveJob = new Job(PDEUIMessages.TargetEditor_1) {

                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        getTarget().resolve(monitor);
                        if (monitor.isCanceled()) {
                            return Status.CANCEL_STATUS;
                        }
                        // Don't return any problems because we don't want an error dialog
                        return Status.OK_STATUS;
                    }

                    @Override
                    public boolean belongsTo(Object family) {
                        return family.equals(getJobFamily());
                    }
                };
                resolveJob.addJobChangeListener(new JobChangeAdapter() {

                    @Override
                    public void done(org.eclipse.core.runtime.jobs.IJobChangeEvent event) {
                        final IStatus status = event.getResult();
                        UIJob job = new UIJob(PDEUIMessages.TargetEditor_2) {

                            @Override
                            public IStatus runInUIThread(IProgressMonitor monitor) {
                                if (fContentTree != null) {
                                    if (status.getSeverity() == IStatus.CANCEL) {
                                        fContentTree.setCancelled();
                                    } else {
                                        fContentTree.setInput(getTarget());
                                    }
                                }
                                if (fLocationTree != null) {
                                    fLocationTree.setInput(getTarget());
                                }
                                return Status.OK_STATUS;
                            }
                        };
                        job.setSystem(true);
                        job.schedule();
                    }
                });
                resolveJob.schedule();
            }
        }
    }
}
