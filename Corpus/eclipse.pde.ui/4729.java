/*******************************************************************************
 *  Copyright (c) 2012, 2014 Christian Pontesegger and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.views.imagebrowser.repositories;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.views.imagebrowser.IImageTarget;
import org.eclipse.pde.internal.ui.views.imagebrowser.ImageElement;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.ImageData;

public abstract class AbstractRepository extends Job {

    protected List<ImageElement> mElementsCache = new LinkedList();

    private IImageTarget mTarget;

    public  AbstractRepository(IImageTarget target) {
        super(PDEUIMessages.AbstractRepository_ScanForUI);
        mTarget = target;
    }

    //$NON-NLS-1$ //$NON-NLS-2$
    private static final String[] KNOWN_EXTENSIONS = new String[] { ".gif", ".png" };

    @Override
    protected synchronized IStatus run(IProgressMonitor monitor) {
        while ((mTarget.needsMore()) && (!monitor.isCanceled())) {
            if (mElementsCache.isEmpty()) {
                if (!populateCache(monitor)) {
                    // could not populate cache, giving up
                    return Status.OK_STATUS;
                }
            } else {
                // return 1 image from cache
                mTarget.notifyImage(mElementsCache.remove(0));
            }
        }
        return Status.OK_STATUS;
    }

    public synchronized void clearCache() {
        mElementsCache.clear();
    }

    protected abstract boolean populateCache(IProgressMonitor monitor);

    protected ImageData createImageData(final IFile file) throws CoreException {
        return new ImageData(new BufferedInputStream(file.getContents()));
    }

    protected boolean isImage(final File resource) {
        if (resource.isFile())
            return isImageName(resource.getName());
        return false;
    }

    protected boolean isImageName(final String fileName) {
        for (String extension : KNOWN_EXTENSIONS) {
            if (fileName.regionMatches(true, fileName.length() - extension.length(), extension, 0, extension.length()))
                return true;
        }
        return false;
    }

    protected boolean isJar(final File file) {
        //$NON-NLS-1$
        return file.getName().toLowerCase().endsWith(".jar");
    }

    protected void searchJarFile(final File jarFile, final IProgressMonitor monitor) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(jarFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while ((entries.hasMoreElements()) && (!monitor.isCanceled())) {
                ZipEntry entry = entries.nextElement();
                if (isImageName(entry.getName())) {
                    InputStream inputStream = null;
                    try {
                        inputStream = zipFile.getInputStream(entry);
                        ImageData imageData = new ImageData(inputStream);
                        addImageElement(new ImageElement(imageData, jarFile.getName(), entry.getName()));
                    } catch (IOException e) {
                        PDEPlugin.log(e);
                    } catch (SWTException e) {
                        PDEPlugin.log(new Status(IStatus.ERROR, PDEPlugin.getPluginId(), NLS.bind(PDEUIMessages.AbstractRepository_ErrorLoadingImageFromJar, jarFile.getAbsolutePath(), entry.getName()), e));
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            }
        } catch (ZipException e) {
            PDEPlugin.log(e);
        } catch (IOException e) {
            PDEPlugin.log(e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                }
            }
        }
    }

    protected void searchDirectory(File directory, final IProgressMonitor monitor) {
        //$NON-NLS-1$
        File manifest = new File(directory, "META-INF/MANIFEST.MF");
        if (manifest.exists()) {
            try {
                String pluginName = getPluginName(new FileInputStream(manifest));
                int directoryPathLength = directory.getAbsolutePath().length();
                Collection<File> locations = new HashSet();
                locations.add(directory);
                do {
                    File next = locations.iterator().next();
                    locations.remove(next);
                    for (File resource : next.listFiles()) {
                        if (monitor.isCanceled())
                            return;
                        if (resource.isDirectory()) {
                            locations.add(resource);
                        } else {
                            try {
                                if (isImage(resource)) {
                                    addImageElement(new ImageElement(createImageData((IFile) resource), pluginName, resource.getAbsolutePath().substring(directoryPathLength)));
                                }
                            } catch (Exception e) {
                            }
                        }
                    }
                } while ((!locations.isEmpty()) && (!monitor.isCanceled()));
            } catch (IOException e) {
                PDEPlugin.log(e);
            }
        }
    }

    protected String getPluginName(final InputStream manifest) throws IOException {
        Properties properties = new Properties();
        BufferedInputStream stream = new BufferedInputStream(manifest);
        properties.load(stream);
        stream.close();
        //$NON-NLS-1$
        String property = properties.getProperty("Bundle-SymbolicName");
        if (//$NON-NLS-1$
        property.contains(";"))
            return property.substring(0, property.indexOf(';')).trim();
        return property.trim();
    }

    protected void addImageElement(ImageElement element) {
        mElementsCache.add(element);
    }
}
