/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching;

import org.eclipse.jdt.internal.launching.LaunchingPlugin;

/**
 * Constant definitions for Java launch configurations.
 * <p>
 * Constant definitions only
 * </p>
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IJavaLaunchConfigurationConstants {

    /**
	 * Identifier for the Local Java Application launch configuration type
	 * (value <code>"org.eclipse.jdt.launching.localJavaApplication"</code>).
	 */
    //$NON-NLS-1$
    public static final String ID_JAVA_APPLICATION = LaunchingPlugin.getUniqueIdentifier() + ".localJavaApplication";

    /**
	 * Identifier for the Remote Java Application launch configuration type
	 * (value <code>"org.eclipse.jdt.launching.remoteJavaApplication"</code>).
	 */
    //$NON-NLS-1$	
    public static final String ID_REMOTE_JAVA_APPLICATION = LaunchingPlugin.getUniqueIdentifier() + ".remoteJavaApplication";

    /**
	 * Identifier for the Java Applet launch configuration type
	 * (value <code>"org.eclipse.jdt.launching.javaApplet"</code>).
	 */
    //$NON-NLS-1$	
    public static final String ID_JAVA_APPLET = LaunchingPlugin.getUniqueIdentifier() + ".javaApplet";

    /**
	 * Identifier for the standard Socket Attaching VM connector
	 * (value <code>"org.eclipse.jdt.launching.socketAttachConnector"</code>).
	 */
    //$NON-NLS-1$	
    public static final String ID_SOCKET_ATTACH_VM_CONNECTOR = LaunchingPlugin.getUniqueIdentifier() + ".socketAttachConnector";

    /**
	 * Identifier for the standard Socket Listening VM connector
	 * (value <code>"org.eclipse.jdt.launching.socketListenConnector"</code>).
	 * @since 3.4
	 */
    //$NON-NLS-1$
    public static final String ID_SOCKET_LISTEN_VM_CONNECTOR = LaunchingPlugin.getUniqueIdentifier() + ".socketListenConnector";

    /**
	 * Identifier for the java process type, which is annotated on processes created
	 * by the local java application launch delegate.
	 * 
	 * (value <code>"java"</code>).
	 */
    //$NON-NLS-1$ 
    public static final String ID_JAVA_PROCESS_TYPE = "java";

    /**
	 * Launch configuration attribute key. The value is a name of
	 * a Java project associated with a Java launch configuration.
	 */
    /*
	 * This constant is referenced from an upper-layer bundle
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget#prepareBreakpointsSearchScope()
	 */
    //$NON-NLS-1$
    public static final String ATTR_PROJECT_NAME = LaunchingPlugin.getUniqueIdentifier() + ".PROJECT_ATTR";

    /**
	 * Launch configuration attribute key. The value is a fully qualified name
	 * of a main type to launch.
	 */
    //$NON-NLS-1$
    public static final String ATTR_MAIN_TYPE_NAME = LaunchingPlugin.getUniqueIdentifier() + ".MAIN_TYPE";

    /**
	 * Launch configuration attribute key. The value is a boolean specifying
	 * whether execution should stop when main is entered. The default value
	 * is <code>false</code>.
	 * 
	 * @since 2.1
	 */
    //$NON-NLS-1$
    public static final String ATTR_STOP_IN_MAIN = LaunchingPlugin.getUniqueIdentifier() + ".STOP_IN_MAIN";

    /**
	 * Launch configuration attribute key. The value is a string specifying
	 * program arguments for a Java launch configuration, as they should appear
	 * on the command line.
	 */
    //$NON-NLS-1$
    public static final String ATTR_PROGRAM_ARGUMENTS = LaunchingPlugin.getUniqueIdentifier() + ".PROGRAM_ARGUMENTS";

    /**
	 * Launch configuration attribute key. The value is a string specifying
	 * VM arguments for a Java launch configuration, as they should appear
	 * on the command line.
	 */
    //$NON-NLS-1$
    public static final String ATTR_VM_ARGUMENTS = LaunchingPlugin.getUniqueIdentifier() + ".VM_ARGUMENTS";

    /**
	 * Launch configuration attribute key. The value is a string specifying a
	 * path to the working directory to use when launching a local VM.
	 * When specified as an absolute path, the path represents a path in the local
	 * file system. When specified as a full path, the path represents a workspace
	 * relative path. When unspecified, the working directory defaults to the project
	 * associated with a launch configuration. When no project is associated with a
	 * launch configuration, the working directory is inherited from the current
	 * process.
	 */
    //$NON-NLS-1$
    public static final String ATTR_WORKING_DIRECTORY = LaunchingPlugin.getUniqueIdentifier() + ".WORKING_DIRECTORY";

    /**
	 * Launch configuration attribute key. The value is a path identifying the JRE used
	 * when launching a local VM. The path is a classpath container corresponding
	 * to the <code>JavaRuntime.JRE_CONTAINER</code> classpath container.
	 * <p>
	 * When unspecified the default JRE for a launch configuration is used (which is the
	 * JRE associated with the project being launched, or the workspace default JRE when
	 * no project is associated with a configuration). The default JRE classpath container
	 * refers explicitly to the workspace default JRE.
	 * </p>
	 * @since 3.2
	 */
    public static final String ATTR_JRE_CONTAINER_PATH = JavaRuntime.JRE_CONTAINER;

    /**
	 * Launch configuration attribute key. The value is a name of a VM install
	 * to use when launching a local VM. This attribute must be qualified
	 * by a VM install type, via the <code>ATTR_VM_INSTALL_TYPE</code>
	 * attribute. When unspecified, the default VM is used.
	 * 
	 * @deprecated use <code>ATTR_JRE_CONTAINER_PATH</code>
	 */
    @Deprecated
    public static final String //$NON-NLS-1$
    ATTR_VM_INSTALL_NAME = LaunchingPlugin.getUniqueIdentifier() + ".VM_INSTALL_NAME";

    /**
	 * Launch configuration attribute key. The value is an identifier of
	 * a VM install type. Used in conjunction with a VM install name, to 
	 * specify the VM to use when launching a local Java application.
	 * The associated VM install name is specified via the attribute
	 * <code>ATTR_VM_INSTALL_NAME</code>.
	 * 
	 * @deprecated use <code>ATTR_JRE_CONTAINER_PATH</code>
	 */
    @Deprecated
    public static final String //$NON-NLS-1$
    ATTR_VM_INSTALL_TYPE = LaunchingPlugin.getUniqueIdentifier() + ".VM_INSTALL_TYPE_ID";

    /**
	 * Launch configuration attribute key. The value is a Map of attributes specific
	 * to a particular VM install type, used when launching a local Java
	 * application. The map is passed to a <code>VMRunner</code> via a <code>VMRunnerConfiguration</code>
	 * when launching a VM. The attributes in the map are implementation dependent
	 * and are limited to String keys and values.
	 */
    //$NON-NLS-1$
    public static final String ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP = LaunchingPlugin.getUniqueIdentifier() + "VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP";

    /**
	 * Launch configuration attribute key. The value is an identifier of
	 * a VM connector, specifying a connector to use when attaching to
	 * a remote VM.
	 */
    //$NON-NLS-1$
    public static final String ATTR_VM_CONNECTOR = LaunchingPlugin.getUniqueIdentifier() + ".VM_CONNECTOR_ID";

    /**
	 * Launch configuration attribute key. The attribute value is an ordered list of strings
	 * which are mementos for runtime class path entries. When unspecified, a default
	 * classpath is generated by the classpath provider associated with a launch
	 * configuration (via the <code>ATTR_CLASSPATH_PROVIDER</code> attribute).
	 */
    //$NON-NLS-1$
    public static final String ATTR_CLASSPATH = LaunchingPlugin.getUniqueIdentifier() + ".CLASSPATH";

    /**
	 * Launch configuration attribute key. The value is a boolean specifying
	 * whether a default classpath should be used when launching a local
	 * Java application. When <code>false</code>, a classpath must be specified
	 * via the <code>ATTR_CLASSPATH</code> attribute. When <code>true</code> or
	 * unspecified, a classpath is computed by the classpath provider associated
	 * with a launch configuration.
	 */
    /*
	 * This constant is referenced from an upper-layer bundle
	 * @see org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget#prepareBreakpointsSearchScope()
	 */
    //$NON-NLS-1$
    public static final String ATTR_DEFAULT_CLASSPATH = LaunchingPlugin.getUniqueIdentifier() + ".DEFAULT_CLASSPATH";

    /**
	 * Launch configuration attribute key. The value is an identifier of a
	 * classpath provider extension used to compute the classpath
	 * for a launch configuration. When unspecified, the default classpath
	 * provider is used - <code>StandardClasspathProvider</code>.
	 */
    //$NON-NLS-1$
    public static final String ATTR_CLASSPATH_PROVIDER = LaunchingPlugin.getUniqueIdentifier() + ".CLASSPATH_PROVIDER";

    /**
	 * Launch configuration attribute key. The value is an ordered list of
	 * strings which are mementos for associated runtime classpath entries
	 * interpreted as locations in which to look for source code. When unspecified,
	 * a default source lookup path is generated by the source path provider
	 * associated with a launch configurations (via the
	 * <code>ATTR_SOURCE_PATH_PROVIDER</code> attribute).
	 */
    //$NON-NLS-1$
    public static final String ATTR_SOURCE_PATH = LaunchingPlugin.getUniqueIdentifier() + ".SOURCE_PATH";

    /**
	 * Launch configuration attribute key. The value is a boolean specifying
	 * whether a default source lookup path should be used. When
	 * <code>false</code> a source path must be specified via the
	 * <code>ATTR_SOURCE_PATH</code> attribute. When <code>true</code> or
	 * unspecified, a source lookup path is computed by the source path
	 * provider associated with a launch configuration.
	 */
    //$NON-NLS-1$
    public static final String ATTR_DEFAULT_SOURCE_PATH = LaunchingPlugin.getUniqueIdentifier() + ".DEFAULT_SOURCE_PATH";

    /**
	 * Launch configuration attribute key. The value is an identifier of a
	 * source path provider extension used to compute the source lookup path
	 * for a launch configuration. When unspecified, the default source lookup
	 * path provider is used - <code>StandardSourcePathProvider</code>.
	 */
    //$NON-NLS-1$
    public static final String ATTR_SOURCE_PATH_PROVIDER = LaunchingPlugin.getUniqueIdentifier() + ".SOURCE_PATH_PROVIDER";

    /**
	 * Launch configuration attribute key. The value is a boolean, indicating
	 * whether a VM will support/allow the terminate action.
	 * This attribute is used for remote debugging.
	 */
    //$NON-NLS-1$
    public static final String ATTR_ALLOW_TERMINATE = LaunchingPlugin.getUniqueIdentifier() + ".ALLOW_TERMINATE";

    /**
	 * Attribute key for VM specific attributes found in the
	 * <code>ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP</code>. The value is a String,
	 * indicating the String to use to invoke the JRE.
	 */
    //$NON-NLS-1$
    public static final String ATTR_JAVA_COMMAND = LaunchingPlugin.getUniqueIdentifier() + ".JAVA_COMMAND";

    /**
	 * Launch configuration attribute key. The value is 
	 * a map. Keys in the map correspond to arguments names
	 * returned by <code>IVMConnector#getDefaultArguments()</code>.
	 * Values are strings corresponding to the values to use when
	 * establishing a connection to a remote VM.
	 */
    //$NON-NLS-1$
    public static final String ATTR_CONNECT_MAP = LaunchingPlugin.getUniqueIdentifier() + ".CONNECT_MAP";

    /**
	 * Launch configuration attribute key. The value is an integer
	 * indicating the width of the applet viewing area.
	 * 
	 * @since 2.1
	 */
    //$NON-NLS-1$
    public static final String ATTR_APPLET_WIDTH = LaunchingPlugin.getUniqueIdentifier() + ".APPLET_WIDTH";

    /**
	 * Launch configuration attribute key. The value is an integer
	 * indicating the height of the applet viewing area.
	 * 
	 * @since 2.1
	 */
    //$NON-NLS-1$
    public static final String ATTR_APPLET_HEIGHT = LaunchingPlugin.getUniqueIdentifier() + ".APPLET_HEIGHT";

    /**
	 * Launch configuration attribute key. The value is a String indicating the
	 * HTML name of the applet.
	 * 
	 * @since 2.1
	 */
    //$NON-NLS-1$
    public static final String ATTR_APPLET_NAME = LaunchingPlugin.getUniqueIdentifier() + ".APPLET_NAME";

    /**
	 * Launch configuration attribute key. The value is a Map. Keys in the map
	 * represent applet parameter names and the values in the map are the
	 * corresponding parameter values
	 * 
	 * @since 2.1
	 */
    //$NON-NLS-1$
    public static final String ATTR_APPLET_PARAMETERS = LaunchingPlugin.getUniqueIdentifier() + ".APPLET_PARAMETERS";

    /**
	 * Launch configuration attribute key. The value is a String indicating the
	 * fully qualified name of the applet viewer utility class to use.
	 * 
	 * @since 2.1
	 */
    //$NON-NLS-1$
    public static final String ATTR_APPLET_APPLETVIEWER_CLASS = LaunchingPlugin.getUniqueIdentifier() + ".APPLET_APPLETVIEWER_CLASS";

    /**
	 * Attribute key for a VM specific argument. Value is an array
	 * of strings describing paths in the local file system that
	 * should be prepended to the bootpath, or <code>null</code>
	 * if none. The value is computed dynamically at launch time
	 * and placed in the VM specific arguments map by the Java 
	 * application launch delegate.
	 * 
	 * @since 3.0
	 */
    //$NON-NLS-1$
    public static final String ATTR_BOOTPATH_PREPEND = LaunchingPlugin.getUniqueIdentifier() + ".-Xbootclasspath/p:";

    /**
	 * Attribute key for a VM specific argument. Value is an array
	 * of strings describing paths in the local file system that
	 * should be placed on the bootpath explicitly, or <code>null</code>
	 * if none. The value is computed dynamically at launch time
	 * and placed in the VM specific arguments map by the Java 
	 * application launch delegate.
	 * 
	 * @since 3.0
	 */
    //$NON-NLS-1$
    public static final String ATTR_BOOTPATH = LaunchingPlugin.getUniqueIdentifier() + ".-Xbootclasspath:";

    /**
	 * Attribute key for a VM specific argument. Value is an array
	 * of strings describing paths in the local file system that
	 * should be appended to the bootpath, or <code>null</code>
	 * if none. The value is computed dynamically at launch time
	 * and placed in the VM specific arguments map by the Java 
	 * application launch delegate.
	 * 
	 * @since 3.0
	 */
    //$NON-NLS-1$
    public static final String ATTR_BOOTPATH_APPEND = LaunchingPlugin.getUniqueIdentifier() + ".-Xbootclasspath/a:";

    /**
	 * Attribute key for a Mac OSX VM-specific argument. Value is a boolean
	 * indicating if the <code>-XStartOnFirstThread</code> argument should be used 
	 * when launching.
	 * 
	 * @since 3.7
	 */
    //$NON-NLS-1$
    public static final String ATTR_USE_START_ON_FIRST_THREAD = LaunchingPlugin.getUniqueIdentifier() + ".ATTR_USE_START_ON_FIRST_THREAD";

    /**
	 * Status code indicating a launch configuration does not
	 * specify a project when a project is required.
	 */
    public static final int ERR_UNSPECIFIED_PROJECT = 100;

    /**
	 * Status code indicating a launch configuration does not
	 * specify a main type to launch.
	 */
    public static final int ERR_UNSPECIFIED_MAIN_TYPE = 101;

    /**
	 * Status code indicating a launch configuration does not
	 * specify a VM Install Type.
	 */
    public static final int ERR_UNSPECIFIED_VM_INSTALL_TYPE = 102;

    /**
	 * Status code indicating a launch configuration does not
	 * specify a VM Install
	 */
    public static final int ERR_UNSPECIFIED_VM_INSTALL = 103;

    /**
	 * Status code indicating a launch configuration's VM install
	 * type could not be found.
	 */
    public static final int ERR_VM_INSTALL_TYPE_DOES_NOT_EXIST = 104;

    /**
	 * Status code indicating a launch configuration's VM install
	 * could not be found.
	 */
    public static final int ERR_VM_INSTALL_DOES_NOT_EXIST = 105;

    /**
	 * Status code indicating a VM runner could not be located
	 * for the VM install specified by a launch configuration.
	 */
    public static final int ERR_VM_RUNNER_DOES_NOT_EXIST = 106;

    /**
	 * Status code indicating the project associated with
	 * a launch configuration is not a Java project.
	 */
    public static final int ERR_NOT_A_JAVA_PROJECT = 107;

    /**
	 * Status code indicating the specified working directory
	 * does not exist.
	 */
    public static final int ERR_WORKING_DIRECTORY_DOES_NOT_EXIST = 108;

    /**
	 * Status code indicating a launch configuration does not
	 * specify a host name value
	 */
    public static final int ERR_UNSPECIFIED_HOSTNAME = 109;

    /**
	 * Status code indicating a launch configuration has
	 * specified an invalid host name attribute
	 */
    public static final int ERR_INVALID_HOSTNAME = 110;

    /**
	 * Status code indicating a launch configuration does not
	 * specify a port number value
	 */
    public static final int ERR_UNSPECIFIED_PORT = 111;

    /**
	 * Status code indicating a launch configuration has
	 * specified an invalid port number attribute
	 */
    public static final int ERR_INVALID_PORT = 112;

    /**
	 * Status code indicating an attempt to connect to a remote VM
	 * has failed or an attempt to listen for a remote VM connecting
	 * has failed.
	 */
    public static final int ERR_REMOTE_VM_CONNECTION_FAILED = 113;

    /**
	 * Status code indicating that the shared memory attach or listen
	 * connector could not be found.
	 */
    public static final int ERR_SHARED_MEMORY_CONNECTOR_UNAVAILABLE = 114;

    /**
	 * Status code indicating that the Eclipse runtime does not support
	 * launching a program with a working directory. This feature is only
	 * available if Eclipse is run on a 1.3 runtime or higher.
	 * <p>
	 * A status handler may be registered for this error condition,
	 * and should return a Boolean indicating whether the program
	 * should be relaunched with the default working directory.
	 * </p>
	 */
    public static final int ERR_WORKING_DIRECTORY_NOT_SUPPORTED = 115;

    /**
	 * Status code indicating that an error occurred launching a VM.
	 * The status error message is the text that
	 * the VM wrote to standard error before exiting.
	 */
    public static final int ERR_VM_LAUNCH_ERROR = 116;

    /**
	 * Status code indicating that a timeout has occurred waiting for
	 * the VM to connect with the debugger.
	 * <p>
	 * A status handler may be registered for this error condition,
	 * and should return a Boolean indicating whether the program
	 * should continue waiting for a connection for the associated
	 * timeout period.
	 * </p>
	 */
    public static final int ERR_VM_CONNECT_TIMEOUT = 117;

    /**
	 * Status code indicating that a free socket was not available to
	 * communicate with the VM.
	 */
    public static final int ERR_NO_SOCKET_AVAILABLE = 118;

    /**
	 * Status code indicating that the JDI connector required for a
	 * debug launch was not available.
	 */
    public static final int ERR_CONNECTOR_NOT_AVAILABLE = 119;

    /**
	 * Status code indicating that the debugger failed to connect
	 * to the VM.
	 */
    public static final int ERR_CONNECTION_FAILED = 120;

    /**
	 * Status code indicating that the applet launcher was asked to
	 * launch a resource that did not extend <code>java.applet.Applet</code>.
	 * 
	 * @since 2.1
	 */
    public static final int ERR_NOT_AN_APPLET = 121;

    /**
	 * Status code indicating that no launch configuration was specified.
	 * 
	 * @since 2.1
	 */
    public static final int ERR_UNSPECIFIED_LAUNCH_CONFIG = 122;

    /**
	 * Status code indicating that the .html file used to initiate an applet
	 * launch could not be built.
	 * 
	 * @since 2.1
	 */
    public static final int ERR_COULD_NOT_BUILD_HTML = 123;

    /**
	 * Status code indicating that the project referenced by a launch configuration
	 * is closed.
	 * 
	 * @since 3.0
	 */
    public static final int ERR_PROJECT_CLOSED = 124;

    /**
	 * Status code indicating an unexpected internal error.
	 */
    public static final int ERR_INTERNAL_ERROR = 150;

    /**
	 * Default value for the 'ATTR_APPLET_APPLETVIEWER' attribute.
	 * 
	 * @since 2.1
	 */
    //$NON-NLS-1$
    public static final String DEFAULT_APPLETVIEWER_CLASS = "sun.applet.AppletViewer";

    /**
	 * Detail code for a custom debug event that is fired when a remote debug configuration with
	 * a standard socket listen connector is ready to accept connections from a remote VM.
	 * 
	 * @since 3.4
	 * @see org.eclipse.jdt.internal.launching.SocketListenConnector
	 * @see org.eclipse.jdt.internal.launching.SocketListenConnectorProcess
	 */
    public static final int DETAIL_CONFIG_READY_TO_ACCEPT_REMOTE_VM_CONNECTION = 1001;
}
