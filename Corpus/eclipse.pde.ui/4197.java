/*******************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alena Laskavaia - Bug 453392 - No debug options help
 *******************************************************************************/
package org.eclipse.pde.internal.core;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;

public class TracingOptionsManager {

    private Properties template;

    public  TracingOptionsManager() {
        super();
    }

    private Properties createTemplate() {
        Properties temp = new Properties();
        IPluginModelBase[] models = PluginRegistry.getAllModels();
        for (int i = 0; i < models.length; i++) {
            addToTemplate(temp, models[i]);
        }
        return temp;
    }

    private void addToTemplate(Properties temp, IPluginModelBase model) {
        Properties modelOptions = getOptions(model);
        if (modelOptions != null) {
            temp.putAll(modelOptions);
        }
    }

    public synchronized Hashtable<String, Object> getTemplateTable(String pluginId) {
        if (template == null)
            template = createTemplate();
        Hashtable<String, Object> defaults = new Hashtable();
        for (Enumeration<Object> keys = template.keys(); keys.hasMoreElements(); ) {
            String key = keys.nextElement().toString();
            if (belongsTo(key, pluginId)) {
                defaults.put(key, template.get(key));
            }
        }
        return defaults;
    }

    private boolean belongsTo(String option, String pluginId) {
        IPath path = new Path(option);
        String firstSegment = path.segment(0);
        return pluginId.equalsIgnoreCase(firstSegment);
    }

    public Properties getTracingOptions(Map<String, String> storedOptions) {
        // Start with the fresh template from plugins
        Properties defaults = getTracingTemplateCopy();
        if (storedOptions != null) {
            // Load stored values, but only for existing keys
            Iterator<?> iter = storedOptions.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next().toString();
                if (defaults.containsKey(key)) {
                    defaults.setProperty(key, storedOptions.get(key));
                }
            }
        }
        return defaults;
    }

    public synchronized Properties getTracingTemplateCopy() {
        if (template == null)
            template = createTemplate();
        return (Properties) createTemplate().clone();
    }

    public static boolean isTraceable(IPluginModelBase model) {
        String location = model.getInstallLocation();
        if (location == null)
            return false;
        File pluginLocation = new File(location);
        InputStream stream = null;
        ZipFile jarFile = null;
        try {
            if (pluginLocation.isDirectory())
                return new File(pluginLocation, ICoreConstants.OPTIONS_FILENAME).exists();
            jarFile = new ZipFile(pluginLocation, ZipFile.OPEN_READ);
            ZipEntry manifestEntry = jarFile.getEntry(ICoreConstants.OPTIONS_FILENAME);
            if (manifestEntry != null) {
                stream = jarFile.getInputStream(manifestEntry);
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (stream != null)
                    stream.close();
                if (jarFile != null)
                    jarFile.close();
            } catch (IOException e) {
            }
        }
        return stream != null;
    }

    public synchronized void reset() {
        template = null;
    }

    private void save(String fileName, Properties properties) {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(fileName);
            //$NON-NLS-1$
            properties.store(stream, "Master Tracing Options");
            stream.flush();
        } catch (IOException e) {
            PDECore.logException(e);
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (IOException e) {
                PDECore.logException(e);
            }
        }
    }

    public void save(String filename, Map<String, String> map, HashSet<?> selected) {
        Properties properties = getTracingOptions(map);
        for (Enumeration<?> keys = properties.keys(); keys.hasMoreElements(); ) {
            String key = keys.nextElement().toString();
            Path path = new Path(key);
            if (path.segmentCount() < 1 || !selected.contains(path.segment(0).toString())) {
                properties.remove(key);
            }
        }
        save(filename, properties);
    }

    public void save(String filename, Map<String, String> map) {
        save(filename, getTracingOptions(map));
    }

    private Properties getOptions(IPluginModelBase model) {
        String location = model.getInstallLocation();
        if (location == null)
            return null;
        try {
            File pluginLocation = new File(location);
            Properties modelOptions = new Properties();
            if (pluginLocation.isDirectory()) {
                File file = new File(pluginLocation, ICoreConstants.OPTIONS_FILENAME);
                if (file.exists()) {
                    try (InputStream stream = new FileInputStream(file)) {
                        modelOptions.load(stream);
                    }
                    try (InputStream stream = new FileInputStream(file)) {
                        loadComments(stream, modelOptions);
                    }
                    return modelOptions;
                }
            } else {
                try (ZipFile jarFile = new ZipFile(pluginLocation, ZipFile.OPEN_READ)) {
                    ZipEntry manifestEntry = jarFile.getEntry(ICoreConstants.OPTIONS_FILENAME);
                    if (manifestEntry != null) {
                        try (InputStream stream = jarFile.getInputStream(manifestEntry)) {
                            modelOptions.load(stream);
                        }
                        try (InputStream stream = jarFile.getInputStream(manifestEntry)) {
                            loadComments(stream, modelOptions);
                        }
                        return modelOptions;
                    }
                }
            }
        } catch (IOException e) {
            PDECore.logException(e);
        }
        return null;
    }

    /**
	 * Loads the comments from the properties files. This is simple version
	 * which won't cover 100% of the cases but hopefully cover 99%. It will find
	 * single or multiline comments starting with # (not !) and attach to the
	 * following property key by creating fake property with the key #key and
	 * value of the comment. Properties object which will receive these comments
	 * cannot be saved back properly without some special handling. It won't
	 * support cases when: key is split in multiple lines; key use escape
	 * characters; comment uses ! start char
	 */
    private void loadComments(InputStream stream, Properties modelOptions) throws IOException {
        //$NON-NLS-1$
        String prevComment = "";
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (//$NON-NLS-1$
                line.startsWith("#") || line.trim().isEmpty()) {
                    prevComment += //$NON-NLS-1$
                    "\n" + //$NON-NLS-1$
                    line.trim();
                } else {
                    if (prevComment.trim().isEmpty())
                        continue;
                    int eq = line.indexOf('=');
                    if (eq >= 0) {
                        String key = line.substring(0, eq).trim();
                        //$NON-NLS-1$
                        modelOptions.put(//$NON-NLS-1$
                        "#" + key, //$NON-NLS-1$
                        prevComment);
                    }
                    //$NON-NLS-1$
                    prevComment = "";
                }
            }
        }
    }
}
