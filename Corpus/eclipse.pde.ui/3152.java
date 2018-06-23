/*******************************************************************************
 *  Copyright (c) 2006, 2016 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.util;

import java.util.HashSet;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * PDELabelUtility
 *
 */
public class PDELabelUtility {

    /**
	 * Get the field label text from the label widget created before the
	 * control (assumption).  Use this method when the control's label is
	 * variable or if the label's text is set elsewhere.  Note:  Hyperlink
	 * label text will not be detected.
	 * @param control
	 */
    public static String getFieldLabel(Control control) {
        // Note:  Does not handle hyperlink labels
        // Get the children of the control's parent
        // The control itself should be in there
        // Assuming the control's label is in there as well
        Control[] controls = control.getParent().getChildren();
        // Ensure has controls
        if (controls.length == 0) {
            return null;
        }
        // Me = control
        // Track the index of myself
        int myIndex = -1;
        // Linearly search controls for myself
        for (int i = 0; i < controls.length; i++) {
            if (controls[i] == control) {
                // Found myself
                myIndex = i;
                break;
            }
        }
        // Ensure I was found and am not the first widget
        if (myIndex <= 0) {
            return null;
        }
        // Assume label is the control created before me
        int labelIndex = myIndex - 1;
        // Ensure the label index points to a label
        if ((controls[labelIndex] instanceof Label) == false) {
            return null;
        }
        // Get the label text
        String labelText = ((Label) controls[labelIndex]).getText();
        // Get rid of the trailing colon (if any)
        int colonIndex = labelText.indexOf(':');
        if (colonIndex >= 0) {
            //$NON-NLS-1$ //$NON-NLS-2$
            labelText = labelText.replaceAll(":", "");
        }
        // Get rid of mnemonics (if any)
        int ampIndex = labelText.indexOf('&');
        if (ampIndex >= 0) {
            //$NON-NLS-1$ //$NON-NLS-2$
            labelText = labelText.replaceAll("&", "");
        }
        return labelText;
    }

    public static String qualifyMessage(String qualification, String message) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(qualification);
        buffer.append(':');
        buffer.append(' ');
        buffer.append(message);
        return buffer.toString();
    }

    private static void addNumberToBase(StringBuffer base, boolean bracketed, HashSet<Integer> set) {
        if (set.size() > 0) {
            // Limit on the number of auto-generated item numbers to check for
            int limit = 100;
            // lowest number accordingly
            if (set.contains(Integer.valueOf(0)) == false) {
            // Use base
            } else {
                for (int x = 1; x < limit; x++) {
                    // existing item
                    if (set.contains(Integer.valueOf(x)) == false) {
                        if (bracketed)
                            //$NON-NLS-1$
                            base.append(" (");
                        base.append(x);
                        if (bracketed)
                            //$NON-NLS-1$
                            base.append(")");
                        break;
                    }
                }
            }
        }
    }

    /**
	 * @param base
	 * @param set
	 * @param title
	 */
    private static void compareTitleWithBase(String base, boolean bracketed, HashSet<Integer> set, String title) {
        // Check to see it the name starts with the prefix
        if (title.toLowerCase().startsWith(base.toLowerCase())) {
            // with brackets add on is: space, (, #, )
            int minSizeNumAddOn = 4;
            if (!bracketed)
                // without brackets and space add on is just number
                minSizeNumAddOn = 1;
            // Determine number
            if (title.length() >= (base.length() + minSizeNumAddOn)) {
                String numPart;
                if (bracketed && title.charAt(base.length()) == ' ') {
                    // We skipped the space since we already checked
                    numPart = title.substring(base.length() + 1);
                } else if (!bracketed) {
                    // without brackets, the numPart is everything after the prefix
                    numPart = title.substring(base.length());
                } else {
                    // We are using brackets and there was no space
                    return;
                }
                if (bracketed) {
                    if (numPart.charAt(0) == '(') {
                        // We are using brackets and confirmed that the open bracket exists
                        // move on to just the number part
                        numPart = numPart.substring(1);
                    } else {
                        // We are using brackets and there is no opening bracket
                        return;
                    }
                }
                // We found an auto-generated name
                StringBuffer buffer = new StringBuffer();
                // Parse the number between the brackets
                for (int j = 0; j < numPart.length(); j++) {
                    char current = numPart.charAt(j);
                    // Make sure its a digit
                    if (Character.isDigit(current)) {
                        buffer.append(current);
                    } else {
                        if (!bracketed || numPart.charAt(j) != ')' || j != numPart.length() - 1) {
                            // with brackets, if this is not the last character it will not conflict
                            return;
                        }
                    // if all conditions passed, this is the last loop, no need to break
                    }
                }
                // Convert the number we found into an actual number
                if (buffer.length() > 0) {
                    set.add(Integer.valueOf(buffer.toString()));
                }
            } else {
                // No number to parse
                // Assume it is just base
                set.add(Integer.valueOf(0));
            }
        }
    }

    public static String generateName(String[] names, String base) {
        return generateName(names, base, true);
    }

    /**
	 * <p>Generates a name that does not conflict with any of the given names with one of two forms:
	 * <ol><li>&quot;&lt;base&gt; (#)&quot;</li><li>&quot;&lt;base&gt;#&quot;</li></ol>
	 * The number will be omitted if the base name alone is available.</p>
	 *
	 * @param names
	 * 			the existing names that should not be conflicted
	 * @param base
	 * 			the base name to add numbers to
	 * @param bracketed
	 * 			if true use the first form, otherwise use the second
	 * @return
	 * 			the non-conflicting name
	 */
    public static String generateName(String[] names, String base, boolean bracketed) {
        StringBuffer result = new StringBuffer(base);
        // Used to track auto-generated numbers used
        HashSet<Integer> set = new HashSet();
        // should be minimal.
        for (int i = 0; i < names.length; i++) {
            PDELabelUtility.compareTitleWithBase(base, bracketed, set, names[i]);
        }
        // Add an auto-generated number
        PDELabelUtility.addNumberToBase(result, bracketed, set);
        return result.toString();
    }

    // Gets the base from a name that was generated by the generateName method.
    public static String getBaseName(String name, boolean bracketed) {
        String result = name;
        if (bracketed) {
            if (result.charAt(result.length() - 1) != ')')
                return name;
            result = result.substring(0, result.length() - 1);
        }
        while (Character.isDigit(result.charAt(result.length() - 1))) result = result.substring(0, result.length() - 1);
        if (bracketed) {
            if (//$NON-NLS-1$
            !result.substring(result.length() - 2).equals(" ("))
                return name;
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }
}
