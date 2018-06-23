/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jsp.launching;

import java.io.File;
import java.text.MessageFormat;
import org.eclipse.jsp.JspUIPlugin;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

/**
 * Launch delegate for a local Tomcat server
 */
public class TomcatLaunchDelegate extends JavaLaunchDelegate {

    /**
	 * Identifier for Tomcat launch configurations.
	 */
    //$NON-NLS-1$
    public static final String ID_TOMCAT_LAUNCH_CONFIGURATION_TYPE = "org.eclipse.jsp.TomcatConfigurationType";

    /**
	 * Identifier for Tomcat classpath provider.
	 */
    //$NON-NLS-1$
    public static final String ID_TOMCAT_CLASSPATH_PROVIDER = "org.eclipse.jsp.tomcatClasspathProvider";

    /**
	 * Launch configuration attribute - value is path to local installation of Tomcat.
	 * The path may be encoded in a launch variable.
	 */
    //$NON-NLS-1$
    public static final String ATTR_CATALINA_HOME = "org.eclipse.jsp.CATALINA_HOME";

    /**
	 * Constructs a new launch delegate
	 */
    public  TomcatLaunchDelegate() {
        super();
    }

    /**
	 * Returns the value of the <code>${catalina_home}</code> launch variable.
	 * 
	 * @return the value of the <code>${catalina_home}</code> launch variable
	 * @exception CoreException if the variable or value is undefined
	 */
    public static String getCatalinaHome() throws CoreException {
        //$NON-NLS-1$
        IValueVariable variable = VariablesPlugin.getDefault().getStringVariableManager().getValueVariable("catalina_home");
        IStatus err = null;
        if (variable == null) {
            err = new Status(IStatus.ERROR, JspUIPlugin.getDefault().getBundle().getSymbolicName(), 0, LaunchingMessages.TomcatLaunchDelegate_9, null);
        } else {
            String home = variable.getValue();
            if (home != null && home.length() > 0) {
                File file = new File(home);
                if (file.exists() && file.isDirectory()) {
                    return home;
                } else {
                    err = new Status(IStatus.ERROR, JspUIPlugin.getDefault().getBundle().getSymbolicName(), 0, MessageFormat.format(LaunchingMessages.TomcatLaunchDelegate_7, new String[] { home }), null);
                }
            } else {
                err = new Status(IStatus.ERROR, JspUIPlugin.getDefault().getBundle().getSymbolicName(), 0, LaunchingMessages.TomcatLaunchDelegate_8, null);
            }
        }
        throw new CoreException(err);
    }
}
