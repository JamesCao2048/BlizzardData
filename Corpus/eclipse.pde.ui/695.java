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
package org.eclipse.pde.api.tools.internal.provisional;

/**
 * Interface that defines all the constants used to create the API Tools
 * markers.
 *
 * This interface is not intended to be extended or implemented.
 *
 * @since 1.0.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IApiMarkerConstants {

    /**
	 * Constant representing the name of the 'problem' attribute on API Tools
	 * markers. Value is: <code>problemid</code>
	 */
    //$NON-NLS-1$
    public static final String MARKER_ATTR_PROBLEM_ID = "problemid";

    /**
	 * Constant representing the name of the 'kind' attribute on API Tools
	 * markers. Value is: <code>kind</code>
	 */
    //$NON-NLS-1$
    public static final String MARKER_ATTR_KIND = "kind";

    /**
	 * Constant representing the handle id attribute of a java element. Value
	 * is: <code>org.eclipse.jdt.internal.core.JavaModelManager.handleId</code>
	 */
    //$NON-NLS-1$
    public static final String MARKER_ATTR_HANDLE_ID = "org.eclipse.jdt.internal.core.JavaModelManager.handleId";

    /**
	 * Constant representing the handle id for an
	 * {@link org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblemFilter}
	 * <br>
	 * Value is: <code>filterhandle</code>
	 */
    //$NON-NLS-1$
    public static final String MARKER_ATTR_FILTER_HANDLE_ID = "filterhandle";

    /**
	 * Constant representing the name of the @since tag version attribute on API
	 * Tools markers, or the new value for the bundle version. Value is:
	 * <code>version</code>
	 */
    //$NON-NLS-1$
    public static final String MARKER_ATTR_VERSION = "version";

    /**
	 * Constant representing the name of the message arguments attribute on API
	 * Tools markers. Value is <code>messagearguments</code>
	 */
    //$NON-NLS-1$
    public static final String MARKER_ATTR_MESSAGE_ARGUMENTS = "messagearguments";

    /**
	 * Constant representing the id for the default API baseline problem marker.
	 * Value is: <code>org.eclipse.pde.api.tools.api_profile</code>
	 */
    //$NON-NLS-1$
    public static final String DEFAULT_API_BASELINE_PROBLEM_MARKER = ApiPlugin.PLUGIN_ID + ".api_profile";

    /**
	 * Constant representing the id for the API component resolution problem
	 * marker. Value is:
	 * <code>org.eclipse.pde.api.tools.api_component_resolution</code>
	 */
    //$NON-NLS-1$
    public static final String API_COMPONENT_RESOLUTION_PROBLEM_MARKER = ApiPlugin.PLUGIN_ID + ".api_component_resolution";

    /**
	 * Constant representing the id for the fatal problem marker. Value is:
	 * <code>org.eclipse.pde.api.tools.fatal_problem</code>
	 *
	 * @since 1.1
	 */
    //$NON-NLS-1$
    public static final String FATAL_PROBLEM_MARKER = ApiPlugin.PLUGIN_ID + ".fatal_problem";

    /**
	 * Constant representing the id for the compatibility problem marker. Value
	 * is: <code>org.eclipse.pde.api.tools.compatibility</code>
	 */
    //$NON-NLS-1$
    public static final String COMPATIBILITY_PROBLEM_MARKER = ApiPlugin.PLUGIN_ID + ".compatibility";

    /**
	 * Constant representing the id for the API usage problem marker. Value is:
	 * <code>org.eclipse.pde.api.tools.api_usage</code>
	 */
    //$NON-NLS-1$
    public static final String API_USAGE_PROBLEM_MARKER = ApiPlugin.PLUGIN_ID + ".api_usage";

    /**
	 * Constant representing the id for the unused problem filter problem
	 * markers <br>
	 * Value is: <code>org.eclipse.pde.api.tools.unused_filter</code>
	 */
    //$NON-NLS-1$
    public static final String UNUSED_FILTER_PROBLEM_MARKER = ApiPlugin.PLUGIN_ID + ".unused_filters";

    /**
	 * Constant representing the id for the version numbering problem marker.
	 * Value is: <code>org.eclipse.pde.api.tools.version_numbering</code>
	 */
    //$NON-NLS-1$
    public static final String VERSION_NUMBERING_PROBLEM_MARKER = ApiPlugin.PLUGIN_ID + ".version_numbering";

    /**
	 * Constant representing the id for the missing @since tag problem marker.
	 * Value is: <code>org.eclipse.pde.api.tools.marker.sincetags</code>
	 */
    //$NON-NLS-1$
    public static final String SINCE_TAGS_PROBLEM_MARKER = ApiPlugin.PLUGIN_ID + ".marker.sincetags";

    /**
	 * Constant representing the id for the unsupported Javadoc tag marker
	 * Values is : <code>org.eclipse.pde.api.tools.unsupported_tags</code>
	 */
    //$NON-NLS-1$
    public static final String UNSUPPORTED_TAG_PROBLEM_MARKER = ApiPlugin.PLUGIN_ID + ".unsupported_tags";

    /**
	 * Constant representing the id for the unsupported annotations marker <br>
	 * <br>
	 * Value is: <code>org.eclipse.pde.api.tools.unsupported_annotations</code>
	 */
    //$NON-NLS-1$
    public static final String UNSUPPORTED_ANNOTATION_PROBLEM_MARKER = ApiPlugin.PLUGIN_ID + ".unsupported_annotations";

    /**
	 * Constant representing the name of the 'apiMarkerID' attribute on API
	 * Tools markers. Value is: <code>apiMarkerID</code>
	 */
    //$NON-NLS-1$
    public static final String API_MARKER_ATTR_ID = "apiMarkerID";

    /**
	 * Constant representing the apiMarkerID value for default API baseline
	 * markers. Value is: <code>1</code>
	 */
    public static final int DEFAULT_API_BASELINE_MARKER_ID = 1;

    /**
	 * Constant representing the apiMarkerID value for compatibility markers.
	 * Value is: <code>2</code>
	 */
    public static final int COMPATIBILITY_MARKER_ID = 2;

    /**
	 * Constant representing the apiMarkerID value for API usage markers. Value
	 * is: <code>3</code>
	 */
    public static final int API_USAGE_MARKER_ID = 3;

    /**
	 * Constant representing the apiMarkerID value for version numbering
	 * markers. Value is: <code>4</code>
	 */
    public static final int VERSION_NUMBERING_MARKER_ID = 4;

    /**
	 * Constant representing the apiMarkerID value for since tags markers. Value
	 * is: <code>5</code>
	 */
    public static final int SINCE_TAG_MARKER_ID = 5;

    /**
	 * Constant representing the apiMarkerID value for unsupported javadoc tag
	 * markers Value is: <code>6</code>
	 */
    public static final int UNSUPPORTED_TAG_MARKER_ID = 6;

    /**
	 * Constant representing the apiMarkerID value for duplicate JavaDoc tag
	 * markers Value is: <code>7</code>
	 */
    public static final int DUPLICATE_TAG_MARKER_ID = 7;

    /**
	 * Constant representing the apiMarkerID value for API component resolution
	 * markers. Value is: <code>8</code>
	 */
    public static final int API_COMPONENT_RESOLUTION_MARKER_ID = 8;

    /**
	 * Constant representing the apiMarkerID value for unused problem filter
	 * markers. Value is: <code>9</code>
	 */
    public static final int UNUSED_PROBLEM_FILTER_MARKER_ID = 9;

    /**
	 * Constant representing the apiMarkerID value for a duplicate annotation <br>
	 * <br>
	 * Value is: <code>10</code>
	 *
	 * @since 1.0.600
	 */
    public static final int DUPLICATE_ANNOTATION_MARKER_ID = 10;

    /**
	 * Constant representing the apiMarkerID for unsupported annotation markers. <br>
	 * <br>
	 * Value is: <code>11</code>
	 *
	 * @since 1.0.600
	 */
    public static final int UNSUPPORTED_ANNOTATION_MARKER_ID = 11;

    /**
	 * Constant representing the description for the bundle version marker.
	 * Value is: <code>description</code>
	 */
    //$NON-NLS-1$
    public static final String VERSION_NUMBERING_ATTR_DESCRIPTION = "description";

    /**
	 * Constant representing the type name of the 'problem' attribute on API
	 * Tools markers. Value is: <code>problemTypeName</code>
	 */
    //$NON-NLS-1$
    public static final String MARKER_ATTR_PROBLEM_TYPE_NAME = "problemTypeName";

    /**
	 * Constant representing the id for the API Use Scan breakage problem
	 * marker. Value is:
	 * <code>org.eclipse.pde.api.tools.marker.apiusescan</code>
	 */
    //$NON-NLS-1$
    public static final String API_USESCAN_PROBLEM_MARKER = ApiPlugin.PLUGIN_ID + ".marker.apiusescan";

    /**
	 * Constant representing the id for the additional marker attribute for
	 * storing the type name This will be used to delete the markers when a
	 * missing type becomes available Value is: <code>apiUseScanType</code>
	 */
    //$NON-NLS-1$
    public static final String API_USESCAN_TYPE = "apiUseScanType";
}
