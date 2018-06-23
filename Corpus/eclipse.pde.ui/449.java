/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.tasks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.api.tools.internal.model.ApiModelFactory;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent;
import org.eclipse.pde.api.tools.internal.util.FilteredElements;
import org.eclipse.pde.api.tools.internal.util.TarException;
import org.eclipse.pde.api.tools.internal.util.Util;

/**
 * Common code for API Tools Ant tasks.
 *
 * @since 1.0.0
 * @noextend This class is not intended to be sub-classed by clients.
 */
public abstract class CommonUtilsTask extends Task {

    //$NON-NLS-1$
    private static final String ECLIPSE_FOLDER_NAME = "eclipse";

    //$NON-NLS-1$
    protected static final String CURRENT = "currentBaseline";

    //$NON-NLS-1$
    protected static final String CURRENT_BASELINE_NAME = "current_baseline";

    //$NON-NLS-1$
    protected static final String REFERENCE = "referenceBaseline";

    //$NON-NLS-1$
    protected static final String REFERENCE_BASELINE_NAME = "reference_baseline";

    protected boolean debug;

    protected String eeFileLocation;

    protected String currentBaselineLocation;

    protected String referenceBaselineLocation;

    protected String excludeListLocation;

    protected String includeListLocation;

    protected String reportLocation;

    /**
	 * Creates a baseline with the given name and EE file location in the given
	 * directory. The installLocation will be searched for bundles to add as API
	 * components. If an error occurs creating the baseline, the exception stack
	 * trace will be printed and an empty baseline will be returned.
	 *
	 * @param baselineName Name to use for the new baseline
	 * @param installLocation Location of an installation or directory of
	 *            bundles to add as API components
	 * @param eeFileLocation execution environment location or <code>null</code>
	 *            to have the EE determined from API components
	 * @return a new {@link IApiBaseline} or <code>null</code> if an error
	 *         occurred
	 */
    protected IApiBaseline createBaseline(String baselineName, String installLocation, String eeFileLocation) {
        try {
            IApiBaseline baseline = null;
            if (ApiPlugin.isRunningInFramework()) {
                baseline = ApiModelFactory.newApiBaseline(baselineName);
            } else if (eeFileLocation != null) {
                baseline = ApiModelFactory.newApiBaseline(baselineName, new File(eeFileLocation));
            } else {
                baseline = ApiModelFactory.newApiBaseline(baselineName, Util.getEEDescriptionFile());
            }
            IApiComponent[] components = ApiModelFactory.addComponents(baseline, installLocation, null);
            if (components.length == 0) {
                throw new BuildException(NLS.bind(Messages.directoryIsEmpty, installLocation));
            }
            return baseline;
        } catch (CoreException e) {
            e.printStackTrace();
            return ApiModelFactory.newApiBaseline(baselineName);
        }
    }

    /**
	 * Deletes an {@link IApiBaseline} from the given folder
	 *
	 * @param referenceLocation
	 * @param folder
	 */
    protected void deleteBaseline(String referenceLocation, File folder) {
        if (Util.isArchive(referenceLocation)) {
            Util.delete(folder.getParentFile());
        }
    }

    /**
	 * Extract extracts the SDK from the given location to the given directory
	 * name
	 *
	 * @param installDirName
	 * @param location
	 * @return the {@link File} handle to the extracted SDK
	 */
    protected File extractSDK(String installDirName, String location) {
        File file = new File(location);
        File locationFile = file;
        if (!locationFile.exists()) {
            throw new BuildException(NLS.bind(Messages.fileDoesnotExist, location));
        }
        if (Util.isArchive(location)) {
            //$NON-NLS-1$
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            File installDir = new File(tempDir, installDirName);
            if (installDir.exists()) {
                // delete existing folder
                if (!Util.delete(installDir)) {
                    throw new BuildException(NLS.bind(Messages.couldNotDelete, installDir.getAbsolutePath()));
                }
            }
            if (!installDir.mkdirs()) {
                throw new BuildException(NLS.bind(Messages.couldNotCreate, installDir.getAbsolutePath()));
            }
            try {
                if (Util.isZipJarFile(location)) {
                    Util.unzip(location, installDir.getAbsolutePath());
                } else if (Util.isTGZFile(location)) {
                    Util.guntar(location, installDir.getAbsolutePath());
                }
            } catch (IOException e) {
                throw new BuildException(NLS.bind(Messages.couldNotUnzip, new String[] { location, installDir.getAbsolutePath() }));
            } catch (TarException e) {
                throw new BuildException(NLS.bind(Messages.couldNotUntar, new String[] { location, installDir.getAbsolutePath() }));
            }
            return new File(installDir, ECLIPSE_FOLDER_NAME);
        } else {
            return locationFile;
        }
    }

    /**
	 * Initializes the include/exclude list from the given file location, and
	 * returns a {@link Set} of project names that should be include/excluded.
	 *
	 * @param excludeListLocation
	 * @return the set of project names to be excluded
	 */
    protected static FilteredElements initializeFilteredElements(String filterListLocation, IApiBaseline baseline, boolean debug) throws BuildException {
        try {
            return Util.initializeRegexFilterList(filterListLocation, baseline, debug);
        } catch (CoreException e) {
            throw new BuildException(e);
        }
    }

    /**
	 * Saves the report with the given name in the report location. If a
	 * componentID is provided, a child directory using that name will be
	 * created to put the report in.
	 *
	 * @param componentID Name of the component to create a child directory for
	 *            or <code>null<code> to put the report in the XML root
	 * @param contents contents to output to the report
	 * @param reportname name of the file to output to
	 */
    protected void saveReport(String componentID, String contents, String reportname) {
        File dir = new File(this.reportLocation);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new BuildException(NLS.bind(Messages.errorCreatingReportDirectory, this.reportLocation));
            }
        }
        // If the caller has provided a component id, create a child directory
        if (componentID != null) {
            dir = new File(dir, componentID);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new BuildException(NLS.bind(Messages.errorCreatingReportDirectory, dir));
                }
            }
        }
        File reportFile = new File(dir, reportname);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(reportFile));
            writer.write(contents);
            writer.flush();
        } catch (IOException e) {
            ApiPlugin.log(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
	 * Parses and returns patterns as an array of Strings or <code>null</code>
	 * if none.
	 *
	 * @param patterns comma separated list or <code>null</code>
	 * @return individual patterns or <code>null</code>
	 */
    protected String[] parsePatterns(String patterns) {
        if (patterns == null || patterns.trim().length() == 0) {
            return null;
        }
        //$NON-NLS-1$
        String[] strings = patterns.split(",");
        List<String> list = new ArrayList();
        for (String string : strings) {
            String pattern = string.trim();
            if (pattern.length() > 0) {
                list.add(pattern);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public static String convertToHtml(String s) {
        char[] contents = s.toCharArray();
        StringBuffer buffer = new StringBuffer();
        for (char c : contents) {
            switch(c) {
                case '<':
                    //$NON-NLS-1$
                    buffer.append(//$NON-NLS-1$
                    "&lt;");
                    break;
                case '>':
                    //$NON-NLS-1$
                    buffer.append(//$NON-NLS-1$
                    "&gt;");
                    break;
                case '\"':
                    //$NON-NLS-1$
                    buffer.append(//$NON-NLS-1$
                    "&quot;");
                    break;
                case '&':
                    //$NON-NLS-1$
                    buffer.append(//$NON-NLS-1$
                    "&amp;");
                    break;
                case '^':
                    //$NON-NLS-1$
                    buffer.append(//$NON-NLS-1$
                    "&and;");
                    break;
                default:
                    buffer.append(c);
            }
        }
        return String.valueOf(buffer);
    }
}
