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
import org.eclipse.ltk.core.refactoring.resource.MoveResourcesDescriptor;

/**
 * Refactoring contribution for the move resources refactoring.
 *
 * @since 3.4
 */
public final class MoveResourcesRefactoringContribution extends RefactoringContribution {

    /**
	 * Key used for the number of resource to be moved
	 */
    //$NON-NLS-1$
    private static final String ATTRIBUTE_NUMBER_OF_RESOURCES = "resources";

    /**
	 * Key prefix used for the path of the resources to be moved.
	 * <p>
	 * The element arguments are simply distinguished by appending a number to
	 * the argument name, e.g. element1. The indices of this argument are one-based.
	 * </p>
	 */
    //$NON-NLS-1$
    private static final String ATTRIBUTE_ELEMENT = "element";

    /**
	 * Key used for the new resource name
	 */
    //$NON-NLS-1$
    private static final String ATTRIBUTE_DESTINATION = "destination";

    /**
	 * Key used for the 'update references' property
	 */
    //$NON-NLS-1$
    private static final String ATTRIBUTE_UPDATE_REFERENCES = "updateReferences";

    @Override
    public Map<String, String> retrieveArgumentMap(final RefactoringDescriptor descriptor) {
        HashMap<String, String> map = new HashMap();
        if (descriptor instanceof MoveResourcesDescriptor) {
            MoveResourcesDescriptor moveDescriptor = (MoveResourcesDescriptor) descriptor;
            IPath[] paths = moveDescriptor.getResourcePathsToMove();
            String project = moveDescriptor.getProject();
            IPath destinationPath = moveDescriptor.getDestinationPath();
            map.put(ATTRIBUTE_NUMBER_OF_RESOURCES, String.valueOf(paths.length));
            for (int i = 0; i < paths.length; i++) {
                map.put(ATTRIBUTE_ELEMENT + (i + 1), ResourceProcessors.resourcePathToHandle(project, paths[i]));
            }
            map.put(ATTRIBUTE_DESTINATION, ResourceProcessors.resourcePathToHandle(project, destinationPath));
            //$NON-NLS-1$//$NON-NLS-2$
            map.put(ATTRIBUTE_UPDATE_REFERENCES, moveDescriptor.isUpdateReferences() ? "true" : "false");
            return map;
        }
        return null;
    }

    @Override
    public RefactoringDescriptor createDescriptor() {
        return new MoveResourcesDescriptor();
    }

    @Override
    public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment, Map<String, String> arguments, int flags) {
        try {
            int numResources = Integer.parseInt(arguments.get(ATTRIBUTE_NUMBER_OF_RESOURCES));
            if (numResources < 0 || numResources > 100000) {
                throw new //$NON-NLS-1$
                IllegalArgumentException(//$NON-NLS-1$
                "Can not restore MoveResourceDescriptor from map, number of moved elements invalid");
            }
            IPath[] resourcePaths = new IPath[numResources];
            for (int i = 0; i < numResources; i++) {
                String resource = arguments.get(ATTRIBUTE_ELEMENT + String.valueOf(i + 1));
                if (resource == null) {
                    throw new IllegalArgumentException("Can not restore MoveResourceDescriptor from map, resource missing");
                }
                resourcePaths[i] = ResourceProcessors.handleToResourcePath(project, resource);
            }
            String destination = arguments.get(ATTRIBUTE_DESTINATION);
            if (destination == null) {
                throw new //$NON-NLS-1$
                IllegalArgumentException(//$NON-NLS-1$
                "Can not restore MoveResourceDescriptor from map, destination missing");
            }
            IPath destPath = ResourceProcessors.handleToResourcePath(project, destination);
            //$NON-NLS-1$
            boolean updateReferences = "true".equals(arguments.get(ATTRIBUTE_UPDATE_REFERENCES));
            MoveResourcesDescriptor descriptor = new MoveResourcesDescriptor();
            descriptor.setProject(project);
            descriptor.setDescription(description);
            descriptor.setComment(comment);
            descriptor.setFlags(flags);
            descriptor.setResourcePathsToMove(resourcePaths);
            descriptor.setDestinationPath(destPath);
            descriptor.setUpdateReferences(updateReferences);
            return descriptor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Can not restore MoveResourceDescriptor from map");
        }
    }
}
