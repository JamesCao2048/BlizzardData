/*******************************************************************************
 *  Copyright (c) 2005, 2013 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.builders;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.core.PDECoreMessages;
import org.eclipse.pde.internal.core.util.IdUtil;
import org.eclipse.pde.internal.core.util.VersionUtil;
import org.w3c.dom.*;

public class ManifestErrorReporter extends XMLErrorReporter {

    /**
	 * @param file
	 */
    public  ManifestErrorReporter(IFile file) {
        super(file);
    }

    protected void reportIllegalElement(Element element, int severity) {
        Node parent = element.getParentNode();
        if (parent == null || parent instanceof org.w3c.dom.Document) {
            report(PDECoreMessages.Builders_Manifest_illegalRoot, getLine(element), severity, PDEMarkerFactory.CAT_FATAL);
        } else {
            report(NLS.bind(PDECoreMessages.Builders_Manifest_child, new String[] { element.getNodeName(), parent.getNodeName() }), getLine(element), severity, PDEMarkerFactory.P_ILLEGAL_XML_NODE, element, null, PDEMarkerFactory.CAT_FATAL);
        }
    }

    protected void reportMissingRequiredAttribute(Element element, String attName, int severity) {
        String message = NLS.bind(PDECoreMessages.Builders_Manifest_missingRequired, (new String[] { attName, //
        element.getNodeName() }));
        report(message, getLine(element), severity, PDEMarkerFactory.CAT_FATAL);
    }

    protected boolean assertAttributeDefined(Element element, String attrName, int severity) {
        Attr attr = element.getAttributeNode(attrName);
        if (attr == null) {
            reportMissingRequiredAttribute(element, attrName, severity);
            return false;
        }
        return true;
    }

    protected void reportUnknownAttribute(Element element, String attName, int severity) {
        String message = NLS.bind(PDECoreMessages.Builders_Manifest_attribute, attName);
        report(message, getLine(element, attName), severity, PDEMarkerFactory.P_ILLEGAL_XML_NODE, element, attName, PDEMarkerFactory.CAT_OTHER);
    }

    protected void reportIllegalAttributeValue(Element element, Attr attr) {
        String message = NLS.bind(PDECoreMessages.Builders_Manifest_att_value, (new String[] { attr.getValue(), attr.getName() }));
        report(message, getLine(element, attr.getName()), CompilerFlags.ERROR, PDEMarkerFactory.CAT_FATAL);
    }

    protected void validateVersionAttribute(Element element, Attr attr) {
        IStatus status = VersionUtil.validateVersion(attr.getValue());
        if (status.getSeverity() != IStatus.OK)
            report(status.getMessage(), getLine(element, attr.getName()), CompilerFlags.ERROR, PDEMarkerFactory.CAT_FATAL);
    }

    protected void validateMatch(Element element, Attr attr) {
        String value = attr.getValue();
        if (//$NON-NLS-1$ //$NON-NLS-2$
        !"perfect".equals(value) && !"equivalent".equals(value) && !"greaterOrEqual".equals(value) && //$NON-NLS-1$ //$NON-NLS-2$
        !"compatible".equals(value))
            reportIllegalAttributeValue(element, attr);
    }

    protected void validateElementWithContent(Element element, boolean hasContent) {
        NodeList children = element.getChildNodes();
        boolean textFound = false;
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Text) {
                textFound = ((Text) child).getNodeValue().trim().length() > 0;
            } else if (child instanceof Element) {
                reportIllegalElement((Element) child, CompilerFlags.ERROR);
            }
        }
        if (!textFound)
            reportMissingElementContent(element);
    }

    private void reportMissingElementContent(Element element) {
        report(NLS.bind(PDECoreMessages.Builders_Feature_empty, element.getNodeName()), getLine(element), CompilerFlags.ERROR, PDEMarkerFactory.CAT_FATAL);
    }

    protected void reportExtraneousElements(NodeList elements, int maximum) {
        if (elements.getLength() > maximum) {
            for (int i = maximum; i < elements.getLength(); i++) {
                Element element = (Element) elements.item(i);
                report(NLS.bind(PDECoreMessages.Builders_Feature_multiplicity, element.getNodeName()), getLine(element), CompilerFlags.ERROR, PDEMarkerFactory.CAT_FATAL);
            }
        }
    }

    protected void validateURL(Element element, String attName) {
        String value = element.getAttribute(attName);
        try {
            if (//$NON-NLS-1$ //$NON-NLS-2$
            !value.startsWith("http:") && !value.startsWith("file:"))
                value = //$NON-NLS-1$
                "file:" + //$NON-NLS-1$
                value;
            new URL(value);
        } catch (MalformedURLException e) {
            report(NLS.bind(PDECoreMessages.Builders_Feature_badURL, attName), getLine(element, attName), CompilerFlags.ERROR, PDEMarkerFactory.CAT_FATAL);
        }
    }

    /**
	 * Checks whether the given attribute value is a valid bundle ID.  If it is not valid, a marker
	 * is created on the element and <code>false</code> is returned. If valid, <code>true</code> is
	 * returned.
	 *
	 * @param element element to add the marker to if invalid
	 * @param attr the attribute to check the value of
	 * @return whether the given attribute value is a valid bundle ID.
	 */
    protected boolean validatePluginID(Element element, Attr attr) {
        if (!IdUtil.isValidCompositeID3_0(attr.getValue())) {
            String message = NLS.bind(PDECoreMessages.Builders_Manifest_pluginID, attr.getValue(), attr.getName());
            report(message, getLine(element, attr.getName()), CompilerFlags.WARNING, PDEMarkerFactory.CAT_OTHER);
            return false;
        }
        return true;
    }

    protected void validateBoolean(Element element, Attr attr) {
        String value = attr.getValue();
        if (//$NON-NLS-1$ //$NON-NLS-2$
        !value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false"))
            reportIllegalAttributeValue(element, attr);
    }

    protected NodeList getChildrenByName(Element element, String name) {
        class NodeListImpl implements NodeList {

            ArrayList<Node> nodes = new ArrayList();

            @Override
            public int getLength() {
                return nodes.size();
            }

            @Override
            public Node item(int index) {
                return nodes.get(index);
            }

            protected void add(Node node) {
                nodes.add(node);
            }
        }
        NodeListImpl list = new NodeListImpl();
        NodeList allChildren = element.getChildNodes();
        for (int i = 0; i < allChildren.getLength(); i++) {
            Node node = allChildren.item(i);
            if (name.equals(node.getNodeName())) {
                list.add(node);
            }
        }
        return list;
    }

    protected void reportDeprecatedAttribute(Element element, Attr attr) {
        int severity = CompilerFlags.getFlag(fProject, CompilerFlags.P_DEPRECATED);
        if (severity != CompilerFlags.IGNORE) {
            report(NLS.bind(PDECoreMessages.Builders_Manifest_deprecated_attribute, attr.getName()), getLine(element, attr.getName()), severity, PDEMarkerFactory.CAT_DEPRECATION);
        }
    }
}
