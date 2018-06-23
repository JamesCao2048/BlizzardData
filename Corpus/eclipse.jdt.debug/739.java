/*******************************************************************************
 *  Copyright (c) 2004, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDISourceViewer;
import org.eclipse.jdt.internal.debug.ui.contentassist.CurrentFrameContext;
import org.eclipse.jdt.internal.debug.ui.contentassist.JavaDebugContentAssistProcessor;
import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * A dialog which prompts the user to enter an expression for
 * evaluation.
 */
public class ExpressionInputDialog extends TrayDialog {

    protected IJavaVariable fVariable;

    protected String fResult = null;

    // Input area composite which acts as a placeholder for
    // input widgetry that is created/disposed dynamically.
    protected Composite fInputArea;

    // Source viewer widgets
    protected Composite fSourceViewerComposite;

    protected JDISourceViewer fSourceViewer;

    protected IContentAssistProcessor fCompletionProcessor;

    protected IDocumentListener fDocumentListener;

    protected IHandlerService fService;

    protected IHandlerActivation fActivation;

    //    protected HandlerSubmission fSubmission;
    // Text for error reporting
    protected Text fErrorText;

    /**
     * @param parentShell the shell to create the dialog in
     * @param variable the variable being edited
     */
    protected  ExpressionInputDialog(Shell parentShell, IJavaVariable variable) {
        super(parentShell);
        setShellStyle(SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
        fVariable = variable;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        IWorkbench workbench = PlatformUI.getWorkbench();
        workbench.getHelpSystem().setHelp(parent, IJavaDebugHelpContextIds.EXPRESSION_INPUT_DIALOG);
        Composite composite = (Composite) super.createDialogArea(parent);
        // Create the composite which will hold the input widgetry
        fInputArea = createInputArea(composite);
        // Create the error reporting text area
        fErrorText = createErrorText(composite);
        // Create the source viewer after creating the error text so that any
        // necessary error messages can be set.
        populateInputArea(fInputArea);
        return composite;
    }

    /**
     * Returns the text widget for reporting errors
     * @param parent parent composite
     * @return the error text widget
     */
    protected Text createErrorText(Composite parent) {
        //$NON-NLS-1$
        Text text = SWTFactory.createText(parent, SWT.READ_ONLY, 1, "");
        text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        return text;
    }

    /**
     * Returns the composite that will be used to contain the
     * input widgetry.
     * @param composite the parent composite
     * @return the composite that will contain the input widgets
     */
    protected Composite createInputArea(Composite parent) {
        Composite composite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
        Dialog.applyDialogFont(composite);
        return composite;
    }

    /**
     * Creates the appropriate widgetry in the input area. This
     * method is intended to be overridden by subclasses who wish
     * to use alternate input widgets.
     * @param parent parent composite
     */
    protected void populateInputArea(Composite parent) {
        fSourceViewerComposite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
        String name = ActionMessages.ExpressionInputDialog_3;
        try {
            name = fVariable.getName();
        } catch (DebugException e) {
            JDIDebugUIPlugin.log(e);
        }
        SWTFactory.createWrapLabel(fSourceViewerComposite, NLS.bind(ActionMessages.ExpressionInputDialog_0, new String[] { name }), 1, convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH));
        fSourceViewer = new JDISourceViewer(fSourceViewerComposite, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        fSourceViewer.setInput(fSourceViewerComposite);
        configureSourceViewer();
        fSourceViewer.doOperation(ITextOperationTarget.SELECT_ALL);
    }

    /**
     * Sets the visibility of the source viewer and the exclude attribute of its layout.
     * @param value If <code>true</code>, the viewer will be visible, if <code>false</code>, the viewer will be hidden.
     */
    protected void setSourceViewerVisible(boolean value) {
        if (fSourceViewerComposite != null) {
            fSourceViewerComposite.setVisible(value);
            GridData data = (GridData) fSourceViewerComposite.getLayoutData();
            data.exclude = !value;
            if (value) {
                fSourceViewer.getDocument().addDocumentListener(fDocumentListener);
                activateHandler();
            } else if (fActivation != null) {
                fSourceViewer.getDocument().removeDocumentListener(fDocumentListener);
                fService.deactivateHandler(fActivation);
            }
        }
    }

    /**
     * Initializes the source viewer. This method is based on code in BreakpointConditionEditor.
     */
    private void configureSourceViewer() {
        JavaTextTools tools = JDIDebugUIPlugin.getDefault().getJavaTextTools();
        IDocument document = new Document();
        tools.setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);
        fSourceViewer.configure(new DisplayViewerConfiguration() {

            @Override
            public IContentAssistProcessor getContentAssistantProcessor() {
                return getCompletionProcessor();
            }
        });
        fSourceViewer.setEditable(true);
        fSourceViewer.setDocument(document);
        final IUndoManager undoManager = new TextViewerUndoManager(10);
        fSourceViewer.setUndoManager(undoManager);
        undoManager.connect(fSourceViewer);
        fSourceViewer.getTextWidget().setFont(JFaceResources.getTextFont());
        Control control = fSourceViewer.getControl();
        GridData gd = new GridData(GridData.FILL_BOTH);
        control.setLayoutData(gd);
        gd = (GridData) fSourceViewer.getControl().getLayoutData();
        gd.heightHint = convertHeightInCharsToPixels(10);
        gd.widthHint = convertWidthInCharsToPixels(40);
        document.set(getInitialText(fVariable));
        fDocumentListener = new IDocumentListener() {

            @Override
            public void documentAboutToBeChanged(DocumentEvent event) {
            }

            @Override
            public void documentChanged(DocumentEvent event) {
                refreshValidState(fSourceViewer);
            }
        };
        fSourceViewer.getDocument().addDocumentListener(fDocumentListener);
        activateHandler();
    }

    /**
     * Activates the content assist handler.
     */
    private void activateHandler() {
        IHandler handler = new AbstractHandler() {

            @Override
            public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
                fSourceViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
                return null;
            }
        };
        IWorkbench workbench = PlatformUI.getWorkbench();
        fService = workbench.getAdapter(IHandlerService.class);
        fActivation = fService.activateHandler(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, handler);
    }

    /**
     * Returns the text that should be shown in the source viewer upon
     * initialization. The text should be presented in such a way that
     * it can be used as an evaluation expression which will return the
     * current value.
     * @param variable the variable
     * @return the initial text to display in the source viewer or <code>null</code>
     *  if none.
     */
    protected String getInitialText(IJavaVariable variable) {
        try {
            String signature = variable.getSignature();
            if (signature.equals("Ljava/lang/String;")) {
                //$NON-NLS-1$
                IValue value = variable.getValue();
                if (!(value instanceof JDINullValue)) {
                    String currentValue = value.getValueString();
                    StringBuffer buffer = new StringBuffer(currentValue.length());
                    // Surround value in quotes
                    buffer.append('"');
                    char[] chars = currentValue.toCharArray();
                    for (int i = 0; i < chars.length; i++) {
                        char c = chars[i];
                        if (c == '\b') {
                            //$NON-NLS-1$
                            buffer.append("\\b");
                        } else if (c == '\t') {
                            //$NON-NLS-1$
                            buffer.append("\\t");
                        } else if (c == '\n') {
                            //$NON-NLS-1$
                            buffer.append("\\n");
                        } else if (c == '\f') {
                            //$NON-NLS-1$
                            buffer.append("\\f");
                        } else if (c == '\r') {
                            //$NON-NLS-1$
                            buffer.append("\\r");
                        } else if (c == '"') {
                            //$NON-NLS-1$
                            buffer.append("\\\"");
                        } else if (c == '\'') {
                            //$NON-NLS-1$
                            buffer.append("\\\'");
                        } else if (c == '\\') {
                            buffer.append("\\\\");
                        } else {
                            buffer.append(c);
                        }
                    }
                    buffer.append('"');
                    return buffer.toString();
                }
            }
        } catch (DebugException e) {
        }
        return null;
    }

    protected IContentAssistProcessor getCompletionProcessor() {
        if (fCompletionProcessor == null) {
            fCompletionProcessor = new JavaDebugContentAssistProcessor(new CurrentFrameContext());
        }
        return fCompletionProcessor;
    }

    protected void refreshValidState(TextViewer viewer) {
        String errorMessage = null;
        if (viewer != null) {
            String text = viewer.getDocument().get();
            boolean valid = text != null && text.trim().length() > 0;
            if (!valid) {
                errorMessage = ActionMessages.ExpressionInputDialog_1;
            }
        }
        setErrorMessage(errorMessage);
    }

    protected void refreshValidState() {
        refreshValidState(fSourceViewer);
    }

    protected void setErrorMessage(String message) {
        if (message == null) {
            message = "";
        }
        fErrorText.setText(message);
        getButton(IDialogConstants.OK_ID).setEnabled(message.length() == 0);
    }

    @Override
    protected void okPressed() {
        fResult = getText();
        super.okPressed();
    }

    protected String getText() {
        return fSourceViewer.getDocument().get();
    }

    public String getResult() {
        return fResult;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(ActionMessages.ExpressionInputDialog_2);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        refreshValidState();
    }

    @Override
    public boolean close() {
        if (fActivation != null) {
            fService.deactivateHandler(fActivation);
        }
        if (fSourceViewer != null) {
            fSourceViewer.getDocument().removeDocumentListener(fDocumentListener);
            fSourceViewer.dispose();
            fSourceViewer = null;
        }
        if (fSourceViewerComposite != null) {
            fSourceViewerComposite.dispose();
            fSourceViewerComposite = null;
        }
        fDocumentListener = null;
        fCompletionProcessor = null;
        return super.close();
    }

    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        IDialogSettings settings = JDIDebugUIPlugin.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(getDialogSettingsSectionName());
        if (section == null) {
            section = settings.addNewSection(getDialogSettingsSectionName());
        }
        return section;
    }

    protected String getDialogSettingsSectionName() {
        return "EXPRESSION_INPUT_DIALOG";
    }
}
