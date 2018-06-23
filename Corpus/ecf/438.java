/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.filetransfer.identity;

import java.net.URI;
import java.net.URL;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter;
import org.eclipse.ecf.internal.filetransfer.Activator;
import org.eclipse.ecf.internal.filetransfer.FileTransferDebugOptions;

/**
 * Factory class entry point for creating IFileID instances.
 * 
 */
public class FileIDFactory implements IFileIDFactory {

    protected static FileIDFactory instance = null;

    static {
        instance = new FileIDFactory();
    }

    /**
	 * Get singleton instance
	 * 
	 * @return FileIDFactory singleton instance. Will not be <code>null</code>.
	 * @since 5.0
	 */
    public static FileIDFactory getDefault() {
        return instance;
    }

    /**
	 * Create an IFileID from a Namespace and a String.
	 * 
	 * @param namespace
	 *            the namespace to use to create the IFileID. Can use
	 *            {@link IRetrieveFileTransferContainerAdapter#getRetrieveNamespace()}.
	 *            Must not be <code>null</code>.
	 * @param remoteFile
	 *            the remote filename to use. Must not be <code>null</code>.
	 * @return IFileID instance. Will not return <code>null</code>.
	 * @throws FileCreateException
	 *             thrown if some problem creating IFileID from given namespace
	 *             and filename
	 */
    public IFileID createFileID(Namespace namespace, URL remoteFile) throws FileCreateException {
        return createFileID(namespace, new Object[] { remoteFile });
    }

    /**
	 * Create an IFileID from a Namespace and a String.
	 * 
	 * @param namespace
	 *            the namespace to use to create the IFileID. Can use
	 *            {@link IRetrieveFileTransferContainerAdapter#getRetrieveNamespace()}.
	 *            Must not be null.
	 * @param remoteFile
	 *            the remote filename to use. Must not be <code>null</code>.
	 * @return IFileID instance. Will not return <code>null</code>.
	 * @throws FileCreateException
	 *             thrown if some problem creating IFileID from given namespace
	 *             and filename
	 */
    public IFileID createFileID(Namespace namespace, String remoteFile) throws FileCreateException {
        return createFileID(namespace, new Object[] { remoteFile });
    }

    /**
	 * Create an IFileID from a Namespace and a String.
	 * 
	 * @param namespace
	 *            the namespace to use to create the IFileID. Can use
	 *            {@link IRetrieveFileTransferContainerAdapter#getRetrieveNamespace()}.
	 *            Must not be null.
	 * @param remoteFile
	 *            the remote resource identifier to use. Must not be <code>null</code>.
	 * @return IFileID instance. Will not return <code>null</code>.
	 * @throws FileCreateException
	 *             thrown if some problem creating IFileID from given namespace
	 *             and filename
	 * @since 5.0
	 */
    public IFileID createFileID(Namespace namespace, URI remoteFile) throws FileCreateException {
        return createFileID(namespace, new Object[] { remoteFile });
    }

    /**
	 * Create an IFileID from a Namespace and a String.
	 * 
	 * @param namespace
	 *            the namespace to use to create the IFileID. Can use
	 *            {@link IRetrieveFileTransferContainerAdapter#getRetrieveNamespace()}.
	 *            Must not be <code>null</code>.
	 * @param arguments
	 *            Object [] of arguments to use to create file ID. These
	 *            arguments will be passed to the
	 *            {@link Namespace#createInstance(Object[])} method of the
	 *            appropriate Namespace setup by the provider
	 * @return IFileID instance. Will not return <code>null</code>.
	 * @throws FileCreateException
	 *             thrown if some problem creating IFileID from given namespace
	 *             and filename
	 */
    public IFileID createFileID(Namespace namespace, Object[] arguments) throws FileCreateException {
        //$NON-NLS-1$
        Trace.entering(Activator.PLUGIN_ID, FileTransferDebugOptions.METHODS_ENTERING, this.getClass(), "createFileID", new Object[] { namespace, arguments });
        try {
            IFileID result = (IFileID) IDFactory.getDefault().createID(namespace, arguments);
            //$NON-NLS-1$
            Trace.exiting(Activator.PLUGIN_ID, FileTransferDebugOptions.METHODS_EXITING, this.getClass(), "createFileID", result);
            return result;
        } catch (Exception e) {
            Trace.throwing(Activator.PLUGIN_ID, FileTransferDebugOptions.EXCEPTIONS_THROWING, FileIDFactory.class, "createFileID", e);
            throw new FileCreateException("Exception in createFileID", e);
        }
    }
}
