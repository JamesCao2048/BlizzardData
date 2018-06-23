/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Chris Aniszczyk <zx@us.ibm.com> - initial API and implementation
 *     Willian Mitsuda <wmitsuda@gmail.com> - bug 209841
 *     Benjamin Cabe <benjamin.cabe@anyware-tech.com> - bug 209487
 *     Alexander Kurtakov <akurtako@redhat.com> - bug 415649
 *******************************************************************************/
package org.eclipse.pde.internal.runtime.spy;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.help.IContext;
import org.eclipse.help.internal.context.Context;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.runtime.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * @since 3.4
 */
public class SpyFormToolkit extends FormToolkit {

    //$NON-NLS-1$
    private static final String CLASS_PROTOCOL_PREFIX = "class://";

    //$NON-NLS-1$
    private static final String BUNDLE_PROTOCOL_PREFIX = "bundle://";

    private class SpyHyperlinkAdapter extends HyperlinkAdapter {

        private PopupDialog fDialog;

        public  SpyHyperlinkAdapter(PopupDialog dialog) {
            this.fDialog = dialog;
        }

        @Override
        public void linkActivated(HyperlinkEvent e) {
            String href = (String) e.getHref();
            if (href.startsWith(CLASS_PROTOCOL_PREFIX)) {
                String clazz = href.substring(CLASS_PROTOCOL_PREFIX.length());
                Bundle bundle = (Bundle) bundleClassByName.get(clazz);
                SpyIDEUtil.openClass(bundle.getSymbolicName(), clazz);
                fDialog.close();
            } else if (href.startsWith(BUNDLE_PROTOCOL_PREFIX)) {
                String bundle = href.substring(BUNDLE_PROTOCOL_PREFIX.length());
                SpyIDEUtil.openBundleManifest(bundle);
                fDialog.close();
            }
        }
    }

    private class SaveImageAction extends Action {

        private Image image;

        public  SaveImageAction(Image image) {
            this.image = image;
        }

        @Override
        public void run() {
            FileDialog fileChooser = new FileDialog(PDERuntimePlugin.getActiveWorkbenchShell(), SWT.SAVE);
            //$NON-NLS-1$
            fileChooser.setFileName("image");
            //$NON-NLS-1$
            fileChooser.setFilterExtensions(new String[] { "*.png" });
            //$NON-NLS-1$
            fileChooser.setFilterNames(new String[] { "PNG (*.png)" });
            String filename = fileChooser.open();
            if (filename == null)
                return;
            int filetype = determineFileType(filename);
            if (filetype == SWT.IMAGE_UNDEFINED) {
                return;
            }
            ImageLoader loader = new ImageLoader();
            loader.data = new ImageData[] { image.getImageData() };
            loader.save(filename, filetype);
        }

        private int determineFileType(String filename) {
            String ext = filename.substring(filename.lastIndexOf('.') + 1);
            if (//$NON-NLS-1$
            ext.equalsIgnoreCase("gif"))
                return SWT.IMAGE_GIF;
            if (//$NON-NLS-1$
            ext.equalsIgnoreCase("ico"))
                return SWT.IMAGE_ICO;
            if (//$NON-NLS-1$//$NON-NLS-2$
            ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg"))
                return SWT.IMAGE_JPEG;
            if (//$NON-NLS-1$
            ext.equalsIgnoreCase("png"))
                return SWT.IMAGE_PNG;
            return SWT.IMAGE_UNDEFINED;
        }
    }

    private Map bundleClassByName = new HashMap();

    private PopupDialog dialog;

    //$NON-NLS-1$
    private static String HELP_KEY = "org.eclipse.ui.help";

    public  SpyFormToolkit(PopupDialog dialog) {
        super(Display.getDefault());
        this.dialog = dialog;
    }

    @Override
    public FormText createFormText(Composite parent, boolean trackFocus) {
        FormText text = super.createFormText(parent, trackFocus);
        if (PDERuntimePlugin.HAS_IDE_BUNDLES) {
            text.addHyperlinkListener(new SpyHyperlinkAdapter(dialog));
            addCopyQNameMenuItem(text);
        }
        return text;
    }

    private void addCopyQNameMenuItem(final FormText formText) {
        Menu menu = formText.getMenu();
        final MenuItem copyQNameItem = new MenuItem(menu, SWT.PUSH);
        copyQNameItem.setImage(PDERuntimePluginImages.get(PDERuntimePluginImages.IMG_COPY_QNAME));
        copyQNameItem.setText(PDERuntimeMessages.SpyFormToolkit_copyQualifiedName);
        SelectionListener listener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.widget == copyQNameItem) {
                    Clipboard clipboard = null;
                    try {
                        clipboard = new Clipboard(formText.getDisplay());
                        clipboard.setContents(new Object[] { ((String) formText.getSelectedLinkHref()).substring(CLASS_PROTOCOL_PREFIX.length()) }, new Transfer[] { TextTransfer.getInstance() });
                    } finally {
                        if (clipboard != null)
                            clipboard.dispose();
                    }
                }
            }
        };
        copyQNameItem.addSelectionListener(listener);
        menu.addMenuListener(new MenuAdapter() {

            @Override
            public void menuShown(MenuEvent e) {
                String href = (String) formText.getSelectedLinkHref();
                copyQNameItem.setEnabled(href != null && href.startsWith(CLASS_PROTOCOL_PREFIX));
            }
        });
    }

    public String createInterfaceSection(FormText text, String title, Class[] clazzes) {
        StringBuffer buffer = new StringBuffer();
        if (clazzes.length > 0) {
            //$NON-NLS-1$
            buffer.append("<p>");
            buffer.append(title);
            //$NON-NLS-1$
            buffer.append("</p>");
            for (int i = 0; i < clazzes.length; i++) {
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "<li bindent=\"20\" style=\"image\" value=\"interface\">");
                createClassReference(buffer, clazzes[i]);
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "</li>");
            }
            Image image = PDERuntimePluginImages.get(PDERuntimePluginImages.IMG_INTERFACE_OBJ);
            //$NON-NLS-1$
            text.setImage("interface", image);
        }
        return buffer.toString();
    }

    public String createClassSection(FormText text, String title, Class[] clazzes) {
        StringBuffer buffer = new StringBuffer();
        if (clazzes.length > 0) {
            //$NON-NLS-1$
            buffer.append("<p>");
            buffer.append(title);
            //$NON-NLS-1$
            buffer.append("</p>");
            for (int i = 0; i < clazzes.length; i++) {
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "<li bindent=\"20\" style=\"image\" value=\"class\">");
                createClassReference(buffer, clazzes[i]);
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "</li>");
            }
            Image image = PDERuntimePluginImages.get(PDERuntimePluginImages.IMG_CLASS_OBJ);
            //$NON-NLS-1$
            text.setImage("class", image);
        }
        return buffer.toString();
    }

    public String createIdentifierSection(FormText text, String title, String[] ids) {
        StringBuffer buffer = new StringBuffer();
        if (ids.length > 0) {
            //$NON-NLS-1$
            buffer.append("<p>");
            buffer.append(title);
            //$NON-NLS-1$
            buffer.append("</p>");
            for (int i = 0; i < ids.length; i++) {
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "<li bindent=\"20\" style=\"image\" value=\"id\">");
                buffer.append(ids[i]);
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "</li>");
            }
            Image image = PDERuntimePluginImages.get(PDERuntimePluginImages.IMG_ID_OBJ);
            //$NON-NLS-1$
            text.setImage("id", image);
        }
        return buffer.toString();
    }

    public String createHelpIdentifierSection(Widget widget) {
        return createHelpIdentifierSection(widget.getData(HELP_KEY));
    }

    public String createHelpIdentifierSection(IContext context) {
        if (context instanceof Context)
            return createHelpIdentifierSection(((Context) context).getId());
        //$NON-NLS-1$
        return "";
    }

    private String createHelpIdentifierSection(Object help) {
        StringBuffer buffer = new StringBuffer();
        if (help != null) {
            //$NON-NLS-1$
            buffer.append("<li bindent=\"20\" style=\"image\" value=\"contextid\">");
            buffer.append(help);
            //$NON-NLS-1$
            buffer.append("</li>");
        }
        return buffer.toString();
    }

    private void createClassReference(StringBuffer buffer, Class clazz) {
        Bundle bundle = PDERuntimePlugin.HAS_IDE_BUNDLES ? FrameworkUtil.getBundle(clazz) : null;
        if (bundle != null) {
            bundleClassByName.put(clazz.getName(), bundle);
            //$NON-NLS-1$
            buffer.append("<a href=\"").append(CLASS_PROTOCOL_PREFIX).append(//$NON-NLS-1$
            clazz.getName()).append(//$NON-NLS-1$
            "\">").append(//$NON-NLS-1$
            getSimpleName(clazz)).append(//$NON-NLS-1$
            "</a>");
        } else {
            buffer.append(clazz.getName());
        }
    }

    // TODO refactor me, I'm ugly
    public void generatePluginDetailsText(Bundle bundle, String objectId, String objectType, StringBuffer buffer, FormText text) {
        if (bundle != null) {
            String version = (String) (bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION));
            //$NON-NLS-1$
            buffer.append("<p>");
            buffer.append(PDERuntimeMessages.SpyDialog_contributingPluginId_title);
            //$NON-NLS-1$
            buffer.append("</p>");
            //$NON-NLS-1$
            buffer.append("<li bindent=\"20\" style=\"image\" value=\"plugin\">");
            if (PDERuntimePlugin.HAS_IDE_BUNDLES) {
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "<a href=\"");
                buffer.append(BUNDLE_PROTOCOL_PREFIX);
                buffer.append(bundle.getSymbolicName());
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "\">");
            }
            buffer.append(bundle.getSymbolicName());
            //$NON-NLS-1$
            buffer.append(" (");
            buffer.append(version);
            //$NON-NLS-1$
            buffer.append(")");
            if (PDERuntimePlugin.HAS_IDE_BUNDLES) {
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "</a>");
            }
            //$NON-NLS-1$
            buffer.append("</li>");
            Image pluginImage = PDERuntimePluginImages.get(PDERuntimePluginImages.IMG_PLUGIN_OBJ);
            //$NON-NLS-1$
            text.setImage("plugin", pluginImage);
            if (objectId != null) {
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "<p>");
                buffer.append(NLS.bind(PDERuntimeMessages.SpyDialog_contributingPluginId_desc, objectType));
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "</p>");
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "<li bindent=\"20\" style=\"image\" value=\"id\">");
                buffer.append(objectId);
                //$NON-NLS-1$
                buffer.append(//$NON-NLS-1$
                "</li>");
            }
        }
    }

    private String getSimpleName(Class clazz) {
        String fullName = clazz.getName();
        int index = fullName.lastIndexOf('.');
        String name = fullName.substring(index + 1, fullName.length());
        if (name != null)
            return name;
        return fullName;
    }

    private ToolBarManager createSectionToolbar(Section section) {
        //$NON-NLS-1$
        Object object = section.getData("toolbarmanager");
        if (object instanceof ToolBarManager) {
            return (ToolBarManager) object;
        }
        ToolBarManager manager = new ToolBarManager(SWT.FLAT);
        ToolBar toolbar = manager.createControl(section);
        final Cursor handCursor = Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND);
        toolbar.setCursor(handCursor);
        section.setTextClient(toolbar);
        //$NON-NLS-1$
        section.setData("toolbarmanager", manager);
        return manager;
    }

    public void createImageAction(Section section, Image image) {
        if (image == null)
            return;
        ToolBarManager manager = createSectionToolbar(section);
        SaveImageAction action = new SaveImageAction(image);
        action.setText(PDERuntimeMessages.SpyFormToolkit_saveImageAs_title);
        action.setImageDescriptor(PDERuntimePluginImages.SAVE_IMAGE_AS_OBJ);
        manager.add(action);
        manager.update(true);
    }
}
