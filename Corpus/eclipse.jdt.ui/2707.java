/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.preferences.cleanup;

import java.util.Iterator;
import java.util.Map;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.preferences.formatter.IProfileVersioner;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager.CustomProfile;

public class CleanUpProfileVersioner implements IProfileVersioner {

    //$NON-NLS-1$
    public static final String PROFILE_KIND = "CleanUpProfile";

    // 3.3M2
    private static final int VERSION_1 = 1;

    // 3.3M3 Added ORGANIZE_IMPORTS
    private static final int VERSION_2 = 2;

    public static final int CURRENT_VERSION = VERSION_2;

    @Override
    public int getFirstVersion() {
        return VERSION_1;
    }

    @Override
    public int getCurrentVersion() {
        return CURRENT_VERSION;
    }

    @Override
    public void update(CustomProfile profile) {
        final Map<String, String> oldSettings = profile.getSettings();
        Map<String, String> newSettings = updateAndComplete(oldSettings, profile.getVersion());
        profile.setVersion(CURRENT_VERSION);
        profile.setSettings(newSettings);
    }

    private Map<String, String> updateAndComplete(Map<String, String> oldSettings, int version) {
        final Map<String, String> newSettings = JavaPlugin.getDefault().getCleanUpRegistry().getDefaultOptions(CleanUpConstants.DEFAULT_CLEAN_UP_OPTIONS).getMap();
        switch(version) {
            case VERSION_1:
                updateFrom1To2(oldSettings);
            //$FALL-THROUGH$
            default:
                for (final Iterator<String> iter = oldSettings.keySet().iterator(); iter.hasNext(); ) {
                    final String key = iter.next();
                    if (!newSettings.containsKey(key))
                        continue;
                    final String value = oldSettings.get(key);
                    if (value != null) {
                        newSettings.put(key, value);
                    }
                }
        }
        return newSettings;
    }

    @Override
    public String getProfileKind() {
        return PROFILE_KIND;
    }

    private static void updateFrom1To2(Map<String, String> settings) {
        CleanUpOptions defaultSettings = JavaPlugin.getDefault().getCleanUpRegistry().getDefaultOptions(CleanUpConstants.DEFAULT_CLEAN_UP_OPTIONS);
        settings.put(CleanUpConstants.ORGANIZE_IMPORTS, defaultSettings.getValue(CleanUpConstants.ORGANIZE_IMPORTS));
    }
}
