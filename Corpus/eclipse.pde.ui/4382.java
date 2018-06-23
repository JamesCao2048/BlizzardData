/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.wizards.plugin;

import java.util.Hashtable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.util.SWTUtil;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;

public class OpenProjectWizardAction extends Action implements ICheatSheetAction {

    /**
	 * @param text
	 */
    public  OpenProjectWizardAction() {
        //$NON-NLS-1$
        super("OpenProject");
    }

    /**
	 * @see IActionDelegate#run(IAction)
	 */
    @Override
    public void run() {
        run(new String[] {}, null);
    }

    @Override
    public void run(String[] params, ICheatSheetManager manager) {
        Hashtable<String, String> defValues = new Hashtable();
        if (params.length > 0)
            defValues.put(NewPluginProjectWizard.DEF_PROJECT_NAME, params[0]);
        if (params.length > 1)
            defValues.put(NewPluginProjectWizard.DEF_TEMPLATE_ID, params[1]);
        NewPluginProjectWizard wizard = new NewPluginProjectWizard();
        wizard.init(PlatformUI.getWorkbench(), new StructuredSelection());
        wizard.init(defValues);
        WizardDialog dialog = new WizardDialog(PDEPlugin.getActiveWorkbenchShell(), wizard);
        dialog.create();
        SWTUtil.setDialogSize(dialog, 500, 500);
        dialog.getShell().setText(wizard.getWindowTitle());
        int result = dialog.open();
        notifyResult(result == Window.OK);
    }
}
