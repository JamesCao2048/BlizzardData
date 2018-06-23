/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.examples.remoteservices.common;

/**
 *
 */
public interface IRemoteEnvironmentInfo {

    /**
	 * Returns all command line arguments specified when the running framework was started.
	 * 
	 * @return the array of command line arguments.
	 */
    public String[] getCommandLineArgs();

    /**
	 * Returns the arguments consumed by the framework implementation itself.  Which
	 * arguments are consumed is implementation specific.
	 * 
	 * @return the array of command line arguments consumed by the framework.
	 */
    public String[] getFrameworkArgs();

    /**
	 * Returns the arguments not consumed by the framework implementation itself.  Which
	 * arguments are consumed is implementation specific.
	 * 
	 * @return the array of command line arguments not consumed by the framework.
	 */
    public String[] getNonFrameworkArgs();

    /**
	 * Returns the string name of the current system architecture.  
	 * The value is a user-defined string if the architecture is 
	 * specified on the command line, otherwise it is the value 
	 * returned by <code>java.lang.System.getProperty("os.arch")</code>.
	 * 
	 * @return the string name of the current system architecture
	 */
    public String getOSArch();

    /**
	 * Returns the string name of the current locale for use in finding files
	 * whose path starts with <code>$nl$</code>.
	 *
	 * @return the string name of the current locale
	 */
    public String getNL();

    /**
	 * Returns the string name of the current operating system for use in finding
	 * files whose path starts with <code>$os$</code>.  Return "unknown" 
	 * if the operating system cannot be determined.
	 * <p>  
	 * The value may indicate one of the operating systems known to the platform
	 * (as specified in <code>org.eclipse.core.runtime.Platform#knownOSValues</code>) 
	 * or a user-defined string if the operating system name is specified on the command line.
	 * </p>
	 *
	 * @return the string name of the current operating system
	 */
    public String getOS();

    /**
	 * Returns the string name of the current window system for use in finding files
	 * whose path starts with <code>$ws$</code>.  Return <code>null</code>
	 * if the window system cannot be determined.
	 *
	 * @return the string name of the current window system or <code>null</code>
	 */
    public String getWS();

    /**
	 * Returns <code>true</code> if the framework is in debug mode and
	 * <code>false</code> otherwise.
	 * 
	 * @return whether or not the framework is in debug mode
	 */
    public Boolean inDebugMode();

    /**
	 * Returns <code>true</code> if the framework is in development mode
	 * and <code>false</code> otherwise.
	 * 
	 * @return whether or not the framework is in development mode
	 */
    public Boolean inDevelopmentMode();
}
