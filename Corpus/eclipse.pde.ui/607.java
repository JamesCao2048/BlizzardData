/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.ui.internal;

import org.eclipse.pde.api.tools.ui.internal.actions.ExportDialog;
import org.eclipse.pde.api.tools.ui.internal.preferences.ApiBaselinePreferencePage;
import org.eclipse.pde.api.tools.ui.internal.preferences.ApiErrorsWarningsPreferencePage;
import org.eclipse.pde.api.tools.ui.internal.preferences.ApiUseScanPreferencePage;
import org.eclipse.pde.api.tools.ui.internal.preferences.ProjectSelectionDialog;
import org.eclipse.pde.api.tools.ui.internal.properties.ApiErrorsWarningsPropertyPage;
import org.eclipse.pde.api.tools.ui.internal.properties.ApiFiltersPropertyPage;
import org.eclipse.pde.api.tools.ui.internal.use.ArchivePatternPage;
import org.eclipse.pde.api.tools.ui.internal.use.DescriptionPatternPage;
import org.eclipse.pde.api.tools.ui.internal.use.PatternSelectionPage;
import org.eclipse.pde.api.tools.ui.internal.use.ReportPatternPage;
import org.eclipse.pde.api.tools.ui.internal.views.APIToolingView;
import org.eclipse.pde.api.tools.ui.internal.wizards.DirectoryBasedApiBaselineWizardPage;
import org.eclipse.pde.api.tools.ui.internal.wizards.ApiToolingSetupWizardPage;
import org.eclipse.pde.api.tools.ui.internal.wizards.CompareToBaselineWizard;
import org.eclipse.pde.api.tools.ui.internal.wizards.JavadocConversionPage;

/**
 * Listing of ids used a help context ids
 *
 * @since 1.0.0
 */
public interface IApiToolsHelpContextIds {

    //$NON-NLS-1$
    public static final String PREFIX = ApiUIPlugin.getPluginIdentifier() + ".";

    /**
	 * Constant representing the help id for the
	 * {@link ApiBaselinePreferencePage}.
	 */
    //$NON-NLS-1$
    public static final String APIBASELINE_PREF_PAGE = PREFIX + "apiprofiles_preference_page";

    /**
	 * Constant representing the help id for the {@link DirectoryBasedApiBaselineWizardPage}
	 */
    //$NON-NLS-1$
    public static final String APIPROFILES_WIZARD_PAGE = PREFIX + "apiprofiles_wizard_page";

    /**
	 * Constant representing the help id for the
	 * {@link ApiToolingSetupWizardPage}
	 */
    //$NON-NLS-1$
    public static final String API_TOOLING_SETUP_WIZARD_PAGE = PREFIX + "api_tooling_setup_wizard_page";

    /**
	 * Constant representing the help id for the {@link JavadocConversionPage}
	 *
	 * @since 1.0.500
	 */
    //$NON-NLS-1$
    public static final String JAVADOC_CONVERSION_WIZARD_PAGE = PREFIX + "javadoc_conversion_wizard_page";

    /**
	 * Constant representing the help id for the
	 * {@link ApiErrorsWarningsPreferencePage}
	 */
    //$NON-NLS-1$
    public static final String APITOOLS_ERROR_WARNING_PREF_PAGE = PREFIX + "apitools_error_warning_preference_page";

    /**
	 * Constant representing the help id for the
	 * {@link ApiErrorsWarningsPropertyPage}
	 */
    //$NON-NLS-1$
    public static final String APITOOLS_ERROR_WARNING_PROP_PAGE = PREFIX + "apitools_error_warning_prop_page";

    /**
	 * Constant representing the help id for the {@link ProjectSelectionDialog}
	 */
    //$NON-NLS-1$
    public static final String APITOOLS_PROJECT_SPECIFIC_SETTINGS_SELECTION_DIALOG = PREFIX + "project_specific_settings_selection_dialog";

    /**
	 * Constant representing the help id for the {@link ApiFiltersPropertyPage}
	 */
    //$NON-NLS-1$
    public static final String APITOOLS_FILTERS_PROPERTY_PAGE = PREFIX + "apitools_filters_property_page";

    /**
	 * Constant representing the help id for the {@link CompareToBaselineWizard}
	 */
    //$NON-NLS-1$
    public static final String API_COMPARE_WIZARD_PAGE = PREFIX + "api_compare_wizard_page";

    /**
	 * Constant representing the help id for the {@link APIToolingView}
	 */
    //$NON-NLS-1$
    public static final String API_TOOLING_VIEW = PREFIX + "api_tooling_view";

    /**
	 * Constant representing the help id for the {@link ExportDialog}
	 */
    //$NON-NLS-1$
    public static final String API_COMPARE_EXPORT_DIALOG = PREFIX + "api_compare_export_dialog";

    /**
	 * Constant representing the help id for the {@link ArchivePatternPage}
	 */
    //$NON-NLS-1$
    public static final String APITOOLS_ARCHIVE_PATTERN_WIZARD_PAGE = PREFIX + "apitools_archive_pattern_wizard_page";

    /**
	 * Constant representing the help id for the {@link ReportPatternPage}
	 */
    //$NON-NLS-1$
    public static final String APITOOLS_REPORT_PATTERN_WIZARD_PAGE = PREFIX + "apitools_report_pattern_wizard_page";

    /**
	 * Constant representing the help id for the {@link DescriptionPatternPage}
	 */
    //$NON-NLS-1$
    public static final String APITOOLS_DESCRIPTION_PATTERN_WIZARD_PAGE = PREFIX + "apitools_description_pattern_wizard_page";

    /**
	 * Constant representing the help id for the {@link PatternSelectionPage}
	 */
    //$NON-NLS-1$
    public static final String APITOOLS_PATTERN_SELECTION_WIZARD_PAGE = PREFIX + "apitools_pattern_selection_wizard_page";

    /**
	 * Constant representing the help id for the
	 * {@link ApiUseScanPreferencePage} .
	 */
    //$NON-NLS-1$
    public static final String APIUSESCANS_PREF_PAGE = PREFIX + "apiusescans_preference_page";

    /**
	 * Constant representing the help id for the Pattern tab on the use scan
	 * reporting tool config
	 */
    //$NON-NLS-1$
    public static final String API_USE_PATTERN_TAB = PREFIX + "api_use_pattern_tab";

    /**
	 * Constant representing the help id for the Use Scan tab on the use scan
	 * reporting tool config
	 */
    //$NON-NLS-1$
    public static final String API_USE_SCAN_TAB = PREFIX + "api_use_main_tab";
}
