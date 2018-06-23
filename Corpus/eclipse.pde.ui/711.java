/*******************************************************************************
 * Copyright (c) 2010, 2015 EclipseSource Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     EclipseSource Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.correction.java;

import org.eclipse.core.commands.*;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

public class SearchRepositoriesForIUProposal implements IJavaCompletionProposal {

    private String fPackageName;

    public  SearchRepositoriesForIUProposal(String packageName) {
        fPackageName = packageName;
    }

    @Override
    public int getRelevance() {
        return 0;
    }

    @Override
    public void apply(IDocument document) {
        try {
            IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
            ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
            //$NON-NLS-1$
            Command command = commandService.getCommand("org.eclipse.pde.ui.searchTargetRepositories");
            //$NON-NLS-1$
            IParameter parameter = command.getParameter("org.eclipse.pde.ui.searchTargetRepositories.term");
            Parameterization parameterization = new Parameterization(parameter, fPackageName);
            ParameterizedCommand pc = new ParameterizedCommand(command, new Parameterization[] { parameterization });
            handlerService.executeCommand(pc, null);
        } catch (ExecutionException e) {
            PDEPlugin.log(e);
        } catch (NotDefinedException e) {
            PDEPlugin.log(e);
        } catch (NotEnabledException e) {
            PDEPlugin.log(e);
        } catch (NotHandledException e) {
            PDEPlugin.log(e);
        }
    }

    @Override
    public String getAdditionalProposalInfo() {
        return NLS.bind(PDEUIMessages.SearchRepositoriesForIUProposal_description, fPackageName);
    }

    @Override
    public IContextInformation getContextInformation() {
        return null;
    }

    @Override
    public String getDisplayString() {
        return NLS.bind(PDEUIMessages.SearchRepositoriesForIUProposal_message, fPackageName);
    }

    @Override
    public Image getImage() {
        return PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_SITE_OBJ);
    }

    @Override
    public Point getSelection(IDocument document) {
        return null;
    }
}
