/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Fix for bug 376057 - Wildcard suport
 *     for adding features in product configuration editor
 *     Red Hat, Inc - 322352
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 449348
 *******************************************************************************/
package org.eclipse.pde.internal.ui.dialogs;

import java.util.Comparator;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.core.ifeature.*;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

public class FeatureSelectionDialog extends FilteredItemsSelectionDialog {

    //$NON-NLS-1$
    private static final String DIALOG_SETTINGS = "org.eclipse.pde.ui.dialogs.FeatureSelectionDialog";

    private IFeatureModel[] fModels;

    private FeatureSearchItemsFilter filter;

    private final class FeatureDetailsLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            if (element instanceof IFeatureModel) {
                IFeatureModel featureModel = (IFeatureModel) element;
                if (filter.matchesFeatureId(featureModel)) {
                    return NLS.bind(PDEUIMessages.FeatureSelectionDialog_IdMatched, featureModel.getFeature().getId());
                }
                String pluginMatch = filter.matchesPluginId((IFeatureModel) element);
                if (pluginMatch != null) {
                    return NLS.bind(PDEUIMessages.FeatureSelectionDialog_PluginMatched, pluginMatch);
                }
            }
            return super.getText(element);
        }
    }

    private class FeatureSearchItemsFilter extends ItemsFilter {

        public  FeatureSearchItemsFilter() {
            super();
            String pattern = patternMatcher.getPattern();
            if (//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            pattern.indexOf("*") != 0 && pattern.indexOf("?") != 0 && pattern.indexOf(".") != 0) {
                //$NON-NLS-1$
                pattern = //$NON-NLS-1$
                "*" + pattern;
                patternMatcher.setPattern(pattern);
            }
        }

        @Override
        public boolean isConsistentItem(Object item) {
            return true;
        }

        @Override
        public boolean matchItem(Object item) {
            if (item instanceof IFeatureModel) {
                IFeatureModel model = (IFeatureModel) item;
                if (matchesFeatureId(model))
                    return true;
                return matchesPluginId(model) != null;
            }
            return false;
        }

        /**
		 *
		 * @param model
		 * @return id of matched plugin or null if no match
		 */
        public String matchesPluginId(IFeatureModel model) {
            IFeaturePlugin[] plugins = model.getFeature().getPlugins();
            for (int i = 0; i < plugins.length; i++) {
                if (matches(plugins[i].getId())) {
                    return plugins[i].getId();
                }
            }
            return null;
        }

        public boolean matchesFeatureId(IFeatureModel model) {
            String id;
            id = model.getFeature().getId();
            if (matches(id)) {
                return true;
            }
            return false;
        }
    }

    private class FeatureSearchComparator implements Comparator<Object> {

        @Override
        public int compare(Object o1, Object o2) {
            int id1 = getId(o1);
            int id2 = getId(o2);
            if (id1 != id2)
                return id1 - id2;
            return compareSimilarObjects(o1, o2);
        }

        private int getId(Object element) {
            if (element instanceof IFeatureModel) {
                return 100;
            }
            return 0;
        }

        private int compareSimilarObjects(Object o1, Object o2) {
            if (o1 instanceof IFeatureModel && o2 instanceof IFeatureModel) {
                IFeatureModel ipmb1 = (IFeatureModel) o1;
                IFeatureModel ipmb2 = (IFeatureModel) o2;
                return compareFeatures(ipmb1.getFeature(), ipmb2.getFeature());
            }
            return 0;
        }

        private int compareFeatures(IFeature ipmb1, IFeature ipmb2) {
            return ipmb1.getId().compareTo(ipmb2.getId());
        }
    }

    /**
	 * @param parent
	 * @param renderer
	 */
    public  FeatureSelectionDialog(Shell parent, IFeatureModel[] models, boolean multiSelect) {
        super(parent, multiSelect);
        setTitle(PDEUIMessages.FeatureSelectionDialog_title);
        setMessage(PDEUIMessages.FeatureSelectionDialog_message);
        setDetailsLabelProvider(new FeatureDetailsLabelProvider());
        this.fModels = models;
        PDEPlugin.getDefault().getLabelProvider().connect(this);
        setListLabelProvider(PDEPlugin.getDefault().getLabelProvider());
    }

    /*
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IHelpContextIds.FEATURE_SELECTION);
    }

    @Override
    public boolean close() {
        PDEPlugin.getDefault().getLabelProvider().disconnect(this);
        return super.close();
    }

    @Override
    protected Control createExtendedContentArea(Composite parent) {
        return null;
    }

    @Override
    protected IDialogSettings getDialogSettings() {
        IDialogSettings settings = PDEPlugin.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS);
        if (settings == null) {
            settings = PDEPlugin.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS);
        }
        return settings;
    }

    @Override
    protected IStatus validateItem(Object item) {
        //$NON-NLS-1$
        return new Status(IStatus.OK, IPDEUIConstants.PLUGIN_ID, 0, "", null);
    }

    @Override
    protected ItemsFilter createFilter() {
        filter = new FeatureSearchItemsFilter();
        return filter;
    }

    @Override
    protected Comparator<?> getItemsComparator() {
        return new FeatureSearchComparator();
    }

    @Override
    protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter, IProgressMonitor progressMonitor) throws CoreException {
        for (int i = 0; i < fModels.length; i++) {
            contentProvider.add(fModels[i], itemsFilter);
            progressMonitor.worked(1);
        }
        progressMonitor.done();
    }

    @Override
    public String getElementName(Object item) {
        if (item instanceof IFeatureModel) {
            IFeatureModel model = (IFeatureModel) item;
            return model.getFeature().getId();
        }
        return null;
    }
}
