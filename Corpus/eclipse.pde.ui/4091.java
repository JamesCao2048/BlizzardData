/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.provisional.problems;

import org.eclipse.pde.api.tools.internal.provisional.comparator.IDelta;
import org.eclipse.pde.api.tools.internal.util.Util;

/**
 * API problem types.
 *
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IApiProblemTypes {

    // Compatibility problems
    public static final String API_COMPONENT_REMOVED_TYPE = Util.getDeltaPrefererenceKey(IDelta.API_COMPONENT_ELEMENT_TYPE, IDelta.REMOVED, IDelta.TYPE);

    public static final String API_COMPONENT_REMOVED_API_TYPE = Util.getDeltaPrefererenceKey(IDelta.API_COMPONENT_ELEMENT_TYPE, IDelta.REMOVED, IDelta.API_TYPE);

    // Compatibility problems
    public static final String API_COMPONENT_REMOVED_REEXPORTED_TYPE = Util.getDeltaPrefererenceKey(IDelta.API_COMPONENT_ELEMENT_TYPE, IDelta.REMOVED, IDelta.REEXPORTED_TYPE);

    public static final String API_COMPONENT_REMOVED_REEXPORTED_API_TYPE = Util.getDeltaPrefererenceKey(IDelta.API_COMPONENT_ELEMENT_TYPE, IDelta.REMOVED, IDelta.REEXPORTED_API_TYPE);

    public static final String ANNOTATION_REMOVED_FIELD = Util.getDeltaPrefererenceKey(IDelta.ANNOTATION_ELEMENT_TYPE, IDelta.REMOVED, IDelta.FIELD);

    public static final String ANNOTATION_REMOVED_METHOD = Util.getDeltaPrefererenceKey(IDelta.ANNOTATION_ELEMENT_TYPE, IDelta.REMOVED, IDelta.METHOD);

    public static final String ANNOTATION_REMOVED_TYPE_MEMBER = Util.getDeltaPrefererenceKey(IDelta.ANNOTATION_ELEMENT_TYPE, IDelta.REMOVED, IDelta.TYPE_MEMBER);

    public static final String ANNOTATION_CHANGED_TYPE_CONVERSION = Util.getDeltaPrefererenceKey(IDelta.ANNOTATION_ELEMENT_TYPE, IDelta.CHANGED, IDelta.TYPE_CONVERSION);

    public static final String ANNOTATION_ADDED_METHOD_NO_DEFAULT_VALUE = Util.getDeltaPrefererenceKey(IDelta.ANNOTATION_ELEMENT_TYPE, IDelta.ADDED, IDelta.METHOD_WITHOUT_DEFAULT_VALUE);

    // interface key constant
    public static final String INTERFACE_ADDED_FIELD = Util.getDeltaPrefererenceKey(IDelta.INTERFACE_ELEMENT_TYPE, IDelta.ADDED, IDelta.FIELD);

    public static final String INTERFACE_ADDED_METHOD = Util.getDeltaPrefererenceKey(IDelta.INTERFACE_ELEMENT_TYPE, IDelta.ADDED, IDelta.METHOD);

    public static final String INTERFACE_ADDED_DEFAULT_METHOD = Util.getDeltaPrefererenceKey(IDelta.INTERFACE_ELEMENT_TYPE, IDelta.ADDED, IDelta.DEFAULT_METHOD);

    public static final String INTERFACE_ADDED_RESTRICTIONS = Util.getDeltaPrefererenceKey(IDelta.INTERFACE_ELEMENT_TYPE, IDelta.ADDED, IDelta.RESTRICTIONS);

    public static final String INTERFACE_ADDED_SUPER_INTERFACE_WITH_METHODS = Util.getDeltaPrefererenceKey(IDelta.INTERFACE_ELEMENT_TYPE, IDelta.ADDED, IDelta.SUPER_INTERFACE_WITH_METHODS);

    public static final String INTERFACE_ADDED_TYPE_PARAMETER = Util.getDeltaPrefererenceKey(IDelta.INTERFACE_ELEMENT_TYPE, IDelta.ADDED, IDelta.TYPE_PARAMETER);

    public static final String INTERFACE_REMOVED_TYPE_PARAMETER = Util.getDeltaPrefererenceKey(IDelta.INTERFACE_ELEMENT_TYPE, IDelta.REMOVED, IDelta.TYPE_PARAMETER);

    public static final String INTERFACE_REMOVED_FIELD = Util.getDeltaPrefererenceKey(IDelta.INTERFACE_ELEMENT_TYPE, IDelta.REMOVED, IDelta.FIELD);

    public static final String INTERFACE_REMOVED_METHOD = Util.getDeltaPrefererenceKey(IDelta.INTERFACE_ELEMENT_TYPE, IDelta.REMOVED, IDelta.METHOD);

    public static final String INTERFACE_REMOVED_TYPE_MEMBER = Util.getDeltaPrefererenceKey(IDelta.INTERFACE_ELEMENT_TYPE, IDelta.REMOVED, IDelta.TYPE_MEMBER);

    public static final String INTERFACE_CHANGED_TYPE_CONVERSION = Util.getDeltaPrefererenceKey(IDelta.INTERFACE_ELEMENT_TYPE, IDelta.CHANGED, IDelta.TYPE_CONVERSION);

    public static final String INTERFACE_CHANGED_CONTRACTED_SUPERINTERFACES_SET = Util.getDeltaPrefererenceKey(IDelta.INTERFACE_ELEMENT_TYPE, IDelta.CHANGED, IDelta.CONTRACTED_SUPERINTERFACES_SET);

    // enum key constant
    public static final String ENUM_CHANGED_CONTRACTED_SUPERINTERFACES_SET = Util.getDeltaPrefererenceKey(IDelta.ENUM_ELEMENT_TYPE, IDelta.CHANGED, IDelta.CONTRACTED_SUPERINTERFACES_SET);

    public static final String ENUM_CHANGED_TYPE_CONVERSION = Util.getDeltaPrefererenceKey(IDelta.ENUM_ELEMENT_TYPE, IDelta.CHANGED, IDelta.TYPE_CONVERSION);

    public static final String ENUM_REMOVED_FIELD = Util.getDeltaPrefererenceKey(IDelta.ENUM_ELEMENT_TYPE, IDelta.REMOVED, IDelta.FIELD);

    public static final String ENUM_REMOVED_ENUM_CONSTANT = Util.getDeltaPrefererenceKey(IDelta.ENUM_ELEMENT_TYPE, IDelta.REMOVED, IDelta.ENUM_CONSTANT);

    public static final String ENUM_REMOVED_METHOD = Util.getDeltaPrefererenceKey(IDelta.ENUM_ELEMENT_TYPE, IDelta.REMOVED, IDelta.METHOD);

    public static final String ENUM_REMOVED_TYPE_MEMBER = Util.getDeltaPrefererenceKey(IDelta.ENUM_ELEMENT_TYPE, IDelta.REMOVED, IDelta.TYPE_MEMBER);

    // class key constant
    public static final String CLASS_ADDED_FIELD = Util.getDeltaPrefererenceKey(IDelta.CLASS_ELEMENT_TYPE, IDelta.ADDED, IDelta.FIELD);

    public static final String CLASS_ADDED_METHOD = Util.getDeltaPrefererenceKey(IDelta.CLASS_ELEMENT_TYPE, IDelta.ADDED, IDelta.METHOD);

    public static final String CLASS_ADDED_RESTRICTIONS = Util.getDeltaPrefererenceKey(IDelta.CLASS_ELEMENT_TYPE, IDelta.ADDED, IDelta.RESTRICTIONS);

    public static final String CLASS_ADDED_TYPE_PARAMETER = Util.getDeltaPrefererenceKey(IDelta.CLASS_ELEMENT_TYPE, IDelta.ADDED, IDelta.TYPE_PARAMETER);

    public static final String CLASS_CHANGED_CONTRACTED_SUPERINTERFACES_SET = Util.getDeltaPrefererenceKey(IDelta.CLASS_ELEMENT_TYPE, IDelta.CHANGED, IDelta.CONTRACTED_SUPERINTERFACES_SET);

    public static final String CLASS_CHANGED_NON_ABSTRACT_TO_ABSTRACT = Util.getDeltaPrefererenceKey(IDelta.CLASS_ELEMENT_TYPE, IDelta.CHANGED, IDelta.NON_ABSTRACT_TO_ABSTRACT);

    public static final String CLASS_CHANGED_NON_FINAL_TO_FINAL = Util.getDeltaPrefererenceKey(IDelta.CLASS_ELEMENT_TYPE, IDelta.CHANGED, IDelta.NON_FINAL_TO_FINAL);

    public static final String CLASS_CHANGED_TYPE_CONVERSION = Util.getDeltaPrefererenceKey(IDelta.CLASS_ELEMENT_TYPE, IDelta.CHANGED, IDelta.TYPE_CONVERSION);

    public static final String CLASS_CHANGED_DECREASE_ACCESS = Util.getDeltaPrefererenceKey(IDelta.CLASS_ELEMENT_TYPE, IDelta.CHANGED, IDelta.DECREASE_ACCESS);

    public static final String CLASS_REMOVED_FIELD = Util.getDeltaPrefererenceKey(IDelta.CLASS_ELEMENT_TYPE, IDelta.REMOVED, IDelta.FIELD);

    public static final String CLASS_REMOVED_METHOD = Util.getDeltaPrefererenceKey(IDelta.CLASS_ELEMENT_TYPE, IDelta.REMOVED, IDelta.METHOD);

    public static final String CLASS_REMOVED_CONSTRUCTOR = Util.getDeltaPrefererenceKey(IDelta.CLASS_ELEMENT_TYPE, IDelta.REMOVED, IDelta.CONSTRUCTOR);

    public static final String CLASS_REMOVED_TYPE_MEMBER = Util.getDeltaPrefererenceKey(IDelta.CLASS_ELEMENT_TYPE, IDelta.REMOVED, IDelta.TYPE_MEMBER);

    public static final String CLASS_REMOVED_SUPERCLASS = Util.getDeltaPrefererenceKey(IDelta.CLASS_ELEMENT_TYPE, IDelta.REMOVED, IDelta.SUPERCLASS);

    public static final String CLASS_REMOVED_TYPE_PARAMETER = Util.getDeltaPrefererenceKey(IDelta.CLASS_ELEMENT_TYPE, IDelta.REMOVED, IDelta.TYPE_PARAMETER);

    // field key constant
    public static final String FIELD_ADDED_VALUE = Util.getDeltaPrefererenceKey(IDelta.FIELD_ELEMENT_TYPE, IDelta.ADDED, IDelta.VALUE);

    public static final String FIELD_CHANGED_TYPE = Util.getDeltaPrefererenceKey(IDelta.FIELD_ELEMENT_TYPE, IDelta.CHANGED, IDelta.TYPE);

    public static final String FIELD_CHANGED_VALUE = Util.getDeltaPrefererenceKey(IDelta.FIELD_ELEMENT_TYPE, IDelta.CHANGED, IDelta.VALUE);

    public static final String FIELD_CHANGED_DECREASE_ACCESS = Util.getDeltaPrefererenceKey(IDelta.FIELD_ELEMENT_TYPE, IDelta.CHANGED, IDelta.DECREASE_ACCESS);

    public static final String FIELD_CHANGED_FINAL_TO_NON_FINAL_STATIC_CONSTANT = Util.getDeltaPrefererenceKey(IDelta.FIELD_ELEMENT_TYPE, IDelta.CHANGED, IDelta.FINAL_TO_NON_FINAL_STATIC_CONSTANT);

    public static final String FIELD_CHANGED_NON_FINAL_TO_FINAL = Util.getDeltaPrefererenceKey(IDelta.FIELD_ELEMENT_TYPE, IDelta.CHANGED, IDelta.NON_FINAL_TO_FINAL);

    public static final String FIELD_CHANGED_STATIC_TO_NON_STATIC = Util.getDeltaPrefererenceKey(IDelta.FIELD_ELEMENT_TYPE, IDelta.CHANGED, IDelta.STATIC_TO_NON_STATIC);

    public static final String FIELD_CHANGED_NON_STATIC_TO_STATIC = Util.getDeltaPrefererenceKey(IDelta.FIELD_ELEMENT_TYPE, IDelta.CHANGED, IDelta.NON_STATIC_TO_STATIC);

    public static final String FIELD_REMOVED_VALUE = Util.getDeltaPrefererenceKey(IDelta.FIELD_ELEMENT_TYPE, IDelta.REMOVED, IDelta.VALUE);

    public static final String FIELD_REMOVED_TYPE_ARGUMENT = Util.getDeltaPrefererenceKey(IDelta.FIELD_ELEMENT_TYPE, IDelta.REMOVED, IDelta.TYPE_ARGUMENT);

    // method key constant
    public static final String METHOD_ADDED_TYPE_PARAMETER = Util.getDeltaPrefererenceKey(IDelta.METHOD_ELEMENT_TYPE, IDelta.ADDED, IDelta.TYPE_PARAMETER);

    public static final String METHOD_ADDED_RESTRICTIONS = Util.getDeltaPrefererenceKey(IDelta.METHOD_ELEMENT_TYPE, IDelta.ADDED, IDelta.RESTRICTIONS);

    public static final String METHOD_CHANGED_VARARGS_TO_ARRAY = Util.getDeltaPrefererenceKey(IDelta.METHOD_ELEMENT_TYPE, IDelta.CHANGED, IDelta.VARARGS_TO_ARRAY);

    public static final String METHOD_CHANGED_DECREASE_ACCESS = Util.getDeltaPrefererenceKey(IDelta.METHOD_ELEMENT_TYPE, IDelta.CHANGED, IDelta.DECREASE_ACCESS);

    public static final String METHOD_CHANGED_NON_ABSTRACT_TO_ABSTRACT = Util.getDeltaPrefererenceKey(IDelta.METHOD_ELEMENT_TYPE, IDelta.CHANGED, IDelta.NON_ABSTRACT_TO_ABSTRACT);

    public static final String METHOD_CHANGED_NON_STATIC_TO_STATIC = Util.getDeltaPrefererenceKey(IDelta.METHOD_ELEMENT_TYPE, IDelta.CHANGED, IDelta.NON_STATIC_TO_STATIC);

    public static final String METHOD_CHANGED_STATIC_TO_NON_STATIC = Util.getDeltaPrefererenceKey(IDelta.METHOD_ELEMENT_TYPE, IDelta.CHANGED, IDelta.STATIC_TO_NON_STATIC);

    public static final String METHOD_CHANGED_NON_FINAL_TO_FINAL = Util.getDeltaPrefererenceKey(IDelta.METHOD_ELEMENT_TYPE, IDelta.CHANGED, IDelta.NON_FINAL_TO_FINAL);

    public static final String METHOD_REMOVED_ANNOTATION_DEFAULT_VALUE = Util.getDeltaPrefererenceKey(IDelta.METHOD_ELEMENT_TYPE, IDelta.REMOVED, IDelta.ANNOTATION_DEFAULT_VALUE);

    public static final String METHOD_REMOVED_TYPE_PARAMETER = Util.getDeltaPrefererenceKey(IDelta.METHOD_ELEMENT_TYPE, IDelta.REMOVED, IDelta.TYPE_PARAMETER);

    // constructor key constant
    public static final String CONSTRUCTOR_ADDED_TYPE_PARAMETER = Util.getDeltaPrefererenceKey(IDelta.CONSTRUCTOR_ELEMENT_TYPE, IDelta.ADDED, IDelta.TYPE_PARAMETER);

    public static final String CONSTRUCTOR_CHANGED_VARARGS_TO_ARRAY = Util.getDeltaPrefererenceKey(IDelta.CONSTRUCTOR_ELEMENT_TYPE, IDelta.CHANGED, IDelta.VARARGS_TO_ARRAY);

    public static final String CONSTRUCTOR_CHANGED_DECREASE_ACCESS = Util.getDeltaPrefererenceKey(IDelta.CONSTRUCTOR_ELEMENT_TYPE, IDelta.CHANGED, IDelta.DECREASE_ACCESS);

    public static final String CONSTRUCTOR_REMOVED_TYPE_PARAMETER = Util.getDeltaPrefererenceKey(IDelta.CONSTRUCTOR_ELEMENT_TYPE, IDelta.REMOVED, IDelta.TYPE_PARAMETER);

    public static final String TYPE_PARAMETER_ADDED_CLASS_BOUND = Util.getDeltaPrefererenceKey(IDelta.TYPE_PARAMETER_ELEMENT_TYPE, IDelta.ADDED, IDelta.CLASS_BOUND);

    public static final String TYPE_PARAMETER_CHANGED_CLASS_BOUND = Util.getDeltaPrefererenceKey(IDelta.TYPE_PARAMETER_ELEMENT_TYPE, IDelta.CHANGED, IDelta.CLASS_BOUND);

    public static final String TYPE_PARAMETER_REMOVED_CLASS_BOUND = Util.getDeltaPrefererenceKey(IDelta.TYPE_PARAMETER_ELEMENT_TYPE, IDelta.REMOVED, IDelta.CLASS_BOUND);

    public static final String TYPE_PARAMETER_ADDED_INTERFACE_BOUND = Util.getDeltaPrefererenceKey(IDelta.TYPE_PARAMETER_ELEMENT_TYPE, IDelta.ADDED, IDelta.INTERFACE_BOUND);

    public static final String TYPE_PARAMETER_CHANGED_INTERFACE_BOUND = Util.getDeltaPrefererenceKey(IDelta.TYPE_PARAMETER_ELEMENT_TYPE, IDelta.CHANGED, IDelta.INTERFACE_BOUND);

    public static final String TYPE_PARAMETER_REMOVED_INTERFACE_BOUND = Util.getDeltaPrefererenceKey(IDelta.TYPE_PARAMETER_ELEMENT_TYPE, IDelta.REMOVED, IDelta.INTERFACE_BOUND);

    //$NON-NLS-1$
    public static final String REPORT_API_BREAKAGE_WHEN_MAJOR_VERSION_INCREMENTED = "report_api_breakage_when_major_version_incremented";

    /**
	 * @since 1.1
	 */
    //$NON-NLS-1$
    public static final String AUTOMATICALLY_REMOVE_UNUSED_PROBLEM_FILTERS = "automatically_removed_unused_problem_filters";

    /**
	 * @since 1.1
	 */
    //$NON-NLS-1$
    public static final String FATAL_PROBLEMS = "fatal_problems";

    // Version numbering problems
    //$NON-NLS-1$
    public static final String MISSING_SINCE_TAG = "missing_since_tag";

    //$NON-NLS-1$
    public static final String MALFORMED_SINCE_TAG = "malformed_since_tag";

    //$NON-NLS-1$
    public static final String INVALID_SINCE_TAG_VERSION = "invalid_since_tag_version";

    //$NON-NLS-1$
    public static final String CHANGED_EXECUTION_ENV = "changed_execution_env";

    //$NON-NLS-1$
    public static final String INCOMPATIBLE_API_COMPONENT_VERSION = "incompatible_api_component_version";

    //$NON-NLS-1$
    public static final String INCOMPATIBLE_API_COMPONENT_VERSION_INCLUDE_INCLUDE_MINOR_WITHOUT_API_CHANGE = "incompatible_api_component_version_include_minor_without_api_change";

    //$NON-NLS-1$
    public static final String INCOMPATIBLE_API_COMPONENT_VERSION_INCLUDE_INCLUDE_MAJOR_WITHOUT_BREAKING_CHANGE = "incompatible_api_component_version_include_major_without_breaking_change";

    //$NON-NLS-1$
    public static final String MISSING_DEFAULT_API_BASELINE = "missing_default_api_profile";

    //$NON-NLS-1$
    public static final String REPORT_RESOLUTION_ERRORS_API_COMPONENT = "report_resolution_errors_api_component";

    // API usage problems
    /**
	 * Key for the severity of illegally implementing an interface marked as
	 * '@noimplement'.
	 */
    //$NON-NLS-1$
    public static final String ILLEGAL_IMPLEMENT = "ILLEGAL_IMPLEMENT";

    /**
	 * Key for the severity of illegally extending a member marked as
	 * '@noextend'.
	 */
    //$NON-NLS-1$
    public static final String ILLEGAL_EXTEND = "ILLEGAL_EXTEND";

    /**
	 * Key for the severity of illegally referencing a member marked as
	 * '@noreference'.
	 */
    //$NON-NLS-1$
    public static final String ILLEGAL_REFERENCE = "ILLEGAL_REFERENCE";

    /**
	 * Key for the severity of illegally instantiating a type marked as
	 * '@noinstantiate'.
	 */
    //$NON-NLS-1$
    public static final String ILLEGAL_INSTANTIATE = "ILLEGAL_INSTANTIATE";

    /**
	 * Key for the severity of illegally overriding a member marked as
	 * '@extend'.
	 */
    //$NON-NLS-1$
    public static final String ILLEGAL_OVERRIDE = "ILLEGAL_OVERRIDE";

    /**
	 * Key for leaking an internal type through extension
	 */
    //$NON-NLS-1$
    public static final String LEAK_EXTEND = "LEAK_EXTEND";

    /**
	 * Key for leaking an internal type through implements
	 */
    //$NON-NLS-1$
    public static final String LEAK_IMPLEMENT = "LEAK_IMPLEMENT";

    /**
	 * Key for leaking an internal type through a field declaration
	 */
    //$NON-NLS-1$
    public static final String LEAK_FIELD_DECL = "LEAK_FIELD_DECL";

    /**
	 * Key for leaking an internal type through a method return type
	 */
    //$NON-NLS-1$
    public static final String LEAK_METHOD_RETURN_TYPE = "LEAK_METHOD_RETURN_TYPE";

    /**
	 * Key for leaking an internal type through a method parameter
	 */
    //$NON-NLS-1$
    public static final String LEAK_METHOD_PARAM = "LEAK_METHOD_PARAM";

    /**
	 * Key for an invalid javadoc tag appearing where it does not belong
	 */
    //$NON-NLS-1$
    public static final String INVALID_JAVADOC_TAG = "INVALID_JAVADOC_TAG";

    /**
	 * Key for an annotation appearing where it is not supported
	 *
	 * @since 1.0.600
	 */
    //$NON-NLS-1$
    public static final String INVALID_ANNOTATION = "INVALID_ANNOTATION";

    /**
	 * Key for the severity of referencing a type/method/field that does not
	 * belong to the corresponding EE of the bundle
	 */
    //$NON-NLS-1$
    public static final String INVALID_REFERENCE_IN_SYSTEM_LIBRARIES = "INVALID_REFERENCE_IN_SYSTEM_LIBRARIES";

    /**
	 * Key for the severity of an unused API problem filter.
	 */
    //$NON-NLS-1$
    public static final String UNUSED_PROBLEM_FILTERS = "UNUSED_PROBLEM_FILTERS";

    /**
	 * Key for the severity of no EE descriptions being installed
	 *
	 * @since 1.0.400
	 */
    //$NON-NLS-1$
    public static final String MISSING_EE_DESCRIPTIONS = "MISSING_EE_DESCRIPTIONS";

    /**
	 * Key for the severity of API Use Scan Type breakage problems.
	 */
    //$NON-NLS-1$
    public static final String API_USE_SCAN_TYPE_SEVERITY = "API_USE_SCAN_TYPE_SEVERITY";

    /**
	 * Key for the severity of API Use Scan Method breakage problems.
	 */
    //$NON-NLS-1$
    public static final String API_USE_SCAN_METHOD_SEVERITY = "API_USE_SCAN_METHOD_SEVERITY";

    /**
	 * Key for the severity of API Use Scan Field breakage problems.
	 */
    //$NON-NLS-1$
    public static final String API_USE_SCAN_FIELD_SEVERITY = "API_USE_SCAN_FIELD_SEVERITY";
}
