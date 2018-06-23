/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.search;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.api.tools.internal.AntFilterStore;
import org.eclipse.pde.api.tools.internal.IApiCoreConstants;
import org.eclipse.pde.api.tools.internal.IApiXmlConstants;
import org.eclipse.pde.api.tools.internal.builder.Reference;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.VisibilityModifiers;
import org.eclipse.pde.api.tools.internal.provisional.builder.IReference;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchReporter;
import org.eclipse.pde.api.tools.internal.provisional.search.IMetadata;
import org.eclipse.pde.api.tools.internal.util.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Search reporter that outputs results to an XML file
 *
 * @since 1.0.1
 */
public class XmlSearchReporter implements IApiSearchReporter {

    private String fLocation = null;

    private DocumentBuilder parser = null;

    private boolean debug = false;

    private int referenceCount = 0;

    private int illegalCount = 0;

    private int internalCount = 0;

    /**
	 * Constructor
	 *
	 * @param location the absolute path in the local file system to the folder
	 *            to write the reports to
	 * @param debug if debugging infos should be written out to the console
	 */
    public  XmlSearchReporter(String location, boolean debug) {
        fLocation = location;
        this.debug = debug;
        try {
            parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            parser.setErrorHandler(new DefaultHandler());
        } catch (FactoryConfigurationError fce) {
            ApiPlugin.log(fce);
        } catch (ParserConfigurationException pce) {
            ApiPlugin.log(pce);
        }
    }

    @Override
    public void reportResults(IApiElement element, final IReference[] references) {
        if (references.length == 0) {
            // references
            return;
        }
        // Use a hashset for counting to remove any duplicate references that
        // the writer would remove
        HashSet<IReferenceDescriptor> writtenReferences = new HashSet();
        XmlReferenceDescriptorWriter writer = new XmlReferenceDescriptorWriter(fLocation);
        List<IReferenceDescriptor> descriptors = new ArrayList(references.length + 1);
        for (IReference referenceInterface : references) {
            Reference reference = (Reference) referenceInterface;
            try {
                IReferenceDescriptor descriptor = reference.getReferenceDescriptor();
                descriptors.add(descriptor);
                // Update counters
                if (!writtenReferences.contains(descriptor)) {
                    referenceCount++;
                    if ((referenceInterface.getReferenceFlags() & IReference.F_ILLEGAL) > 0) {
                        illegalCount++;
                    }
                    // visibility equality
                    if (descriptor.getVisibility() == VisibilityModifiers.PRIVATE) {
                        internalCount++;
                    }
                    writtenReferences.add(descriptor);
                }
            } catch (CoreException e) {
                ApiPlugin.log(e.getStatus());
            }
        }
        writer.writeReferences(descriptors.toArray(new IReferenceDescriptor[descriptors.size()]));
    }

    /**
	 * Resolves the id to use for the component in the mapping
	 *
	 * @param component
	 * @return the id to use for the component in the mapping, includes the
	 *         version information as well
	 * @throws CoreException
	 */
    String getId(IApiComponent component) throws CoreException {
        StringBuffer buffer = new StringBuffer();
        //$NON-NLS-1$
        buffer.append(component.getSymbolicName()).append(" ").append('(').append(component.getVersion()).append(')');
        return buffer.toString();
    }

    /**
	 * @see org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchReporter#reportNotSearched(org.eclipse.pde.api.tools.internal.provisional.model.IApiElement[])
	 */
    @Override
    public void reportNotSearched(IApiElement[] elements) {
        if (elements == null) {
            return;
        }
        BufferedWriter writer = null;
        try {
            if (this.debug) {
                //$NON-NLS-1$
                System.out.println(//$NON-NLS-1$
                "Writing file for projects that were not searched...");
            }
            File rootfile = new File(fLocation);
            if (!rootfile.exists()) {
                rootfile.mkdirs();
            }
            //$NON-NLS-1$
            File file = new File(rootfile, "not_searched.xml");
            if (!file.exists()) {
                file.createNewFile();
            }
            Document doc = Util.newDocument();
            Element root = doc.createElement(IApiXmlConstants.ELEMENT_COMPONENTS);
            doc.appendChild(root);
            Element comp = null;
            SkippedComponent component = null;
            for (IApiElement element : elements) {
                component = (SkippedComponent) element;
                comp = doc.createElement(IApiXmlConstants.ELEMENT_COMPONENT);
                comp.setAttribute(IApiXmlConstants.ATTR_ID, component.getComponentId());
                comp.setAttribute(IApiXmlConstants.ATTR_VERSION, component.getVersion());
                comp.setAttribute(IApiXmlConstants.SKIPPED_DETAILS, component.getErrorDetails());
                root.appendChild(comp);
            }
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), IApiCoreConstants.UTF_8));
            writer.write(Util.serializeDocument(doc));
            writer.flush();
        } catch (FileNotFoundException fnfe) {
        } catch (IOException ioe) {
        } catch (CoreException ce) {
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public void reportMetadata(IMetadata data) {
        if (data == null) {
            return;
        }
        try {
            if (this.debug) {
                //$NON-NLS-1$
                System.out.println(//$NON-NLS-1$
                "Writing file for projects that were not searched...");
            }
            File rootfile = new File(fLocation);
            if (!rootfile.exists()) {
                rootfile.mkdirs();
            }
            //$NON-NLS-1$
            File file = new File(rootfile, "meta.xml");
            if (!file.exists()) {
                file.createNewFile();
            }
            data.serializeToFile(file);
        } catch (FileNotFoundException fnfe) {
            ApiPlugin.log(fnfe);
        } catch (IOException ioe) {
            ApiPlugin.log(ioe);
        } catch (CoreException ce) {
            ApiPlugin.log(ce);
        }
    }

    @Override
    public void reportCounts() {
        BufferedWriter writer = null;
        try {
            if (this.debug) {
                //$NON-NLS-1$
                System.out.println(//$NON-NLS-1$
                "Writing file for counting total references...");
            }
            File rootfile = new File(fLocation);
            if (!rootfile.exists()) {
                rootfile.mkdirs();
            }
            //$NON-NLS-1$
            File file = new File(rootfile, "counts.xml");
            if (!file.exists()) {
                file.createNewFile();
            }
            Document doc = Util.newDocument();
            Element root = doc.createElement(IApiXmlConstants.ELEMENT_REPORTED_COUNT);
            doc.appendChild(root);
            root.setAttribute(IApiXmlConstants.ATTR_TOTAL, Integer.toString(referenceCount));
            root.setAttribute(IApiXmlConstants.ATTR_COUNT_ILLEGAL, Integer.toString(illegalCount));
            root.setAttribute(IApiXmlConstants.ATTR_COUNT_INTERNAL, Integer.toString(internalCount));
            root.setAttribute(IApiXmlConstants.ATTR_COUNT_FILTERED, Integer.toString(AntFilterStore.filteredAPIProblems.size()));
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), IApiCoreConstants.UTF_8));
            writer.write(Util.serializeDocument(doc));
            writer.flush();
        } catch (FileNotFoundException fnfe) {
        } catch (IOException ioe) {
        } catch (CoreException ce) {
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
