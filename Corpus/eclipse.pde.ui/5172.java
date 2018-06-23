/*******************************************************************************
 * Copyright (c) 2016 Ecliptical Software Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ecliptical Software Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.ds.internal.annotations;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences defaults = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        defaults.putBoolean(Activator.PREF_ENABLED, false);
        defaults.put(Activator.PREF_PATH, Activator.DEFAULT_PATH);
        defaults.put(Activator.PREF_VALIDATION_ERROR_LEVEL, ValidationErrorLevel.error.toString());
        defaults.put(Activator.PREF_MISSING_UNBIND_METHOD_ERROR_LEVEL, ValidationErrorLevel.error.toString());
    }
}
