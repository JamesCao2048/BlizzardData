/*******************************************************************************
 * Copyright (c) 2009, 2015 EclipseSource Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     EclipseSource Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.launching;

public interface IPDEConstants {

    //$NON-NLS-1$
    String PLUGIN_ID = "org.eclipse.pde.launching";

    //$NON-NLS-1$
    String UI_PLUGIN_ID = "org.eclipse.pde.ui";

    // JUnit application identifiers
    //$NON-NLS-1$
    String LEGACY_UI_TEST_APPLICATION = "org.eclipse.pde.junit.runtime.legacytestapplication";

    //$NON-NLS-1$
    String NON_UI_THREAD_APPLICATION = "org.eclipse.pde.junit.runtime.nonuithreadtestapplication";

    //$NON-NLS-1$
    String UI_TEST_APPLICATION = "org.eclipse.pde.junit.runtime.uitestapplication";

    //$NON-NLS-1$
    String CORE_TEST_APPLICATION = "org.eclipse.pde.junit.runtime.coretestapplication";

    /**
	 * Launch configuration attribute key. The value is a boolean specifies
	 * whether the launch configuration is being restarted. This does not need
	 * to be promoted to IPDELauncherConstants since clients should not need to
	 * know about restarts.
	 */
    //$NON-NLS-1$
    String RESTART = "restart";

    /**
	 * Launch configuration attribute key. The value is a boolean specifying
	 * whether the workspace log for an Eclipse application should be cleared
	 * prior to launching.
	 *
	 * TODO, move to IPDELauncherConstants in 3.4
	 */
    //$NON-NLS-1$
    String DOCLEARLOG = "clearwslog";

    //$NON-NLS-1$
    String LAUNCHER_PDE_VERSION = "pde.version";

    //$NON-NLS-1$
    String APPEND_ARGS_EXPLICITLY = "append.args";
}
