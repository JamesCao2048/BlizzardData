/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Pascal Gruen (pascal.gruen@googlemail.com) - Bug 217994 Run/Debug honors JRE VM args before Launcher VM args
 *******************************************************************************/
package org.eclipse.jdt.launching;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.osgi.service.environment.Constants;

/**
 * Abstract implementation of a VM runner.
 * <p>
 * Clients implementing VM runners should subclass this class.
 * </p>
 * @see IVMRunner
 * @since 2.0
 */
public abstract class AbstractVMRunner implements IVMRunner {

    /**
	 * Throws a core exception with an error status object built from
	 * the given message, lower level exception, and error code.
	 * 
	 * @param message the status message
	 * @param exception lower level exception associated with the
	 *  error, or <code>null</code> if none
	 * @param code error code
	 * @throws CoreException The exception encapsulating the reason for the abort
	 */
    protected void abort(String message, Throwable exception, int code) throws CoreException {
        throw new CoreException(new Status(IStatus.ERROR, getPluginIdentifier(), code, message, exception));
    }

    /**
	 * Returns the identifier of the plug-in this VM runner 
	 * originated from.
	 * 
	 * @return plug-in identifier
	 */
    protected abstract String getPluginIdentifier();

    /**
	 * Executes the given command line using the given working directory
	 * 
	 * @param cmdLine the command line
	 * @param workingDirectory the working directory
	 * @return the {@link Process}
	 * @throws CoreException if the execution fails
	 * @see DebugPlugin#exec(String[], File)
	 */
    protected Process exec(String[] cmdLine, File workingDirectory) throws CoreException {
        cmdLine = quoteWindowsArgs(cmdLine);
        return DebugPlugin.exec(cmdLine, workingDirectory);
    }

    /**
	 * Executes the given command line using the given working directory and environment
	 * 
	 * @param cmdLine the command line
	 * @param workingDirectory the working directory
	 * @param envp the environment
	 * @return the {@link Process}
	 * @throws CoreException is the execution fails
	 * @since 3.0
	 * @see DebugPlugin#exec(String[], File, String[])
	 */
    protected Process exec(String[] cmdLine, File workingDirectory, String[] envp) throws CoreException {
        cmdLine = quoteWindowsArgs(cmdLine);
        return DebugPlugin.exec(cmdLine, workingDirectory, envp);
    }

    private static String[] quoteWindowsArgs(String[] cmdLine) {
        // see https://bugs.eclipse.org/387504 , workaround for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6511002
        if (Platform.getOS().equals(Constants.OS_WIN32)) {
            String[] winCmdLine = new String[cmdLine.length];
            if (cmdLine.length > 0) {
                winCmdLine[0] = cmdLine[0];
            }
            for (int i = 1; i < cmdLine.length; i++) {
                winCmdLine[i] = winQuote(cmdLine[i]);
            }
            cmdLine = winCmdLine;
        }
        return cmdLine;
    }

    private static boolean needsQuoting(String s) {
        int len = s.length();
        if (// empty string has to be quoted
        len == 0)
            return true;
        if (//$NON-NLS-1$
        "\"\"".equals(s))
            // empty quotes must not be quoted again
            return false;
        for (int i = 0; i < len; i++) {
            switch(s.charAt(i)) {
                case ' ':
                case '\t':
                case '\\':
                case '"':
                    return true;
            }
        }
        return false;
    }

    private static String winQuote(String s) {
        if (!needsQuoting(s))
            return s;
        //$NON-NLS-1$ //$NON-NLS-2$
        s = s.replaceAll("([\\\\]*)\"", "$1$1\\\\\"");
        //$NON-NLS-1$ //$NON-NLS-2$
        s = s.replaceAll("([\\\\]*)\\z", "$1$1");
        //$NON-NLS-1$ //$NON-NLS-2$
        return "\"" + s + "\"";
    }

    /**
	 * Returns the given array of strings as a single space-delimited string.
	 * 
	 * @param cmdLine array of strings
	 * @return a single space-delimited string
	 */
    protected String getCmdLineAsString(String[] cmdLine) {
        StringBuffer buff = new StringBuffer();
        for (int i = 0, numStrings = cmdLine.length; i < numStrings; i++) {
            buff.append(cmdLine[i]);
            buff.append(' ');
        }
        return buff.toString().trim();
    }

    /**
	 * Returns the default process attribute map for Java processes.
	 * 
	 * @return default process attribute map for Java processes
	 */
    protected Map<String, String> getDefaultProcessMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(IProcess.ATTR_PROCESS_TYPE, IJavaLaunchConfigurationConstants.ID_JAVA_PROCESS_TYPE);
        return map;
    }

    /**
	 * Returns a new process aborting if the process could not be created.
	 * @param launch the launch the process is contained in
	 * @param p the system process to wrap
	 * @param label the label assigned to the process
	 * @param attributes values for the attribute map
	 * @return the new process
	 * @throws CoreException problems occurred creating the process
	 * @since 3.0
	 */
    protected IProcess newProcess(ILaunch launch, Process p, String label, Map<String, String> attributes) throws CoreException {
        IProcess process = DebugPlugin.newProcess(launch, p, label, attributes);
        if (process == null) {
            p.destroy();
            abort(LaunchingMessages.AbstractVMRunner_0, null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
        }
        return process;
    }

    /**
	 * Combines and returns VM arguments specified by the runner configuration,
	 * with those specified by the VM install, if any.
	 * 
	 * @param configuration runner configuration
	 * @param vmInstall VM install
	 * @return combined VM arguments specified by the runner configuration
	 *  and VM install
	 * @since 3.0
	 */
    protected String[] combineVmArgs(VMRunnerConfiguration configuration, IVMInstall vmInstall) {
        String[] launchVMArgs = configuration.getVMArguments();
        String[] vmVMArgs = vmInstall.getVMArguments();
        if (vmVMArgs == null || vmVMArgs.length == 0) {
            return launchVMArgs;
        }
        // string substitution
        IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
        for (int i = 0; i < vmVMArgs.length; i++) {
            try {
                vmVMArgs[i] = manager.performStringSubstitution(vmVMArgs[i], false);
            } catch (CoreException e) {
                LaunchingPlugin.log(e.getStatus());
            }
        }
        //https://bugs.eclipse.org/bugs/show_bug.cgi?id=217994
        // merge default VM + launcher VM arguments. Make sure to pass launcher arguments in last so that
        // they can take precedence over the default VM args!
        String[] allVMArgs = new String[launchVMArgs.length + vmVMArgs.length];
        System.arraycopy(vmVMArgs, 0, allVMArgs, 0, vmVMArgs.length);
        System.arraycopy(launchVMArgs, 0, allVMArgs, vmVMArgs.length, launchVMArgs.length);
        return allVMArgs;
    }
}
