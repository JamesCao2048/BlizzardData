/*******************************************************************************
 *  Copyright (c) 2005, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.plugin;

import org.eclipse.core.resources.*;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.ResourceUtil;

public class ManifestEditorMatchingStrategy implements IEditorMatchingStrategy {

    @Override
    public boolean matches(IEditorReference editorRef, IEditorInput input) {
        IFile inputFile = ResourceUtil.getFile(input);
        if (input instanceof IFileEditorInput && inputFile != null) {
            try {
                // a positive match if there is an editor already open on the same file
                if (input.equals(editorRef.getEditorInput()))
                    return true;
                // a quick no-match if the project of the file being opened is not the
                // same as the project of the file associated with the open editor
                IFile editorFile = ResourceUtil.getFile(editorRef.getEditorInput());
                if (editorFile == null || !inputFile.getProject().equals(editorFile.getProject()))
                    return false;
                // only if it is colocated with the plugin.xml/fragment.xml file already open
                if (inputFile.getName().equals(ICoreConstants.MANIFEST_FILENAME)) {
                    IContainer parent = inputFile.getParent();
                    return parent instanceof IFolder && //$NON-NLS-1$
                    parent.getName().equals(//$NON-NLS-1$
                    "META-INF") && parent.getParent().equals(editorFile.getParent());
                }
                // only if the editor that is open is associated with a colocated MANIFEST.MF
                if (inputFile.getName().equals(ICoreConstants.PLUGIN_FILENAME_DESCRIPTOR) || inputFile.getName().equals(ICoreConstants.FRAGMENT_FILENAME_DESCRIPTOR)) {
                    IContainer parent = inputFile.getParent();
                    IFile file = parent.getFile(ICoreConstants.MANIFEST_PATH);
                    return file.exists() && editorFile.equals(file);
                }
                // if an editor is already open on a sibling plugin.xml/fragment.xml or a META-INF/MANIFEST.MF
                if (inputFile.getName().equals(ICoreConstants.BUILD_FILENAME_DESCRIPTOR)) {
                    IContainer parent = inputFile.getParent();
                    if (parent.equals(editorFile.getParent())) {
                        return editorFile.getName().equals(ICoreConstants.PLUGIN_FILENAME_DESCRIPTOR) || editorFile.getName().equals(ICoreConstants.FRAGMENT_FILENAME_DESCRIPTOR);
                    }
                    IFile file = parent.getFile(ICoreConstants.MANIFEST_PATH);
                    return file.exists() && editorFile.equals(file);
                }
            } catch (PartInitException e) {
                return false;
            }
        } else if (input instanceof IStorageEditorInput) {
            try {
                IEditorInput existing = editorRef.getEditorInput();
                return input.equals(existing);
            } catch (PartInitException e1) {
            }
        }
        return false;
    }
}
