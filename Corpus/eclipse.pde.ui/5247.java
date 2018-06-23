/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alena Laskavaia - Bug 453392 - No debug options help
 *******************************************************************************/
package org.eclipse.pde.internal.ui.launcher;

import java.util.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class TracingPropertySource {

    private IPluginModelBase fModel;

    private Vector<PropertyEditor> fDescriptors;

    private Hashtable<?, ?> fTemplate;

    private Hashtable<String, Object> fValues;

    //$NON-NLS-1$ //$NON-NLS-2$
    private static final String[] fBooleanChoices = { "false", "true" };

    private Properties fMasterOptions;

    private boolean fModified;

    private TracingBlock fBlock;

    private abstract class PropertyEditor {

        private String key;

        private String label;

        private String comment;

        public  PropertyEditor(String key, String label, String comment) {
            this.key = key;
            this.label = label;
            this.comment = comment;
        }

        public String getKey() {
            return key;
        }

        public String getLabel() {
            return label;
        }

        public String getComment() {
            return comment;
        }

        abstract void create(Composite parent, boolean enabled);

        abstract void initialize();

        protected void valueModified(Object value) {
            fValues.put(getKey(), value);
            fModified = true;
            fBlock.getTab().scheduleUpdateJob();
        }

        /**
		 * Creates a comment decorator for the options, currently it is a
		 * tooltip, but technically it can be label or decorator on control
		 *
		 * @param target
		 *            - the control to be decorated
		 * @param enabled
		 */
        protected void createCommentDecorator(Control target, boolean enabled) {
            String commentText = getFormattedComment();
            if (!commentText.isEmpty()) {
                target.setToolTipText(commentText);
            }
        }

        /**
		 * Takes the comment lines prefixed by # and formats them. If two or
		 * more lines sequentially start with # without empty lines in between
		 * it will be joined. Empty line between comment sections will be
		 * preserved
		 *
		 * @return formatted comment
		 */
        protected String getFormattedComment() {
            String commentOrig = getComment();
            if (commentOrig == null || commentOrig.trim().isEmpty())
                //$NON-NLS-1$
                return "";
            //$NON-NLS-1$
            String lines[] = commentOrig.trim().split("\\r?\\n");
            StringBuilder commentBuilder = new StringBuilder();
            boolean needsSpace = false;
            for (String string : lines) {
                // remove leading hash and trim spaces around
                //$NON-NLS-1$ //$NON-NLS-2$
                string = string.replaceFirst("^#", "").trim();
                if (string.isEmpty()) {
                    //$NON-NLS-1$
                    commentBuilder.append(//$NON-NLS-1$
                    "\n\n");
                    needsSpace = false;
                } else {
                    if (needsSpace)
                        //$NON-NLS-1$
                        commentBuilder.append(//$NON-NLS-1$
                        " ");
                    commentBuilder.append(string);
                    needsSpace = true;
                }
            }
            String processed = commentBuilder.toString().trim();
            //$NON-NLS-1$
            int k = processed.lastIndexOf("\n\n");
            if (// multi section comment, just keep last section
            k > 0) {
                // since we trimmed the string k guaranteed to be >0 and <length-2 (unless -1)
                return processed.substring(k + 2);
            }
            return processed;
        }
    }

    private class BooleanEditor extends PropertyEditor {

        private Button checkbox;

        public  BooleanEditor(String key, String label, String comment) {
            super(key, label, comment);
        }

        @Override
        public void create(Composite parent, boolean enabled) {
            checkbox = fBlock.getToolkit().createButton(parent, getLabel(), SWT.CHECK);
            TableWrapData td = new TableWrapData();
            td.colspan = 2;
            checkbox.setLayoutData(td);
            checkbox.setEnabled(enabled);
            createCommentDecorator(checkbox, enabled);
        }

        public void update() {
            Integer value = (Integer) fValues.get(getKey());
            checkbox.setSelection(value.intValue() == 1);
        }

        @Override
        public void initialize() {
            update();
            checkbox.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    int value = checkbox.getSelection() ? 1 : 0;
                    valueModified(Integer.valueOf(value));
                }
            });
        }
    }

    private class TextEditor extends PropertyEditor {

        private Text text;

        public  TextEditor(String key, String label, String comment) {
            super(key, label, comment);
        }

        @Override
        public void create(Composite parent, boolean enabled) {
            Label label = fBlock.getToolkit().createLabel(parent, getLabel());
            label.setEnabled(enabled);
            TableWrapData td = new TableWrapData();
            td.valign = TableWrapData.MIDDLE;
            label.setLayoutData(td);
            //$NON-NLS-1$
            text = fBlock.getToolkit().createText(parent, "");
            td = new TableWrapData(TableWrapData.FILL_GRAB);
            //gd.widthHint = 100;
            text.setLayoutData(td);
            text.setEnabled(enabled);
            createCommentDecorator(label, enabled);
        }

        public void update() {
            String value = (String) fValues.get(getKey());
            text.setText(value);
        }

        @Override
        public void initialize() {
            update();
            text.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    valueModified(text.getText());
                }
            });
        }
    }

    public  TracingPropertySource(IPluginModelBase model, Properties masterOptions, Hashtable<?, ?> template, TracingBlock block) {
        fModel = model;
        fMasterOptions = masterOptions;
        fTemplate = template;
        fBlock = block;
        fValues = new Hashtable();
    }

    public IPluginModelBase getModel() {
        return fModel;
    }

    private Object[] getSortedKeys(int size) {
        Object[] keyArray = new Object[size];
        int i = 0;
        for (Enumeration<?> keys = fTemplate.keys(); keys.hasMoreElements(); ) {
            String key = (String) keys.nextElement();
            keyArray[i++] = key;
        }
        Arrays.sort(keyArray, new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                return compareKeys(o1, o2);
            }
        });
        return keyArray;
    }

    private int compareKeys(Object o1, Object o2) {
        String s1 = (String) o1;
        String s2 = (String) o2;
        // equal
        return s1.compareTo(s2);
    }

    public void createContents(Composite parent, boolean enabled) {
        fDescriptors = new Vector();
        TableWrapLayout layout = new TableWrapLayout();
        layout.numColumns = 2;
        layout.rightMargin = 10;
        layout.leftMargin = 10;
        parent.setLayout(layout);
        boolean bordersNeeded = false;
        Object[] sortedKeys = getSortedKeys(fTemplate.size());
        for (int i = 0; i < sortedKeys.length; i++) {
            String key = (String) sortedKeys[i];
            IPath path = new Path(key);
            path = path.removeFirstSegments(1);
            String shortKey = path.toString();
            String value = (String) fTemplate.get(key);
            String lvalue = null;
            String masterValue = fMasterOptions.getProperty(key);
            //$NON-NLS-1$
            String commentValue = fMasterOptions.getProperty("#" + key);
            PropertyEditor editor;
            if (value != null)
                lvalue = value.toLowerCase(Locale.ENGLISH);
            if (//$NON-NLS-1$ //$NON-NLS-2$
            lvalue != null && (lvalue.equals("true") || lvalue.equals("false"))) {
                editor = new BooleanEditor(shortKey, shortKey, commentValue);
                if (masterValue != null) {
                    Integer mvalue = Integer.valueOf(//$NON-NLS-1$
                    masterValue.equals(//$NON-NLS-1$
                    "true") ? 1 : 0);
                    fValues.put(shortKey, mvalue);
                }
            } else {
                editor = new TextEditor(shortKey, shortKey, commentValue);
                if (masterValue != null) {
                    fValues.put(shortKey, masterValue);
                }
                bordersNeeded = true;
            }
            editor.create(parent, enabled);
            editor.initialize();
            fDescriptors.add(editor);
            if (bordersNeeded)
                fBlock.getToolkit().paintBordersFor(parent);
        }
    }

    /**
	 */
    public void save() {
        String pid = fModel.getPluginBase().getId();
        for (Enumeration<String> keys = fValues.keys(); keys.hasMoreElements(); ) {
            String shortKey = keys.nextElement();
            Object value = fValues.get(shortKey);
            String svalue = value.toString();
            if (value instanceof Integer)
                svalue = fBooleanChoices[((Integer) value).intValue()];
            IPath path = new Path(pid).append(shortKey);
            fMasterOptions.setProperty(path.toString(), svalue);
        }
        fModified = false;
    }

    public void dispose() {
    }

    public boolean isModified() {
        return fModified;
    }
}
