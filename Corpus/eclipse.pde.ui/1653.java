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
package org.eclipse.pde.internal.ui.editor.actions;

import java.util.*;
import org.eclipse.jface.action.*;
import org.eclipse.pde.internal.ui.editor.PDELauncherFormEditor.LauncherAction;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class ActionMenu extends Action implements IMenuCreator {

    List<LauncherAction> fActions;

    Menu fMenu;

    public  ActionMenu(LauncherAction[] actions) {
        fActions = new LinkedList();
        for (int i = 0; i < actions.length; i++) {
            fActions.add(actions[i]);
        }
        if (!fActions.isEmpty()) {
            setToolTipText(fActions.get(0).getToolTipText());
            setImageDescriptor(fActions.get(0).getImageDescriptor());
            if (fActions.size() > 1)
                setMenuCreator(this);
        }
    }

    @Override
    public void run() {
        if (!fActions.isEmpty())
            fActions.get(0).run();
    }

    @Override
    public void dispose() {
        if (fMenu != null) {
            fMenu.dispose();
            fMenu = null;
        }
    }

    @Override
    public Menu getMenu(Control parent) {
        if (fMenu != null)
            fMenu.dispose();
        fMenu = new Menu(parent);
        for (Iterator<LauncherAction> iterator = fActions.iterator(); iterator.hasNext(); ) {
            ActionContributionItem item = new ActionContributionItem(iterator.next());
            item.fill(fMenu, -1);
        }
        return fMenu;
    }

    @Override
    public Menu getMenu(Menu parent) {
        return null;
    }

    /**
	 * Reorders the actions in the menu based on the most recently launched
	 *
	 * @param orderedLauncherIds list of string launcher ids to order the actions by
	 */
    public void updateActionOrder(final List<String> orderedLauncherIds) {
        if (!fActions.isEmpty()) {
            Collections.sort(fActions, new Comparator<LauncherAction>() {

                @Override
                public int compare(LauncherAction o1, LauncherAction o2) {
                    // Entries in the recent launcher list go first
                    String id1 = //$NON-NLS-1$
                    o1.getConfigurationElement().getAttribute(//$NON-NLS-1$
                    "id");
                    String id2 = //$NON-NLS-1$
                    o2.getConfigurationElement().getAttribute(//$NON-NLS-1$
                    "id");
                    int index1 = orderedLauncherIds.indexOf(id1);
                    int index2 = orderedLauncherIds.indexOf(id2);
                    if (index1 == -1 && index2 == -1) {
                        return 0;
                    //						if (id1.contains("pde"))
                    //						org.eclipse.pde.ui.runtimeWorkbenchShortcut
                    //
                    //						String label1 = o1.getConfigurationElement().getAttribute("label"); //$NON-NLS-1$
                    //						String label2 = o2.getConfigurationElement().getAttribute("label"); //$NON-NLS-1$
                    //						return label1.compareTo(label2);
                    }
                    if (index1 == -1) {
                        return 1;
                    }
                    if (index2 == -1) {
                        return -1;
                    }
                    if (index1 <= index2) {
                        return -1;
                    }
                    return 1;
                }
            });
            setToolTipText(fActions.get(0).getToolTipText());
            setImageDescriptor(fActions.get(0).getImageDescriptor());
        }
    }
}
