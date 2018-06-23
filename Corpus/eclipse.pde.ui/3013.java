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
package org.eclipse.pde.internal.ui.editor.plugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.pde.core.*;
import org.eclipse.pde.core.plugin.ISharedExtensionsModel;
import org.eclipse.pde.internal.core.*;
import org.eclipse.pde.internal.core.bundle.*;
import org.eclipse.pde.internal.core.ibundle.IBundleModel;
import org.eclipse.pde.internal.ui.editor.FormOutlinePage;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.build.BuildInputContext;
import org.eclipse.pde.internal.ui.editor.context.InputContext;
import org.eclipse.pde.internal.ui.editor.context.InputContextManager;
import org.eclipse.ui.forms.IFormPart;

public class PluginInputContextManager extends InputContextManager {

    private BundlePluginModelBase bmodel;

    /**
	 *
	 */
    public  PluginInputContextManager(PDEFormEditor editor) {
        super(editor);
    }

    @Override
    public IBaseModel getAggregateModel() {
        if (bmodel != null)
            return bmodel;
        return findPluginModel();
    }

    public IModel getPluginModel() {
        if (bmodel != null)
            return bmodel.getExtensionsModel();
        return findPluginModel();
    }

    @Override
    protected void structureChanged(IFile file, boolean added) {
        // If a plugin.xml file has been added to the project the editor should update
        if (added && ICoreConstants.PLUGIN_FILENAME_DESCRIPTOR.equalsIgnoreCase(file.getName())) {
            IProject project = getCommonProject();
            if (project == null || project.equals(file.getProject())) {
                monitorFile(file);
            }
        }
        super.structureChanged(file, added);
    }

    @Override
    protected void fireContextChange(InputContext context, boolean added) {
        super.fireContextChange(context, added);
        if (context.getId().equals(BundleInputContext.CONTEXT_ID)) {
            // bundle arriving
            if (added)
                bundleAdded(context);
            else
                // bundle going away
                bundleRemoved(context);
        } else if (context.getId().equals(BuildInputContext.CONTEXT_ID)) {
            if (added)
                buildAdded(context);
            else
                buildRemoved(context);
        } else if (context.getId().equals(PluginInputContext.CONTEXT_ID)) {
            if (added)
                pluginAdded(context);
            else
                pluginRemoved(context);
        }
    }

    private void bundleAdded(InputContext bundleContext) {
        IBundleModel model = (IBundleModel) bundleContext.getModel();
        if (model.isFragmentModel())
            bmodel = new BundleFragmentModel();
        else
            bmodel = new BundlePluginModel();
        bmodel.setBundleModel(model);
        syncExtensions();
    }

    private void syncExtensions() {
        IModel emodel = findPluginModel();
        if (emodel != null && emodel instanceof ISharedExtensionsModel) {
            bmodel.setExtensionsModel((ISharedExtensionsModel) emodel);
            transferListeners(emodel, bmodel);
        } else
            bmodel.setExtensionsModel(null);
    }

    private IModel findPluginModel() {
        InputContext pcontext = findContext(PluginInputContext.CONTEXT_ID);
        return (pcontext != null) ? (IModel) pcontext.getModel() : null;
    }

    private void bundleRemoved(InputContext bundleContext) {
        if (bmodel != null) {
            BundlePluginModelBase preserved = bmodel;
            bmodel = null;
            IModel emodel = findPluginModel();
            if (emodel != null)
                transferListeners(preserved, emodel);
        }
    }

    private void transferListeners(IModel source, IModel target) {
        if (source instanceof IModelChangeProviderExtension && target instanceof IModelChangeProviderExtension) {
            IModelChangeProviderExtension smodel = (IModelChangeProviderExtension) source;
            IModelChangeProviderExtension tmodel = (IModelChangeProviderExtension) target;
            // first fire one last event to all the listeners to
            // refresh
            smodel.fireModelChanged(new ModelChangedEvent(smodel, IModelChangedEvent.WORLD_CHANGED, null, null));
            // now pass the listener to the target model
            smodel.transferListenersTo(tmodel, new IModelChangedListenerFilter() {

                @Override
                public boolean accept(IModelChangedListener listener) {
                    if (listener instanceof IFormPart || listener instanceof FormOutlinePage)
                        return true;
                    return false;
                }
            });
        }
    }

    private void pluginAdded(InputContext pluginContext) {
        if (bmodel != null)
            syncExtensions();
    }

    private void pluginRemoved(InputContext pluginContext) {
        if (bmodel != null)
            syncExtensions();
    }

    private void buildAdded(InputContext buildContext) {
    }

    private void buildRemoved(InputContext buildContext) {
    }
}
