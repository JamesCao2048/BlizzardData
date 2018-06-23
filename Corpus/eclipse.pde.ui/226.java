/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
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
import org.eclipse.core.commands.*;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.service.pluginconversion.PluginConverter;
import org.eclipse.pde.core.build.*;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.build.WorkspaceBuildModel;
import org.eclipse.pde.internal.core.project.PDEProject;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.editor.*;
import org.eclipse.pde.internal.ui.editor.build.BuildInputContext;
import org.eclipse.pde.internal.ui.editor.build.BuildPage;
import org.eclipse.pde.internal.ui.editor.context.InputContext;
import org.eclipse.pde.internal.ui.nls.GetNonExternalizedStringsAction;
import org.eclipse.pde.internal.ui.util.SharedLabelProvider;
import org.eclipse.pde.internal.ui.wizards.tools.OrganizeManifestsAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.progress.UIJob;
import org.osgi.service.prefs.BackingStoreException;

public class OverviewPage extends LaunchShortcutOverviewPage {

    //$NON-NLS-1$
    public static final String P2_INSTALL_COMMAND_HANDLER = "org.eclipse.equinox.p2.ui.sdk.install";

    //$NON-NLS-1$
    public static final String PAGE_ID = "overview";

    private PluginExportAction fExportAction;

    private GeneralInfoSection fInfoSection;

    private boolean fDisposed = false;

    private ILauncherFormPageHelper fLauncherHelper;

    public  OverviewPage(PDELauncherFormEditor editor) {
        super(editor, PAGE_ID, PDEUIMessages.OverviewPage_tabName);
    }

    @Override
    protected String getHelpResource() {
        return IHelpContextIds.MANIFEST_PLUGIN_OVERVIEW;
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        ScrolledForm form = managedForm.getForm();
        FormToolkit toolkit = managedForm.getToolkit();
        if (isFragment()) {
            form.setImage(PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_FRAGMENT_MF_OBJ));
        } else {
            form.setImage(PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_PLUGIN_MF_OBJ));
        }
        form.setText(PDEUIMessages.ManifestEditor_OverviewPage_title);
        fillBody(managedForm, toolkit);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(), IHelpContextIds.MANIFEST_PLUGIN_OVERVIEW);
        // Add warning about missing manifest (Bug 407755)
        if (!isBundle() && isEditable()) {
            // We have to use a job so that the form header has been created
            UIJob messageJob = new UIJob(PDEUIMessages.OverviewPage_ManifestWarning) {

                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    IManagedForm form = getManagedForm();
                    if (form != null) {
                        form.getMessageManager().addMessage(PDEUIMessages.OverviewPage_ManifestWarning, isFragment() ? PDEUIMessages.OverviewPage_WarnAboutMissingManifestFragment : PDEUIMessages.OverviewPage_WarnAboutMissingManifest, null, IMessageProvider.WARNING);
                    }
                    return Status.OK_STATUS;
                }
            };
            messageJob.setSystem(true);
            messageJob.schedule();
        }
    }

    private void fillBody(IManagedForm managedForm, FormToolkit toolkit) {
        Composite body = managedForm.getForm().getBody();
        body.setLayout(FormLayoutFactory.createFormTableWrapLayout(true, 2));
        Composite left = toolkit.createComposite(body);
        left.setLayout(FormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
        left.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        if (isFragment())
            fInfoSection = new FragmentGeneralInfoSection(this, left);
        else
            fInfoSection = new PluginGeneralInfoSection(this, left);
        managedForm.addPart(fInfoSection);
        if (isBundle())
            managedForm.addPart(new ExecutionEnvironmentSection(this, left));
        Composite right = toolkit.createComposite(body);
        right.setLayout(FormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
        right.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        createContentSection(managedForm, right, toolkit);
        if (isEditable() || getPDEEditor().hasInputContext(PluginInputContext.CONTEXT_ID))
            createExtensionSection(managedForm, right, toolkit);
        if (isEditable()) {
            createTestingSection(managedForm, isBundle() ? right : left, toolkit);
        }
        if (isEditable())
            createExportingSection(managedForm, right, toolkit);
    }

    private void createContentSection(IManagedForm managedForm, Composite parent, FormToolkit toolkit) {
        String sectionTitle;
        if (isFragment()) {
            sectionTitle = PDEUIMessages.ManifestEditor_ContentSection_ftitle;
        } else {
            sectionTitle = PDEUIMessages.ManifestEditor_ContentSection_title;
        }
        Section section = createStaticSection(toolkit, parent, sectionTitle);
        Composite container = createStaticSectionClient(toolkit, section);
        PDELabelProvider lp = PDEPlugin.getDefault().getLabelProvider();
        if (!isBundle() && isEditable()) {
            String content;
            PluginConverter converter = (PluginConverter) PDECore.getDefault().acquireService(PluginConverter.class.getName());
            if (converter == null) {
                // Help the user install the plugin converter service
                if (isFragment()) {
                    content = PDEUIMessages.OverviewPage_NoPluginConverterFragment;
                } else {
                    content = PDEUIMessages.OverviewPage_NoPluginConverterPlugin;
                }
            } else {
                // Provide link to run the conversion service
                if (isFragment()) {
                    content = PDEUIMessages.OverviewPage_fOsgi;
                } else {
                    content = PDEUIMessages.OverviewPage_osgi;
                }
            }
            FormText warningText = createClient(container, content, toolkit);
            //$NON-NLS-1$
            warningText.setImage("warning", lp.get(PDEPluginImages.DESC_WARNING_ST_OBJ, 0));
            //$NON-NLS-1$
            warningText.setImage("error", lp.get(PDEPluginImages.DESC_ERROR_ST_OBJ, 0));
        }
        FormText text = createClient(container, isFragment() ? PDEUIMessages.OverviewPage_fContent : PDEUIMessages.OverviewPage_content, toolkit);
        //$NON-NLS-1$
        text.setImage("page", lp.get(PDEPluginImages.DESC_PAGE_OBJ, SharedLabelProvider.F_EDIT));
        section.setClient(container);
    }

    private void createExtensionSection(IManagedForm managedForm, Composite parent, FormToolkit toolkit) {
        String sectionTitle = PDEUIMessages.ManifestEditor_ExtensionSection_title;
        Section section = createStaticSection(toolkit, parent, sectionTitle);
        Composite container = createStaticSectionClient(toolkit, section);
        FormText text = createClient(container, isFragment() ? PDEUIMessages.OverviewPage_fExtensionContent : PDEUIMessages.OverviewPage_extensionContent, toolkit);
        PDELabelProvider lp = PDEPlugin.getDefault().getLabelProvider();
        //$NON-NLS-1$
        text.setImage("page", lp.get(PDEPluginImages.DESC_PAGE_OBJ, SharedLabelProvider.F_EDIT));
        section.setClient(container);
    }

    private void createTestingSection(IManagedForm managedForm, Composite parent, FormToolkit toolkit) {
        Section section = createStaticSection(toolkit, parent, PDEUIMessages.ManifestEditor_TestingSection_title);
        PDELabelProvider lp = PDEPlugin.getDefault().getLabelProvider();
        Composite container = createStaticSectionClient(toolkit, section);
        String prefixText = (!((ManifestEditor) getEditor()).showExtensionTabs()) ? PDEUIMessages.OverviewPage_OSGiTesting : isFragment() ? PDEUIMessages.OverviewPage_fTesting : PDEUIMessages.OverviewPage_testing;
        FormText text = createClient(container, getLauncherText(getLauncherHelper().isOSGi(), prefixText), toolkit);
        //$NON-NLS-1$
        text.setImage("run", lp.get(PDEPluginImages.DESC_RUN_EXC));
        //$NON-NLS-1$
        text.setImage("debug", lp.get(PDEPluginImages.DESC_DEBUG_EXC));
        //$NON-NLS-1$
        text.setImage("profile", lp.get(PDEPluginImages.DESC_PROFILE_EXC));
        section.setClient(container);
    }

    private void createExportingSection(IManagedForm managedForm, Composite parent, FormToolkit toolkit) {
        Section section = createStaticSection(toolkit, parent, PDEUIMessages.ManifestEditor_DeployingSection_title);
        Composite container = createStaticSectionClient(toolkit, section);
        createClient(container, isFragment() ? PDEUIMessages.OverviewPage_fDeploying : PDEUIMessages.OverviewPage_deploying, toolkit);
        section.setClient(container);
    }

    protected Composite createStaticSectionClient(FormToolkit toolkit, Composite parent) {
        Composite container = toolkit.createComposite(parent, SWT.NONE);
        container.setLayout(FormLayoutFactory.createSectionClientTableWrapLayout(false, 1));
        TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
        container.setLayoutData(data);
        return container;
    }

    private boolean isFragment() {
        IPluginModelBase model = (IPluginModelBase) getPDEEditor().getContextManager().getAggregateModel();
        return model.isFragmentModel();
    }

    private boolean isBundle() {
        return getPDEEditor().getContextManager().findContext(BundleInputContext.CONTEXT_ID) != null;
    }

    private boolean isEditable() {
        IPluginModelBase model = (IPluginModelBase) getPDEEditor().getContextManager().getAggregateModel();
        return model.isEditable();
    }

    @Override
    public void linkActivated(HyperlinkEvent e) {
        String href = (String) e.getHref();
        // try page references
        if (//$NON-NLS-1$
        href.equals("dependencies"))
            getEditor().setActivePage(DependenciesPage.PAGE_ID);
        else if (//$NON-NLS-1$
        href.equals("runtime"))
            getEditor().setActivePage(RuntimePage.PAGE_ID);
        else if (//$NON-NLS-1$
        href.equals("extensions")) {
            if (getEditor().setActivePage(ExtensionsPage.PAGE_ID) == null)
                activateExtensionPages(ExtensionsPage.PAGE_ID);
        } else if (//$NON-NLS-1$
        href.equals("ex-points")) {
            if (getEditor().setActivePage(ExtensionPointsPage.PAGE_ID) == null)
                activateExtensionPages(ExtensionPointsPage.PAGE_ID);
        } else if (//$NON-NLS-1$
        href.equals("build")) {
            if (!getPDEEditor().hasInputContext(BuildInputContext.CONTEXT_ID)) {
                if (!MessageDialog.openQuestion(PDEPlugin.getActiveWorkbenchShell(), PDEUIMessages.OverviewPage_buildTitle, PDEUIMessages.OverviewPage_buildQuestion))
                    return;
                IFile file = PDEProject.getBuildProperties(getPDEEditor().getCommonProject());
                WorkspaceBuildModel model = new WorkspaceBuildModel(file);
                model.save();
                IEditorInput in = new FileEditorInput(file);
                getPDEEditor().getContextManager().putContext(in, new BuildInputContext(getPDEEditor(), in, false));
            }
            getEditor().setActivePage(BuildPage.PAGE_ID);
        } else if (//$NON-NLS-1$
        href.equals("export")) {
            getExportAction().run();
        } else if (//$NON-NLS-1$
        href.equals("action.convert")) {
            handleConvert();
        } else if (//$NON-NLS-1$
        href.equals("action.installPluginConverter")) {
            handleInstallPluginConverter();
        } else if (//$NON-NLS-1$
        href.equals("organize")) {
            getEditor().doSave(null);
            OrganizeManifestsAction organizeAction = new OrganizeManifestsAction();
            organizeAction.runOrganizeManfestsAction(new StructuredSelection(getPDEEditor().getCommonProject()));
        } else if (//$NON-NLS-1$
        href.equals("externalize")) {
            getEditor().doSave(null);
            GetNonExternalizedStringsAction externalizeAction = new GetNonExternalizedStringsAction();
            externalizeAction.runGetNonExternalizedStringsAction(new StructuredSelection(getPDEEditor().getCommonProject()));
        } else
            super.linkActivated(e);
    }

    private PluginExportAction getExportAction() {
        if (fExportAction == null)
            fExportAction = new PluginExportAction((PDEFormEditor) getEditor());
        return fExportAction;
    }

    private void handleConvert() {
        try {
            // remove listeners of Info section before we convert.  If we don't
            // we may get a model changed event while disposing the page.  Bug 156414
            fInfoSection.removeListeners();
            PDEFormEditor editor = getPDEEditor();
            IPluginModelBase model = (IPluginModelBase) editor.getAggregateModel();
            IRunnableWithProgress op = new CreateManifestOperation(model);
            IProgressService service = PlatformUI.getWorkbench().getProgressService();
            editor.doSave(null);
            service.runInUI(service, op, PDEPlugin.getWorkspace().getRoot());
            updateBuildProperties();
            editor.doSave(null);
        } catch (InvocationTargetException e) {
            MessageDialog.openError(PDEPlugin.getActiveWorkbenchShell(), PDEUIMessages.OverviewPage_error, e.getCause().getMessage());
            if (!fDisposed)
                fInfoSection.addListeners();
        } catch (InterruptedException e) {
            if (!fDisposed)
                fInfoSection.addListeners();
        }
    }

    private void handleInstallPluginConverter() {
        ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        final Command command = commandService.getCommand(P2_INSTALL_COMMAND_HANDLER);
        if (command.isHandled()) {
            IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
            try {
                handlerService.executeCommand(P2_INSTALL_COMMAND_HANDLER, null);
            } catch (ExecutionException ex) {
                handleCommandException();
            } catch (NotDefinedException ex) {
                handleCommandException();
            } catch (NotEnabledException ex) {
                handleCommandException();
            } catch (NotHandledException ex) {
                handleCommandException();
            }
        }
    }

    private void handleCommandException() {
        MessageDialog.openError(PDEPlugin.getActiveWorkbenchShell(), PDEUIMessages.OverviewPage_ErrorOccurred, PDEUIMessages.OverviewPage_InstallNewSoftwareCouldNotBeOpened);
    }

    private void updateBuildProperties() throws InvocationTargetException {
        try {
            InputContext context = getPDEEditor().getContextManager().findContext(BuildInputContext.CONTEXT_ID);
            if (context != null) {
                IBuildModel buildModel = (IBuildModel) context.getModel();
                IBuild build = buildModel.getBuild();
                IBuildEntry entry = //$NON-NLS-1$
                build.getEntry(//$NON-NLS-1$
                "bin.includes");
                if (entry == null) {
                    entry = //$NON-NLS-1$
                    buildModel.getFactory().createEntry(//$NON-NLS-1$
                    "bin.includes");
                    build.add(entry);
                }
                if (!//$NON-NLS-1$
                entry.contains(//$NON-NLS-1$
                "META-INF"))
                    //$NON-NLS-1$
                    entry.addToken(//$NON-NLS-1$
                    "META-INF/");
            }
        } catch (CoreException e) {
            throw new InvocationTargetException(e);
        }
    }

    private void activateExtensionPages(String activePageId) {
        MessageDialog mdiag = new MessageDialog(PDEPlugin.getActiveWorkbenchShell(), PDEUIMessages.OverviewPage_extensionPageMessageTitle, null, PDEUIMessages.OverviewPage_extensionPageMessageBody, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
        if (mdiag.open() != Window.OK)
            return;
        try {
            ManifestEditor manifestEditor = (ManifestEditor) getEditor();
            manifestEditor.addExtensionTabs();
            manifestEditor.setShowExtensions(true);
            manifestEditor.setActivePage(activePageId);
        } catch (PartInitException e) {
        } catch (BackingStoreException e) {
        }
    }

    @Override
    public void dispose() {
        fDisposed = true;
        super.dispose();
    }

    @Override
    protected short getIndent() {
        return 5;
    }

    protected ILauncherFormPageHelper getLauncherHelper() {
        if (fLauncherHelper == null)
            fLauncherHelper = new PluginLauncherFormPageHelper(getPDELauncherEditor());
        return fLauncherHelper;
    }
}
