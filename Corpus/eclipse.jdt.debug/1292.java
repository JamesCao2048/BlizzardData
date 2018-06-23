/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alex Smirnoff   - Bug 289916
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.jdi.Bootstrap;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.SocketUtil;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import com.ibm.icu.text.DateFormat;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;

/**
 * A launcher for debugging Java main classes. Uses JDI to launch a VM in debug 
 * mode.
 */
public class StandardVMDebugger extends StandardVMRunner {

    /**
	 * @since 3.3 OSX environment variable specifying JRE to use
	 */
    //$NON-NLS-1$
    protected static final String JAVA_JVM_VERSION = "JAVA_JVM_VERSION";

    /**
	 * JRE path segment descriptor
	 * 
	 * String equals the word: <code>jre</code>
	 * 
	 * @since 3.3.1
	 */
    //$NON-NLS-1$
    protected static final String JRE = "jre";

    /**
	 * Bin path segment descriptor
	 * 
	 * String equals the word: <code>bin</code>
	 * 
	 * @since 3.3.1
	 */
    //$NON-NLS-1$
    protected static final String BIN = "bin";

    /**
	 * Used to attach to a VM in a separate thread, to allow for cancellation
	 * and detect that the associated System process died before the connect
	 * occurred.
	 */
    class ConnectRunnable implements Runnable {

        private VirtualMachine fVirtualMachine = null;

        private ListeningConnector fConnector = null;

        private Map<String, Connector.Argument> fConnectionMap = null;

        private Exception fException = null;

        /**
		 * Constructs a runnable to connect to a VM via the given connector
		 * with the given connection arguments.
		 * 
		 * @param connector the connector to use
		 * @param map the argument map
		 */
        public  ConnectRunnable(ListeningConnector connector, Map<String, Connector.Argument> map) {
            fConnector = connector;
            fConnectionMap = map;
        }

        @Override
        public void run() {
            try {
                fVirtualMachine = fConnector.accept(fConnectionMap);
            } catch (IOException e) {
                fException = e;
            } catch (IllegalConnectorArgumentsException e) {
                fException = e;
            }
        }

        /**
		 * Returns the VM that was attached to, or <code>null</code> if none.
		 * 
		 * @return the VM that was attached to, or <code>null</code> if none
		 */
        public VirtualMachine getVirtualMachine() {
            return fVirtualMachine;
        }

        /**
		 * Returns any exception that occurred while attaching, or <code>null</code>.
		 * 
		 * @return IOException or IllegalConnectorArgumentsException
		 */
        public Exception getException() {
            return fException;
        }
    }

    /**
	 * Creates a new launcher
	 * @param vmInstance the backing {@link IVMInstall} to launch
	 */
    public  StandardVMDebugger(IVMInstall vmInstance) {
        super(vmInstance);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMRunner#run(org.eclipse.jdt.launching.VMRunnerConfiguration, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
    @Override
    public void run(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
        subMonitor.beginTask(LaunchingMessages.StandardVMDebugger_Launching_VM____1, 4);
        subMonitor.subTask(LaunchingMessages.StandardVMDebugger_Finding_free_socket____2);
        int port = SocketUtil.findFreePort();
        if (port == -1) {
            abort(LaunchingMessages.StandardVMDebugger_Could_not_find_a_free_socket_for_the_debugger_1, null, IJavaLaunchConfigurationConstants.ERR_NO_SOCKET_AVAILABLE);
        }
        subMonitor.worked(1);
        // check for cancellation
        if (monitor.isCanceled()) {
            return;
        }
        subMonitor.subTask(LaunchingMessages.StandardVMDebugger_Constructing_command_line____3);
        String program = constructProgramString(config);
        List<String> arguments = new ArrayList<String>(12);
        arguments.add(program);
        if (fVMInstance instanceof StandardVM && ((StandardVM) fVMInstance).getDebugArgs() != null) {
            //$NON-NLS-1$ //$NON-NLS-2$
            String debugArgString = ((StandardVM) fVMInstance).getDebugArgs().replaceAll("\\Q" + StandardVM.VAR_PORT + "\\E", Integer.toString(port));
            String[] debugArgs = DebugPlugin.parseArguments(debugArgString);
            for (int i = 0; i < debugArgs.length; i++) {
                arguments.add(debugArgs[i]);
            }
        } else {
            // VM arguments are the first thing after the java program so that users can specify
            // options like '-client' & '-server' which are required to be the first options
            double version = getJavaVersion();
            if (version < 1.5) {
                //$NON-NLS-1$
                arguments.add(//$NON-NLS-1$
                "-Xdebug");
                //$NON-NLS-1$
                arguments.add(//$NON-NLS-1$
                "-Xnoagent");
            }
            //check if java 1.4 or greater
            if (version < 1.4) {
                //$NON-NLS-1$
                arguments.add(//$NON-NLS-1$
                "-Djava.compiler=NONE");
            }
            if (version < 1.5) {
                //$NON-NLS-1$
                arguments.add(//$NON-NLS-1$
                "-Xrunjdwp:transport=dt_socket,suspend=y,address=localhost:" + port);
            } else {
                //$NON-NLS-1$
                arguments.add(//$NON-NLS-1$
                "-agentlib:jdwp=transport=dt_socket,suspend=y,address=localhost:" + port);
            }
        }
        String[] allVMArgs = combineVmArgs(config, fVMInstance);
        addArguments(ensureEncoding(launch, allVMArgs), arguments);
        addBootClassPathArguments(arguments, config);
        String[] cp = config.getClassPath();
        int cpidx = -1;
        if (cp.length > 0) {
            cpidx = arguments.size();
            //$NON-NLS-1$
            arguments.add("-classpath");
            arguments.add(convertClassPath(cp));
        }
        arguments.add(config.getClassToLaunch());
        addArguments(config.getProgramArguments(), arguments);
        //With the newer VMs and no backwards compatibility we have to always prepend the current env path (only the runtime one)
        //with a 'corrected' path that points to the location to load the debug dlls from, this location is of the standard JDK installation 
        //format: <jdk path>/jre/bin
        String[] envp = prependJREPath(config.getEnvironment(), new Path(program));
        String[] newenvp = checkClasspath(arguments, cp, envp);
        if (newenvp != null) {
            envp = newenvp;
            arguments.remove(cpidx);
            arguments.remove(cpidx);
        }
        String[] cmdLine = new String[arguments.size()];
        arguments.toArray(cmdLine);
        // check for cancellation
        if (monitor.isCanceled()) {
            return;
        }
        subMonitor.worked(1);
        subMonitor.subTask(LaunchingMessages.StandardVMDebugger_Starting_virtual_machine____4);
        ListeningConnector connector = getConnector();
        if (connector == null) {
            abort(LaunchingMessages.StandardVMDebugger_Couldn__t_find_an_appropriate_debug_connector_2, null, IJavaLaunchConfigurationConstants.ERR_CONNECTOR_NOT_AVAILABLE);
        }
        Map<String, Connector.Argument> map = connector.defaultArguments();
        specifyArguments(map, port);
        Process p = null;
        try {
            try {
                // check for cancellation
                if (monitor.isCanceled()) {
                    return;
                }
                connector.startListening(map);
                File workingDir = getWorkingDir(config);
                String[] newCmdLine = validateCommandLine(launch.getLaunchConfiguration(), cmdLine);
                if (newCmdLine != null) {
                    cmdLine = newCmdLine;
                }
                p = exec(cmdLine, workingDir, envp);
                if (p == null) {
                    return;
                }
                // check for cancellation
                if (monitor.isCanceled()) {
                    p.destroy();
                    return;
                }
                String timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(System.currentTimeMillis()));
                IProcess process = newProcess(launch, p, renderProcessLabel(cmdLine, timestamp), getDefaultProcessMap());
                process.setAttribute(DebugPlugin.ATTR_PATH, cmdLine[0]);
                process.setAttribute(IProcess.ATTR_CMDLINE, renderCommandLine(cmdLine));
                String ltime = launch.getAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP);
                process.setAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP, ltime != null ? ltime : timestamp);
                if (workingDir != null) {
                    process.setAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, workingDir.getAbsolutePath());
                }
                if (envp != null) {
                    Arrays.sort(envp);
                    StringBuffer buff = new StringBuffer();
                    for (int i = 0; i < envp.length; i++) {
                        buff.append(envp[i]);
                        if (i < envp.length - 1) {
                            buff.append('\n');
                        }
                    }
                    process.setAttribute(DebugPlugin.ATTR_ENVIRONMENT, buff.toString());
                }
                subMonitor.worked(1);
                subMonitor.subTask(LaunchingMessages.StandardVMDebugger_Establishing_debug_connection____5);
                int retryCount = 0;
                boolean retry = false;
                do {
                    try {
                        ConnectRunnable runnable = new ConnectRunnable(connector, map);
                        Thread connectThread = new Thread(runnable, "Listening Connector");
                        connectThread.setDaemon(true);
                        connectThread.start();
                        while (connectThread.isAlive()) {
                            if (monitor.isCanceled()) {
                                try {
                                    connector.stopListening(map);
                                } catch (IOException ioe) {
                                }
                                p.destroy();
                                return;
                            }
                            try {
                                p.exitValue();
                                // process has terminated - stop waiting for a connection
                                try {
                                    connector.stopListening(map);
                                } catch (IOException e) {
                                }
                                checkErrorMessage(process);
                            } catch (IllegalThreadStateException e) {
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                            }
                        }
                        Exception ex = runnable.getException();
                        if (ex instanceof IllegalConnectorArgumentsException) {
                            throw (IllegalConnectorArgumentsException) ex;
                        }
                        if (ex instanceof InterruptedIOException) {
                            throw (InterruptedIOException) ex;
                        }
                        if (ex instanceof IOException) {
                            throw (IOException) ex;
                        }
                        VirtualMachine vm = runnable.getVirtualMachine();
                        if (vm != null) {
                            createDebugTarget(config, launch, port, process, vm);
                            subMonitor.worked(1);
                            subMonitor.done();
                        }
                        return;
                    } catch (InterruptedIOException e) {
                        checkErrorMessage(process);
                        IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_VM_CONNECT_TIMEOUT, "", e);
                        IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);
                        retry = false;
                        if (handler == null) {
                            throw new CoreException(status);
                        }
                        Object result = handler.handleStatus(status, this);
                        if (result instanceof Boolean) {
                            retry = ((Boolean) result).booleanValue();
                        }
                        if (!retry && retryCount < 5) {
                            retry = true;
                            retryCount++;
                            LaunchingPlugin.log("Retrying count: " + retryCount);
                        }
                    }
                } while (retry);
            } finally {
                connector.stopListening(map);
            }
        } catch (IOException e) {
            abort(LaunchingMessages.StandardVMDebugger_Couldn__t_connect_to_VM_4, e, IJavaLaunchConfigurationConstants.ERR_CONNECTION_FAILED);
        } catch (IllegalConnectorArgumentsException e) {
            abort(LaunchingMessages.StandardVMDebugger_Couldn__t_connect_to_VM_5, e, IJavaLaunchConfigurationConstants.ERR_CONNECTION_FAILED);
        }
        if (p != null) {
            p.destroy();
        }
    }

    /**
	 * This method performs platform specific operations to modify the runtime path for JREs prior to launching.
	 * Nothing is written back to the original system path.
	 * 
	 * <p>
	 * For Windows:
	 * Prepends the location of the JRE bin directory for the given JDK path to the PATH variable in Windows.
	 * This method assumes that the JRE is located within the JDK install directory
	 * in: <code><JDK install dir>/jre/bin/</code> where the JRE itself would be located 
	 * in: <code><JDK install dir>/bin/</code>  where the JDK itself is located
	 * </p>
	 * <p>
	 * For Mac OS:
	 * Searches for and sets the correct state of the JAVA_VM_VERSION environment variable to ensure it matches
	 * the currently chosen VM of the launch config
	 * </p>
	 * 
	 * @param env the current array of environment variables to run with
	 * @param jdkpath the path to the executable (javaw).
	 * @return the altered JRE path
	 * @since 3.3
	 */
    protected String[] prependJREPath(String[] env, IPath jdkpath) {
        if (Platform.OS_WIN32.equals(Platform.getOS())) {
            IPath jrepath = jdkpath.removeLastSegments(1);
            if (jrepath.lastSegment().equals(BIN)) {
                int count = jrepath.segmentCount();
                if (count > 1 && !jrepath.segment(count - 2).equalsIgnoreCase(JRE)) {
                    jrepath = jrepath.removeLastSegments(1).append(JRE).append(BIN);
                }
            } else {
                jrepath = jrepath.append(JRE).append(BIN);
            }
            if (jrepath.toFile().exists()) {
                String jrestr = jrepath.toOSString();
                if (env == null) {
                    Map<String, String> map = DebugPlugin.getDefault().getLaunchManager().getNativeEnvironment();
                    env = new String[map.size()];
                    String var = null;
                    int index = 0;
                    for (Iterator<String> iter = map.keySet().iterator(); iter.hasNext(); ) {
                        var = iter.next();
                        String value = map.get(var);
                        if (value == null) {
                            //$NON-NLS-1$
                            value = "";
                        }
                        if (//$NON-NLS-1$
                        var.equalsIgnoreCase(//$NON-NLS-1$
                        "path")) {
                            if (value.indexOf(jrestr) == -1) {
                                value = jrestr + ';' + value;
                            }
                        }
                        //$NON-NLS-1$
                        env[index] = //$NON-NLS-1$
                        var + "=" + value;
                        index++;
                    }
                } else {
                    String var = null;
                    int esign = -1;
                    for (int i = 0; i < env.length; i++) {
                        esign = env[i].indexOf('=');
                        if (esign > -1) {
                            var = env[i].substring(0, esign);
                            if (var != null && //$NON-NLS-1$
                            var.equalsIgnoreCase(//$NON-NLS-1$
                            "path")) {
                                if (env[i].indexOf(jrestr) == -1) {
                                    env[i] = var + "=" + jrestr + ';' + (//$NON-NLS-1$ //$NON-NLS-2$
                                    esign == env[i].length() ? //$NON-NLS-1$ //$NON-NLS-2$
                                    "" : //$NON-NLS-1$ //$NON-NLS-2$
                                    env[i].substring(esign + 1));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.prependJREPath(env);
    }

    /**
	 * Creates a new debug target for the given virtual machine and system process
	 * that is connected on the specified port for the given launch.
	 * 
	 * @param config run configuration used to launch the VM
	 * @param launch launch to add the target to
	 * @param port port the VM is connected to
	 * @param process associated system process
	 * @param vm JDI virtual machine
	 * @return the {@link IDebugTarget}
	 */
    protected IDebugTarget createDebugTarget(VMRunnerConfiguration config, ILaunch launch, int port, IProcess process, VirtualMachine vm) {
        return JDIDebugModel.newDebugTarget(launch, vm, renderDebugTarget(config.getClassToLaunch(), port), process, true, false, config.isResumeOnStartup());
    }

    /**
	 * Returns the version of the current VM in use
	 * @return the VM version
	 */
    private double getJavaVersion() {
        String version = null;
        if (fVMInstance instanceof IVMInstall2) {
            version = ((IVMInstall2) fVMInstance).getJavaVersion();
        } else {
            LibraryInfo libInfo = LaunchingPlugin.getLibraryInfo(fVMInstance.getInstallLocation().getAbsolutePath());
            if (libInfo == null) {
                return 0D;
            }
            version = libInfo.getVersion();
        }
        if (version == null) {
            // unknown version
            return 0D;
        }
        //$NON-NLS-1$
        int index = version.indexOf(".");
        //$NON-NLS-1$
        int nextIndex = version.indexOf(".", index + 1);
        try {
            if (index > 0 && nextIndex > index) {
                return Double.parseDouble(version.substring(0, nextIndex));
            }
            return Double.parseDouble(version);
        } catch (NumberFormatException e) {
            return 0D;
        }
    }

    /**
	 * Checks and forwards an error from the specified process
	 * @param process the process to get the error message from
	 * @throws CoreException if a problem occurs
	 */
    protected void checkErrorMessage(IProcess process) throws CoreException {
        IStreamsProxy streamsProxy = process.getStreamsProxy();
        if (streamsProxy != null) {
            String errorMessage = streamsProxy.getErrorStreamMonitor().getContents();
            if (errorMessage.length() == 0) {
                errorMessage = streamsProxy.getOutputStreamMonitor().getContents();
            }
            if (errorMessage.length() != 0) {
                abort(errorMessage, null, IJavaLaunchConfigurationConstants.ERR_VM_LAUNCH_ERROR);
            }
        }
    }

    /**
	 * Allows arguments to be specified
	 * @param map argument map
	 * @param portNumber the port number
	 */
    protected void specifyArguments(Map<String, Connector.Argument> map, int portNumber) {
        // XXX: Revisit - allows us to put a quote (") around the classpath
        //$NON-NLS-1$
        Connector.IntegerArgument port = (Connector.IntegerArgument) map.get("port");
        port.setValue(portNumber);
        //$NON-NLS-1$
        Connector.IntegerArgument timeoutArg = (Connector.IntegerArgument) map.get("timeout");
        if (timeoutArg != null) {
            int timeout = Platform.getPreferencesService().getInt(LaunchingPlugin.ID_PLUGIN, JavaRuntime.PREF_CONNECT_TIMEOUT, JavaRuntime.DEF_CONNECT_TIMEOUT, null);
            timeoutArg.setValue(timeout);
        }
    }

    /**
	 * Returns the default 'com.sun.jdi.SocketListen' connector
	 * @return the {@link ListeningConnector}
	 */
    @SuppressWarnings("nls")
    protected ListeningConnector getConnector() {
        List<ListeningConnector> connectors = Bootstrap.virtualMachineManager().listeningConnectors();
        for (int i = 0; i < connectors.size(); i++) {
            ListeningConnector c = connectors.get(i);
            if ("com.sun.jdi.SocketListen".equals(c.name())) {
                return c;
            }
        }
        return null;
    }
}
