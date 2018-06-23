/*******************************************************************************
 *  Copyright (c) 2006, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.refactoring;

import org.eclipse.pde.internal.ui.PDEUIMessages;

public class ContainerMoveParticipant extends ResourceMoveParticipant {

    @Override
    public String getName() {
        return PDEUIMessages.ContainerRenameParticipant_renameFolders;
    }
}
