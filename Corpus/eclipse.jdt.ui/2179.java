/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids, sdavids@gmx.de - bug 38507
 *     Sebastian Davids, sdavids@gmx.de - 113998 [JUnit] New Test Case Wizard: Class Under Test Dialog -- allow Enums
 *     Kris De Volder <kris.de.volder@gmail.com> - Allow changing the default superclass in NewTestCaseWizardPageOne - https://bugs.eclipse.org/312204
 *******************************************************************************/
package org.eclipse.jdt.junit.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import com.ibm.icu.text.UTF16;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.corext.util.JavaConventionsUtil;
import org.eclipse.jdt.internal.junit.BasicElementLabels;
import org.eclipse.jdt.internal.junit.JUnitCorePlugin;
import org.eclipse.jdt.internal.junit.Messages;
import org.eclipse.jdt.internal.junit.buildpath.BuildPathSupport;
import org.eclipse.jdt.internal.junit.ui.IJUnitHelpContextIds;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jdt.internal.junit.util.CoreTestSearchEngine;
import org.eclipse.jdt.internal.junit.util.JUnitStatus;
import org.eclipse.jdt.internal.junit.util.JUnitStubUtility;
import org.eclipse.jdt.internal.junit.util.JUnitStubUtility.GenStubSettings;
import org.eclipse.jdt.internal.junit.util.LayoutUtil;
import org.eclipse.jdt.internal.junit.wizards.MethodStubsSelectionButtonGroup;
import org.eclipse.jdt.internal.junit.wizards.WizardMessages;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.ControlContentAssistHelper;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.JavaTypeCompletionProcessor;

/**
 * The class <code>NewTestCaseWizardPageOne</code> contains controls and validation routines
 * for the first page of the  'New JUnit TestCase Wizard'.
 * <p>
 * Clients can use the page as-is and add it to their own wizard, or extend it to modify
 * validation or add and remove controls.
 * </p>
 *
 * @since 3.1
 */
public class NewTestCaseWizardPageOne extends NewTypeWizardPage {

    //$NON-NLS-1$
    private static final String PAGE_NAME = "NewTestCaseCreationWizardPage";

    /** Field ID of the class under test field. */
    //$NON-NLS-1$
    public static final String CLASS_UNDER_TEST = PAGE_NAME + ".classundertest";

    /**
	 * Field ID of the Junit4 toggle
	 * @since 3.2
	 */
    //$NON-NLS-1$
    public static final String JUNIT4TOGGLE = PAGE_NAME + ".junit4toggle";

    //$NON-NLS-1$
    private static final String COMPLIANCE_PAGE_ID = "org.eclipse.jdt.ui.propertyPages.CompliancePreferencePage";

    //$NON-NLS-1$
    private static final String BUILD_PATH_PAGE_ID = "org.eclipse.jdt.ui.propertyPages.BuildPathsPropertyPage";

    //$NON-NLS-1$
    private static final String BUILD_PATH_KEY_ADD_ENTRY = "add_classpath_entry";

    //$NON-NLS-1$
    private static final String BUILD_PATH_BLOCK = "block_until_buildpath_applied";

    //$NON-NLS-1$
    private static final String KEY_NO_LINK = "PropertyAndPreferencePage.nolink";

    //$NON-NLS-1$
    private static final String QUESTION_MARK_TAG = "Q";

    //$NON-NLS-1$
    private static final String OF_TAG = "Of";

    //$NON-NLS-1$
    private static final String TEST_SUFFIX = "Test";

    //$NON-NLS-1$
    private static final String PREFIX = "test";

    //$NON-NLS-1$
    private static final String STORE_SETUP = PAGE_NAME + ".USE_SETUP";

    //$NON-NLS-1$
    private static final String STORE_TEARDOWN = PAGE_NAME + ".USE_TEARDOWN";

    //$NON-NLS-1$
    private static final String STORE_SETUP_CLASS = PAGE_NAME + ".USE_SETUPCLASS";

    //$NON-NLS-1$
    private static final String STORE_TEARDOWN_CLASS = PAGE_NAME + ".USE_TEARDOWNCLASS";

    //$NON-NLS-1$
    private static final String STORE_CONSTRUCTOR = PAGE_NAME + ".USE_CONSTRUCTOR";

    private static final int IDX_SETUP_CLASS = 0;

    private static final int IDX_TEARDOWN_CLASS = 1;

    private static final int IDX_SETUP = 2;

    private static final int IDX_TEARDOWN = 3;

    private static final int IDX_CONSTRUCTOR = 4;

    private NewTestCaseWizardPageTwo fPage2;

    private MethodStubsSelectionButtonGroup fMethodStubsButtons;

    // model
    private String fClassUnderTestText;

    // resolved model, can be null
    private IType fClassUnderTest;

    // control
    private Text fClassUnderTestControl;

    // status
    private IStatus fClassUnderTestStatus;

    private Button fClassUnderTestButton;

    private JavaTypeCompletionProcessor fClassToTestCompletionProcessor;

    private Button fJUnit4Toggle;

    private boolean fIsJunit4;

    // status
    private IStatus fJunit4Status;

    private boolean fIsJunit4Enabled;

    private Link fLink;

    private Label fImage;

    /**
	 * Creates a new <code>NewTestCaseCreationWizardPage</code>.
	 * @param page2 The second page
	 *
	 * @since 3.1
	 */
    public  NewTestCaseWizardPageOne(NewTestCaseWizardPageTwo page2) {
        super(true, PAGE_NAME);
        fPage2 = page2;
        setTitle(WizardMessages.NewTestCaseWizardPageOne_title);
        setDescription(WizardMessages.NewTestCaseWizardPageOne_description);
        String[] buttonNames = new String[] { /* IDX_SETUP_CLASS */
        WizardMessages.NewTestCaseWizardPageOne_methodStub_setUpBeforeClass, /* IDX_TEARDOWN_CLASS */
        WizardMessages.NewTestCaseWizardPageOne_methodStub_tearDownAfterClass, /* IDX_SETUP */
        WizardMessages.NewTestCaseWizardPageOne_methodStub_setUp, /* IDX_TEARDOWN */
        WizardMessages.NewTestCaseWizardPageOne_methodStub_tearDown, /* IDX_CONSTRUCTOR */
        WizardMessages.NewTestCaseWizardPageOne_methodStub_constructor };
        enableCommentControl(true);
        fMethodStubsButtons = new MethodStubsSelectionButtonGroup(SWT.CHECK, buttonNames, 2) {

            @Override
            protected void doWidgetSelected(SelectionEvent e) {
                super.doWidgetSelected(e);
                saveWidgetValues();
            }
        };
        fMethodStubsButtons.setLabelText(WizardMessages.NewTestCaseWizardPageOne_method_Stub_label);
        fClassToTestCompletionProcessor = new JavaTypeCompletionProcessor(false, false, true);
        fClassUnderTestStatus = new JUnitStatus();
        //$NON-NLS-1$
        fClassUnderTestText = "";
        fJunit4Status = new JUnitStatus();
        fIsJunit4 = false;
    }

    /**
	 * Initialized the page with the current selection
	 * @param selection The selection
	 */
    public void init(IStructuredSelection selection) {
        IJavaElement element = getInitialJavaElement(selection);
        initContainerPage(element);
        initTypePage(element);
        // put default class to test
        if (element != null) {
            IType classToTest = null;
            // evaluate the enclosing type
            IType typeInCompUnit = (IType) element.getAncestor(IJavaElement.TYPE);
            if (typeInCompUnit != null) {
                if (typeInCompUnit.getCompilationUnit() != null) {
                    classToTest = typeInCompUnit;
                }
            } else {
                ICompilationUnit cu = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
                if (cu != null)
                    classToTest = cu.findPrimaryType();
                else {
                    if (element instanceof IClassFile) {
                        try {
                            IClassFile cf = (IClassFile) element;
                            if (cf.isStructureKnown())
                                classToTest = cf.getType();
                        } catch (JavaModelException e) {
                            JUnitPlugin.log(e);
                        }
                    }
                }
            }
            if (classToTest != null) {
                try {
                    if (!CoreTestSearchEngine.isTestImplementor(classToTest)) {
                        setClassUnderTest(classToTest.getFullyQualifiedName('.'));
                    }
                } catch (JavaModelException e) {
                    JUnitPlugin.log(e);
                }
            }
        }
        restoreWidgetValues();
        boolean isJunit4 = false;
        if (element != null && element.getElementType() != IJavaElement.JAVA_MODEL) {
            IJavaProject project = element.getJavaProject();
            isJunit4 = CoreTestSearchEngine.hasTestAnnotation(project);
            if (!isJunit4 && !CoreTestSearchEngine.hasTestCaseType(project) && JUnitStubUtility.is50OrHigher(project)) {
                isJunit4 = true;
            }
        }
        setJUnit4(isJunit4, true);
        updateStatus(getStatusList());
    }

    private IStatus junit4Changed() {
        JUnitStatus status = new JUnitStatus();
        return status;
    }

    /**
	 * Specifies if the test should be created as JUnit 4 test.
	 * @param isJUnit4 If set, a Junit 4 test will be created
	 * @param isEnabled if <code>true</code> the modifier fields are
	 * editable; otherwise they are read-only
	 *
	 * @since 3.2
	 */
    public void setJUnit4(boolean isJUnit4, boolean isEnabled) {
        fIsJunit4Enabled = isEnabled;
        if (fJUnit4Toggle != null && !fJUnit4Toggle.isDisposed()) {
            fJUnit4Toggle.setSelection(isJUnit4);
            fJUnit4Toggle.setEnabled(isEnabled);
        }
        internalSetJUnit4(isJUnit4);
    }

    /**
	 * Returns <code>true</code> if the test should be created as Junit 4 test
	 * @return returns <code>true</code> if the test should be created as Junit 4 test
	 *
	 * @since 3.2
	 */
    public boolean isJUnit4() {
        return fIsJunit4;
    }

    private void internalSetJUnit4(boolean isJUnit4) {
        fIsJunit4 = isJUnit4;
        fJunit4Status = junit4Changed();
        if (//$NON-NLS-1$
        isDefaultSuperClass() || getSuperClass().trim().equals(""))
            setSuperClass(getDefaultSuperClassName(), true);
        //validate superclass field when toggled
        fSuperClassStatus = superClassChanged();
        handleFieldChanged(JUNIT4TOGGLE);
    }

    /**
	 * Returns whether the super class name is one of the default super class names.
	 * 
	 * @return <code>true</code> if the super class name is one of the default super class names,
	 *         <code>false</code> otherwise
	 * @since 3.7
	 */
    private boolean isDefaultSuperClass() {
        String superClass = getSuperClass();
        //$NON-NLS-1$
        return superClass.equals(getJUnit3TestSuperclassName()) || superClass.equals("java.lang.Object");
    }

    @Override
    protected void handleFieldChanged(String fieldName) {
        super.handleFieldChanged(fieldName);
        if (fieldName.equals(CONTAINER)) {
            fClassUnderTestStatus = classUnderTestChanged();
            if (fClassUnderTestButton != null && !fClassUnderTestButton.isDisposed()) {
                fClassUnderTestButton.setEnabled(getPackageFragmentRoot() != null);
            }
            fJunit4Status = junit4Changed();
            updateBuildPathMessage();
        } else if (fieldName.equals(JUNIT4TOGGLE)) {
            updateBuildPathMessage();
            fMethodStubsButtons.setEnabled(IDX_SETUP_CLASS, isJUnit4());
            fMethodStubsButtons.setEnabled(IDX_TEARDOWN_CLASS, isJUnit4());
            fMethodStubsButtons.setEnabled(IDX_CONSTRUCTOR, !isJUnit4());
        }
        updateStatus(getStatusList());
    }

    /**
	 * Returns all status to be consider for the validation. Clients can override.
	 * @return The list of status to consider for the validation.
	 */
    protected IStatus[] getStatusList() {
        return new IStatus[] { fContainerStatus, fPackageStatus, fTypeNameStatus, fClassUnderTestStatus, fModifierStatus, fSuperClassStatus, fJunit4Status };
    }

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        Composite composite = new Composite(parent, SWT.NONE);
        int nColumns = 4;
        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout(layout);
        createJUnit4Controls(composite, nColumns);
        createContainerControls(composite, nColumns);
        createPackageControls(composite, nColumns);
        createSeparator(composite, nColumns);
        createTypeNameControls(composite, nColumns);
        createSuperClassControls(composite, nColumns);
        createMethodStubSelectionControls(composite, nColumns);
        createCommentControls(composite, nColumns);
        createSeparator(composite, nColumns);
        createClassUnderTestControls(composite, nColumns);
        createBuildPathConfigureControls(composite, nColumns);
        setControl(composite);
        //set default and focus
        String classUnderTest = getClassUnderTestText();
        if (classUnderTest.length() > 0) {
            setTypeName(Signature.getSimpleName(classUnderTest) + TEST_SUFFIX, true);
        }
        Dialog.applyDialogFont(composite);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IJUnitHelpContextIds.NEW_TESTCASE_WIZARD_PAGE);
        setFocus();
    }

    /**
	 * Creates the controls for the method stub selection buttons. Expects a <code>GridLayout</code> with
	 * at least 3 columns.
	 *
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */
    protected void createMethodStubSelectionControls(Composite composite, int nColumns) {
        LayoutUtil.setHorizontalSpan(fMethodStubsButtons.getLabelControl(composite), nColumns);
        LayoutUtil.createEmptySpace(composite, 1);
        LayoutUtil.setHorizontalSpan(fMethodStubsButtons.getSelectionButtonsGroup(composite), nColumns - 1);
    }

    /**
	 * Creates the controls for the 'class under test' field. Expects a <code>GridLayout</code> with
	 * at least 3 columns.
	 *
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */
    protected void createClassUnderTestControls(Composite composite, int nColumns) {
        Label classUnderTestLabel = new Label(composite, SWT.LEFT | SWT.WRAP);
        classUnderTestLabel.setFont(composite.getFont());
        classUnderTestLabel.setText(WizardMessages.NewTestCaseWizardPageOne_class_to_test_label);
        classUnderTestLabel.setLayoutData(new GridData());
        fClassUnderTestControl = new Text(composite, SWT.SINGLE | SWT.BORDER);
        fClassUnderTestControl.setEnabled(true);
        fClassUnderTestControl.setFont(composite.getFont());
        fClassUnderTestControl.setText(fClassUnderTestText);
        fClassUnderTestControl.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                internalSetClassUnderText(((Text) e.widget).getText());
            }
        });
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = nColumns - 2;
        fClassUnderTestControl.setLayoutData(gd);
        fClassUnderTestButton = new Button(composite, SWT.PUSH);
        fClassUnderTestButton.setText(WizardMessages.NewTestCaseWizardPageOne_class_to_test_browse);
        fClassUnderTestButton.setEnabled(getPackageFragmentRoot() != null);
        fClassUnderTestButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                classToTestButtonPressed();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                classToTestButtonPressed();
            }
        });
        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 1;
        gd.widthHint = LayoutUtil.getButtonWidthHint(fClassUnderTestButton);
        fClassUnderTestButton.setLayoutData(gd);
        ControlContentAssistHelper.createTextContentAssistant(fClassUnderTestControl, fClassToTestCompletionProcessor);
    }

    /**
	 * Creates the controls for the JUnit 4 toggle control. Expects a <code>GridLayout</code> with
	 * at least 3 columns.
	 *
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 *
	 * @since 3.2
	 */
    protected void createJUnit4Controls(Composite composite, int nColumns) {
        Composite inner = new Composite(composite, SWT.NONE);
        inner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, nColumns, 1));
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        inner.setLayout(layout);
        SelectionAdapter listener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean isSelected = ((Button) e.widget).getSelection();
                internalSetJUnit4(isSelected);
            }
        };
        Button junti3Toggle = new Button(inner, SWT.RADIO);
        junti3Toggle.setText(WizardMessages.NewTestCaseWizardPageOne_junit3_radio_label);
        junti3Toggle.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1));
        junti3Toggle.setSelection(!fIsJunit4);
        junti3Toggle.setEnabled(fIsJunit4Enabled);
        fJUnit4Toggle = new Button(inner, SWT.RADIO);
        fJUnit4Toggle.setText(WizardMessages.NewTestCaseWizardPageOne_junit4_radio_label);
        fJUnit4Toggle.setSelection(fIsJunit4);
        fJUnit4Toggle.setEnabled(fIsJunit4Enabled);
        fJUnit4Toggle.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1));
        fJUnit4Toggle.addSelectionListener(listener);
    }

    /**
	 * Creates the controls for the JUnit 4 toggle control. Expects a <code>GridLayout</code> with
	 * at least 3 columns.
	 *
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 *
	 * @since 3.2
	 */
    protected void createBuildPathConfigureControls(Composite composite, int nColumns) {
        Composite inner = new Composite(composite, SWT.NONE);
        inner.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false, nColumns, 1));
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        inner.setLayout(layout);
        fImage = new Label(inner, SWT.NONE);
        fImage.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING));
        fImage.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        fLink = new Link(inner, SWT.WRAP);
        //$NON-NLS-1$
        fLink.setText("\n\n");
        fLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                performBuildpathConfiguration(e.text);
            }
        });
        GridData gd = new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1);
        gd.widthHint = convertWidthInCharsToPixels(60);
        fLink.setLayoutData(gd);
        updateBuildPathMessage();
    }

    private void performBuildpathConfiguration(Object data) {
        IPackageFragmentRoot root = getPackageFragmentRoot();
        if (root == null) {
            // should not happen. Link shouldn't be visible
            return;
        }
        IJavaProject javaProject = root.getJavaProject();
        if (// add and configure JUnit 3 //$NON-NLS-1$
        "a3".equals(data)) {
            String id = BUILD_PATH_PAGE_ID;
            Map<String, Object> input = new HashMap();
            IClasspathEntry newEntry = BuildPathSupport.getJUnit3ClasspathEntry();
            input.put(BUILD_PATH_KEY_ADD_ENTRY, newEntry);
            input.put(BUILD_PATH_BLOCK, Boolean.TRUE);
            PreferencesUtil.createPropertyDialogOn(getShell(), javaProject, id, new String[] { id }, input).open();
        } else if (// add and configure JUnit 4 //$NON-NLS-1$
        "a4".equals(data)) {
            String id = BUILD_PATH_PAGE_ID;
            Map<String, Object> input = new HashMap();
            IClasspathEntry newEntry = BuildPathSupport.getJUnit4ClasspathEntry();
            input.put(BUILD_PATH_KEY_ADD_ENTRY, newEntry);
            input.put(BUILD_PATH_BLOCK, Boolean.TRUE);
            PreferencesUtil.createPropertyDialogOn(getShell(), javaProject, id, new String[] { id }, input).open();
        } else if (// open build path //$NON-NLS-1$
        "b".equals(data)) {
            String id = BUILD_PATH_PAGE_ID;
            Map<String, Object> input = new HashMap();
            input.put(BUILD_PATH_BLOCK, Boolean.TRUE);
            PreferencesUtil.createPropertyDialogOn(getShell(), javaProject, id, new String[] { id }, input).open();
        } else if (// open compliance //$NON-NLS-1$
        "c".equals(data)) {
            String buildPath = BUILD_PATH_PAGE_ID;
            String complianceId = COMPLIANCE_PAGE_ID;
            Map<String, Boolean> input = new HashMap();
            input.put(BUILD_PATH_BLOCK, Boolean.TRUE);
            input.put(KEY_NO_LINK, Boolean.TRUE);
            PreferencesUtil.createPropertyDialogOn(getShell(), javaProject, complianceId, new String[] { buildPath, complianceId }, data).open();
        }
        updateBuildPathMessage();
    }

    private void updateBuildPathMessage() {
        if (fLink == null || fLink.isDisposed()) {
            return;
        }
        String message = null;
        IPackageFragmentRoot root = getPackageFragmentRoot();
        if (root != null) {
            IJavaProject project = root.getJavaProject();
            if (project.exists()) {
                if (isJUnit4()) {
                    if (!JUnitStubUtility.is50OrHigher(project)) {
                        message = WizardMessages.NewTestCaseWizardPageOne_linkedtext_java5required;
                    }
                }
            }
        }
        fLink.setVisible(message != null);
        fImage.setVisible(message != null);
        if (message != null) {
            fLink.setText(message);
        }
    }

    private void classToTestButtonPressed() {
        IType type = chooseClassToTestType();
        if (type != null) {
            setClassUnderTest(type.getFullyQualifiedName('.'));
        }
    }

    private IType chooseClassToTestType() {
        IPackageFragmentRoot root = getPackageFragmentRoot();
        if (root == null)
            return null;
        IJavaElement[] elements = new IJavaElement[] { root.getJavaProject() };
        IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elements);
        try {
            SelectionDialog dialog = JavaUI.createTypeDialog(getShell(), getWizard().getContainer(), scope, IJavaElementSearchConstants.CONSIDER_CLASSES_AND_ENUMS, false, getClassUnderTestText());
            dialog.setTitle(WizardMessages.NewTestCaseWizardPageOne_class_to_test_dialog_title);
            dialog.setMessage(WizardMessages.NewTestCaseWizardPageOne_class_to_test_dialog_message);
            if (dialog.open() == Window.OK) {
                Object[] resultArray = dialog.getResult();
                if (resultArray != null && resultArray.length > 0)
                    return (IType) resultArray[0];
            }
        } catch (JavaModelException e) {
            JUnitPlugin.log(e);
        }
        return null;
    }

    @Override
    protected IStatus packageChanged() {
        IStatus status = super.packageChanged();
        fClassToTestCompletionProcessor.setPackageFragment(getPackageFragment());
        return status;
    }

    /**
	 * Hook method that gets called when the class under test has changed. The method class under test
	 * returns the status of the validation.
	 * <p>
	 * Subclasses may extend this method to perform their own validation.
	 * </p>
	 *
	 * @return the status of the validation
	 */
    protected IStatus classUnderTestChanged() {
        JUnitStatus status = new JUnitStatus();
        fClassUnderTest = null;
        IPackageFragmentRoot root = getPackageFragmentRoot();
        if (root == null) {
            return status;
        }
        String classToTestName = getClassUnderTestText();
        if (classToTestName.length() == 0) {
            return status;
        }
        IStatus val = JavaConventionsUtil.validateJavaTypeName(classToTestName, root);
        if (val.getSeverity() == IStatus.ERROR) {
            status.setError(WizardMessages.NewTestCaseWizardPageOne_error_class_to_test_not_valid);
            return status;
        }
        // can be null
        IPackageFragment pack = getPackageFragment();
        try {
            IType type = resolveClassNameToType(root.getJavaProject(), pack, classToTestName);
            if (type == null) {
                status.setError(WizardMessages.NewTestCaseWizardPageOne_error_class_to_test_not_exist);
                return status;
            }
            if (type.isInterface()) {
                status.setWarning(Messages.format(WizardMessages.NewTestCaseWizardPageOne_warning_class_to_test_is_interface, BasicElementLabels.getJavaElementName(classToTestName)));
            }
            if (pack != null && !JUnitStubUtility.isVisible(type, pack)) {
                status.setWarning(Messages.format(WizardMessages.NewTestCaseWizardPageOne_warning_class_to_test_not_visible, BasicElementLabels.getJavaElementName(classToTestName)));
            }
            fClassUnderTest = type;
            fPage2.setClassUnderTest(fClassUnderTest);
        } catch (JavaModelException e) {
            status.setError(WizardMessages.NewTestCaseWizardPageOne_error_class_to_test_not_valid);
        }
        return status;
    }

    /**
	 * Returns the content of the class to test text field.
	 *
	 * @return the name of the class to test
	 */
    public String getClassUnderTestText() {
        return fClassUnderTestText;
    }

    /**
	 * Returns the class to be tested.
	 *
	 * 	@return the class under test or <code>null</code> if the entered values are not valid
	 */
    public IType getClassUnderTest() {
        return fClassUnderTest;
    }

    /**
	 * Sets the name of the class under test.
	 *
	 * @param name The name to set
	 */
    public void setClassUnderTest(String name) {
        if (fClassUnderTestControl != null && !fClassUnderTestControl.isDisposed()) {
            fClassUnderTestControl.setText(name);
        }
        internalSetClassUnderText(name);
    }

    private void internalSetClassUnderText(String name) {
        fClassUnderTestText = name;
        fClassUnderTestStatus = classUnderTestChanged();
        handleFieldChanged(CLASS_UNDER_TEST);
    }

    @Override
    protected void createTypeMembers(IType type, ImportsManager imports, IProgressMonitor monitor) throws CoreException {
        if (fMethodStubsButtons.isSelected(IDX_CONSTRUCTOR))
            createConstructor(type, imports);
        if (fMethodStubsButtons.isSelected(IDX_SETUP_CLASS)) {
            createSetUpClass(type, imports);
        }
        if (fMethodStubsButtons.isSelected(IDX_TEARDOWN_CLASS)) {
            createTearDownClass(type, imports);
        }
        if (fMethodStubsButtons.isSelected(IDX_SETUP)) {
            createSetUp(type, imports);
        }
        if (fMethodStubsButtons.isSelected(IDX_TEARDOWN)) {
            createTearDown(type, imports);
        }
        if (fClassUnderTest != null || isJUnit4()) {
            createTestMethodStubs(type, imports);
        }
        if (isJUnit4()) {
            //$NON-NLS-1$ //$NON-NLS-2$
            imports.addStaticImport("org.junit.Assert", "*", false);
        }
    }

    private void createConstructor(IType type, ImportsManager imports) throws CoreException {
        ITypeHierarchy typeHierarchy = null;
        IType[] superTypes = null;
        String content;
        IMethod methodTemplate = null;
        if (type.exists()) {
            typeHierarchy = type.newSupertypeHierarchy(null);
            superTypes = typeHierarchy.getAllSuperclasses(type);
            for (IType superType : superTypes) {
                if (superType.exists()) {
                    IMethod constrMethod = superType.getMethod(superType.getElementName(), new String[] { "Ljava.lang.String;" });
                    if (constrMethod.exists() && constrMethod.isConstructor()) {
                        methodTemplate = constrMethod;
                        break;
                    }
                }
            }
        }
        GenStubSettings settings = JUnitStubUtility.getCodeGenerationSettings(type.getJavaProject());
        settings.createComments = isAddComments();
        if (methodTemplate != null) {
            settings.callSuper = true;
            settings.methodOverwrites = true;
            content = JUnitStubUtility.genStub(type.getCompilationUnit(), getTypeName(), methodTemplate, settings, null, imports);
        } else {
            final String delimiter = getLineDelimiter();
            StringBuffer buffer = new StringBuffer(32);
            //$NON-NLS-1$
            buffer.append("public ");
            buffer.append(getTypeName());
            buffer.append('(');
            if (!isJUnit4()) {
                //$NON-NLS-1$ //$NON-NLS-2$
                buffer.append(imports.addImport("java.lang.String")).append(" name");
            }
            //$NON-NLS-1$
            buffer.append(") {");
            buffer.append(delimiter);
            if (!isJUnit4()) {
                //$NON-NLS-1$
                buffer.append("super(name);").append(//$NON-NLS-1$
                delimiter);
            }
            buffer.append('}');
            buffer.append(delimiter);
            content = buffer.toString();
        }
        type.createMethod(content, null, true, null);
    }

    private IMethod findInHierarchy(IType type, String methodName) throws JavaModelException {
        ITypeHierarchy typeHierarchy = null;
        IType[] superTypes = null;
        if (type.exists()) {
            typeHierarchy = type.newSupertypeHierarchy(null);
            superTypes = typeHierarchy.getAllSuperclasses(type);
            for (IType superType : superTypes) {
                if (superType.exists()) {
                    IMethod testMethod = superType.getMethod(methodName, new String[] {});
                    if (testMethod.exists()) {
                        return testMethod;
                    }
                }
            }
        }
        return null;
    }

    private void createSetupStubs(IType type, String methodName, boolean isStatic, String annotationType, ImportsManager imports) throws CoreException {
        String content = null;
        IMethod methodTemplate = findInHierarchy(type, methodName);
        String annotation = null;
        if (isJUnit4()) {
            annotation = '@' + imports.addImport(annotationType);
        }
        GenStubSettings settings = JUnitStubUtility.getCodeGenerationSettings(type.getJavaProject());
        settings.createComments = isAddComments();
        if (methodTemplate != null) {
            settings.callSuper = true;
            settings.methodOverwrites = true;
            content = JUnitStubUtility.genStub(type.getCompilationUnit(), getTypeName(), methodTemplate, settings, annotation, imports);
        } else {
            final String delimiter = getLineDelimiter();
            StringBuffer buffer = new StringBuffer();
            if (settings.createComments) {
                String[] excSignature = { //$NON-NLS-1$
                Signature.createTypeSignature("java.lang.Exception", true) };
                String comment = CodeGeneration.getMethodComment(type.getCompilationUnit(), type.getElementName(), methodName, new String[0], excSignature, Signature.SIG_VOID, null, delimiter);
                if (comment != null) {
                    buffer.append(comment);
                }
            }
            if (annotation != null) {
                buffer.append(annotation).append(delimiter);
            }
            if (isJUnit4()) {
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "public ");
            } else {
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "protected ");
            }
            if (isStatic) {
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "static ");
            }
            //$NON-NLS-1$
            buffer.append("void ");
            buffer.append(methodName);
            //$NON-NLS-1$
            buffer.append("() throws ");
            //$NON-NLS-1$
            buffer.append(imports.addImport("java.lang.Exception"));
            //$NON-NLS-1$
            buffer.append(" {}");
            buffer.append(delimiter);
            content = buffer.toString();
        }
        type.createMethod(content, null, false, null);
    }

    private void createSetUp(IType type, ImportsManager imports) throws CoreException {
        //$NON-NLS-1$ //$NON-NLS-2$
        createSetupStubs(type, "setUp", false, "org.junit.Before", imports);
    }

    private void createTearDown(IType type, ImportsManager imports) throws CoreException {
        //$NON-NLS-1$ //$NON-NLS-2$
        createSetupStubs(type, "tearDown", false, "org.junit.After", imports);
    }

    private void createSetUpClass(IType type, ImportsManager imports) throws CoreException {
        //$NON-NLS-1$ //$NON-NLS-2$
        createSetupStubs(type, "setUpBeforeClass", true, "org.junit.BeforeClass", imports);
    }

    private void createTearDownClass(IType type, ImportsManager imports) throws CoreException {
        //$NON-NLS-1$ //$NON-NLS-2$
        createSetupStubs(type, "tearDownAfterClass", true, "org.junit.AfterClass", imports);
    }

    private void createTestMethodStubs(IType type, ImportsManager imports) throws CoreException {
        IMethod[] methods = fPage2.getCheckedMethods();
        if (methods.length == 0) {
            if (isJUnit4()) {
                List<String> names = new ArrayList();
                createTestMethod(type, imports, null, null, names);
            }
            return;
        }
        /* find overloaded methods */
        IMethod[] allMethodsArray = fPage2.getAllMethods();
        List<IMethod> allMethods = new ArrayList();
        allMethods.addAll(Arrays.asList(allMethodsArray));
        List<IMethod> overloadedMethods = getOverloadedMethods(allMethods);
        /* used when for example both sum and Sum methods are present. Then
		 * sum -> testSum
		 * Sum -> testSum1
		 */
        List<String> names = new ArrayList();
        for (IMethod method : methods) {
            createTestMethod(type, imports, method, overloadedMethods, names);
        }
    }

    /**
	 * Creates a test method.
	 * 
	 * @param type the type to create the method
	 * @param imports the imports manager
	 * @param method the method or <code>null</code>
	 * @param overloadedMethods the list of overloaded methods or <code>null</code>
	 * @param names the list of method names
	 * @throws CoreException if the element could not be created
	 * @since 3.7
	 */
    private void createTestMethod(IType type, ImportsManager imports, IMethod method, List<IMethod> overloadedMethods, List<String> names) throws CoreException {
        StringBuffer buffer = new StringBuffer();
        StringBuffer name;
        if (method != null) {
            String elementName = method.getElementName();
            name = new StringBuffer(PREFIX).append(Character.toUpperCase(elementName.charAt(0))).append(elementName.substring(1));
            final boolean contains = overloadedMethods.contains(method);
            if (contains)
                appendParameterNamesToMethodName(name, method.getParameterTypes());
        } else {
            name = new StringBuffer(PREFIX);
        }
        replaceIllegalCharacters(name);
        /* void foo(java.lang.StringBuffer sb) {}
		 *  void foo(mypackage1.StringBuffer sb) {}
		 *  void foo(mypackage2.StringBuffer sb) {}
		 * ->
		 *  testFooStringBuffer()
		 *  testFooStringBuffer1()
		 *  testFooStringBuffer2()
		 */
        String testName = name.toString();
        if (names.contains(testName)) {
            int suffix = 1;
            while (names.contains(testName + Integer.toString(suffix))) suffix++;
            name.append(Integer.toString(suffix));
        }
        testName = name.toString();
        names.add(testName);
        if (isAddComments() && method != null) {
            appendMethodComment(buffer, method);
        }
        if (isJUnit4()) {
            ISourceRange typeSourceRange = type.getSourceRange();
            int pos = typeSourceRange.getOffset() + typeSourceRange.getLength() - 1;
            buffer.append('@').append(imports.addImport(JUnitCorePlugin.JUNIT4_ANNOTATION_NAME, pos)).append(getLineDelimiter());
        }
        //$NON-NLS-1$
        buffer.append("public ");
        if (fPage2.getCreateFinalMethodStubsButtonSelection())
            //$NON-NLS-1$
            buffer.append("final ");
        //$NON-NLS-1$
        buffer.append("void ");
        buffer.append(testName);
        //$NON-NLS-1$
        buffer.append("()");
        appendTestMethodBody(buffer, type.getCompilationUnit());
        type.createMethod(buffer.toString(), null, false, null);
    }

    private void replaceIllegalCharacters(StringBuffer buffer) {
        char character = 0;
        for (int index = buffer.length() - 1; index >= 0; index--) {
            character = buffer.charAt(index);
            if (Character.isWhitespace(character))
                buffer.deleteCharAt(index);
            else if (character == '<')
                buffer.replace(index, index + 1, OF_TAG);
            else if (character == '?')
                buffer.replace(index, index + 1, QUESTION_MARK_TAG);
            else if (!Character.isJavaIdentifierPart(character)) {
                // Check for surrogates
                if (!UTF16.isSurrogate(character)) {
                    /*
					 * XXX: Here we should create the code point and test whether
					 * it is a Java identifier part. Currently this is not possible
					 * because java.lang.Character in 1.4 does not support surrogates
					 * and because com.ibm.icu.lang.UCharacter.isJavaIdentifierPart(int)
					 * is not correctly implemented.
					 */
                    buffer.deleteCharAt(index);
                }
            }
        }
    }

    private String getLineDelimiter() throws JavaModelException {
        IType classToTest = getClassUnderTest();
        if (classToTest != null && classToTest.exists() && classToTest.getCompilationUnit() != null)
            return classToTest.getCompilationUnit().findRecommendedLineSeparator();
        return getPackageFragment().findRecommendedLineSeparator();
    }

    private void appendTestMethodBody(StringBuffer buffer, ICompilationUnit targetCu) throws CoreException {
        final String delimiter = getLineDelimiter();
        buffer.append('{').append(delimiter);
        //$NON-NLS-1$
        String todoTask = "";
        if (fPage2.isCreateTasks()) {
            String todoTaskTag = JUnitStubUtility.getTodoTaskTag(targetCu.getJavaProject());
            if (todoTaskTag != null) {
                todoTask = //$NON-NLS-1$
                " // " + //$NON-NLS-1$
                todoTaskTag;
            }
        }
        String message = WizardMessages.NewTestCaseWizardPageOne_not_yet_implemented_string;
        //$NON-NLS-1$
        buffer.append(Messages.format("fail(\"{0}\");", message)).append(todoTask).append(delimiter);
        buffer.append('}').append(delimiter);
    }

    private void appendParameterNamesToMethodName(StringBuffer buffer, String[] parameters) {
        for (String parameter : parameters) {
            final StringBuffer buf = new StringBuffer(Signature.getSimpleName(Signature.toString(Signature.getElementType(parameter))));
            final char character = buf.charAt(0);
            if (buf.length() > 0 && !Character.isUpperCase(character))
                buf.setCharAt(0, Character.toUpperCase(character));
            buffer.append(buf.toString());
            for (int j = 0, arrayCount = Signature.getArrayCount(parameter); j < arrayCount; j++) {
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "Array");
            }
        }
    }

    private void appendMethodComment(StringBuffer buffer, IMethod method) throws JavaModelException {
        final String delimiter = getLineDelimiter();
        //$NON-NLS-1$
        final StringBuffer buf = new StringBuffer("{@link ");
        JavaElementLabels.getTypeLabel(method.getDeclaringType(), JavaElementLabels.T_FULLY_QUALIFIED, buf);
        buf.append('#');
        buf.append(method.getElementName());
        buf.append('(');
        String[] paramTypes = JUnitStubUtility.getParameterTypeNamesForSeeTag(method);
        for (int i = 0; i < paramTypes.length; i++) {
            if (i != 0) {
                //$NON-NLS-1$
                buf.append(//$NON-NLS-1$
                ", ");
            }
            buf.append(paramTypes[i]);
        }
        buf.append(')');
        buf.append('}');
        //$NON-NLS-1$
        buffer.append("/**");
        buffer.append(delimiter);
        //$NON-NLS-1$
        buffer.append(" * ");
        buffer.append(Messages.format(WizardMessages.NewTestCaseWizardPageOne_comment_class_to_test, buf.toString()));
        buffer.append(delimiter);
        //$NON-NLS-1$
        buffer.append(" */");
        buffer.append(delimiter);
    }

    private List<IMethod> getOverloadedMethods(List<IMethod> allMethods) {
        List<IMethod> overloadedMethods = new ArrayList();
        for (int i = 0; i < allMethods.size(); i++) {
            IMethod current = allMethods.get(i);
            String currentName = current.getElementName();
            boolean currentAdded = false;
            for (ListIterator<IMethod> iter = allMethods.listIterator(i + 1); iter.hasNext(); ) {
                IMethod iterMethod = iter.next();
                if (iterMethod.getElementName().equals(currentName)) {
                    //method is overloaded
                    if (!currentAdded) {
                        overloadedMethods.add(current);
                        currentAdded = true;
                    }
                    overloadedMethods.add(iterMethod);
                    iter.remove();
                }
            }
        }
        return overloadedMethods;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible) {
            saveWidgetValues();
        }
    //if (visible) setFocus();
    }

    /**
	 * The method is called when the container has changed to validate if the project
	 * is suited for the JUnit test class. Clients can override to modify or remove that validation.
	 *
	 * @return the status of the validation
	 */
    protected IStatus validateIfJUnitProject() {
        JUnitStatus status = new JUnitStatus();
        IPackageFragmentRoot root = getPackageFragmentRoot();
        if (root != null) {
            try {
                IJavaProject project = root.getJavaProject();
                if (project.exists()) {
                    if (isJUnit4()) {
                        if (!JUnitStubUtility.is50OrHigher(project)) {
                            status.setError(WizardMessages.NewTestCaseWizardPageOne_error_java5required);
                            return status;
                        }
                        if (project.findType(JUnitCorePlugin.JUNIT4_ANNOTATION_NAME) == null) {
                            status.setWarning(WizardMessages.NewTestCaseWizardPageOne__error_junit4NotOnbuildpath);
                            return status;
                        }
                    } else {
                        if (project.findType(JUnitCorePlugin.TEST_SUPERCLASS_NAME) == null) {
                            status.setWarning(WizardMessages.NewTestCaseWizardPageOne_error_junitNotOnbuildpath);
                            return status;
                        }
                    }
                }
            } catch (JavaModelException e) {
            }
        }
        return status;
    }

    @Override
    protected IStatus superClassChanged() {
        IStatus stat = super.superClassChanged();
        if (stat.getSeverity() != IStatus.OK)
            return stat;
        String superClassName = getSuperClass();
        JUnitStatus status = new JUnitStatus();
        boolean isJUnit4 = isJUnit4();
        if (//$NON-NLS-1$
        superClassName == null || superClassName.trim().equals("")) {
            if (!isJUnit4)
                status.setError(WizardMessages.NewTestCaseWizardPageOne_error_superclass_empty);
            return status;
        }
        if (//$NON-NLS-1$
        isJUnit4 && superClassName.equals("java.lang.Object"))
            return status;
        if (getPackageFragmentRoot() != null) {
            try {
                IType type = resolveClassNameToType(getPackageFragmentRoot().getJavaProject(), getPackageFragment(), superClassName);
                if (type == null) {
                    status.setWarning(WizardMessages.NewTestCaseWizardPageOne_error_superclass_not_exist);
                    return status;
                }
                if (type.isInterface()) {
                    status.setError(WizardMessages.NewTestCaseWizardPageOne_error_superclass_is_interface);
                    return status;
                }
                if (// TODO: expensive!
                !isJUnit4 && !CoreTestSearchEngine.isTestImplementor(type)) {
                    status.setError(Messages.format(WizardMessages.NewTestCaseWizardPageOne_error_superclass_not_implementing_test_interface, BasicElementLabels.getJavaElementName(JUnitCorePlugin.TEST_INTERFACE_NAME)));
                    return status;
                }
            } catch (JavaModelException e) {
                JUnitPlugin.log(e);
            }
        }
        return status;
    }

    @Override
    public boolean canFlipToNextPage() {
        return super.canFlipToNextPage() && getClassUnderTest() != null;
    }

    private IType resolveClassNameToType(IJavaProject jproject, IPackageFragment pack, String classToTestName) throws JavaModelException {
        if (!jproject.exists()) {
            return null;
        }
        IType type = jproject.findType(classToTestName);
        // search in current package
        if (type == null && pack != null && !pack.isDefaultPackage()) {
            type = jproject.findType(pack.getElementName(), classToTestName);
        }
        // search in java.lang
        if (type == null) {
            //$NON-NLS-1$
            type = jproject.findType("java.lang", classToTestName);
        }
        return type;
    }

    /**
	 *	Use the dialog store to restore widget values to the values that they held
	 *	last time this wizard was used to completion
	 */
    private void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            fMethodStubsButtons.setSelection(IDX_SETUP, settings.getBoolean(STORE_SETUP));
            fMethodStubsButtons.setSelection(IDX_TEARDOWN, settings.getBoolean(STORE_TEARDOWN));
            fMethodStubsButtons.setSelection(IDX_SETUP_CLASS, settings.getBoolean(STORE_SETUP_CLASS));
            fMethodStubsButtons.setSelection(IDX_TEARDOWN_CLASS, settings.getBoolean(STORE_TEARDOWN_CLASS));
            fMethodStubsButtons.setSelection(IDX_CONSTRUCTOR, settings.getBoolean(STORE_CONSTRUCTOR));
        } else {
            //setUp
            fMethodStubsButtons.setSelection(//setUp
            IDX_SETUP, //setUp
            false);
            //tearDown
            fMethodStubsButtons.setSelection(IDX_TEARDOWN, false);
            //setUpBeforeClass
            fMethodStubsButtons.setSelection(IDX_SETUP_CLASS, false);
            //setUpAfterClass
            fMethodStubsButtons.setSelection(IDX_TEARDOWN_CLASS, false);
            //constructor
            fMethodStubsButtons.setSelection(IDX_CONSTRUCTOR, false);
        }
    }

    /**
	 * 	Since Finish was pressed, write widget values to the dialog store so that they
	 *	will persist into the next invocation of this wizard page
	 */
    private void saveWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            settings.put(STORE_SETUP, fMethodStubsButtons.isSelected(IDX_SETUP));
            settings.put(STORE_TEARDOWN, fMethodStubsButtons.isSelected(IDX_TEARDOWN));
            settings.put(STORE_SETUP_CLASS, fMethodStubsButtons.isSelected(IDX_SETUP_CLASS));
            settings.put(STORE_TEARDOWN_CLASS, fMethodStubsButtons.isSelected(IDX_TEARDOWN_CLASS));
            settings.put(STORE_CONSTRUCTOR, fMethodStubsButtons.isSelected(IDX_CONSTRUCTOR));
        }
    }

    /**
	 * Hook method that is called to determine the name of the superclass set for
	 * a JUnit 3 style test case. By default, the name of the JUnit 3 TestCase class is
	 * returned. Implementors can override this behavior to return the name of a
	 * subclass instead.
	 *
	 * @return the fully qualified name of a subclass of the JUnit 3 TestCase class. 
	 *
	 * @since 3.7
	 */
    protected String getJUnit3TestSuperclassName() {
        return JUnitCorePlugin.TEST_SUPERCLASS_NAME;
    }

    /**
	 * Returns the default value for the super class field.
	 * 
	 * @return the default value for the super class field
	 * @since 3.7
	 */
    private String getDefaultSuperClassName() {
        //$NON-NLS-1$
        return isJUnit4() ? "java.lang.Object" : getJUnit3TestSuperclassName();
    }
}
