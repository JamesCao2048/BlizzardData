/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.viewsupport;

import java.util.ArrayList;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jdt.ui.JavaElementLabels;

public class JavaUILabelProvider implements ILabelProvider, IColorProvider, IStyledLabelProvider {

    protected ListenerList<ILabelProviderListener> fListeners = new ListenerList();

    protected JavaElementImageProvider fImageLabelProvider;

    protected StorageLabelProvider fStorageLabelProvider;

    private ArrayList<ILabelDecorator> fLabelDecorators;

    private int fImageFlags;

    private long fTextFlags;

    /**
	 * Creates a new label provider with default flags.
	 */
    public  JavaUILabelProvider() {
        this(JavaElementLabels.ALL_DEFAULT, JavaElementImageProvider.OVERLAY_ICONS);
    }

    /**
	 * @param textFlags Flags defined in <code>JavaElementLabels</code>.
	 * @param imageFlags Flags defined in <code>JavaElementImageProvider</code>.
	 */
    public  JavaUILabelProvider(long textFlags, int imageFlags) {
        fImageLabelProvider = new JavaElementImageProvider();
        fLabelDecorators = null;
        fStorageLabelProvider = new StorageLabelProvider();
        fImageFlags = imageFlags;
        fTextFlags = textFlags;
    }

    /**
	 * Adds a decorator to the label provider
	 * @param decorator the decorator to add
	 */
    public void addLabelDecorator(ILabelDecorator decorator) {
        if (fLabelDecorators == null) {
            fLabelDecorators = new ArrayList(2);
        }
        fLabelDecorators.add(decorator);
    }

    /**
	 * Sets the textFlags.
	 * @param textFlags The textFlags to set
	 */
    public final void setTextFlags(long textFlags) {
        fTextFlags = textFlags;
    }

    /**
	 * Sets the imageFlags
	 * @param imageFlags The imageFlags to set
	 */
    public final void setImageFlags(int imageFlags) {
        fImageFlags = imageFlags;
    }

    /**
	 * Gets the image flags.
	 * Can be overwritten by super classes.
	 * @return Returns a int
	 */
    public final int getImageFlags() {
        return fImageFlags;
    }

    /**
	 * Gets the text flags.
	 * @return Returns a int
	 */
    public final long getTextFlags() {
        return fTextFlags;
    }

    /**
	 * Evaluates the image flags for a element.
	 * Can be overwritten by super classes.
	 * @param element the element to compute the image flags for
	 * @return Returns a int
	 */
    protected int evaluateImageFlags(Object element) {
        return getImageFlags();
    }

    /**
	 * Evaluates the text flags for a element. Can be overwritten by super classes.
	 * @param element the element to compute the text flags for
	 * @return Returns a int
	 */
    protected long evaluateTextFlags(Object element) {
        return getTextFlags();
    }

    protected Image decorateImage(Image image, Object element) {
        if (fLabelDecorators != null && image != null) {
            for (int i = 0; i < fLabelDecorators.size(); i++) {
                ILabelDecorator decorator = fLabelDecorators.get(i);
                image = decorator.decorateImage(image, element);
            }
        }
        return image;
    }

    @Override
    public Image getImage(Object element) {
        Image result = fImageLabelProvider.getImageLabel(element, evaluateImageFlags(element));
        if (result == null && (element instanceof IStorage)) {
            result = fStorageLabelProvider.getImage(element);
        }
        return decorateImage(result, element);
    }

    protected String decorateText(String text, Object element) {
        if (fLabelDecorators != null && text.length() > 0) {
            for (int i = 0; i < fLabelDecorators.size(); i++) {
                ILabelDecorator decorator = fLabelDecorators.get(i);
                String decorated = decorator.decorateText(text, element);
                if (decorated != null) {
                    text = decorated;
                }
            }
        }
        return text;
    }

    @Override
    public String getText(Object element) {
        String result = JavaElementLabels.getTextLabel(element, evaluateTextFlags(element));
        if (result.length() == 0 && (element instanceof IStorage)) {
            result = fStorageLabelProvider.getText(element);
        }
        return decorateText(result, element);
    }

    @Override
    public StyledString getStyledText(Object element) {
        StyledString string = JavaElementLabels.getStyledTextLabel(element, (evaluateTextFlags(element) | JavaElementLabels.COLORIZE));
        if (string.length() == 0 && (element instanceof IStorage)) {
            string = new StyledString(fStorageLabelProvider.getText(element));
        }
        String decorated = decorateText(string.getString(), element);
        if (decorated != null) {
            return StyledCellLabelProvider.styleDecoratedString(decorated, StyledString.DECORATIONS_STYLER, string);
        }
        return string;
    }

    @Override
    public void dispose() {
        if (fLabelDecorators != null) {
            for (int i = 0; i < fLabelDecorators.size(); i++) {
                ILabelDecorator decorator = fLabelDecorators.get(i);
                decorator.dispose();
            }
            fLabelDecorators = null;
        }
        fStorageLabelProvider.dispose();
        fImageLabelProvider.dispose();
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
        if (fLabelDecorators != null) {
            for (int i = 0; i < fLabelDecorators.size(); i++) {
                ILabelDecorator decorator = fLabelDecorators.get(i);
                decorator.addListener(listener);
            }
        }
        fListeners.add(listener);
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        if (fLabelDecorators != null) {
            for (int i = 0; i < fLabelDecorators.size(); i++) {
                ILabelDecorator decorator = fLabelDecorators.get(i);
                decorator.removeListener(listener);
            }
        }
        fListeners.remove(listener);
    }

    public static ILabelDecorator[] getDecorators(boolean errortick, ILabelDecorator extra) {
        if (errortick) {
            if (extra == null) {
                return new ILabelDecorator[] {};
            } else {
                return new ILabelDecorator[] { extra };
            }
        }
        if (extra != null) {
            return new ILabelDecorator[] { extra };
        }
        return null;
    }

    @Override
    public Color getForeground(Object element) {
        return null;
    }

    @Override
    public Color getBackground(Object element) {
        return null;
    }

    /**
     * Fires a label provider changed event to all registered listeners
     * Only listeners registered at the time this method is called are notified.
     *
     * @param event a label provider changed event
     *
     * @see ILabelProviderListener#labelProviderChanged
     */
    protected void fireLabelProviderChanged(final LabelProviderChangedEvent event) {
        for (final ILabelProviderListener l : fListeners) {
            SafeRunner.run(new SafeRunnable() {

                @Override
                public void run() {
                    l.labelProviderChanged(event);
                }
            });
        }
    }
}
