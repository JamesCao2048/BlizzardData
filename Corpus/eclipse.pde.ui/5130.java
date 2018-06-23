/*******************************************************************************
 * Copyright (c) 2016 Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Xenos (Google) - initial API and implementation
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 499226
 *******************************************************************************/
package org.eclipse.pde.internal.runtime.spy.dialogs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.databinding.observable.list.ComputedList;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffectFactory;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.databinding.swt.*;
import org.eclipse.jface.databinding.viewers.*;
import org.eclipse.jface.layout.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.runtime.PDERuntimePluginImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * Implementation of the "layout spy" dialog, a diagnostic tool for fixing bugs
 * related to control positioning and the implementation of SWT {@link Control}s
 * and {@link Layout}s.
 */
public class LayoutSpyDialog {

    private static final int EDGE_SIZE = 4;

    private static final RGB SELECTED_PARENT_OVERLAY_COLOR = new RGB(255, 0, 0);

    private static final RGB SELECTED_CHILD_OVERLAY_COLOR = new RGB(255, 255, 0);

    /**
	 * Value used to indicate an unknown hint value
	 */
    private static final int UNKNOWN = -2;

    private Shell shell;

    // Controls
    private TableViewer childList;

    private Text details;

    private Button selectWidgetButton;

    private Button goUpButton;

    private Button goDownButton;

    private Shell overlay;

    // Model
    private WritableValue<@Nullable Composite> parentControl = new WritableValue(null, null);

    private WritableValue<Boolean> controlSelectorOpen = new WritableValue(Boolean.FALSE, null);

    private ComputedList<Control> listContents;

    private IViewerObservableValue selectedChild;

    private Color parentRectangleColor;

    private Color childRectangleColor;

    private ResourceManager resources;

    private Region region;

    private ISWTObservableValue overlayEnabled;

    private Image upImage;

    private Text diagnostics;

    /**
	 * Creates the dialog but does not make it visible.
	 * 
	 * @param parentShell
	 *            the parent shell
	 */
    public  LayoutSpyDialog(Shell parentShell) {
        overlay = new Shell(SWT.ON_TOP | SWT.NO_TRIM);
        {
            overlay.addPaintListener(this::<>paintOverlay);
            region = new Region();
            overlay.addDisposeListener(( DisposeEvent) -> {
                region.dispose();
            });
            overlay.setRegion(region);
        }
        shell = new Shell(parentShell, SWT.SHELL_TRIM);
        shell.setText(Messages.LayoutSpyDialog_shell_text);
        resources = new LocalResourceManager(JFaceResources.getResources(), shell);
        parentRectangleColor = resources.createColor(SELECTED_PARENT_OVERLAY_COLOR);
        childRectangleColor = resources.createColor(SELECTED_CHILD_OVERLAY_COLOR);
        upImage = resources.createImage(PDERuntimePluginImages.UP_NAV);
        Composite infoRegion = new Composite(shell, SWT.NONE);
        {
            Composite headerRegion = new Composite(infoRegion, SWT.NONE);
            {
                Button upButton = new Button(headerRegion, SWT.PUSH | SWT.CENTER);
                upButton.setImage(upImage);
                upButton.addListener(SWT.Selection,  event -> goUp());
                GridDataFactory.fillDefaults().applyTo(upButton);
                Label childrenLabel = new Label(headerRegion, SWT.NONE);
                childrenLabel.setText(Messages.LayoutSpyDialog_label_children);
            }
            GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(headerRegion);
            Label detailsLabel = new Label(infoRegion, SWT.NONE);
            detailsLabel.setText(Messages.LayoutSpyDialog_label_layout);
            childList = new TableViewer(infoRegion);
            details = new Text(infoRegion, SWT.READ_ONLY | SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
            GridDataFactory.fillDefaults().hint(300, 300).grab(true, true).applyTo(details);
            GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).generateLayout(infoRegion);
        }
        diagnostics = new Text(shell, SWT.READ_ONLY | SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        GridDataFactory.fillDefaults().hint(300, 300).grab(true, true).applyTo(diagnostics);
        Button showOverlayButton = new Button(shell, SWT.CHECK);
        showOverlayButton.setText(Messages.LayoutSpyDialog_button_show_overlay);
        Composite buttonBar = new Composite(shell, SWT.NONE);
        {
            selectWidgetButton = new Button(buttonBar, SWT.PUSH);
            selectWidgetButton.setText(Messages.LayoutSpyDialog_button_select_control);
            goUpButton = new Button(buttonBar, SWT.PUSH);
            goUpButton.setText(Messages.LayoutSpyDialog_button_open_parent);
            goDownButton = new Button(buttonBar, SWT.PUSH);
            goDownButton.setText(Messages.LayoutSpyDialog_button_open_child);
            GridLayoutFactory.fillDefaults().numColumns(3).generateLayout(buttonBar);
        }
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(buttonBar);
        GridLayoutFactory.fillDefaults().margins(LayoutConstants.getMargins()).generateLayout(shell);
        // Attach listeners
        shell.addDisposeListener( event -> disposed());
        selectWidgetButton.addListener(SWT.Selection,  event -> selectControl());
        goUpButton.addListener(SWT.Selection,  event -> goUp());
        goDownButton.addListener(SWT.Selection,  event -> goDown());
        childList.addOpenListener( event -> {
            goDown();
        });
        // Set up the model
        selectedChild = ViewerProperties.singleSelection().observe(childList);
        overlayEnabled = WidgetProperties.selection().observe(showOverlayButton);
        childList.setContentProvider(new ObservableListContentProvider());
        listContents = new ComputedList<Control>() {

            @Override
            protected List<Control> calculate() {
                Composite control = parentControl.getValue();
                if (control == null) {
                    return Arrays.asList(Display.getCurrent().getShells());
                }
                return Arrays.asList(control.getChildren());
            }
        };
        childList.setInput(listContents);
        ISideEffectFactory sideEffectFactory = WidgetSideEffects.createFactory(shell);
        sideEffectFactory.create(this::<>computeParentInfo, details::<>setText);
        sideEffectFactory.create(this::<>computeChildInfo, diagnostics::<>setText);
        sideEffectFactory.create(this::<>updateOverlay);
        openComposite(parentShell);
    }

    /**
	 * Opens the dialog box, revealing it to the user.
	 */
    public void open() {
        this.shell.pack();
        this.shell.open();
    }

    /**
	 * Disposes the dialog box.
	 */
    public void close() {
        this.shell.dispose();
    }

    /**
	 * Invoked as a callback when the main shell is disposed.
	 */
    private void disposed() {
        listContents.dispose();
        selectedChild.dispose();
        parentControl.dispose();
        overlay.dispose();
    }

    private void openComposite(Composite composite) {
        parentControl.setValue(composite);
    }

    /**
	 * Returns the currently-selected child control or null if none.
	 */
    @Nullable
    private Control getSelectedChild() {
        return (@Nullable Control) selectedChild.getValue();
    }

    /**
	 * Opens the given control in the layout spy.
	 */
    @SuppressWarnings("unchecked")
    private void openControl(Control control) {
        Composite parent = control.getParent();
        if (parent == null) {
            if (control instanceof Composite) {
                parentControl.setValue((Composite) control);
            }
        } else {
            parentControl.setValue(parent);
            selectedChild.setValue(control);
        }
    }

    // Overlay management
    // -----------------------------------------------------------------
    /**
	 * This callback is used to update the bounds and visible region for the
	 * overlay shell. It is used as part of a side-effect, so if it makes use of
	 * any tracked getters, it will automatically be invoked again whenever one
	 * of those tracked getters changes state.
	 *
	 * @TrackedGetter
	 */
    public void updateOverlay() {
        @Nullable Composite parent = parentControl.getValue();
        boolean enabled = Boolean.TRUE.equals(overlayEnabled.getValue());
        overlay.setVisible(parent != null && !controlSelectorOpen.getValue() && enabled);
        if (parent == null) {
            return;
        }
        Shell shell = parent.getShell();
        Rectangle outerBounds = Geometry.copy(shell.getBounds());
        overlay.setBounds(outerBounds);
        Rectangle parentBoundsWrtDisplay = GeometryUtil.getDisplayBounds(parent);
        Rectangle parentBoundsWrtOverlay = Geometry.toControl(overlay, parentBoundsWrtDisplay);
        Rectangle innerBoundsWrtOverlay = Geometry.copy(parentBoundsWrtOverlay);
        Geometry.expand(innerBoundsWrtOverlay, -EDGE_SIZE, -EDGE_SIZE, -EDGE_SIZE, -EDGE_SIZE);
        region.dispose();
        region = new Region();
        @Nullable Control child = (@Nullable Control) selectedChild.getValue();
        if (child != null) {
            Rectangle childBoundsWrtOverlay = Geometry.toControl(overlay, GeometryUtil.getDisplayBounds(child));
            Rectangle childInnerBoundsWrtOverlay = Geometry.copy(childBoundsWrtOverlay);
            Geometry.expand(childInnerBoundsWrtOverlay, -EDGE_SIZE, -EDGE_SIZE, -EDGE_SIZE, -EDGE_SIZE);
            region.add(parentBoundsWrtOverlay);
            int distanceToTop = childBoundsWrtOverlay.y - innerBoundsWrtOverlay.y;
            subtractRect(region, GeometryUtil.extrudeEdge(innerBoundsWrtOverlay, distanceToTop, SWT.TOP));
            int distanceToLeft = childBoundsWrtOverlay.x - innerBoundsWrtOverlay.x;
            subtractRect(region, GeometryUtil.extrudeEdge(innerBoundsWrtOverlay, distanceToLeft, SWT.LEFT));
            int distanceToRight = GeometryUtil.getRight(innerBoundsWrtOverlay) - GeometryUtil.getRight(childBoundsWrtOverlay);
            subtractRect(region, GeometryUtil.extrudeEdge(innerBoundsWrtOverlay, distanceToRight, SWT.RIGHT));
            int distanceToBottom = GeometryUtil.getBottom(innerBoundsWrtOverlay) - GeometryUtil.getBottom(childBoundsWrtOverlay);
            subtractRect(region, GeometryUtil.extrudeEdge(innerBoundsWrtOverlay, distanceToBottom, SWT.BOTTOM));
            subtractRect(region, childInnerBoundsWrtOverlay);
        } else {
            region.add(parentBoundsWrtOverlay);
            region.subtract(innerBoundsWrtOverlay);
        }
        overlay.redraw();
        overlay.setRegion(region);
    }

    /**
	 * Paint callback for the overlay shell. This draws rectangles around the
	 * selected layout and the selected child.
	 */
    protected void paintOverlay(PaintEvent e) {
        @Nullable Composite parent = parentControl.getValue();
        if (parent == null) {
            return;
        }
        int halfSize = EDGE_SIZE / 2;
        Rectangle parentDisplayBounds = GeometryUtil.getDisplayBounds(parent);
        Rectangle parentBoundsWrtOverlay = Geometry.toControl(overlay, parentDisplayBounds);
        Geometry.expand(parentBoundsWrtOverlay, -halfSize, -halfSize, -halfSize, -halfSize);
        @Nullable Control child = (@Nullable Control) selectedChild.getValue();
        e.gc.setLineWidth(EDGE_SIZE);
        e.gc.setForeground(parentRectangleColor);
        e.gc.drawRectangle(parentBoundsWrtOverlay.x, parentBoundsWrtOverlay.y, parentBoundsWrtOverlay.width, parentBoundsWrtOverlay.height);
        if (child != null) {
            Rectangle childBoundsWrtOverlay = Geometry.toControl(overlay, GeometryUtil.getDisplayBounds(child));
            Geometry.expand(childBoundsWrtOverlay, -halfSize, -halfSize, -halfSize, -halfSize);
            e.gc.setForeground(childRectangleColor);
            e.gc.drawRectangle(childBoundsWrtOverlay.x, childBoundsWrtOverlay.y, childBoundsWrtOverlay.width, childBoundsWrtOverlay.height);
        }
    }

    // User gesture callbacks
    // -----------------------------------------------------------
    /**
	 * Invoked when the user clicks the "select control" button. It opens some
	 * UI that allows the user to select a new input control for the layout spy.
	 */
    private void selectControl() {
        this.controlSelectorOpen.setValue(true);
        this.shell.setVisible(false);
        new ControlSelector((@Nullable Control control) -> {
            if (control != null) {
                openControl(control);
            }
            this.controlSelectorOpen.setValue(false);
            this.shell.setVisible(true);
        });
    }

    /**
	 * Invoked when the user clicks the "go up" button, which opens the parent.
	 */
    @SuppressWarnings("unchecked")
    private void goUp() {
        @Nullable Composite parent = parentControl.getValue();
        if (parent == null) {
            return;
        }
        Composite ancestor = parent.getParent();
        openComposite(ancestor);
        this.selectedChild.setValue(parent);
    }

    /**
	 * Invoked when the user clicks the "go down" button, which opens the
	 * selected child.
	 */
    private void goDown() {
        Control child = getSelectedChild();
        if (child instanceof Composite) {
            Composite composite = (Composite) child;
            openComposite(composite);
        }
    }

    // Utility functions -----------------------------------------------------
    /**
	 * Subtracts the given rectangle from the given region unless the rectangle
	 * is empty.
	 */
    private static void subtractRect(Region region, Rectangle rect) {
        if (rect.isEmpty()) {
            return;
        }
        region.subtract(rect);
    }

    private String getWarningMessage(String string) {
        return NLS.bind(Messages.LayoutSpyDialog_warning_prefix, string);
    }

    private static String printHint(int hint) {
        if (hint == SWT.DEFAULT) {
            //$NON-NLS-1$
            return "SWT.DEFAULT";
        }
        return Integer.toString(hint);
    }

    private static String printPoint(Point toPrint) {
        //$NON-NLS-1$
        return NLS.bind("({0}, {1})", new Object[] { toPrint.x, toPrint.y });
    }

    // Control classification ------------------------------------------------
    private static boolean isHorizontallyScrollable(Control child) {
        return (child.getStyle() & SWT.H_SCROLL) != 0;
    }

    private static boolean isVerticallyScrollable(Control child) {
        return (child.getStyle() & SWT.V_SCROLL) != 0;
    }

    /**
	 * Computes the values that should be subtracted off the width and height
	 * hints from computeSize on the given control.
	 */
    private static Point computeHintAdjustment(Control control) {
        int widthAdjustment;
        int heightAdjustment;
        if (control instanceof Scrollable) {
            // For composites, subtract off the trim size
            Scrollable composite = (Scrollable) control;
            Rectangle trim = composite.computeTrim(0, 0, 0, 0);
            widthAdjustment = trim.width;
            heightAdjustment = trim.height;
        } else {
            // For non-composites, subtract off 2 * the border size
            widthAdjustment = control.getBorderWidth() * 2;
            heightAdjustment = widthAdjustment;
        }
        return new Point(widthAdjustment, heightAdjustment);
    }

    /**
	 * Returns true if the given control is a composite which can expand in the
	 * given dimension. Returns false if the control either cannot expand in the
	 * given dimension or if its growable characteristics cannot be computed in
	 * that dimension.
	 */
    private static boolean isGrowableLayout(Control control, boolean horizontal) {
        if (control instanceof Composite) {
            Composite composite = (Composite) control;
            Layout theLayout = composite.getLayout();
            if (theLayout instanceof GridLayout) {
                Control[] children = composite.getChildren();
                for (int i = 0; i < children.length; i++) {
                    Control child = children[i];
                    GridData data = (GridData) child.getLayoutData();
                    if (data != null) {
                        if (horizontal) {
                            if (data.grabExcessHorizontalSpace) {
                                return true;
                            }
                        } else {
                            if (data.grabExcessVerticalSpace) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
	 * Returns true iff another visible widget in the same shell overlaps the
	 * given control.
	 */
    private static boolean overlapsSibling(Control toFind) {
        Composite parent = toFind.getParent();
        Control current = toFind;
        Rectangle displayBounds = GeometryUtil.getDisplayBounds(toFind);
        while (parent != null && !(parent instanceof Shell)) {
            for (Control nextSibling : parent.getChildren()) {
                if (nextSibling == current) {
                    continue;
                }
                if (!nextSibling.isVisible()) {
                    continue;
                }
                Rectangle nextSiblingBounds = GeometryUtil.getDisplayBounds(nextSibling);
                if (nextSiblingBounds.intersects(displayBounds)) {
                    return true;
                }
            }
            current = parent;
            parent = parent.getParent();
        }
        return false;
    }

    /**
	 * Computes the string that will be shown in the text box which displays
	 * information about the selected child. This is a tracked getter -- if it
	 * reads from a databinding observable, the text box will automatically
	 * refresh in response to changes in that observable.
	 * 
	 * @TrackedGetter
	 */
    private String computeChildInfo() {
        StringBuilder builder = new StringBuilder();
        Control child = getSelectedChild();
        if (child != null) {
            builder.append(child.getClass().getName());
            //$NON-NLS-1$
            builder.append("\n\n");
            int widthHintFromLayoutData = UNKNOWN;
            int heightHintFromLayoutData = UNKNOWN;
            Object layoutData = child.getLayoutData();
            if (layoutData == null) {
                //$NON-NLS-1$
                builder.append(//$NON-NLS-1$
                "getLayoutData() == null\n");
            } else if (layoutData instanceof GridData) {
                GridData grid = (GridData) layoutData;
                builder.append(GridDataFactory.createFrom(grid));
                widthHintFromLayoutData = grid.widthHint;
                heightHintFromLayoutData = grid.heightHint;
                if (!grid.grabExcessHorizontalSpace) {
                    if (isHorizontallyScrollable(child) || isGrowableLayout(child, true)) {
                        builder.append(getWarningMessage(Messages.LayoutSpyDialog_warning_grab_horizontally_scrolling));
                    }
                }
                if (!grid.grabExcessVerticalSpace) {
                    if (isVerticallyScrollable(child) || isGrowableLayout(child, false)) {
                        builder.append(getWarningMessage(Messages.LayoutSpyDialog_warning_grab_vertical_scrolling));
                    }
                }
            } else if (layoutData instanceof FormData) {
                FormData data = (FormData) layoutData;
                widthHintFromLayoutData = data.width;
                heightHintFromLayoutData = data.height;
            } else {
                describeObject(//$NON-NLS-1$
                builder, //$NON-NLS-1$
                "data", //$NON-NLS-1$
                layoutData);
            }
            if (isHorizontallyScrollable(child)) {
                if (widthHintFromLayoutData == SWT.DEFAULT) {
                    builder.append(getWarningMessage(Messages.LayoutSpyDialog_warning_hint_for_horizontally_scrollable));
                }
            }
            if (isVerticallyScrollable(child)) {
                if (heightHintFromLayoutData == SWT.DEFAULT) {
                    builder.append(getWarningMessage(Messages.LayoutSpyDialog_warning_hint_for_vertically_scrollable));
                }
            }
            //$NON-NLS-1$
            builder.append("\n");
            // Print the current dimensions
            Rectangle bounds = child.getBounds();
            //$NON-NLS-1$
            builder.append(NLS.bind("getBounds() = {0}", bounds.toString()));
            //$NON-NLS-1$
            builder.append("\n");
            Point adjustment = computeHintAdjustment(child);
            int widthHint = Math.max(0, bounds.width - adjustment.x);
            int heightHint = Math.max(0, bounds.height - adjustment.y);
            builder.append(//$NON-NLS-1$
            NLS.bind(//$NON-NLS-1$
            "widthAdjustment = {0}, heightAdjustment = {1}", new Object[] { adjustment.x, adjustment.y }));
            //$NON-NLS-1$
            builder.append("\n\n");
            // Print the default size
            Point defaultSize = child.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
            //$NON-NLS-1$
            builder.append(NLS.bind("computeSize(SWT.DEFAULT, SWT.DEFAULT, false) = {0}", printPoint(defaultSize)));
            //$NON-NLS-1$
            builder.append("\n");
            // Print the preferred horizontally-wrapped size:
            Point hWrappedSize = child.computeSize(widthHint, SWT.DEFAULT, false);
            builder.append(//$NON-NLS-1$
            NLS.bind(//$NON-NLS-1$
            "computeSize({0} - widthAdjustment, SWT.DEFAULT, false) = {1}", new Object[] { bounds.width, printPoint(hWrappedSize) }));
            //$NON-NLS-1$
            builder.append("\n");
            // Print the preferred vertically-wrapped size:
            Point vWrappedSize = child.computeSize(SWT.DEFAULT, heightHint, false);
            builder.append(//$NON-NLS-1$
            NLS.bind(//$NON-NLS-1$
            "computeSize(SWT.DEFAULT, {0} - heightAdjustment, false) = {1}", new Object[] { bounds.height, printPoint(vWrappedSize) }));
            //$NON-NLS-1$
            builder.append("\n");
            // Check for warnings
            Point noOpSize = child.computeSize(widthHint, heightHint, false);
            if (noOpSize.x != bounds.width || noOpSize.y != bounds.height) {
                builder.append(getWarningMessage(NLS.bind(Messages.LayoutSpyDialog_warning_unexpected_compute_size, new Object[] { printHint(widthHint), printHint(heightHint), printPoint(noOpSize) })));
            }
            if (bounds.height < hWrappedSize.y) {
                builder.append(getWarningMessage(Messages.LayoutSpyDialog_warning_shorter_than_preferred_size));
            }
            printReasonControlIsInvisible(builder, child);
        }
        return builder.toString();
    }

    /**
	 * If the control cannot be seen by the user, this method adds a warning
	 * message to the given builder explaining the reason why the control cannot
	 * be seen.
	 */
    private void printReasonControlIsInvisible(StringBuilder builder, Control control) {
        if (!control.isVisible()) {
            //$NON-NLS-1$
            builder.append(getWarningMessage("isVisible() == false"));
            return;
        }
        Rectangle bounds = control.getBounds();
        if (bounds.isEmpty()) {
            builder.append(getWarningMessage(Messages.LayoutSpyDialog_warning_zero_size));
            return;
        }
        Rectangle displayBounds = GeometryUtil.getDisplayBounds(control);
        Composite parent = control.getParent();
        if (parent != null) {
            Rectangle parentDisplayBounds = GeometryUtil.getDisplayBounds(parent);
            Rectangle intersection = displayBounds.intersection(parentDisplayBounds);
            if (intersection.isEmpty()) {
                builder.append(getWarningMessage(Messages.LayoutSpyDialog_warning_bounds_outside_parent));
                return;
            }
            if (intersection.width < bounds.width || intersection.height < bounds.height) {
                builder.append(getWarningMessage(Messages.LayoutSpyDialog_warning_control_partially_clipped));
                return;
            }
            if (overlapsSibling(control)) {
                builder.append(getWarningMessage(Messages.LayoutSpyDialog_warning_control_overlaps_siblings));
                return;
            }
        }
    }

    /**
	 * Computes the string that will be shown in the text box which displays
	 * information about the selected layout. This is a tracked getter: if it
	 * reads from an observable, the text box will update automatically when the
	 * observable changes.
	 * 
	 * @TrackedGetter
	 */
    private String computeParentInfo() {
        StringBuilder builder = new StringBuilder();
        @Nullable Composite parent = parentControl.getValue();
        if (parent != null) {
            builder.append(parent.getClass().getName());
            //$NON-NLS-1$
            builder.append("\n\n");
            Rectangle parentBounds = GeometryUtil.getDisplayBounds(parent);
            Layout layout = parent.getLayout();
            if (layout != null) {
                if (layout instanceof GridLayout) {
                    GridLayout grid = (GridLayout) layout;
                    builder.append(GridLayoutFactory.createFrom(grid));
                    boolean hasVerticallyTruncadeControls = false;
                    boolean hasHorizontallyTruncadeControls = false;
                    boolean hasHorizontalGrab = false;
                    boolean hasVerticalGrab = false;
                    for (Control next : parent.getChildren()) {
                        @Nullable GridData data = (GridData) next.getLayoutData();
                        if (data == null) {
                            continue;
                        }
                        Rectangle childBounds = GeometryUtil.getDisplayBounds(parent);
                        Rectangle intersection = childBounds.intersection(parentBounds);
                        if (intersection.width < childBounds.width) {
                            hasHorizontallyTruncadeControls = true;
                        }
                        if (intersection.height < childBounds.height) {
                            hasVerticallyTruncadeControls = true;
                        }
                        hasHorizontalGrab = hasHorizontalGrab || data.grabExcessHorizontalSpace;
                        hasVerticalGrab = hasVerticalGrab || data.grabExcessVerticalSpace;
                    }
                    if (hasHorizontallyTruncadeControls && !hasHorizontalGrab) {
                        builder.append(getWarningMessage(Messages.LayoutSpyDialog_warning_not_grabbing_horizontally));
                    }
                    if (hasVerticallyTruncadeControls && !hasVerticalGrab) {
                        builder.append(getWarningMessage(Messages.LayoutSpyDialog_warning_not_grabbing_vertically));
                    }
                } else {
                    describeObject(//$NON-NLS-1$
                    builder, //$NON-NLS-1$
                    "layout", //$NON-NLS-1$
                    layout);
                }
            }
        } else {
            builder.append(Messages.LayoutSpyDialog_label_no_parent_control_selected);
        }
        return builder.toString();
    }

    /**
	 * Uses reflection to print the values of the given object's public fields.
	 */
    void describeObject(StringBuilder result, String variableName, Object toDescribe) {
        @SuppressWarnings("rawtypes") Class clazz = toDescribe.getClass();
        result.append(clazz.getName());
        //$NON-NLS-1$
        result.append(" ");
        result.append(variableName);
        //$NON-NLS-1$
        result.append(";\n");
        Field[] fields = clazz.getFields();
        for (Field nextField : fields) {
            int modifiers = nextField.getModifiers();
            if (!Modifier.isPublic(modifiers)) {
                continue;
            }
            try {
                //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                String next = variableName + "." + nextField.getName() + " = " + nextField.get(toDescribe) + ";";
                result.append(next);
                //$NON-NLS-1$
                result.append(//$NON-NLS-1$
                "\n");
            } catch (IllegalArgumentException | IllegalAccessException e) {
            }
        }
    }
}
