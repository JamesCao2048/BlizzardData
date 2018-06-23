/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     EclipseSource Corporation - ongoing enhancements
 *******************************************************************************/
package org.eclipse.pde.internal.ui;

import org.eclipse.pde.internal.launching.ILaunchingPreferenceConstants;

/**
 * Listing of constants used in PDE preferences
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPreferenceConstants extends ILaunchingPreferenceConstants {

    // Main preference page
    //$NON-NLS-1$
    public static final String PROP_SHOW_OBJECTS = "Preferences.MainPage.showObjects";

    //$NON-NLS-1$
    public static final String VALUE_USE_IDS = "useIds";

    //$NON-NLS-1$
    public static final String VALUE_USE_NAMES = "useNames";

    //$NON-NLS-1$
    public static final String PROP_SHOW_SOURCE_BUNDLES = "Preferences.MainPage.showSourceBundles";

    //$NON-NLS-1$
    public static final String OVERWRITE_BUILD_FILES_ON_EXPORT = "Preferences.MainPage.overwriteBuildFilesOnExport";

    //$NON-NLS-1$
    public static final String PROP_PROMPT_REMOVE_TARGET = "Preferences.MainPage.promptRemoveTarget";

    //$NON-NLS-1$
    public static final String ADD_TO_JAVA_SEARCH = "Preferences.MainPage.addToJavaSearch";

    /**
	 * Boolean preference whether to display the active target platform in the main window status bar
	 */
    //$NON-NLS-1$
    public static final String SHOW_TARGET_STATUS = "Preferences.MainPage.showTargetStatus";

    // Editor Outline
    //$NON-NLS-1$
    public static final String PROP_OUTLINE_SORTING = "PDEMultiPageContentOutline.SortingAction.isChecked";

    // Editor folding
    //$NON-NLS-1$
    public static final String EDITOR_FOLDING_ENABLED = "editor.folding";

    // Dependencies view
    //$NON-NLS-1$
    public static final String DEPS_VIEW_SHOW_CALLERS = "DependenciesView.show.callers";

    //$NON-NLS-1$
    public static final String DEPS_VIEW_SHOW_LIST = "DependenciesView.show.list";

    //$NON-NLS-1$
    public static final String DEPS_VIEW_SHOW_STATE = "DependenciesView.show.state";

    //Run Configurations - Plug-ins Tab - Feature launching
    //$NON-NLS-1$
    public static final String FEATURE_SORT_COLUMN = "Preferences.RunConfigs.Feature.SortColumn";

    //$NON-NLS-1$
    public static final String FEATURE_SORT_ORDER = "Preferences.RunConfigs.Feature.SortOrder";
}
