/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.AbstractVMRunner;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.osgi.util.NLS;
import com.ibm.icu.text.DateFormat;

/**
 * A launcher for running Java main classes.
 */
public class StandardVMRunner extends AbstractVMRunner {

    /**
	 * Constant representing the <code>-XstartOnFirstThread</code> VM argument
	 * 
	 * @since 3.2.200
	 */
    //$NON-NLS-1$
    public static final String XSTART_ON_FIRST_THREAD = "-XstartOnFirstThread";

    /**
	 * The VM install instance
	 */
    protected IVMInstall fVMInstance;

    /**
	 * Constructor
	 * @param vmInstance the VM
	 */
    public  StandardVMRunner(IVMInstall vmInstance) {
        fVMInstance = vmInstance;
    }

    /**
	 * Returns the 'rendered' name for the current target
	 * @param classToRun the class
	 * @param host the host name
	 * @return the name for the current target
	 */
    protected String renderDebugTarget(String classToRun, int host) {
        String format = LaunchingMessages.StandardVMRunner__0__at_localhost__1__1;
        return NLS.bind(format, new String[] { classToRun, String.valueOf(host) });
    }

    /**
	 * Returns the 'rendered' name for the specified command line
	 * @param commandLine the command line
	 * @param timestamp the run-at time for the process
	 * @return the name for the process
	 */
    public static String renderProcessLabel(String[] commandLine, String timestamp) {
        String format = LaunchingMessages.StandardVMRunner__0____1___2;
        return NLS.bind(format, new String[] { commandLine[0], timestamp });
    }

    /**
	 * Prepares the command line from the specified array of strings
	 * @param commandLine the command line
	 * @return the command line label
	 */
    protected String renderCommandLine(String[] commandLine) {
        return DebugPlugin.renderArguments(commandLine, null);
    }

    /**
	 * Adds the array of {@link String}s to the given {@link List}
	 * @param args the strings
	 * @param v the list
	 */
    protected void addArguments(String[] args, List<String> v) {
        if (args == null) {
            return;
        }
        for (int i = 0; i < args.length; i++) {
            v.add(args[i]);
        }
    }

    /**
	 * This method allows consumers to have a last look at the command line that will be used 
	 * to start the runner just prior to launching. This method returns the new array of commands
	 * to use to start the runner with or <code>null</code> if the existing command line should be used.
	 * <br><br>
	 * By default this method returns <code>null</code> indicating that the existing command line should be used to launch
	 * 
	 * @param configuration the backing {@link ILaunchConfiguration}
	 * @param cmdLine the existing command line
	 * @return the new command line to launch with or <code>null</code> if the existing one should be used
	 * @since 3.7.0
	 */
    protected String[] validateCommandLine(ILaunchConfiguration configuration, String[] cmdLine) {
        try {
            return wrap(configuration, cmdLine);
        } catch (CoreException ce) {
            LaunchingPlugin.log(ce);
        }
        return cmdLine;
    }

    /**
	 * Adds in special command line arguments if SWT or the <code>-ws</code> directive 
	 * are used
	 * 
	 * @param config the backing {@link ILaunchConfiguration}
	 * @param cmdLine the original VM arguments
	 * @return the (possibly) modified command line to launch with
	 * @throws CoreException 
	 */
    private String[] wrap(ILaunchConfiguration config, String[] cmdLine) throws CoreException {
        if (config != null && Platform.OS_MACOSX.equals(Platform.getOS())) {
            for (int i = 0; i < cmdLine.length; i++) {
                if (//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                "-ws".equals(cmdLine[i]) || cmdLine[i].indexOf("swt.jar") > -1 || cmdLine[i].indexOf("org.eclipse.swt") > -1) {
                    return createSWTlauncher(cmdLine, cmdLine[0], config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_START_ON_FIRST_THREAD, true));
                }
            }
        }
        return cmdLine;
    }

    /**
	 * Returns path to executable.
	 * @param cmdLine the old command line
	 * @param vmVersion the version of the VM
	 * @param startonfirstthread
	 * @return the new command line
	 */
    private String[] createSWTlauncher(String[] cmdLine, String vmVersion, boolean startonfirstthread) {
        // the following property is defined if Eclipse is started via java_swt
        //$NON-NLS-1$
        String java_swt = System.getProperty("org.eclipse.swtlauncher");
        if (java_swt == null) {
            // not started via java_swt -> now we require that the VM supports the "-XstartOnFirstThread" option
            boolean found = false;
            ArrayList<String> args = new ArrayList<String>();
            for (int i = 0; i < cmdLine.length; i++) {
                if (XSTART_ON_FIRST_THREAD.equals(cmdLine[i])) {
                    found = true;
                }
                args.add(cmdLine[i]);
            }
            // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=211625
            if (!found && startonfirstthread) {
                //add it as the first VM argument
                args.add(1, XSTART_ON_FIRST_THREAD);
            }
            return args.toArray(new String[args.size()]);
        }
        try {
            // copy java_swt to /tmp in order to get the app name right
            //$NON-NLS-1$ //$NON-NLS-2$
            Process process = Runtime.getRuntime().exec(new String[] { "/bin/cp", java_swt, "/tmp" });
            process.waitFor();
            //$NON-NLS-1$
            java_swt = "/tmp/java_swt";
        } catch (IOException e) {
        } catch (InterruptedException e) {
        }
        String[] newCmdLine = new String[cmdLine.length + 1];
        int argCount = 0;
        newCmdLine[argCount++] = java_swt;
        //$NON-NLS-1$
        newCmdLine[argCount++] = "-XXvm=" + vmVersion;
        for (int i = 1; i < cmdLine.length; i++) {
            newCmdLine[argCount++] = cmdLine[i];
        }
        return newCmdLine;
    }

    /**
	 * Returns the working directory to use for the launched VM,
	 * or <code>null</code> if the working directory is to be inherited
	 * from the current process.
	 * 
	 * @param config the VM configuration
	 * @return the working directory to use
	 * @exception CoreException if the working directory specified by
	 *  the configuration does not exist or is not a directory
	 */
    protected File getWorkingDir(VMRunnerConfiguration config) throws CoreException {
        String path = config.getWorkingDirectory();
        if (path == null) {
            return null;
        }
        File dir = new File(path);
        if (!dir.isDirectory()) {
            abort(NLS.bind(LaunchingMessages.StandardVMRunner_Specified_working_directory_does_not_exist_or_is_not_a_directory___0__3, new String[] { path }), null, IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST);
        }
        return dir;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractVMRunner#getPluginIdentifier()
	 */
    @Override
    protected String getPluginIdentifier() {
        return LaunchingPlugin.getUniqueIdentifier();
    }

    /**
	 * Construct and return a String containing the full path of a java executable
	 * command such as 'java' or 'javaw.exe'.  If the configuration specifies an
	 * explicit executable, that is used.
	 * 
	 * @param config the runner configuration
	 * @return full path to java executable
	 * @exception CoreException if unable to locate an executable
	 */
    protected String constructProgramString(VMRunnerConfiguration config) throws CoreException {
        // Look for the user-specified java executable command
        String command = null;
        Map<String, Object> map = config.getVMSpecificAttributesMap();
        if (map != null) {
            command = (String) map.get(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND);
        }
        // If no java command was specified, use default executable
        if (command == null) {
            File exe = null;
            if (fVMInstance instanceof StandardVM) {
                exe = ((StandardVM) fVMInstance).getJavaExecutable();
            } else {
                exe = StandardVMType.findJavaExecutable(fVMInstance.getInstallLocation());
            }
            if (exe == null) {
                abort(NLS.bind(LaunchingMessages.StandardVMRunner_Unable_to_locate_executable_for__0__1, new String[] { fVMInstance.getName() }), null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
            } else {
                return exe.getAbsolutePath();
            }
        }
        // Build the path to the java executable.  First try 'bin', and if that
        // doesn't exist, try 'jre/bin'
        String installLocation = fVMInstance.getInstallLocation().getAbsolutePath() + File.separatorChar;
        //$NON-NLS-1$ 		
        File exe = new File(installLocation + "bin" + File.separatorChar + command);
        if (fileExists(exe)) {
            return exe.getAbsolutePath();
        }
        //$NON-NLS-1$
        exe = new File(exe.getAbsolutePath() + ".exe");
        if (fileExists(exe)) {
            return exe.getAbsolutePath();
        }
        //$NON-NLS-1$ //$NON-NLS-2$
        exe = new File(installLocation + "jre" + File.separatorChar + "bin" + File.separatorChar + command);
        if (fileExists(exe)) {
            return exe.getAbsolutePath();
        }
        //$NON-NLS-1$
        exe = new File(exe.getAbsolutePath() + ".exe");
        if (fileExists(exe)) {
            return exe.getAbsolutePath();
        }
        // not found
        abort(NLS.bind(LaunchingMessages.StandardVMRunner_Specified_executable__0__does_not_exist_for__1__4, new String[] { command, fVMInstance.getName() }), null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
        // NOTE: an exception will be thrown - null cannot be returned
        return null;
    }

    /**
	 * Convenience method to determine if the specified file exists or not
	 * @param file the file to check
	 * @return true if the file indeed exists, false otherwise
	 */
    protected boolean fileExists(File file) {
        return file.exists() && file.isFile();
    }

    protected String convertClassPath(String[] cp) {
        int pathCount = 0;
        StringBuffer buf = new StringBuffer();
        if (cp.length == 0) {
            //$NON-NLS-1$
            return "";
        }
        for (int i = 0; i < cp.length; i++) {
            if (pathCount > 0) {
                buf.append(File.pathSeparator);
            }
            buf.append(cp[i]);
            pathCount++;
        }
        return buf.toString();
    }

    /**
	 * This method is used to ensure that the JVM file encoding matches that of the console preference for file encoding.
	 * If the user explicitly declares a file encoding in the launch configuration, then that file encoding is used.
	 * 
	 * @param launch the {@link Launch}
	 * @param vmargs the original listing of JVM arguments
	 * @return the listing of JVM arguments including file encoding if one was not specified
	 * 
	 * @since 3.4
	 */
    protected String[] ensureEncoding(ILaunch launch, String[] vmargs) {
        boolean foundencoding = false;
        for (int i = 0; i < vmargs.length; i++) {
            if (//$NON-NLS-1$
            vmargs[i].startsWith("-Dfile.encoding=")) {
                foundencoding = true;
            }
        }
        if (!foundencoding) {
            String encoding = launch.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING);
            if (encoding == null) {
                return vmargs;
            }
            String[] newargs = new String[vmargs.length + 1];
            System.arraycopy(vmargs, 0, newargs, 0, vmargs.length);
            //$NON-NLS-1$
            newargs[newargs.length - 1] = "-Dfile.encoding=" + encoding;
            return newargs;
        }
        return vmargs;
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
        subMonitor.beginTask(LaunchingMessages.StandardVMRunner_Launching_VM____1, 2);
        subMonitor.subTask(LaunchingMessages.StandardVMRunner_Constructing_command_line____2);
        String program = constructProgramString(config);
        List<String> arguments = new ArrayList<String>();
        arguments.add(program);
        // VM args are the first thing after the java program so that users can specify
        // options like '-client' & '-server' which are required to be the first option
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
        String[] programArgs = config.getProgramArguments();
        addArguments(programArgs, arguments);
        String[] envp = prependJREPath(config.getEnvironment());
        String[] newenvp = checkClasspath(arguments, cp, envp);
        if (newenvp != null) {
            envp = newenvp;
            arguments.remove(cpidx);
            arguments.remove(cpidx);
        }
        String[] cmdLine = new String[arguments.size()];
        arguments.toArray(cmdLine);
        subMonitor.worked(1);
        // check for cancellation
        if (monitor.isCanceled()) {
            return;
        }
        subMonitor.subTask(LaunchingMessages.StandardVMRunner_Starting_virtual_machine____3);
        Process p = null;
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
        subMonitor.done();
    }

    /**
	 * Returns the index in the given array for the CLASSPATH variable
	 * @param env the environment array or <code>null</code>
	 * @return -1 or the index of the CLASSPATH variable
	 * @since 3.6.200
	 */
    int getCPIndex(String[] env) {
        if (env != null) {
            for (int i = 0; i < env.length; i++) {
                if (//$NON-NLS-1$
                env[i].regionMatches(true, 0, "CLASSPATH=", 0, 10)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
	 * Checks to see if the command / classpath needs to be shortened for Windows. Returns the modified
	 * environment or <code>null</code> if no changes are needed.
	 * 
	 * @param args the raw arguments from the runner
	 * @param cp the raw classpath from the runner configuration
	 * @param env the current environment
	 * @return the modified environment or <code>null</code> if no changes were made
	 * @sine 3.6.200
	 */
    String[] checkClasspath(List<String> args, String[] cp, String[] env) {
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            //count the complete command length
            int size = 0;
            for (String arg : args) {
                if (arg != null) {
                    size += arg.length();
                }
            }
            //see http://msdn.microsoft.com/en-us/library/windows/desktop/ms682425(v=vs.85).aspx
            if (size > 32767) {
                StringBuffer newcp = new //$NON-NLS-1$
                StringBuffer(//$NON-NLS-1$
                "CLASSPATH=");
                for (int i = 0; i < cp.length; i++) {
                    newcp.append(cp[i]);
                    newcp.append(File.pathSeparatorChar);
                }
                String[] newenvp = null;
                int index = -1;
                if (env == null) {
                    Map<String, String> nenv = DebugPlugin.getDefault().getLaunchManager().getNativeEnvironment();
                    Entry<String, String> entry = null;
                    newenvp = new String[nenv.size()];
                    int idx = 0;
                    for (Iterator<Entry<String, String>> i = nenv.entrySet().iterator(); i.hasNext(); ) {
                        entry = i.next();
                        String value = entry.getValue();
                        if (value == null) {
                            //$NON-NLS-1$
                            value = "";
                        }
                        String key = entry.getKey();
                        if (//$NON-NLS-1$
                        key.equalsIgnoreCase(//$NON-NLS-1$
                        "CLASSPATH")) {
                            index = idx;
                        }
                        newenvp[idx] = key + '=' + value;
                        idx++;
                    }
                } else {
                    newenvp = env;
                    index = getCPIndex(newenvp);
                }
                if (index < 0) {
                    String[] newenv = new String[newenvp.length + 1];
                    System.arraycopy(newenvp, 0, newenv, 0, newenvp.length);
                    newenv[newenvp.length] = newcp.toString();
                    return newenv;
                }
                newenvp[index] = newcp.toString();
                return newenvp;
            }
        }
        return null;
    }

    /**
	 * Prepends the correct java version variable state to the environment path for Mac VMs
	 * 
	 * @param env the current array of environment variables to run with
	 * @return the new path segments
	 * @since 3.3
	 */
    protected String[] prependJREPath(String[] env) {
        if (Platform.OS_MACOSX.equals(Platform.getOS())) {
            if (fVMInstance instanceof IVMInstall2) {
                IVMInstall2 vm = (IVMInstall2) fVMInstance;
                String javaVersion = vm.getJavaVersion();
                if (javaVersion != null) {
                    if (env == null) {
                        Map<String, String> map = DebugPlugin.getDefault().getLaunchManager().getNativeEnvironmentCasePreserved();
                        if (map.containsKey(StandardVMDebugger.JAVA_JVM_VERSION)) {
                            String[] env2 = new String[map.size()];
                            Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
                            int i = 0;
                            while (iterator.hasNext()) {
                                Entry<String, String> entry = iterator.next();
                                String key = entry.getKey();
                                if (StandardVMDebugger.JAVA_JVM_VERSION.equals(key)) {
                                    //$NON-NLS-1$
                                    env2[i] = key + "=" + javaVersion;
                                } else {
                                    //$NON-NLS-1$
                                    env2[i] = //$NON-NLS-1$
                                    key + "=" + entry.getValue();
                                }
                                i++;
                            }
                            env = env2;
                        }
                    } else {
                        for (int i = 0; i < env.length; i++) {
                            String string = env[i];
                            if (string.startsWith(StandardVMDebugger.JAVA_JVM_VERSION)) {
                                env[i] = //$NON-NLS-1$
                                StandardVMDebugger.JAVA_JVM_VERSION + "=" + javaVersion;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return env;
    }

    /**
	 * Adds arguments to the bootpath
	 * @param arguments the arguments
	 * @param config the VM config
	 */
    protected void addBootClassPathArguments(List<String> arguments, VMRunnerConfiguration config) {
        String[] prependBootCP = null;
        String[] bootCP = null;
        String[] appendBootCP = null;
        Map<String, Object> map = config.getVMSpecificAttributesMap();
        if (map != null) {
            prependBootCP = (String[]) map.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_PREPEND);
            bootCP = (String[]) map.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH);
            appendBootCP = (String[]) map.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_APPEND);
        }
        if (prependBootCP == null && bootCP == null && appendBootCP == null) {
            // use old single attribute instead of new attributes if not specified
            bootCP = config.getBootClassPath();
        }
        if (prependBootCP != null) {
            //$NON-NLS-1$
            arguments.add("-Xbootclasspath/p:" + convertClassPath(prependBootCP));
        }
        if (bootCP != null) {
            if (bootCP.length > 0) {
                //$NON-NLS-1$
                arguments.add(//$NON-NLS-1$
                "-Xbootclasspath:" + convertClassPath(bootCP));
            }
        }
        if (appendBootCP != null) {
            //$NON-NLS-1$
            arguments.add("-Xbootclasspath/a:" + convertClassPath(appendBootCP));
        }
    }
}
