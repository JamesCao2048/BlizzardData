/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.provisional;

import org.eclipse.pde.api.tools.internal.util.Util;

/**
 * Class containing constants and utility methods for restriction modifiers
 *
 * @since 1.0.0
 */
public final class RestrictionModifiers {

    /**
	 * Restriction kind constant indicating there are no restrictions on a type
	 */
    public static final int NO_RESTRICTIONS = 0x0000;

    /**
	 * Restriction kind constant indicating an interface cannot be implemented
	 */
    public static final int NO_IMPLEMENT = 0x0001;

    /**
	 * Restriction kind constant indicating a type or member cannot be extended.
	 */
    public static final int NO_EXTEND = 0x0002;

    /**
	 * Restriction kind constant indicating a class cannot be instantiated.
	 */
    public static final int NO_INSTANTIATE = 0x0004;

    /**
	 * Restriction kind constant indicating a class cannot have a member
	 * referenced
	 */
    public static final int NO_REFERENCE = 0x0008;

    /**
	 * Restriction kind constant indicating a method cannot be overridden
	 */
    public static final int NO_OVERRIDE = 0x0010;

    /**
	 * Bit mask of all restrictions.
	 */
    public static final int ALL_RESTRICTIONS = 0xFF;

    /**
	 * Constructor no instantiating
	 */
    private  RestrictionModifiers() {
    }

    /**
	 * Returns if the no_implement modifier has been set in the specified
	 * modifiers
	 *
	 * @param modifiers the modifiers to resolve
	 * @return if the no_implement modifier has been set in the specified
	 *         modifiers
	 */
    public static final boolean isImplementRestriction(int modifiers) {
        return (modifiers & NO_IMPLEMENT) > 0;
    }

    /**
	 * Returns if the no_extend modifier has been set in the specified modifiers
	 *
	 * @param modifiers the modifiers to resolve
	 * @return if the no_extend modifier has been set in the specified modifiers
	 */
    public static final boolean isExtendRestriction(int modifiers) {
        return (modifiers & NO_EXTEND) > 0;
    }

    /**
	 * Returns if the no_override modifier has been set in the specified
	 * modifiers
	 *
	 * @param modifiers the modifiers to resolve
	 * @return if the no_override modifier has been set in the specified
	 *         modifiers
	 */
    public static final boolean isOverrideRestriction(int modifiers) {
        return (modifiers & NO_OVERRIDE) > 0;
    }

    /**
	 * Returns if the no_instantiate modifier has been set in the specified
	 * modifiers
	 *
	 * @param modifiers the modifiers to resolve
	 * @return if the no_instantiate modifier has been set in the specified
	 *         modifiers
	 */
    public static final boolean isInstantiateRestriction(int modifiers) {
        return (modifiers & NO_INSTANTIATE) > 0;
    }

    /**
	 * Returns if the no_reference modifier has been set in the specified
	 * modifiers
	 *
	 * @param modifiers the modifiers to resolve
	 * @return if the no_reference modifier has been set in the specified
	 *         modifiers
	 */
    public static final boolean isReferenceRestriction(int modifiers) {
        return (modifiers & NO_REFERENCE) > 0;
    }

    /**
	 * Returns if the modifiers indicate no restrictions.
	 *
	 * @param modifiers the modifiers to test
	 * @return if the modifiers indicate no restrictions
	 */
    public static final boolean isUnrestricted(int modifiers) {
        return modifiers == NO_RESTRICTIONS;
    }

    /**
	 * Returns the string representation of the specified restriction(s) or
	 * <code>UNKNOWN_KIND</code> if the kind is unknown.
	 *
	 * @param restrictions the restrictions to get the display string for
	 * @return the string representation for the given restrictions or
	 *         <code>UNKNOWN_KIND</code>
	 * @since 1.0.1
	 */
    public static String getRestrictionText(int restrictions) {
        StringBuffer buffer = new StringBuffer();
        if (restrictions == NO_RESTRICTIONS) {
            //$NON-NLS-1$
            return "NO_RESTRICTIONS";
        }
        if (restrictions == ALL_RESTRICTIONS) {
            //$NON-NLS-1$
            buffer.append("ALL_RESTRICTIONS");
        } else {
            if ((restrictions & NO_EXTEND) > 0) {
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "NO_EXTEND");
            }
            if ((restrictions & NO_IMPLEMENT) > 0) {
                if (buffer.length() > 0) {
                    //$NON-NLS-1$
                    buffer.append(//$NON-NLS-1$
                    " | ");
                }
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "NO_IMPLEMENT");
            }
            if ((restrictions & NO_INSTANTIATE) > 0) {
                if (buffer.length() > 0) {
                    //$NON-NLS-1$
                    buffer.append(//$NON-NLS-1$
                    " | ");
                }
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "NO_INSTANTIATE");
            }
            if ((restrictions & NO_REFERENCE) > 0) {
                if (buffer.length() > 0) {
                    //$NON-NLS-1$
                    buffer.append(//$NON-NLS-1$
                    " | ");
                }
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "NO_REFERENCE");
            }
            if ((restrictions & NO_RESTRICTIONS) > 0) {
                if (buffer.length() > 0) {
                    //$NON-NLS-1$
                    buffer.append(//$NON-NLS-1$
                    " | ");
                }
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "NO_RESTRICTIONS");
            }
            if ((restrictions & NO_OVERRIDE) > 0) {
                if (buffer.length() > 0) {
                    //$NON-NLS-1$
                    buffer.append(//$NON-NLS-1$
                    " | ");
                }
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "NO_OVERRIDE");
            }
        }
        if (buffer.length() == 0) {
            return Util.UNKNOWN_KIND;
        }
        return buffer.toString();
    }
}
