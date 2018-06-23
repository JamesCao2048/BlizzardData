/*******************************************************************************
 *  Copyright (c) 2000, 2013 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.ant;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import org.apache.tools.ant.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.plugin.IPluginExtensionPoint;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.*;
import org.eclipse.pde.internal.core.builders.SchemaTransformer;
import org.eclipse.pde.internal.core.ischema.ISchemaInclude;
import org.eclipse.pde.internal.core.plugin.*;
import org.eclipse.pde.internal.core.schema.Schema;
import org.eclipse.pde.internal.core.schema.SchemaDescriptor;
import org.eclipse.pde.internal.core.util.HeaderMap;
import org.eclipse.pde.internal.core.util.SAXParserWrapper;
import org.osgi.framework.Constants;

/**
 * Ant task that takes a plug-in and created HTML reference documents for all schema (.exsd) files.
 *
 */
public class ConvertSchemaToHTML extends Task {

    private SchemaTransformer fTransformer = new SchemaTransformer();

    private String manifest;

    private String destination;

    private URL cssURL;

    private String additionalSearchPaths;

    @Override
    public void execute() throws BuildException {
        if (destination == null) {
            //$NON-NLS-1$
            throw new BuildException(NLS.bind(PDECoreMessages.Builders_Convert_missingAttribute, "destination"));
        }
        if (manifest == null) {
            //$NON-NLS-1$
            throw new BuildException(NLS.bind(PDECoreMessages.Builders_Convert_missingAttribute, "manifest"));
        }
        if (!new Path(destination).isValidPath(destination)) {
            //$NON-NLS-1$
            throw new BuildException(NLS.bind(PDECoreMessages.Builders_Convert_illegalValue, "destination"));
        }
        IPluginModelBase model = readManifestFile();
        if (model == null)
            return;
        String pluginID = model.getPluginBase().getId();
        if (pluginID == null) {
            pluginID = getPluginID();
        }
        List<IPath> searchPaths = getSearchPaths();
        IPluginExtensionPoint[] extPoints = model.getPluginBase().getExtensionPoints();
        for (int i = 0; i < extPoints.length; i++) {
            String schemaLocation = extPoints[i].getSchema();
            PrintWriter out = null;
            if (//$NON-NLS-1$
            schemaLocation == null || schemaLocation.equals(""))
                continue;
            Schema schema = null;
            SAXParserWrapper parser = null;
            try {
                parser = new SAXParserWrapper();
                File schemaFile = new File(model.getInstallLocation(), schemaLocation);
                XMLDefaultHandler handler = new XMLDefaultHandler();
                parser.parse(schemaFile, handler);
                URL url = schemaFile.toURL();
                SchemaDescriptor desc = new SchemaDescriptor(extPoints[i].getFullId(), url, searchPaths);
                schema = (Schema) desc.getSchema(false);
                // Check that all included schemas are available
                ISchemaInclude[] includes = schema.getIncludes();
                for (int j = 0; j < includes.length; j++) {
                    if (includes[j].getIncludedSchema() == null) {
                        log(NLS.bind(PDECoreMessages.ConvertSchemaToHTML_CannotFindIncludedSchema, includes[j].getLocation(), schemaFile), Project.MSG_ERR);
                    }
                }
                File directory = new Path(destination).isAbsolute() ? new File(destination) : new File(getProject().getBaseDir(), destination);
                if (!directory.exists() || !directory.isDirectory())
                    if (!directory.mkdirs()) {
                        schema.dispose();
                        return;
                    }
                String id = extPoints[i].getId();
                if (id.indexOf('.') == -1)
                    id = //$NON-NLS-1$
                    pluginID + "." + //$NON-NLS-1$
                    id;
                File file = new //$NON-NLS-1$
                File(//$NON-NLS-1$
                directory, //$NON-NLS-1$
                id.replace('.', '_') + ".html");
                out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), ICoreConstants.UTF_8), true);
                fTransformer.transform(schema, out, cssURL, SchemaTransformer.BUILD);
            } catch (Exception e) {
                throw new BuildException(e);
            } finally {
                if (out != null)
                    out.close();
                if (schema != null)
                    schema.dispose();
            }
        }
    }

    /**
	 * Required attribute describing the location of the plugin.xml file
	 * for the plug-in to create schema html docs for.  May be an absolute
	 * file path or a path relative to the ant base directory <code>${basedir}</code>
	 *
	 * @param manifest string file path to plugin.xml for the plug-in to convert
	 */
    public void setManifest(String manifest) {
        this.manifest = manifest;
    }

    /**
	 * Required attribute describing the location to output the HTML.
	 *
	 * @param destination string file path to output html to
	 */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
	 * Optional attribute providing a comma <code>','</code> delimited
	 * list of file paths to search for plug-ins that provide schema
	 * files included by the schema files being converted.
	 * <p>
	 * When a schema file includes another, the html will include the
	 * element definitions from the included schema if it is available.
	 * If the schema does not exist in the same plug-in, the task will
	 * assume the schema url is of the form
	 * <code>schema://<pluginID>/<schemaPath>. It will extract the plug-in
	 * ID and look for a folder of that name in the same directory as the
	 * parent schema's host plug-in. If the plug-ins are not all in the same
	 * directory, this attribute can be used to locate them.
	 * </p><p>
	 * The paths can be absolute file paths or paths relative to the ant
	 * base directory <code>${basedir}</code>.
	 * </p>
	 *
	 * @param additionalSearchPaths comma delimited list of search paths
	 */
    public void setAdditionalSearchPaths(String additionalSearchPaths) {
        this.additionalSearchPaths = additionalSearchPaths;
    }

    public URL getCSSURL() {
        return cssURL;
    }

    /**
	 * Sets a url location to lookup a CSS file to use during
	 * the schema transformation.  If not set, the task will search
	 * for a default CSS in the product plug-in.
	 *
	 * @param url string form of url pointing to a CSS file
	 */
    public void setCSSURL(String url) {
        try {
            cssURL = new URL(url);
        } catch (MalformedURLException e) {
            PDECore.logException(e);
        }
    }

    public void setCSSURL(URL url) {
        cssURL = url;
    }

    private String getPluginID() {
        File file = new Path(manifest).isAbsolute() ? new File(manifest) : new File(getProject().getBaseDir(), manifest);
        File OSGiFile = new File(file.getParentFile(), ICoreConstants.BUNDLE_FILENAME_DESCRIPTOR);
        if (OSGiFile.exists()) {
            try {
                Map<String, String> headers = ManifestElement.parseBundleManifest(new FileInputStream(OSGiFile), new HeaderMap<String, String>());
                String value = headers.get(Constants.BUNDLE_SYMBOLICNAME).toString();
                if (value == null)
                    return null;
                ManifestElement[] elements = ManifestElement.parseHeader(Constants.BUNDLE_SYMBOLICNAME, value);
                if (elements.length > 0)
                    return elements[0].getValue();
            } catch (Exception e1) {
                System.out.print(e1.getMessage());
            }
        }
        return null;
    }

    /**
	 * @return user specified search paths or <code>null</code>
	 */
    private List<IPath> getSearchPaths() {
        if (this.additionalSearchPaths == null) {
            return null;
        }
        //$NON-NLS-1$
        String[] paths = this.additionalSearchPaths.split(",");
        List<IPath> result = new ArrayList(paths.length);
        for (int i = 0; i < paths.length; i++) {
            IPath path = new Path(paths[i]);
            if (path.isValidPath(paths[i])) {
                if (!path.isAbsolute()) {
                    File baseDir = getProject().getBaseDir();
                    path = new Path(baseDir.getPath()).append(path);
                }
                result.add(path);
            } else {
                System.out.println(NLS.bind(PDECoreMessages.ConvertSchemaToHTML_InvalidAdditionalSearchPath, paths[i]));
            }
        }
        return result;
    }

    private IPluginModelBase readManifestFile() throws BuildException {
        File file = new Path(manifest).isAbsolute() ? new File(manifest) : new File(getProject().getBaseDir(), manifest);
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(file));
        } catch (Exception e) {
            throw new BuildException(e);
        }
        ExternalPluginModelBase model = null;
        try {
            if (file.getName().toLowerCase(Locale.ENGLISH).equals(ICoreConstants.FRAGMENT_FILENAME_DESCRIPTOR))
                model = new ExternalFragmentModel();
            else if (file.getName().toLowerCase(Locale.ENGLISH).equals(ICoreConstants.PLUGIN_FILENAME_DESCRIPTOR))
                model = new ExternalPluginModel();
            else {
                stream.close();
                throw new //$NON-NLS-1$
                BuildException(//$NON-NLS-1$
                NLS.bind(PDECoreMessages.Builders_Convert_illegalValue, "manifest"));
            }
            String parentPath = file.getParentFile().getAbsolutePath();
            model.setInstallLocation(parentPath);
            model.load(stream, false);
            stream.close();
        } catch (Exception e) {
            throw new BuildException(e);
        }
        return model;
    }
}
