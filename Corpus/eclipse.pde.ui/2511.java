/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.search;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.Signature;
import org.eclipse.osgi.util.NLS;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.pde.api.tools.internal.IApiCoreConstants;
import org.eclipse.pde.api.tools.internal.IApiXmlConstants;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.VisibilityModifiers;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IComponentDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IElementDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IFieldDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IMemberDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IMethodDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IReferenceTypeDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.search.IMetadata;
import org.eclipse.pde.api.tools.internal.util.Signatures;
import org.eclipse.pde.api.tools.internal.util.Util;
import org.osgi.framework.Version;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class converts a collection of API use report XML files from a given
 * location to a corresponding collection of HTML in a given location
 *
 * @since 1.0.1
 */
public class UseReportConverter extends HTMLConvertor {

    /**
	 * Use visitor to write the reports
	 */
    class Visitor extends UseScanVisitor {

        ArrayList<Report> reports = new ArrayList();

        Report currentreport = null;

        Type currenttype = null, currentreferee = null;

        Member currentmember = null;

        HashMap<IReferenceTypeDescriptor, Type> keys = new HashMap();

        ArrayList<Type> referees = new ArrayList();

        /**
		 * Returns if the reference should be reported or not
		 *
		 * @param desc
		 * @return true if the reference should be reported false otherwise
		 */
        private boolean acceptReference(IMemberDescriptor desc, Pattern[] patterns) {
            if (patterns != null) {
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(desc.getPackage().getName()).find()) {
                        return false;
                    }
                }
            }
            return true;
        }

        /**
		 * Returns the enclosing {@link IReferenceTypeDescriptor} for the given
		 * member descriptor
		 *
		 * @param member
		 * @return the enclosing {@link IReferenceTypeDescriptor} or
		 *         <code>null</code>
		 */
        IReferenceTypeDescriptor getEnclosingDescriptor(IMemberDescriptor member) {
            switch(member.getElementType()) {
                case IElementDescriptor.TYPE:
                    {
                        return (IReferenceTypeDescriptor) member;
                    }
                case IElementDescriptor.METHOD:
                case IElementDescriptor.FIELD:
                    {
                        return member.getEnclosingType();
                    }
                default:
                    break;
            }
            return null;
        }

        @Override
        public boolean visitComponent(IComponentDescriptor target) {
            this.currentreport = new Report();
            this.currentreport.name = composeName(target.getId(), target.getVersion());
            this.reports.add(this.currentreport);
            return true;
        }

        @Override
        public void endVisitComponent(IComponentDescriptor target) {
            try {
                long start = 0;
                if (ApiPlugin.DEBUG_USE_REPORT_CONVERTER) {
                    System.out.println(//$NON-NLS-1$
                    "Writing report for bundle: " + //$NON-NLS-1$
                    target.getId());
                    start = System.currentTimeMillis();
                }
                if (this.currentreport.counts.getTotalRefCount() > 0) {
                    writeReferencedMemberPage(this.currentreport, this.referees);
                } else {
                    this.reports.remove(this.currentreport);
                }
                if (ApiPlugin.DEBUG_USE_REPORT_CONVERTER) {
                    //$NON-NLS-1$ //$NON-NLS-2$
                    System.out.println("done in: " + (System.currentTimeMillis() - start) + " ms");
                }
            } catch (Exception e) {
                ApiPlugin.log(e);
            } finally {
                // clear any children as we have written them out - keep the
                // report object to write a sorted index page
                this.currentreport.children.clear();
                this.keys.clear();
                this.referees.clear();
            }
        }

        @Override
        public boolean visitReferencingComponent(IComponentDescriptor component) {
            this.currentreferee = new Type(component);
            return true;
        }

        @Override
        public void endVisitReferencingComponent(IComponentDescriptor component) {
            if (this.currentreferee.counts.getTotalRefCount() > 0) {
                this.referees.add(this.currentreferee);
            }
        }

        @Override
        public boolean visitMember(IMemberDescriptor referencedMember) {
            IReferenceTypeDescriptor desc = getEnclosingDescriptor(referencedMember);
            if (desc == null) {
                return false;
            }
            this.currenttype = this.keys.get(desc);
            if (this.currenttype == null) {
                this.currenttype = new Type(desc);
                this.keys.put(desc, this.currenttype);
            }
            TreeMap<IMemberDescriptor, Member> map = this.currentreport.children.get(this.currenttype);
            if (map == null) {
                map = new TreeMap(compare);
                this.currentreport.children.put(this.currenttype, map);
            }
            this.currentmember = map.get(referencedMember);
            if (this.currentmember == null) {
                this.currentmember = new Member(referencedMember);
                map.put(referencedMember, this.currentmember);
            }
            return true;
        }

        @Override
        public void endVisitMember(IMemberDescriptor referencedMember) {
            if (this.currentmember.children.size() == 0) {
                TreeMap<IMemberDescriptor, Member> map = this.currentreport.children.get(this.currenttype);
                map.remove(referencedMember);
            }
            if (this.currenttype.counts.getTotalRefCount() == 0) {
                IReferenceTypeDescriptor desc = getEnclosingDescriptor(referencedMember);
                if (desc != null) {
                    this.keys.remove(desc);
                    this.currentreport.children.remove(this.currenttype);
                }
            }
        }

        /**
		 * Formats the arrays of messages
		 *
		 * @param messages
		 * @return the formatted messages or <code>null</code>
		 */
        String formatMessages(String[] messages) {
            if (messages != null) {
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < messages.length; i++) {
                    buffer.append(messages[i]);
                    if (i < messages.length - 1) {
                        //$NON-NLS-1$
                        buffer.append("\n");
                    }
                }
                return buffer.toString();
            }
            return null;
        }

        @Override
        public void visitReference(IReferenceDescriptor reference) {
            IMemberDescriptor fromMember = reference.getMember();
            if (!acceptReference(reference.getReferencedMember(), topatterns) || !acceptReference(fromMember, frompatterns)) {
                return;
            }
            int lineNumber = reference.getLineNumber();
            int refKind = reference.getReferenceKind();
            int visibility = reference.getVisibility();
            String refname = org.eclipse.pde.api.tools.internal.builder.Reference.getReferenceText(refKind);
            List<Reference> refs = this.currentmember.children.get(refname);
            if (refs == null) {
                refs = new ArrayList();
                this.currentmember.children.put(refname, refs);
            }
            refs.add(new Reference(fromMember, lineNumber, visibility, formatMessages(reference.getProblemMessages())));
            switch(fromMember.getElementType()) {
                case IElementDescriptor.TYPE:
                    {
                        switch(visibility) {
                            case VisibilityModifiers.API:
                                {
                                    this.currentmember.counts.total_api_type_count++;
                                    this.currenttype.counts.total_api_type_count++;
                                    this.currentreferee.counts.total_api_type_count++;
                                    this.currentreport.counts.total_api_type_count++;
                                    break;
                                }
                            case VisibilityModifiers.PRIVATE:
                                {
                                    this.currentmember.counts.total_private_type_count++;
                                    this.currenttype.counts.total_private_type_count++;
                                    this.currentreferee.counts.total_private_type_count++;
                                    this.currentreport.counts.total_private_type_count++;
                                    break;
                                }
                            case VisibilityModifiers.PRIVATE_PERMISSIBLE:
                                {
                                    this.currentmember.counts.total_permissable_type_count++;
                                    this.currenttype.counts.total_permissable_type_count++;
                                    this.currentreferee.counts.total_permissable_type_count++;
                                    this.currentreport.counts.total_permissable_type_count++;
                                    break;
                                }
                            case FRAGMENT_PERMISSIBLE:
                                {
                                    this.currentmember.counts.total_fragment_permissible_type_count++;
                                    this.currenttype.counts.total_fragment_permissible_type_count++;
                                    this.currentreferee.counts.total_fragment_permissible_type_count++;
                                    this.currentreport.counts.total_fragment_permissible_type_count++;
                                    break;
                                }
                            case VisibilityModifiers.ILLEGAL_API:
                                {
                                    this.currentmember.counts.total_illegal_type_count++;
                                    this.currenttype.counts.total_illegal_type_count++;
                                    this.currentreferee.counts.total_illegal_type_count++;
                                    this.currentreport.counts.total_illegal_type_count++;
                                    break;
                                }
                            default:
                                break;
                        }
                        break;
                    }
                case IElementDescriptor.METHOD:
                    {
                        switch(visibility) {
                            case VisibilityModifiers.API:
                                {
                                    this.currentmember.counts.total_api_method_count++;
                                    this.currenttype.counts.total_api_method_count++;
                                    this.currentreferee.counts.total_api_method_count++;
                                    this.currentreport.counts.total_api_method_count++;
                                    break;
                                }
                            case VisibilityModifiers.PRIVATE:
                                {
                                    this.currentmember.counts.total_private_method_count++;
                                    this.currenttype.counts.total_private_method_count++;
                                    this.currentreferee.counts.total_private_method_count++;
                                    this.currentreport.counts.total_private_method_count++;
                                    break;
                                }
                            case VisibilityModifiers.PRIVATE_PERMISSIBLE:
                                {
                                    this.currentmember.counts.total_permissable_method_count++;
                                    this.currenttype.counts.total_permissable_method_count++;
                                    this.currentreferee.counts.total_permissable_method_count++;
                                    this.currentreport.counts.total_permissable_method_count++;
                                    break;
                                }
                            case FRAGMENT_PERMISSIBLE:
                                {
                                    this.currentmember.counts.total_fragment_permissible_method_count++;
                                    this.currenttype.counts.total_fragment_permissible_method_count++;
                                    this.currentreferee.counts.total_fragment_permissible_method_count++;
                                    this.currentreport.counts.total_fragment_permissible_method_count++;
                                    break;
                                }
                            case VisibilityModifiers.ILLEGAL_API:
                                {
                                    this.currentmember.counts.total_illegal_method_count++;
                                    this.currenttype.counts.total_illegal_method_count++;
                                    this.currentreferee.counts.total_illegal_method_count++;
                                    this.currentreport.counts.total_illegal_method_count++;
                                    break;
                                }
                            default:
                                break;
                        }
                        break;
                    }
                case IElementDescriptor.FIELD:
                    {
                        switch(visibility) {
                            case VisibilityModifiers.API:
                                {
                                    this.currentmember.counts.total_api_field_count++;
                                    this.currenttype.counts.total_api_field_count++;
                                    this.currentreferee.counts.total_api_field_count++;
                                    this.currentreport.counts.total_api_field_count++;
                                    break;
                                }
                            case VisibilityModifiers.PRIVATE:
                                {
                                    this.currentmember.counts.total_private_field_count++;
                                    this.currenttype.counts.total_private_field_count++;
                                    this.currentreferee.counts.total_private_field_count++;
                                    this.currentreport.counts.total_private_field_count++;
                                    break;
                                }
                            case VisibilityModifiers.PRIVATE_PERMISSIBLE:
                                {
                                    this.currentmember.counts.total_permissable_field_count++;
                                    this.currenttype.counts.total_permissable_field_count++;
                                    this.currentreferee.counts.total_permissable_field_count++;
                                    this.currentreport.counts.total_permissable_field_count++;
                                    break;
                                }
                            case FRAGMENT_PERMISSIBLE:
                                {
                                    this.currentmember.counts.total_fragment_permissible_field_count++;
                                    this.currenttype.counts.total_fragment_permissible_field_count++;
                                    this.currentreferee.counts.total_fragment_permissible_field_count++;
                                    this.currentreport.counts.total_fragment_permissible_field_count++;
                                    break;
                                }
                            case VisibilityModifiers.ILLEGAL_API:
                                {
                                    this.currentmember.counts.total_illegal_field_count++;
                                    this.currenttype.counts.total_illegal_field_count++;
                                    this.currentreferee.counts.total_illegal_field_count++;
                                    this.currentreport.counts.total_illegal_field_count++;
                                    break;
                                }
                            default:
                                break;
                        }
                        break;
                    }
                default:
                    break;
            }
        }
    }

    /**
	 * Comparator for use report items
	 */
    static Comparator<Object> compare = new Comparator<Object>() {

        @Override
        public int compare(Object o1, Object o2) {
            if (o1 instanceof String && o2 instanceof String) {
                return ((String) o1).compareTo((String) o2);
            }
            if (o1 instanceof Type && o2 instanceof Type) {
                return compare(((Type) o1).desc, ((Type) o2).desc);
            }
            if (o1 instanceof IReferenceTypeDescriptor && o2 instanceof IReferenceTypeDescriptor) {
                return ((IReferenceTypeDescriptor) o1).getQualifiedName().compareTo(((IReferenceTypeDescriptor) o2).getQualifiedName());
            }
            if (o1 instanceof IMethodDescriptor && o2 instanceof IMethodDescriptor) {
                try {
                    return Signatures.getQualifiedMethodSignature((IMethodDescriptor) o1).compareTo(Signatures.getQualifiedMethodSignature((IMethodDescriptor) o2));
                } catch (CoreException ce) {
                    return -1;
                }
            }
            if (o1 instanceof IFieldDescriptor && o2 instanceof IFieldDescriptor) {
                return Signatures.getQualifiedFieldSignature((IFieldDescriptor) o1).compareTo(Signatures.getQualifiedFieldSignature((IFieldDescriptor) o2));
            }
            if (o1 instanceof IComponentDescriptor && o2 instanceof IComponentDescriptor) {
                return ((IComponentDescriptor) o1).getId().compareTo(((IComponentDescriptor) o2).getId());
            }
            return -1;
        }
    };

    /**
	 * Root item describing the use of one component
	 */
    static class Report {

        String name = null;

        TreeMap<Type, TreeMap<IMemberDescriptor, Member>> children = new TreeMap(compare);

        CountGroup counts = new CountGroup();
    }

    /**
	 * Describes a type, used to key a collection of {@link Member}s
	 */
    static class Type {

        IElementDescriptor desc = null;

        CountGroup counts = new CountGroup();

        public  Type(IElementDescriptor desc) {
            this.desc = desc;
        }
    }

    /**
	 * Describes a member that is being used
	 */
    static class Member {

        IElementDescriptor descriptor = null;

        TreeMap<String, List<Reference>> children = new TreeMap(compare);

        CountGroup counts = new CountGroup();

        public  Member(IElementDescriptor desc) {
            this.descriptor = desc;
        }
    }

    /**
	 * Describes a reference from a given descriptor
	 */
    static class Reference {

        IElementDescriptor desc = null;

        int line = -1, vis = -1;

        String message = null;

        public  Reference(IElementDescriptor desc, int line, int vis, String message) {
            this.desc = desc;
            this.line = line;
            this.vis = vis;
            this.message = message;
        }
    }

    /**
	 * A group of counters to origin meta-data
	 */
    static final class CountGroup {

        int total_api_field_count = 0;

        int total_private_field_count = 0;

        int total_permissable_field_count = 0;

        int total_fragment_permissible_field_count = 0;

        int total_illegal_field_count = 0;

        int total_api_method_count = 0;

        int total_private_method_count = 0;

        int total_permissable_method_count = 0;

        int total_fragment_permissible_method_count = 0;

        int total_illegal_method_count = 0;

        int total_api_type_count = 0;

        int total_private_type_count = 0;

        int total_permissable_type_count = 0;

        int total_fragment_permissible_type_count = 0;

        int total_illegal_type_count = 0;

        public int getTotalRefCount() {
            return total_api_field_count + total_api_method_count + total_api_type_count + total_private_field_count + total_private_method_count + total_private_type_count + total_permissable_field_count + total_permissable_method_count + total_permissable_type_count + total_fragment_permissible_field_count + total_fragment_permissible_method_count + total_fragment_permissible_type_count + total_illegal_field_count + total_illegal_method_count + total_illegal_type_count;
        }

        public int getTotalApiRefCount() {
            return total_api_field_count + total_api_method_count + total_api_type_count;
        }

        public int getTotalInternalRefCount() {
            return total_private_field_count + total_private_method_count + total_private_type_count;
        }

        public int getTotalPermissableRefCount() {
            return total_permissable_field_count + total_permissable_method_count + total_permissable_type_count;
        }

        public int getTotalFragmentPermissibleRefCount() {
            return total_fragment_permissible_field_count + total_fragment_permissible_method_count + total_fragment_permissible_type_count;
        }

        public int getTotalIllegalRefCount() {
            return total_illegal_field_count + total_illegal_method_count + total_illegal_type_count;
        }
    }

    /**
	 * Handler for parsing the not_searched.xml file to output a summary or
	 * missing required bundles
	 */
    static final class MissingHandler extends DefaultHandler {

        List<String> missing = new ArrayList();

        //$NON-NLS-1$
        static String pattern = "Require-Bundle:";

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (IApiXmlConstants.ELEMENT_COMPONENT.equals(qName)) {
                String value = //$NON-NLS-1$
                attributes.getValue(//$NON-NLS-1$
                "details");
                StringTokenizer tokenizer = new //$NON-NLS-1$
                StringTokenizer(//$NON-NLS-1$
                value, //$NON-NLS-1$
                "<>");
                int index = -1;
                while (tokenizer.hasMoreTokens()) {
                    value = tokenizer.nextToken();
                    index = value.indexOf(pattern);
                    if (index > -1) {
                        missing.add(value.replaceAll(pattern, Util.EMPTY_STRING));
                    }
                }
            }
        }
    }

    /**
	 * Visibility constant indicating an element has host-fragment level of
	 * visibility. i.e. fragments have {@link #PRIVATE_PERMISSIBLE}-like access
	 * to the internals of their host.
	 *
	 * @since 1.0.1
	 */
    public static final int FRAGMENT_PERMISSIBLE = 0x0000005;

    /**
	 * Default XSLT file name
	 */
    //$NON-NLS-1$
    public static final String DEFAULT_XSLT = "/references.xsl";

    /**
	 * Colour white for normal / permissible references Possibility: #C0E0C0
	 */
    //$NON-NLS-1$
    public static final String NORMAL_REFS_COLOUR = "#FFFFFF";

    /**
	 * Colour red for internal references Old colour: #E0A0A0
	 */
    //$NON-NLS-1$
    public static final String INTERNAL_REFS_COLOUR = "#F2C3C3";

    /**
	 * Colour gray for illegal references
	 *
	 * @since 1.1
	 */
    //$NON-NLS-1$
    public static final String ILLEGAL_REFS_COLOUR = "#E0E0E0";

    /**
	 * Colour gold for the references table header. Old colour: #CC9933
	 *
	 * @since 1.1
	 */
    //$NON-NLS-1$
    public static final String REFERENCES_TABLE_HEADER_COLOUR = "#E0C040";

    /**
	 * Style HTML bits for a page that shows references
	 */
    static final String REF_STYLE;

    /**
	 * The script block used to show an expanding table of references
	 */
    static final String REF_SCRIPT;

    static {
        StringBuffer buffer = new StringBuffer();
        //$NON-NLS-1$
        buffer.append("<style type=\"text/css\">\n");
        //$NON-NLS-1$
        buffer.append("\t.main {\t\tfont-family:Arial, Helvetica, sans-serif;\n\t}\n");
        //$NON-NLS-1$
        buffer.append("\t.main h3 {\n\t\tfont-family:Arial, Helvetica, sans-serif;\n\t\t\background-color:#FFFFFF;\n\t\tfont-size:14px;\n\t\tmargin:0.1em;\n\t}\n");
        //$NON-NLS-1$
        buffer.append("\t.main h4 {\n\t\tbackground-color:#CCCCCC;\n\t\tmargin:0.15em;\n\t}\n");
        //$NON-NLS-1$
        buffer.append("\ta.typeslnk {\n\t\tfont-family:Arial, Helvetica, sans-serif;\n\t\ttext-decoration:none;\n\t\tmargin-left:0.25em;\n\t}\n");
        //$NON-NLS-1$
        buffer.append("\ta.typeslnk:hover {\n\t\ttext-decoration:underline;\n\t}\n");
        //$NON-NLS-1$
        buffer.append("\ta.kindslnk {\n\t\tfont-family:Arial, Helvetica, sans-serif;\n\t\ttext-decoration:none;\n\t\tmargin-left:0.25em;\n\t}\n");
        //$NON-NLS-1$
        buffer.append("\t.types {\n\t\tdisplay:none;\n\t\tmargin-bottom:0.25em;\n\t\tmargin-top:0.25em;\n\t\tmargin-right:0.25em;\n\t\tmargin-left:0.75em;\n\t}\n");
        //$NON-NLS-1$
        buffer.append("</style>\n");
        REF_STYLE = buffer.toString();
        buffer = new StringBuffer();
        //$NON-NLS-1$
        buffer.append("<script type=\"text/javascript\">\n\tfunction expand(location) {\n\t\tif(document.getElementById) {\n\t\t\tvar childhtml = location.firstChild;\n\t\t\tif(!childhtml.innerHTML) {\n\t\t\t\tchildhtml = childhtml.nextSibling;\n\t\t\t}\n\t\t\tchildhtml.innerHTML = childhtml.innerHTML == '[+] ' ? '[-] ' : '[+] ';\n\t\t\tvar parent = location.parentNode;\n\t\t\tchildhtml = parent.nextSibling.style ? parent.nextSibling : parent.nextSibling.nextSibling;\n\t\t\tchildhtml.style.display = childhtml.style.display == 'block' ? 'none' : 'block';\n\t\t}\n\t}\n</script>\n");
        //$NON-NLS-1$
        buffer.append("<noscript>\n\t<style type=\"text/css\">\n\t\t.types {display:block;}\n\t\t.kinds{display:block;}\n\t</style>\n</noscript>\n");
        REF_SCRIPT = buffer.toString();
    }

    private String xmlLocation = null;

    private String htmlLocation = null;

    private File reportsRoot = null;

    private File htmlIndex = null;

    private boolean hasmissing = false;

    private boolean useNotSearchedXml = false;

    SAXParser parser = null;

    private UseMetadata metadata = null;

    private int filteredCount = -1;

    Pattern[] topatterns = null;

    Pattern[] frompatterns = null;

    /**
	 * Constructor
	 *
	 * @param htmlroot the folder root where the HTML reports should be written
	 * @param xmlroot the folder root where the current API use scan output is
	 *            located
	 * @param topatterns array of regular expressions used to prune references
	 *            to a given name pattern
	 * @param frompatterns array of regular expressions used to prune references
	 *            from a given name pattern
	 */
    public  UseReportConverter(String htmlroot, String xmlroot, String[] topatterns, String[] frompatterns) {
        this.xmlLocation = xmlroot;
        this.htmlLocation = htmlroot;
        if (topatterns != null) {
            ArrayList<Pattern> pats = new ArrayList(topatterns.length);
            for (String topattern : topatterns) {
                try {
                    pats.add(Pattern.compile(topattern));
                } catch (PatternSyntaxException pse) {
                    if (ApiPlugin.DEBUG_USE_REPORT_CONVERTER) {
                        System.out.println(NLS.bind(SearchMessages.UseReportConverter_filter_pattern_not_valid, topattern));
                        System.out.println(pse.getMessage());
                    }
                }
            }
            if (!pats.isEmpty()) {
                this.topatterns = pats.toArray(new Pattern[pats.size()]);
            }
        }
        if (frompatterns != null) {
            ArrayList<Pattern> pats = new ArrayList(frompatterns.length);
            for (String frompattern : frompatterns) {
                try {
                    pats.add(Pattern.compile(frompattern));
                } catch (PatternSyntaxException pse) {
                    if (ApiPlugin.DEBUG_USE_REPORT_CONVERTER) {
                        System.out.println(NLS.bind(SearchMessages.UseReportConverter_filter_pattern_not_valid, frompattern));
                        System.out.println(pse.getMessage());
                    }
                }
            }
            if (!pats.isEmpty()) {
                this.frompatterns = pats.toArray(new Pattern[pats.size()]);
            }
        }
    }

    protected String getHtmlLocation() {
        return this.htmlLocation;
    }

    protected String getXmlLocation() {
        return this.xmlLocation;
    }

    protected File getReportsRoot() {
        if (this.reportsRoot == null) {
            this.reportsRoot = new File(getXmlLocation());
        }
        return this.reportsRoot;
    }

    protected boolean hasMissing() {
        return this.hasmissing;
    }

    /**
	 * Runs the converter on the given locations
	 */
    public void convert(String xslt, IProgressMonitor monitor) throws Exception {
        if (getHtmlLocation() == null) {
            return;
        }
        SubMonitor localmonitor = SubMonitor.convert(monitor, SearchMessages.UseReportConverter_preparing_report_metadata, 11);
        try {
            localmonitor.setTaskName(SearchMessages.UseReportConverter_preparing_html_root);
            localmonitor.split(1);
            File htmlRoot = new File(getHtmlLocation());
            if (!htmlRoot.exists()) {
                if (!htmlRoot.mkdirs()) {
                    throw new Exception(NLS.bind(SearchMessages.could_not_create_file, getHtmlLocation()));
                }
            } else {
                htmlRoot.mkdirs();
            }
            localmonitor.setTaskName(SearchMessages.UseReportConverter_preparing_xml_root);
            localmonitor.split(1);
            if (getXmlLocation() == null) {
                throw new Exception(SearchMessages.missing_xml_files_location);
            }
            File lreportsRoot = getReportsRoot();
            if (!lreportsRoot.exists() || !lreportsRoot.isDirectory()) {
                throw new Exception(NLS.bind(SearchMessages.invalid_directory_name, getXmlLocation()));
            }
            localmonitor.setTaskName(SearchMessages.UseReportConverter_preparing_xslt_file);
            localmonitor.split(1);
            File xsltFile = null;
            if (xslt != null) {
                // we will use the default XSLT transform from the ant jar when
                // this is null
                xsltFile = new File(xslt);
                if (!xsltFile.exists() || !xsltFile.isFile()) {
                    throw new Exception(SearchMessages.UseReportConverter_xslt_file_not_valid);
                }
            }
            long start = 0;
            if (ApiPlugin.DEBUG_USE_REPORT_CONVERTER) {
                start = System.currentTimeMillis();
            }
            localmonitor.setTaskName(SearchMessages.UseReportConverter_writing_not_searched);
            this.hasmissing = writeMissingBundlesPage(htmlRoot);
            writeNotSearchedPage(htmlRoot);
            localmonitor.split(1);
            if (ApiPlugin.DEBUG_USE_REPORT_CONVERTER) {
                //$NON-NLS-1$ //$NON-NLS-2$
                System.out.println("done in: " + (System.currentTimeMillis() - start) + " ms");
                //$NON-NLS-1$
                System.out.println(//$NON-NLS-1$
                "Parsing use scan...");
                start = System.currentTimeMillis();
            }
            localmonitor.setTaskName(SearchMessages.UseReportConverter_parsing_use_scan);
            List<?> result = parse(localmonitor.split(5));
            localmonitor.split(1);
            if (ApiPlugin.DEBUG_USE_REPORT_CONVERTER) {
                //$NON-NLS-1$ //$NON-NLS-2$
                System.out.println("done in: " + (System.currentTimeMillis() - start) + " ms");
                //$NON-NLS-1$
                System.out.println(//$NON-NLS-1$
                "Sorting reports and writing index...");
                start = System.currentTimeMillis();
            }
            localmonitor.setTaskName(SearchMessages.UseReportConverter_writing_root_index);
            writeIndexPage(result);
            localmonitor.split(1);
            if (ApiPlugin.DEBUG_USE_REPORT_CONVERTER) {
                //$NON-NLS-1$ //$NON-NLS-2$
                System.out.println("done in: " + (System.currentTimeMillis() - start) + " ms");
            }
            writeMetaPage(htmlRoot);
        } finally {
            if (localmonitor != null) {
                localmonitor.done();
            }
        }
    }

    protected List<?> parse(IProgressMonitor monitor) throws Exception {
        UseScanParser lparser = new UseScanParser();
        Visitor convertor = new Visitor();
        lparser.parse(getXmlLocation(), monitor, convertor);
        return convertor.reports;
    }

    /**
	 * Returns the handle to the default parser, caches the handle once it has
	 * been created
	 *
	 * @return the handle to the default parser
	 * @throws Exception forwarded general exception that can be trapped in Ant
	 *             builds
	 */
    SAXParser getParser() throws Exception {
        if (this.parser == null) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            try {
                this.parser = factory.newSAXParser();
            } catch (ParserConfigurationException pce) {
                throw new Exception(SearchMessages.UseReportConverter_pce_error_getting_parser, pce);
            } catch (SAXException se) {
                throw new Exception(SearchMessages.UseReportConverter_se_error_parser_handle, se);
            }
            if (this.parser == null) {
                throw new Exception(SearchMessages.could_not_create_sax_parser);
            }
        }
        return this.parser;
    }

    /**
	 * Builds the name for the component
	 *
	 * @param id id of the component
	 * @param version version of the component, can be <code>null</code>
	 * @return string name
	 */
    protected String composeName(String id, String version) {
        String versionName = version;
        if (version == null) {
            versionName = Version.emptyVersion.toString();
        }
        StringBuffer buffer = new StringBuffer(3 + id.length() + versionName.length());
        //$NON-NLS-1$ //$NON-NLS-2$
        buffer.append(id).append(" (").append(versionName).append(")");
        return buffer.toString();
    }

    /**
	 * @return the index.html file created from the report conversion or
	 *         <code>null</code> if the conversion failed
	 */
    public File getReportIndex() {
        return htmlIndex;
    }

    protected void setReportIndex(File index) {
        htmlIndex = index;
    }

    /**
	 * Applies the given XSLT to the given XML to produce HTML in the given file
	 *
	 * @param xsltfile
	 * @param xmlfile
	 * @param htmloutput
	 * @throws TransformerException
	 */
    protected void applyXSLT(File xsltFile, File xmlfile, File htmloutput) throws TransformerException, Exception {
        Source xslt = null;
        if (xsltFile != null) {
            xslt = new StreamSource(xsltFile);
        } else {
            InputStream defaultXsltInputStream = UseReportConverter.class.getResourceAsStream(DEFAULT_XSLT);
            if (defaultXsltInputStream != null) {
                xslt = new StreamSource(new BufferedInputStream(defaultXsltInputStream));
            }
        }
        if (xslt == null) {
            throw new Exception(SearchMessages.UseReportConverter_no_xstl_specified);
        }
        applyXSLT(xslt, xmlfile, htmloutput);
    }

    /**
	 * Applies the given XSLT source to the given XML file outputting to the
	 * given HTML file
	 *
	 * @param xslt
	 * @param xmlfile
	 * @param htmlfile
	 * @throws TransformerException
	 */
    protected void applyXSLT(Source xslt, File xmlfile, File htmlfile) throws TransformerException {
        Source xml = new StreamSource(xmlfile);
        Result html = new StreamResult(htmlfile);
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer former = factory.newTransformer(xslt);
        former.transform(xml, html);
    }

    /**
	 * Transforms the given set of xml files with the given XSLT and places the
	 * result into a corresponding HTML file
	 *
	 * @param xmlfiles
	 * @param xsltFile
	 * @param html
	 */
    protected void tranformXml(File[] xmlfiles, File xsltFile) {
        File html = null;
        for (File xmlfile : xmlfiles) {
            try {
                File htmlroot = new File(getHtmlLocation(), getHTMLFileLocation(xmlfile));
                if (!htmlroot.exists()) {
                    htmlroot.mkdirs();
                }
                html = new File(getNameFromXMLFilename(xmlfile));
                applyXSLT(xsltFile, xmlfile, html);
            } catch (TransformerException te) {
            } catch (Exception e) {
                ApiPlugin.log(e);
            }
        }
    }

    /**
	 * Gets the HTML path to write out the transformed XML file to
	 *
	 * @param reportroot
	 * @param xmlfile
	 * @return
	 */
    protected String getHTMLFileLocation(File xmlfile) {
        File reportRoot = new File(getXmlLocation());
        IPath xml = new Path(xmlfile.getPath());
        IPath report = new Path(reportRoot.getPath());
        int segments = xml.matchingFirstSegments(report);
        if (segments > 0) {
            if (xml.getDevice() != null) {
                xml = xml.setDevice(null);
            }
            IPath html = xml.removeFirstSegments(segments);
            return html.removeLastSegments(1).toOSString();
        }
        return null;
    }

    /**
	 * Returns the name to use for the corresponding HTML file from the given
	 * XML file
	 *
	 * @param xmlFile
	 * @return the HTML name to use
	 */
    protected String getNameFromXMLFilename(File xmlFile) {
        String fileName = xmlFile.getAbsolutePath();
        int index = fileName.lastIndexOf('.');
        StringBuffer buffer = new StringBuffer();
        buffer.append(fileName.substring(getReportsRoot().getAbsolutePath().length(), index)).append(HTML_EXTENSION);
        File htmlFile = new File(getHtmlLocation(), String.valueOf(buffer));
        return htmlFile.getAbsolutePath();
    }

    /**
	 * Returns the collection of missing bundle names
	 *
	 * @param missingfile
	 * @return the collection of missing bundle names
	 * @throws Exception
	 */
    protected String[] getMissingBundles(File missingfile) throws Exception {
        MissingHandler handler = new MissingHandler();
        InputStream inputFile = null;
        try {
            inputFile = new FileInputStream(missingfile.getAbsolutePath());
            getParser().parse(inputFile, handler);
        } catch (IOException ioe) {
            ApiPlugin.log(ioe);
        } finally {
            if (inputFile != null) {
                try {
                    inputFile.close();
                } catch (IOException e) {
                }
            }
        }
        return handler.missing.toArray(new String[handler.missing.size()]);
    }

    /**
	 * Returns the sentence describing the purpose / reason of the missing
	 * bundles
	 *
	 * @return a blurb describing the table of missing bundles
	 */
    protected String getMissingBundlesHeader() {
        return SearchMessages.UseReportConverter_reported_missing_bundles;
    }

    /**
	 * Writes any existing metadata out to a meta.html file in the root of the
	 * HTML report location
	 *
	 * @param htmlroot
	 * @throws Exception
	 */
    void writeMetaPage(File htmlroot) throws Exception {
        File meta = null;
        PrintWriter writer = null;
        try {
            //$NON-NLS-1$
            File file = new File(getReportsRoot(), "meta.xml");
            if (!file.exists()) {
                // do nothing if no meta.xml file
                return;
            }
            //$NON-NLS-1$
            String filename = "meta";
            meta = new File(htmlroot, filename + HTML_EXTENSION);
            if (!meta.exists()) {
                meta.createNewFile();
            }
            StringBuffer buffer = new StringBuffer();
            buffer.append(HTML_HEADER);
            buffer.append(OPEN_HTML).append(OPEN_HEAD).append(CONTENT_TYPE_META);
            buffer.append(OPEN_TITLE).append(SearchMessages.UseReportConverter_use_scan_info).append(CLOSE_TITLE);
            buffer.append(CLOSE_HEAD);
            buffer.append(OPEN_BODY);
            buffer.append(OPEN_H3).append(SearchMessages.UseReportConverter_use_scan_info).append(CLOSE_H3);
            writeMetadataSummary(buffer);
            buffer.append(W3C_FOOTER);
            // write file
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(meta), IApiCoreConstants.UTF_8));
            writer.println(buffer.toString());
            writer.flush();
        } catch (IOException ioe) {
            throw new Exception(NLS.bind(SearchMessages.ioexception_writing_html_file, meta.getAbsolutePath()));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
	 * Writes out a summary of the missing required bundles
	 *
	 * @param htmlroot
	 * @return <code>true</code> if there was at least one missing bundle
	 */
    protected boolean writeMissingBundlesPage(final File htmlroot) throws Exception {
        boolean hasMissing = false;
        File missing = null;
        PrintWriter writer = null;
        try {
            //$NON-NLS-1$
            String filename = "missing";
            missing = new File(htmlroot, filename + HTML_EXTENSION);
            if (!missing.exists()) {
                missing.createNewFile();
            }
            //$NON-NLS-1$
            File file = new File(getReportsRoot(), "not_searched.xml");
            if (!file.exists()) {
                // try <root>/xml in case a raw reports root was specified
                //$NON-NLS-1$ //$NON-NLS-2$
                file = new File(getReportsRoot() + File.separator + "xml", "not_searched.xml");
            }
            TreeSet<String> sorted = new TreeSet(Util.componentsorter);
            if (file.exists()) {
                String[] missingBundles = getMissingBundles(file);
                hasMissing = missingBundles.length > 0;
                for (String missingBundle : missingBundles) {
                    sorted.add(missingBundle);
                }
            }
            StringBuffer buffer = new StringBuffer();
            buffer.append(HTML_HEADER);
            buffer.append(OPEN_HTML).append(OPEN_HEAD).append(CONTENT_TYPE_META);
            buffer.append(OPEN_TITLE).append(SearchMessages.UseReportConverter_missing_required).append(CLOSE_TITLE);
            buffer.append(CLOSE_HEAD);
            buffer.append(OPEN_BODY);
            buffer.append(OPEN_H3).append(SearchMessages.UseReportConverter_missing_required).append(CLOSE_H3);
            if (sorted.isEmpty()) {
                buffer.append(SearchMessages.UseReportConverter_no_required_missing).append(BR);
            } else {
                buffer.append(OPEN_P).append(getMissingBundlesHeader()).append(CLOSE_P);
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "<table border=\"1\" width=\"50%\">\n");
                //$NON-NLS-1$ //$NON-NLS-2$
                buffer.append(OPEN_TR).append("<td bgcolor=\"").append(REFERENCES_TABLE_HEADER_COLOUR).append("\" width=\"36%\">").append(OPEN_B).append(SearchMessages.UseReportConverter_required_bundles).append(CLOSE_B).append(CLOSE_TD).append(CLOSE_TR);
            }
            for (String value : sorted) {
                buffer.append(OPEN_TR).append(OPEN_TD).append(value).append(CLOSE_TD).append(CLOSE_TR);
            }
            buffer.append(CLOSE_TABLE);
            //$NON-NLS-1$
            buffer.append(BR).append("<a href=\"not_searched.html\">").append(SearchMessages.UseReportConverter_back_to_not_searched).append(CLOSE_A);
            buffer.append(W3C_FOOTER);
            // write file
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(missing), IApiCoreConstants.UTF_8));
            writer.println(buffer.toString());
            writer.flush();
        } catch (IOException ioe) {
            throw new Exception(NLS.bind(SearchMessages.ioexception_writing_html_file, missing.getAbsolutePath()));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        return hasMissing;
    }

    /**
	 * Writes out the file of components that were not searched: either because
	 * they appeared in an exclude list or they have no .api_description file
	 *
	 * @param htmlroot
	 */
    void writeNotSearchedPage(final File htmlroot) throws Exception {
        File originhtml = null;
        InputStream defaultXsltInputStream = null;
        try {
            //$NON-NLS-1$
            String filename = "not_searched";
            originhtml = new File(htmlroot, filename + HTML_EXTENSION);
            if (!originhtml.exists()) {
                originhtml.createNewFile();
            }
            File xml = new File(getReportsRoot(), filename + XML_EXTENSION);
            if (!xml.exists()) {
                // try <root>/xml in case a raw report root is specified
                xml = new //$NON-NLS-1$
                File(//$NON-NLS-1$
                getReportsRoot() + File.separator + "xml", //$NON-NLS-1$
                filename + XML_EXTENSION);
            }
            defaultXsltInputStream = UseReportConverter.class.getResourceAsStream(getNotSearchedXSLPath());
            Source xslt = null;
            if (defaultXsltInputStream != null) {
                xslt = new StreamSource(new BufferedInputStream(defaultXsltInputStream));
            }
            if (xslt == null) {
                throw new Exception(SearchMessages.UseReportConverter_no_xstl_specified);
            }
            if (xml.exists()) {
                try {
                    applyXSLT(xslt, xml, originhtml);
                } catch (TransformerException e) {
                    useNotSearchedXml = true;
                    ApiPlugin.logErrorMessage(SearchMessages.UseReportConverter_te_applying_xslt_skipped);
                }
            }
        } catch (IOException ioe) {
            throw new Exception(NLS.bind(SearchMessages.ioexception_writing_html_file, originhtml.getAbsolutePath()));
        } catch (TransformerException te) {
            throw new Exception(SearchMessages.UseReportConverter_te_applying_xslt_skipped, te);
        } catch (CoreException e) {
            throw new Exception(NLS.bind(SearchMessages.UseReportConverter_coreexception_writing_html_file, originhtml.getAbsolutePath()));
        } finally {
            if (defaultXsltInputStream != null) {
                defaultXsltInputStream.close();
            }
        }
    }

    /**
	 * Returns path of XSL file to use when generating "not searched"
	 * information.
	 *
	 * @return path to the XSL file
	 */
    String getNotSearchedXSLPath() {
        //$NON-NLS-1$
        return "/notsearched.xsl";
    }

    /**
	 * Writes the referenced member index page
	 *
	 * @param report
	 * @param referees the listing of referencing bundles
	 */
    protected void writeReferencedMemberPage(final Report report, final List<Type> referees) throws Exception {
        PrintWriter writer = null;
        File originhtml = null;
        try {
            File htmlroot = new File(getHtmlLocation(), report.name);
            if (!htmlroot.exists()) {
                htmlroot.mkdirs();
            }
            //$NON-NLS-1$
            originhtml = new File(htmlroot, "index.html");
            if (!originhtml.exists()) {
                originhtml.createNewFile();
            }
            StringBuffer buffer = new StringBuffer();
            buffer.append(HTML_HEADER);
            buffer.append(OPEN_HTML).append(OPEN_HEAD).append(CONTENT_TYPE_META);
            buffer.append(REF_STYLE);
            buffer.append(REF_SCRIPT);
            buffer.append(OPEN_TITLE).append(getReferencedTypeTitle(report.name)).append(CLOSE_TITLE);
            buffer.append(CLOSE_HEAD);
            buffer.append(OPEN_BODY);
            buffer.append(OPEN_H3).append(getReferencedTypeTitle(report.name)).append(CLOSE_H3);
            buffer.append(OPEN_P).append(NLS.bind(SearchMessages.UseReportConverter_list_of_all_refing_bundles, new String[] { "<a href=\"#bundles\">", "</a>" })).append(//$NON-NLS-1$ //$NON-NLS-2$
            CLOSE_P);
            String additional = getAdditionalReferencedTypeInformation();
            if (additional != null) {
                buffer.append(additional);
            }
            buffer.append(getReferencesTableHeader(SearchMessages.UseReportConverter_references, SearchMessages.UseReportConverter_referenced_type, false));
            CountGroup counts = null;
            String link = null;
            File typefile = null;
            TreeMap<IMemberDescriptor, Member> map = null;
            Type type = null;
            for (Entry<Type, TreeMap<IMemberDescriptor, Member>> entry : report.children.entrySet()) {
                map = entry.getValue();
                type = entry.getKey();
                counts = type.counts;
                String fqname = Signatures.getQualifiedTypeSignature((IReferenceTypeDescriptor) type.desc);
                typefile = new File(htmlroot, fqname + HTML_EXTENSION);
                if (!typefile.exists()) {
                    typefile.createNewFile();
                }
                link = extractLinkFrom(htmlroot, typefile.getAbsolutePath());
                buffer.append(getReferenceTableEntry(counts, link, fqname, false));
                writeTypePage(map, type, typefile, fqname);
            }
            buffer.append(CLOSE_TABLE);
            buffer.append(BR);
            buffer.append(OPEN_H4).append(SearchMessages.UseReportConverter_referencing_bundles).append(CLOSE_H4);
            buffer.append(OPEN_P).append(NLS.bind(SearchMessages.UseReportConverter_following_bundles_have_refs, report.name)).append(CLOSE_P);
            //$NON-NLS-1$
            buffer.append("<a name=\"bundles\">").append(CLOSE_A);
            //$NON-NLS-1$
            buffer.append("<table border=\"1\" width=\"80%\">\n");
            buffer.append(OPEN_TR);
            //$NON-NLS-1$ //$NON-NLS-2$
            buffer.append("\t<td bgcolor=\"").append(REFERENCES_TABLE_HEADER_COLOUR).append("\" width=\"50%\">").append(OPEN_B).append(SearchMessages.UseReportConverter_bundle).append(CLOSE_B).append(CLOSE_TD);
            //$NON-NLS-1$ //$NON-NLS-2$
            buffer.append("\t<td bgcolor=\"").append(REFERENCES_TABLE_HEADER_COLOUR).append("\" width=\"20%\" align=\"center\">").append(OPEN_B).append(SearchMessages.UseReportConverter_version).append(CLOSE_B).append(CLOSE_TD);
            //$NON-NLS-1$ //$NON-NLS-2$
            buffer.append("\t<td bgcolor=\"").append(REFERENCES_TABLE_HEADER_COLOUR).append("\" width=\"10%\" align=\"center\">").append(OPEN_B).append(SearchMessages.UseReportConverter_reference_count).append(CLOSE_B).append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            Collections.sort(referees, compare);
            IComponentDescriptor comp = null;
            for (int i = 0; i < referees.size(); i++) {
                type = referees.get(i);
                comp = (IComponentDescriptor) type.desc;
                //$NON-NLS-1$//$NON-NLS-2$
                buffer.append("<tr bgcolor=\"").append(getRowColour(counts)).append("\">\n");
                //$NON-NLS-1$
                buffer.append("\t").append(OPEN_TD).append(OPEN_B).append(comp.getId()).append(CLOSE_B).append(//$NON-NLS-1$
                CLOSE_TD);
                //$NON-NLS-1$
                buffer.append("\t").append(OPEN_TD).append(comp.getVersion()).append(//$NON-NLS-1$
                CLOSE_TD);
                //$NON-NLS-1$
                buffer.append("\t<td align=\"center\">").append(type.counts.getTotalRefCount()).append(//$NON-NLS-1$
                CLOSE_TD);
                buffer.append(CLOSE_TR);
            }
            buffer.append(CLOSE_TABLE);
            //$NON-NLS-1$
            buffer.append(OPEN_P).append("<a href=\"../index.html\">").append(SearchMessages.UseReportConverter_back_to_bundle_index).append(CLOSE_A).append(CLOSE_P);
            buffer.append(W3C_FOOTER);
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(originhtml), IApiCoreConstants.UTF_8));
            writer.println(buffer.toString());
            writer.flush();
        } catch (IOException ioe) {
            throw new Exception(NLS.bind(SearchMessages.ioexception_writing_html_file, originhtml.getAbsolutePath()));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
	 * Returns the colour to use based on certain counts
	 *
	 * @param counts
	 * @return the colour to use
	 * @since 1.1
	 */
    String getRowColour(CountGroup counts) {
        if (counts.getTotalInternalRefCount() > 0) {
            return INTERNAL_REFS_COLOUR;
        }
        if (counts.getTotalIllegalRefCount() > 0) {
            return ILLEGAL_REFS_COLOUR;
        }
        return NORMAL_REFS_COLOUR;
    }

    /**
	 * Returns a string of additional information to print out at the top of the
	 * referenced types page.
	 *
	 * @return additional referenced type information.
	 */
    protected String getAdditionalReferencedTypeInformation() {
        return null;
    }

    /**
	 * Returns the page title to use for the referenced types page
	 *
	 * @param bundle
	 * @return the page title for the referenced types page
	 */
    protected String getReferencedTypeTitle(String bundle) {
        return NLS.bind(SearchMessages.UseReportConverter_types_used_in, bundle);
    }

    /**
	 * Writes the page that displays all of the members used in a type
	 *
	 * @param map
	 * @param type
	 * @param typefile
	 * @param typename
	 * @throws Exception
	 */
    void writeTypePage(Map<IMemberDescriptor, Member> map, Type type, File typefile, String typename) throws Exception {
        PrintWriter writer = null;
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append(HTML_HEADER);
            buffer.append(OPEN_HTML).append(OPEN_HEAD).append(CONTENT_TYPE_META);
            buffer.append(REF_STYLE);
            buffer.append(REF_SCRIPT);
            buffer.append(OPEN_TITLE).append(getTypeTitle(typename)).append(CLOSE_TITLE);
            buffer.append(CLOSE_HEAD);
            buffer.append(OPEN_BODY);
            buffer.append(OPEN_H3).append(getTypeTitle(typename)).append(CLOSE_H3);
            buffer.append(getTypeCountSummary(typename, type.counts, map.size()));
            buffer.append(OPEN_H4).append(getTypeDetailsHeader()).append(CLOSE_H4);
            buffer.append(OPEN_P).append(getTypeDetails()).append(CLOSE_P);
            //$NON-NLS-1$
            buffer.append("<div align=\"left\" class=\"main\">");
            //$NON-NLS-1$
            buffer.append("<table border=\"1\" width=\"80%\">\n");
            buffer.append(OPEN_TR);
            //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            buffer.append("<td bgcolor=\"").append(REFERENCES_TABLE_HEADER_COLOUR).append("\">").append(OPEN_B).append(SearchMessages.UseReportConverter_member).append("</b></td>\n");
            buffer.append(CLOSE_TR);
            IElementDescriptor desc = null;
            for (Entry<IMemberDescriptor, Member> entry : map.entrySet()) {
                desc = entry.getKey();
                buffer.append(OPEN_TR);
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "<td align=\"left\">\n");
                buffer.append(OPEN_B);
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "<a href=\"javascript:void(0)\" class=\"typeslnk\" onclick=\"expand(this)\" title=\"");
                //$NON-NLS-1$
                buffer.append(getDisplayName(desc, true, true)).append(//$NON-NLS-1$
                "\">\n");
                //$NON-NLS-1$//$NON-NLS-2$
                buffer.append("<span>[+] </span>").append(getDisplayName(desc, true, false)).append("\n");
                buffer.append(CLOSE_A).append(CLOSE_B);
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "<div colspan=\"6\" class=\"types\">\n");
                //$NON-NLS-1$
                buffer.append(getReferencesTable(entry.getValue())).append(//$NON-NLS-1$
                "\n");
                buffer.append(CLOSE_DIV);
                buffer.append(CLOSE_TR);
            }
            buffer.append(CLOSE_TABLE);
            buffer.append(CLOSE_DIV);
            //$NON-NLS-1$
            buffer.append(OPEN_P).append("<a href=\"index.html\">").append(SearchMessages.UseReportConverter_back_to_bundle_index).append(CLOSE_A).append(CLOSE_P);
            buffer.append(W3C_FOOTER);
            // write the file
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(typefile), IApiCoreConstants.UTF_8));
            writer.print(buffer.toString());
            writer.flush();
        } catch (IOException ioe) {
            throw new Exception(NLS.bind(SearchMessages.ioexception_writing_html_file, typefile.getAbsolutePath()));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
	 * Returns the header to use for the section that describes the type details
	 * table
	 *
	 * @return the details header
	 */
    protected String getTypeDetailsHeader() {
        return SearchMessages.UseReportConverter_reference_details;
    }

    /**
	 * Returns the blurb that follows the type details header
	 *
	 * @return the details information
	 * @see #getTypeDetailsHeader()
	 */
    protected String getTypeDetails() {
        return SearchMessages.UseReportConverter_click_an_entry_to_see_details;
    }

    /**
	 * Returns the title to use for the type references page
	 *
	 * @param typename
	 * @return the type references page title
	 */
    protected String getTypeTitle(String typename) {
        return NLS.bind(SearchMessages.UseReportConverter_usage_details, Signature.getSimpleName(typename));
    }

    /**
	 * Returns the nested table of references
	 *
	 * @return the nested table of references as a string
	 */
    String getReferencesTable(Member member) {
        StringBuffer buffer = new StringBuffer();
        //$NON-NLS-1$
        buffer.append("<table width=\"100%\" border=\"0\" cellspacing=\"1\" cellpadding=\"6\">\n");
        List<Reference> refs = null;
        Reference ref = null;
        for (Entry<String, List<Reference>> entry : member.children.entrySet()) {
            //$NON-NLS-1$
            buffer.append("<tr align=\"left\"> \n");
            //$NON-NLS-1$
            buffer.append("<td colspan=\"3\" bgcolor=\"#CCCCCC\">").append(OPEN_B).append(entry.getKey()).append(CLOSE_B).append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            //$NON-NLS-1$ //$NON-NLS-2$
            buffer.append("<tr bgcolor=\"").append(REFERENCES_TABLE_HEADER_COLOUR).append("\">");
            //$NON-NLS-1$
            buffer.append("<td align=\"left\" width=\"84%\">").append(OPEN_B).append(SearchMessages.UseReportConverter_reference_location).append(CLOSE_B).append(CLOSE_TD);
            //$NON-NLS-1$
            buffer.append("<td align=\"center\" width=\"8%\">").append(OPEN_B).append(SearchMessages.UseReportConverter_line_number).append(CLOSE_B).append(CLOSE_TD);
            //$NON-NLS-1$
            buffer.append("<td align=\"center\" width=\"8%\">").append(OPEN_B).append(SearchMessages.UseReportConverter_reference_kind).append(CLOSE_B).append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            refs = entry.getValue();
            Collections.sort(refs, compare);
            for (Iterator<Reference> iter2 = refs.iterator(); iter2.hasNext(); ) {
                ref = iter2.next();
                try {
                    String name = getDisplayName(ref.desc, false, true);
                    buffer.append(OPEN_TR);
                    buffer.append(OPEN_TD).append(name).append(CLOSE_TD);
                    //$NON-NLS-1$
                    buffer.append("<td align=\"center\">").append(ref.line).append(//$NON-NLS-1$
                    CLOSE_TD);
                    //$NON-NLS-1$//$NON-NLS-2$
                    buffer.append("<td align=\"center\">").append("<span class=\"typeslnk\"");
                    if (ref.message != null) {
                        //$NON-NLS-1$ //$NON-NLS-2$
                        buffer.append(" title=\"").append(ref.message).append("\"");
                    }
                    //$NON-NLS-1$ //$NON-NLS-2$
                    buffer.append(">").append(VisibilityModifiers.getVisibilityName(ref.vis)).append("</span>");
                    buffer.append(CLOSE_TD).append(CLOSE_TR);
                } catch (CoreException ce) {
                    ApiPlugin.log(ce);
                }
            }
        }
        buffer.append(CLOSE_TABLE);
        return buffer.toString();
    }

    /**
	 * Returns the name to display for the given {@link IElementDescriptor}
	 * which can be qualified or not
	 *
	 * @param desc
	 * @param qualifiedparams
	 * @param qualified
	 * @return the (un)-qualified name to display for the given
	 *         {@link IElementDescriptor}
	 * @throws CoreException
	 */
    String getDisplayName(IElementDescriptor desc, boolean qualifiedparams, boolean qualified) throws CoreException {
        String displayname = null;
        switch(desc.getElementType()) {
            case IElementDescriptor.TYPE:
                {
                    IReferenceTypeDescriptor rtype = (IReferenceTypeDescriptor) desc;
                    displayname = Signatures.getTypeSignature(rtype.getSignature(), rtype.getGenericSignature(), qualified);
                    break;
                }
            case IElementDescriptor.METHOD:
                {
                    IMethodDescriptor method = (IMethodDescriptor) desc;
                    if (qualified) {
                        displayname = Signatures.getQualifiedMethodSignature(method, qualifiedparams, qualifiedparams);
                    } else {
                        displayname = Signatures.getMethodSignature(method, qualifiedparams);
                    }
                    break;
                }
            case IElementDescriptor.FIELD:
                {
                    IFieldDescriptor field = (IFieldDescriptor) desc;
                    if (qualified) {
                        displayname = Signatures.getQualifiedFieldSignature(field);
                    } else {
                        displayname = field.getName();
                    }
                    break;
                }
            default:
                break;
        }
        return displayname;
    }

    /**
	 * Extracts underlying link text from the given absolute filename based off
	 * the root file
	 *
	 * @param root
	 * @param fileName
	 * @return link text pruned via the given root file
	 */
    String extractLinkFrom(File root, String fileName) {
        StringBuffer buffer = new StringBuffer();
        String substring = fileName.substring(root.getAbsolutePath().length()).replace('\\', '/');
        buffer.append('.');
        if (substring.charAt(0) != '/') {
            buffer.append('/');
        }
        buffer.append(substring);
        return String.valueOf(buffer);
    }

    protected String getIndexTitle() {
        return SearchMessages.UseReportConverter_bundle_usage_information;
    }

    protected void writeIndexPage(List<?> scanResult) throws Exception {
        Collections.sort(scanResult, new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                return ((Report) o1).name.compareTo(((Report) o2).name);
            }
        });
        PrintWriter writer = null;
        try {
            File reportIndex = new File(getHtmlLocation(), "index.html");
            if (!reportIndex.exists()) {
                reportIndex.createNewFile();
            }
            setReportIndex(reportIndex);
            StringBuffer buffer = new StringBuffer();
            buffer.append(HTML_HEADER);
            buffer.append(OPEN_HTML).append(OPEN_HEAD).append(CONTENT_TYPE_META);
            writeMetadataHeaders(buffer);
            buffer.append(OPEN_TITLE).append(getIndexTitle()).append(CLOSE_TITLE);
            buffer.append(CLOSE_HEAD);
            buffer.append(OPEN_BODY);
            buffer.append(OPEN_H3).append(getIndexTitle()).append(CLOSE_H3);
            try {
                getMetadata();
                writeMetadataSummary(buffer);
                getFilteredCount();
                writeFilterCount(buffer);
            } catch (Exception e) {
            }
            buffer.append(OPEN_H4).append(SearchMessages.UseReportConvertor_additional_infos_section).append(CLOSE_H4);
            if (hasMissing()) {
                buffer.append(OPEN_P);
                buffer.append(NLS.bind(SearchMessages.UseReportConverter_missing_bundles_prevented_scan, new String[] { " <a href=\"./missing.html\">", "</a>" }));
                buffer.append(CLOSE_P);
            }
            buffer.append(OPEN_P);
            buffer.append(NLS.bind(SearchMessages.UseReportConverter_bundles_that_were_not_searched, new String[] { useNotSearchedXml == false ? "<a href=\"./not_searched.html\">" : "<a href=\"../xml/not_searched.xml\">", "</a></p>\n" }));
            String additional = getAdditionalIndexInfo(scanResult.size() > 0);
            if (additional != null) {
                buffer.append(additional);
            }
            if (scanResult.size() > 0) {
                buffer.append(OPEN_P).append(SearchMessages.UseReportConverter_inlined_description).append(CLOSE_P);
                buffer.append(getColourLegend());
                buffer.append(getReferencesTableHeader(SearchMessages.UseReportConverter_references, SearchMessages.UseReportConverter_bundle, true));
                if (scanResult.size() > 0) {
                    File refereehtml = null;
                    String link = null;
                    for (Object obj : scanResult) {
                        if (obj instanceof Report) {
                            Report report = (Report) obj;
                            refereehtml = new File(getReportsRoot(), report.name + File.separator + "index.html");
                            link = extractLinkFrom(getReportsRoot(), refereehtml.getAbsolutePath());
                            buffer.append(getReferenceTableEntry(report.counts, link, report.name, true));
                        }
                    }
                    buffer.append(CLOSE_TABLE);
                }
            } else {
                buffer.append(getNoReportsInformation());
            }
            buffer.append(W3C_FOOTER);
            buffer.append(CLOSE_BODY).append(CLOSE_HTML);
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(reportIndex), IApiCoreConstants.UTF_8));
            writer.print(buffer.toString());
            writer.flush();
        } catch (IOException e) {
            throw new Exception(NLS.bind(SearchMessages.ioexception_writing_html_file, getReportIndex().getAbsolutePath()));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    protected String getColourLegend() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(OPEN_P);
        buffer.append("<table width=\"20%\" border=\"1\">");
        buffer.append(OPEN_TR);
        buffer.append("<td width=\"25px\" bgcolor=\"").append(INTERNAL_REFS_COLOUR).append("\">\n").append("&nbsp;").append(CLOSE_TD);
        buffer.append("<td width=\"82%\">").append(SearchMessages.UseReportConverter_marks_internal_references).append(CLOSE_TD);
        buffer.append(CLOSE_TR);
        buffer.append(OPEN_TR);
        buffer.append("<td width=\"25px\" bgcolor=\"").append(ILLEGAL_REFS_COLOUR).append("\">\n").append("&nbsp;").append(CLOSE_TD);
        buffer.append("<td width=\"82%\">").append(SearchMessages.UseReportConverter_marks_illegal_use_references).append(CLOSE_TD);
        buffer.append(CLOSE_TR);
        buffer.append(CLOSE_TABLE);
        buffer.append(CLOSE_P);
        return buffer.toString();
    }

    protected String getNoReportsInformation() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(OPEN_P).append(BR).append(SearchMessages.UseReportConverter_no_reported_usage).append(CLOSE_P);
        return buffer.toString();
    }

    void writeMetadataHeaders(StringBuffer buffer) throws Exception {
        writeMetaTag(buffer, "description", SearchMessages.UseReportConverter_root_index_description);
    }

    void writeMetadataSummary(StringBuffer buffer) throws Exception {
        buffer.append(OPEN_H4).append(SearchMessages.UseReportConverter_scan_details).append(CLOSE_H4);
        if (this.metadata != null) {
            buffer.append("<table border=\"0px\" title=\"").append(SearchMessages.UseReportConverter_scan_details).append("\"width=\"50%\">");
            buffer.append(OPEN_TR);
            buffer.append(openTD(14)).append(SearchMessages.UseReportConverter_scan_date).append(CLOSE_TD);
            buffer.append(openTD(36)).append(this.metadata.getRunAtDate()).append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            buffer.append(OPEN_TR);
            buffer.append(openTD(14)).append(SearchMessages.UseReportConverter_description).append(CLOSE_TD);
            String desc = this.metadata.getDescription();
            buffer.append(openTD(36)).append((desc != null ? desc : SearchMessages.UseReportConverter_none)).append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            buffer.append(OPEN_TR);
            buffer.append(openTD(14)).append(SearchMessages.UseReportConverter_includes_API_refs).append(CLOSE_TD);
            buffer.append(openTD(36)).append(this.metadata.includesAPI() ? SearchMessages.UseReportConverter_includes_true : SearchMessages.UseReportConverter_includes_false).append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            buffer.append(OPEN_TR);
            buffer.append(openTD(14)).append(SearchMessages.UseReportConverter_includes_internal_refs).append(CLOSE_TD);
            buffer.append(openTD(36)).append(this.metadata.includesInternal() ? SearchMessages.UseReportConverter_includes_true : SearchMessages.UseReportConverter_includes_false).append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            buffer.append(OPEN_TR);
            buffer.append(openTD(14)).append(SearchMessages.UseReportConverter_includes_illegal_use).append(CLOSE_TD);
            buffer.append(openTD(36)).append(this.metadata.includesIllegalUse() ? SearchMessages.UseReportConverter_includes_true : SearchMessages.UseReportConverter_includes_false).append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            buffer.append(OPEN_TR);
            buffer.append(openTD(14)).append(SearchMessages.UseReportConverter_baseline_loc).append(CLOSE_TD);
            buffer.append(openTD(36)).append(TextProcessor.process(this.metadata.getBaselineLocation())).append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            buffer.append(OPEN_TR);
            buffer.append(openTD(14)).append(SearchMessages.UseReportConverter_scope_pattern).append(CLOSE_TD);
            buffer.append(openTD(36)).append(this.metadata.getScopePattern()).append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            buffer.append(OPEN_TR);
            buffer.append(openTD(14)).append(SearchMessages.UseReportConverter_reference_pattern).append(CLOSE_TD);
            buffer.append(openTD(36)).append(this.metadata.getReferencePattern()).append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            buffer.append(OPEN_TR);
            buffer.append(openTD(14)).append(SearchMessages.UseReportConverter_report_location).append(CLOSE_TD);
            buffer.append(openTD(36)).append(TextProcessor.process(this.metadata.getReportLocation())).append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            buffer.append(OPEN_TR);
            buffer.append(openTD(14)).append(SearchMessages.UseReportConverter_api_pattern).append(CLOSE_TD);
            buffer.append(openTD(36));
            String[] patterns = this.metadata.getApiPatterns();
            if (patterns != null) {
                for (String pattern : patterns) {
                    buffer.append(pattern).append(BR);
                }
            } else {
                buffer.append(SearchMessages.UseReportConverter_none);
            }
            buffer.append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            buffer.append(OPEN_TR);
            buffer.append(openTD(14)).append(SearchMessages.UseReportConverter_internal_patterns).append(CLOSE_TD);
            buffer.append(openTD(36));
            patterns = this.metadata.getInternalPatterns();
            if (patterns != null) {
                for (String pattern : patterns) {
                    buffer.append(pattern).append(BR);
                }
            } else {
                buffer.append(SearchMessages.UseReportConverter_none);
            }
            buffer.append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            buffer.append(OPEN_TR);
            buffer.append(openTD(14)).append(SearchMessages.UseReportConverter_archive_patterns).append(CLOSE_TD);
            buffer.append(openTD(36));
            patterns = this.metadata.getArchivePatterns();
            if (patterns != null) {
                for (String pattern : patterns) {
                    buffer.append(pattern).append(BR);
                }
            } else {
                buffer.append(SearchMessages.UseReportConverter_none);
            }
            buffer.append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            buffer.append(OPEN_TR);
            buffer.append(openTD(14)).append(SearchMessages.UseReportConverter_filter_pattern).append(CLOSE_TD);
            buffer.append(openTD(36));
            if (this.frompatterns != null) {
                for (Pattern frompattern : this.frompatterns) {
                    buffer.append(frompattern.pattern()).append(BR);
                }
            } else {
                buffer.append(SearchMessages.UseReportConverter_none);
            }
            buffer.append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            buffer.append(OPEN_TR);
            buffer.append(openTD(14)).append(SearchMessages.UseReportConverter_to_filter_patterns).append(CLOSE_TD);
            buffer.append(openTD(36));
            if (this.topatterns != null) {
                for (Pattern topattern : this.topatterns) {
                    buffer.append(topattern.pattern()).append(BR);
                }
            } else {
                buffer.append(SearchMessages.UseReportConverter_none);
            }
            buffer.append(OPEN_TR);
            buffer.append(openTD(14)).append(SearchMessages.UseReportConverter_additional_filters).append(CLOSE_TD);
            buffer.append(openTD(36));
            if (this.metadata.getAdditionalfilters() != null && !this.metadata.getAdditionalfilters().isEmpty()) {
                buffer.append(TextProcessor.process(this.metadata.getAdditionalfilters()));
            } else {
                buffer.append(SearchMessages.UseReportConverter_none);
            }
            buffer.append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            buffer.append(CLOSE_TD);
            buffer.append(CLOSE_TR);
            buffer.append(CLOSE_TABLE);
        } else {
            buffer.append(OPEN_P).append(SearchMessages.UseReportConverter_no_additional_scan_info).append(CLOSE_P);
        }
    }

    void writeFilterCount(StringBuffer buffer) throws Exception {
        boolean isAdditionFilterProvided = this.metadata.getAdditionalfilters() != null && !this.metadata.getAdditionalfilters().isEmpty();
        if (this.filteredCount != -1 && isAdditionFilterProvided) {
            buffer.append(OPEN_H4).append(SearchMessages.UseReportConverter_filter_information).append(CLOSE_H4);
            buffer.append(OPEN_TR);
            buffer.append(openTD(14)).append(SearchMessages.UseReportConverter_number_filtered).append(CLOSE_TD);
            buffer.append(openTD(36)).append(this.filteredCount).append(CLOSE_TD);
            buffer.append(CLOSE_TR);
        }
    }

    IMetadata getMetadata() throws Exception {
        if (this.metadata == null) {
            File xml = null;
            try {
                xml = new File(getReportsRoot(), "meta" + XML_EXTENSION);
                if (!xml.exists()) {
                    xml = new File(getReportsRoot() + File.separator + "xml", "meta" + XML_EXTENSION);
                }
                if (xml.exists()) {
                    String xmlstr = Util.getFileContentAsString(xml);
                    Element doc = Util.parseDocument(xmlstr.trim());
                    this.metadata = new UseMetadata();
                    Element element = null;
                    String value = null, name = null;
                    NodeList nodes = doc.getElementsByTagName("*");
                    for (int i = 0; i < nodes.getLength(); i++) {
                        element = (Element) nodes.item(i);
                        value = element.getAttribute(UseMetadata.VALUE);
                        name = element.getNodeName();
                        if (UseMetadata.FLAGS.equals(name)) {
                            try {
                                this.metadata.setSearchflags(Integer.parseInt(value));
                            } catch (NumberFormatException nfe) {
                            }
                            continue;
                        }
                        if (UseMetadata.RUNATDATE.equals(name)) {
                            this.metadata.setRunAtDate(value);
                            continue;
                        }
                        if (UseMetadata.DESCRIPTION.equals(name)) {
                            this.metadata.setDescription(value);
                            continue;
                        }
                        if (UseMetadata.BASELINELOCATION.equals(name)) {
                            this.metadata.setBaselineLocation(value);
                            continue;
                        }
                        if (UseMetadata.REPORTLOCATION.equals(name)) {
                            this.metadata.setReportLocation(value);
                            continue;
                        }
                        if (UseMetadata.SCOPEPATTERN.equals(name)) {
                            this.metadata.setScopePattern(value);
                            continue;
                        }
                        if (UseMetadata.REFERENCEPATTERN.equals(name)) {
                            this.metadata.setReferencePattern(value);
                            continue;
                        }
                        if (UseMetadata.APIPATTERNS.equals(name)) {
                            this.metadata.setApiPatterns(readPatterns(element));
                            continue;
                        }
                        if (UseMetadata.INTERNALPATTERNS.equals(name)) {
                            this.metadata.setInternalPatterns(readPatterns(element));
                            continue;
                        }
                        if (UseMetadata.ARCHIVEPATTERNS.equals(name)) {
                            this.metadata.setArchivePatterns(readPatterns(element));
                            continue;
                        }
                        if (UseMetadata.ADDITIONALFILTERS.equals(name)) {
                            this.metadata.setAdditionalfilters(value);
                            continue;
                        }
                    }
                }
            } catch (CoreException e) {
                throw new Exception(NLS.bind(SearchMessages.UseReportConverter_core_exep_reading_metadata, xml.getAbsolutePath()));
            }
        }
        return this.metadata;
    }

    int getFilteredCount() throws Exception {
        if (this.filteredCount == -1) {
            File xml = null;
            try {
                xml = new File(getReportsRoot(), "counts" + XML_EXTENSION);
                if (!xml.exists()) {
                    xml = new File(getReportsRoot() + File.separator + "xml", "meta" + XML_EXTENSION);
                }
                if (xml.exists()) {
                    String xmlstr = Util.getFileContentAsString(xml);
                    Element doc = Util.parseDocument(xmlstr.trim());
                    NamedNodeMap nodes = doc.getAttributes();
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Node element = nodes.item(i);
                        String value = element.getNodeValue();
                        String name = element.getNodeName();
                        if (name.equals(IApiXmlConstants.ATTR_COUNT_FILTERED)) {
                            filteredCount = Integer.parseInt(value);
                        }
                    }
                }
            } catch (CoreException e) {
                throw new Exception(NLS.bind(SearchMessages.UseReportConverter_core_exep_reading_metadata, xml.getAbsolutePath()));
            }
        }
        return filteredCount;
    }

    private String[] readPatterns(Element element) {
        String[] pats = null;
        NodeList patterns = element.getElementsByTagName(UseMetadata.PATTERN);
        int length = patterns.getLength();
        if (length > 0) {
            pats = new String[length];
            for (int j = 0; j < length; j++) {
                pats[j] = ((Element) patterns.item(j)).getAttribute(UseMetadata.VALUE);
            }
        }
        return pats;
    }

    void writeMetaTag(StringBuffer buffer, String name, String content) {
        buffer.append("<meta name=\"").append(name).append("\" content=\"").append(content).append("\">");
    }

    String getReferencesTableHeader(String sectionname, String columnname, boolean includeversion) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(OPEN_H4).append(sectionname).append(CLOSE_H4);
        buffer.append("<table border=\"1\" width=\"80%\">\n");
        buffer.append(OPEN_TR);
        buffer.append("\t<td bgcolor=\"").append(REFERENCES_TABLE_HEADER_COLOUR).append("\" width=\"30%\">").append(OPEN_B).append(columnname).append(CLOSE_B).append(CLOSE_TD);
        if (includeversion) {
            buffer.append("\t<td bgcolor=\"").append(REFERENCES_TABLE_HEADER_COLOUR).append("\" align=\"center\" width=\"20%\" title=\"");
            buffer.append(SearchMessages.UseReportConverter_version_column_description).append("\"\">");
            buffer.append(OPEN_B).append(SearchMessages.UseReportConverter_version).append(CLOSE_B).append(CLOSE_TD);
        }
        buffer.append("\t<td bgcolor=\"").append(REFERENCES_TABLE_HEADER_COLOUR).append("\" align=\"center\" width=\"8%\" title=\"");
        buffer.append(SearchMessages.UseReportConverter_api_ref_description).append("\">");
        buffer.append(OPEN_B).append(SearchMessages.UseReportConverter_api_references).append(CLOSE_B).append(CLOSE_TD);
        buffer.append("\t<td bgcolor=\"").append(REFERENCES_TABLE_HEADER_COLOUR).append("\" align=\"center\" width=\"8%\" title=\"");
        buffer.append(SearchMessages.UseReportConverter_internal_ref_description).append("\">");
        buffer.append(OPEN_B).append(SearchMessages.UseReportConverter_internal_references).append(CLOSE_B).append(CLOSE_TD);
        buffer.append("\t<td bgcolor=\"").append(REFERENCES_TABLE_HEADER_COLOUR).append("\" align=\"center\" width=\"8%\" title=\"");
        buffer.append(SearchMessages.UseReportConverter_permissible_ref_description).append("\">");
        buffer.append(OPEN_B).append(SearchMessages.UseReportConverter_internal_permissible_references).append(CLOSE_B).append(CLOSE_TD);
        buffer.append("\t<td bgcolor=\"").append(REFERENCES_TABLE_HEADER_COLOUR).append("\" align=\"center\" width=\"8%\" title=\"");
        buffer.append(SearchMessages.UseReportConverter_fragment_ref_description).append("\">");
        buffer.append(OPEN_B).append(SearchMessages.UseReportConverter_fragment_permissible_references).append(CLOSE_B).append(CLOSE_TD);
        buffer.append("\t<td bgcolor=\"").append(REFERENCES_TABLE_HEADER_COLOUR).append("\" align=\"center\" width=\"8%\" title=\"");
        buffer.append(SearchMessages.UseReportConverter_illegal_ref_description).append("\">");
        buffer.append(OPEN_B).append(SearchMessages.UseReportConverter_illegal).append(CLOSE_B).append(CLOSE_TD);
        return buffer.toString();
    }

    String getReferenceTableEntry(CountGroup counts, String link, String linktext, boolean includeversion) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<tr bgcolor=\"").append(getRowColour(counts)).append("\">\n");
        buffer.append("\t<td><b><a href=\"").append(link).append("\">").append(getBundleOnlyName(linktext)).append("</a>").append(CLOSE_B).append(CLOSE_TD);
        if (includeversion) {
            buffer.append("\t<td align=\"left\">").append(getVersion(linktext)).append(CLOSE_TD);
        }
        buffer.append("\t<td align=\"center\">").append(counts.getTotalApiRefCount()).append(CLOSE_TD);
        buffer.append("\t<td align=\"center\">").append(counts.getTotalInternalRefCount()).append(CLOSE_TD);
        buffer.append("\t<td align=\"center\">").append(counts.getTotalPermissableRefCount()).append(CLOSE_TD);
        buffer.append("\t<td align=\"center\">").append(counts.getTotalFragmentPermissibleRefCount()).append(CLOSE_TD);
        buffer.append("\t<td align=\"center\">").append(counts.getTotalIllegalRefCount()).append(CLOSE_TD);
        buffer.append(CLOSE_TR);
        return buffer.toString();
    }

    String getBundleOnlyName(String text) {
        int idx = text.indexOf('(');
        if (idx > -1) {
            return text.substring(0, idx - 1);
        }
        return text;
    }

    String getVersion(String text) {
        int idx = text.indexOf('(');
        if (idx > -1) {
            int idx2 = text.indexOf(')', idx);
            String version = text.substring(idx + 1, idx2);
            try {
                Version ver = new Version(version);
                return ver.toString();
            } catch (IllegalArgumentException iae) {
            }
        }
        return "-";
    }

    protected String getAdditionalIndexInfo(boolean hasreports) {
        return null;
    }

    protected String getTypeCountSummary(String typename, CountGroup counts, int membercount) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(OPEN_H4).append(SearchMessages.UseReportConverter_summary).append(CLOSE_H4);
        buffer.append(OPEN_P).append(NLS.bind(SearchMessages.UseReportConverter___has_total_refs, new String[] { typename, Integer.toString(counts.getTotalRefCount()), Integer.toString(membercount) })).append(CLOSE_P);
        return buffer.toString();
    }
}
