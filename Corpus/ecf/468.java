/*
 * Copyright (c) OSGi Alliance (2009, 2014). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osgi.service.remoteserviceadmin;

import org.osgi.annotation.versioning.ProviderType;

/**
 * An Import Registration associates an active proxy service to a remote
 * endpoint.
 * 
 * The Import Registration can be used to delete the proxy associated with an
 * endpoint. It is created with the
 * {@link RemoteServiceAdmin#importService(EndpointDescription)} method.
 * 
 * When this Import Registration has been closed, all methods must return
 * {@code null}.
 * 
 * @ThreadSafe
 * @author $Id: 4dfea3f10524d18ba3b46176b9df33e7de0e7176 $
 */
@ProviderType
public interface ImportRegistration {

    /**
	 * Return the Import Reference for the imported service.
	 * 
	 * @return The Import Reference for this registration, or <code>null</code>
	 *         if this Import Registration is closed.
	 * @throws IllegalStateException When this registration was not properly
	 *         initialized. See {@link #getException()}.
	 */
    ImportReference getImportReference();

    /**
	 * Update the local service represented by this {@link ImportRegistration}.
	 * After this method returns the {@link EndpointDescription} returned via
	 * {@link #getImportReference()} must have been updated.
	 * 
	 * @param endpoint The updated endpoint
	 * 
	 * @return <code>true</code> if the endpoint was successfully updated,
	 *         <code>false</code> otherwise. If the update fails then the
	 *         failure can be retrieved from {@link #getException()}.
	 * 
	 * @throws IllegalStateException When this registration is closed, or if it
	 *         was not properly initialized. See {@link #getException()}.
	 * @throws IllegalArgumentException When the supplied
	 *         {@link EndpointDescription} does not represent the same endpoint
	 *         as this {@link ImportRegistration}.
	 * 
	 * @since 1.1
	 */
    boolean update(EndpointDescription endpoint);

    /**
	 * Close this Import Registration. This must close the connection to the
	 * endpoint and unregister the proxy. After this method returns, all other
	 * methods must return {@code null}.
	 * 
	 * This method has no effect when this registration has already been closed
	 * or is being closed.
	 */
    void close();

    /**
	 * Return the exception for any error during the import process.
	 * 
	 * If the Remote Service Admin for some reasons is unable to properly
	 * initialize this registration, then it must return an exception from this
	 * method. If no error occurred, this method must return {@code null}.
	 * 
	 * The error must be set before this Import Registration is returned.
	 * Asynchronously occurring errors must be reported to the log.
	 * 
	 * @return The exception that occurred during the initialization of this
	 *         registration or {@code null} if no exception occurred.
	 */
    Throwable getException();
}
