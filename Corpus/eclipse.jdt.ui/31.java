/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.resource;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceDescriptor;

/**
 * Refactoring contribution for the rename resource refactoring.
 *
 * @since 3.4
 */
public final class RenameResourceRefactoringContribution extends RefactoringContribution {

    /**
	 * Key used for the path of the resource to be renamed
	 */
    //$NON-NLS-1$
    private static final String ATTRIBUTE_INPUT = "input";

    /**
	 * Key used for the new resource name
	 */
    //$NON-NLS-1$
    private static final String ATTRIBUTE_NAME = "name";

    /**
	 * Key used for the 'update references' property
	 */
    //$NON-NLS-1$
    private static final String ATTRIBUTE_UPDATE_REFERENCES = "updateReferences";

    @Override
    public Map<String, String> retrieveArgumentMap(final RefactoringDescriptor descriptor) {
        HashMap<String, String> map = new HashMap();
        if (descriptor instanceof RenameResourceDescriptor) {
            RenameResourceDescriptor resourceDescriptor = (RenameResourceDescriptor) descriptor;
            map.put(ATTRIBUTE_INPUT, ResourceProcessors.resourcePathToHandle(descriptor.getProject(), resourceDescriptor.getResourcePath()));
            map.put(ATTRIBUTE_NAME, resourceDescriptor.getNewName());
            //$NON-NLS-1$//$NON-NLS-2$
            map.put(ATTRIBUTE_UPDATE_REFERENCES, resourceDescriptor.isUpdateReferences() ? "true" : "false");
            return map;
        }
        return null;
    }

    @Override
    public RefactoringDescriptor createDescriptor() {
        return new RenameResourceDescriptor();
    }

    @Override
    public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment, Map<String, String> arguments, int flags) {
        String pathString = arguments.get(ATTRIBUTE_INPUT);
        String newName = arguments.get(ATTRIBUTE_NAME);
        //$NON-NLS-1$
        boolean updateReferences = "true".equals(arguments.get(ATTRIBUTE_UPDATE_REFERENCES));
        if (pathString != null && newName != null) {
            IPath path = ResourceProcessors.handleToResourcePath(project, pathString);
            RenameResourceDescriptor descriptor = new RenameResourceDescriptor();
            descriptor.setProject(project);
            descriptor.setDescription(description);
            descriptor.setComment(comment);
            descriptor.setFlags(flags);
            descriptor.setNewName(newName);
            descriptor.setResourcePath(path);
            descriptor.setUpdateReferences(updateReferences);
            return descriptor;
        }
        //$NON-NLS-1$
        throw new IllegalArgumentException("Can not restore RenameResourceDescriptor from map");
    }
}
