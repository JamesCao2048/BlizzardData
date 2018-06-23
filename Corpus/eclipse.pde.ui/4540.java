/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     EclipseSource Corporation - ongoing enhancements
 *     Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bugs 202583, 207344, 207101
 *******************************************************************************/
package org.eclipse.ui.internal.views.log;

import com.ibm.icu.text.DateFormat;
import java.util.ArrayList;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class LogViewLabelProvider extends LabelProvider implements ITableLabelProvider, ITableFontProvider {

    private static int MAX_LABEL_LENGTH = 200;

    private Image infoImage;

    private Image okImage;

    private Image errorImage;

    private Image warningImage;

    private Image errorWithStackImage;

    private Image hierarchicalImage;

    ArrayList consumers = new ArrayList();

    private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    private LogView logView;

    public  LogViewLabelProvider(LogView logView) {
        errorImage = SharedImages.getImage(SharedImages.DESC_ERROR_ST_OBJ);
        warningImage = SharedImages.getImage(SharedImages.DESC_WARNING_ST_OBJ);
        infoImage = SharedImages.getImage(SharedImages.DESC_INFO_ST_OBJ);
        okImage = SharedImages.getImage(SharedImages.DESC_OK_ST_OBJ);
        errorWithStackImage = SharedImages.getImage(SharedImages.DESC_ERROR_STACK_OBJ);
        hierarchicalImage = SharedImages.getImage(SharedImages.DESC_HIERARCHICAL_LAYOUT_OBJ);
        this.logView = logView;
    }

    @Override
    public void dispose() {
        if (consumers.size() == 0) {
            super.dispose();
        }
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (element instanceof Group) {
            return (columnIndex == 0) ? hierarchicalImage : null;
        }
        LogEntry entry = (LogEntry) element;
        if (columnIndex == 0) {
            switch(entry.getSeverity()) {
                case IStatus.INFO:
                    return infoImage;
                case IStatus.OK:
                    return okImage;
                case IStatus.WARNING:
                    return warningImage;
                default:
                    return (entry.getStack() == null ? errorImage : errorWithStackImage);
            }
        }
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        if ((element instanceof LogSession) && (columnIndex == 2)) {
            LogSession session = (LogSession) element;
            if (session.getDate() == null)
                //$NON-NLS-1$
                return "";
            return dateFormat.format(session.getDate());
        }
        if ((element instanceof Group) && (columnIndex == 0)) {
            return element.toString();
        }
        if (element instanceof LogEntry) {
            LogEntry entry = (LogEntry) element;
            switch(columnIndex) {
                case 0:
                    if (entry.getMessage() != null) {
                        String message = entry.getMessage();
                        if (message.length() > MAX_LABEL_LENGTH) {
                            String warning = Messages.LogViewLabelProvider_truncatedMessage;
                            StringBuffer sb = new StringBuffer(message.substring(0, MAX_LABEL_LENGTH - warning.length()));
                            sb.append(warning);
                            return sb.toString();
                        }
                        return entry.getMessage();
                    }
                case 1:
                    if (entry.getPluginId() != null)
                        return entry.getPluginId();
                case 2:
                    return dateFormat.format(entry.getDate());
            }
        }
        //$NON-NLS-1$
        return "";
    }

    public void connect(Object consumer) {
        if (!consumers.contains(consumer))
            consumers.add(consumer);
    }

    public void disconnect(Object consumer) {
        consumers.remove(consumer);
        if (consumers.size() == 0) {
            dispose();
        }
    }

    @Override
    public Font getFont(Object element, int columnIndex) {
        if ((element instanceof LogSession) && (logView.isCurrentLogSession((LogSession) element))) {
            return JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
        }
        return null;
    }
}
