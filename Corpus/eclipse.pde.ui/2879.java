/*******************************************************************************
 *  Copyright (c) 2007, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.plugin;

import org.eclipse.pde.internal.ui.editor.ILauncherFormPageHelper;
import org.eclipse.pde.internal.ui.editor.PDELauncherFormEditor;

public class PluginLauncherFormPageHelper implements ILauncherFormPageHelper {

    PDELauncherFormEditor fEditor;

    public  PluginLauncherFormPageHelper(PDELauncherFormEditor editor) {
        fEditor = editor;
    }

    @Override
    public Object getLaunchObject() {
        return fEditor.getCommonProject();
    }

    @Override
    public boolean isOSGi() {
        return !((ManifestEditor) fEditor).showExtensionTabs();
    }

    @Override
    public void preLaunch() {
    }
}
