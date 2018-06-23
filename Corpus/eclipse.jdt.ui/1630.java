/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.text.java.hover;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.dialogs.OptionalMessageDialog;
import org.eclipse.jdt.internal.ui.preferences.JavadocProblemsConfigurationBlock;
import org.eclipse.jdt.internal.ui.preferences.JavadocProblemsPreferencePage;
import org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock;
import org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock.Key;
import org.eclipse.jdt.internal.ui.preferences.ProblemSeveritiesConfigurationBlock;
import org.eclipse.jdt.internal.ui.preferences.ProblemSeveritiesPreferencePage;

/**
 * Action to configure the problem severity of a compiler option.
 *
 * @since 3.4
 */
public class ConfigureProblemSeverityAction extends Action {

    //$NON-NLS-1$
    private static final String CONFIGURE_PROBLEM_SEVERITY_DIALOG_ID = "configure_problem_severity_dialog_id";

    private final IJavaProject fProject;

    private final String fOptionId;

    private final boolean fIsJavadocOption;

    private final IInformationControl fInfoControl;

    public  ConfigureProblemSeverityAction(IJavaProject project, String optionId, boolean isJavadocOption, IInformationControl infoControl) {
        super();
        fProject = project;
        fOptionId = optionId;
        fIsJavadocOption = isJavadocOption;
        fInfoControl = infoControl;
        setImageDescriptor(JavaPluginImages.DESC_ELCL_CONFIGURE_PROBLEM_SEVERITIES);
        setDisabledImageDescriptor(JavaPluginImages.DESC_DLCL_CONFIGURE_PROBLEM_SEVERITIES);
        setToolTipText(JavaHoverMessages.ProblemHover_action_configureProblemSeverity);
    }

    @Override
    public void run() {
        boolean showPropertyPage;
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        if (!hasProjectSpecificOptions()) {
            String message = Messages.format(JavaHoverMessages.ProblemHover_chooseSettingsTypeDialog_message, new Object[] { JavaElementLabels.getElementLabel(fProject, JavaElementLabels.ALL_DEFAULT) });
            String[] buttons = new String[] { JavaHoverMessages.ProblemHover_chooseSettingsTypeDialog_button_project, JavaHoverMessages.ProblemHover_chooseSettingsTypeDialog_button_workspace, IDialogConstants.CANCEL_LABEL };
            int result = OptionalMessageDialog.open(CONFIGURE_PROBLEM_SEVERITY_DIALOG_ID, shell, JavaHoverMessages.ProblemHover_chooseSettingsTypeDialog_title, null, message, MessageDialog.QUESTION, buttons, 0, JavaHoverMessages.ProblemHover_chooseSettingsTypeDialog_checkBox_dontShowAgain);
            if (result == OptionalMessageDialog.NOT_SHOWN) {
                showPropertyPage = false;
            } else if (result == 2 || result == SWT.DEFAULT) {
                return;
            } else if (result == 0) {
                showPropertyPage = true;
            } else {
                showPropertyPage = false;
            }
        } else {
            showPropertyPage = true;
        }
        Map<String, Object> data = new HashMap();
        String pageId;
        if (fIsJavadocOption) {
            if (showPropertyPage) {
                pageId = JavadocProblemsPreferencePage.PROP_ID;
                data.put(JavadocProblemsPreferencePage.DATA_USE_PROJECT_SPECIFIC_OPTIONS, Boolean.TRUE);
            } else {
                pageId = JavadocProblemsPreferencePage.PREF_ID;
            }
            data.put(JavadocProblemsPreferencePage.DATA_SELECT_OPTION_KEY, fOptionId);
            data.put(JavadocProblemsPreferencePage.DATA_SELECT_OPTION_QUALIFIER, JavaCore.PLUGIN_ID);
        } else {
            if (showPropertyPage) {
                pageId = ProblemSeveritiesPreferencePage.PROP_ID;
                data.put(ProblemSeveritiesPreferencePage.USE_PROJECT_SPECIFIC_OPTIONS, Boolean.TRUE);
            } else {
                pageId = ProblemSeveritiesPreferencePage.PREF_ID;
            }
            data.put(ProblemSeveritiesPreferencePage.DATA_SELECT_OPTION_KEY, fOptionId);
            data.put(ProblemSeveritiesPreferencePage.DATA_SELECT_OPTION_QUALIFIER, JavaCore.PLUGIN_ID);
        }
        if (fInfoControl != null) {
            //FIXME: should have protocol to hide, rather than dispose
            fInfoControl.dispose();
        }
        if (showPropertyPage) {
            PreferencesUtil.createPropertyDialogOn(shell, fProject, pageId, null, data).open();
        } else {
            PreferencesUtil.createPreferenceDialogOn(shell, pageId, null, data).open();
        }
    }

    private boolean hasProjectSpecificOptions() {
        Key[] keys = fIsJavadocOption ? JavadocProblemsConfigurationBlock.getKeys() : ProblemSeveritiesConfigurationBlock.getKeys();
        return OptionsConfigurationBlock.hasProjectSpecificOptions(fProject.getProject(), keys, null);
    }
}
