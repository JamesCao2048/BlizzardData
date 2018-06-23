/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bugs 202583, 207466, 207344
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - bug 272985
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 485843
 *******************************************************************************/
package org.eclipse.ui.internal.views.log;

import com.ibm.icu.text.DateFormat;
import java.io.*;
import java.text.Collator;
import java.util.*;
import java.util.List;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

/**
 * Displays details about Log Entry.
 * Event information is split in three sections: details, stack trace and session. Details
 * contain event date, message and severity. Stack trace is displayed if an exception is bound
 * to event. Stack trace entries can be filtered.
 */
public class EventDetailsDialog extends TrayDialog {

    //$NON-NLS-1$
    public static final String FILTER_ENABLED = "detailsStackFilterEnabled";

    //$NON-NLS-1$
    public static final String FILTER_LIST = "detailsStackFilterList";

    private LogView logView;

    private IMemento memento;

    private AbstractEntry entry;

    // parent of the entry
    private AbstractEntry parentEntry;

    // children of the entry
    private AbstractEntry[] entryChildren;

    private LogViewLabelProvider labelProvider;

    private TreeViewer provider;

    private static int COPY_ID = 22;

    private int childIndex = 0;

    private boolean isOpen;

    private boolean isLastChild;

    private boolean isAtEndOfLog;

    private Label plugInIdLabel;

    private Label severityImageLabel;

    private Label severityLabel;

    private Label dateLabel;

    private Text msgText;

    private Text stackTraceText;

    private Text sessionDataText;

    private Clipboard clipboard;

    private Button copyButton;

    private Button backButton;

    private Button nextButton;

    private SashForm sashForm;

    // sorting
    private Comparator comparator = null;

    Collator collator;

    // patterns for filtering stack traces
    private String[] stackFilterPatterns = null;

    // location configuration
    private Point dialogLocation;

    private Point dialogSize;

    private int[] sashWeights;

    private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    /**
	 *
	 * @param parentShell shell in which dialog is displayed
	 * @param selection entry initially selected and to be displayed
	 * @param provider viewer
	 * @param comparator comparator used to order all entries
	 */
    protected  EventDetailsDialog(Shell parentShell, LogView logView, IAdaptable selection, ISelectionProvider provider, Comparator comparator, IMemento memento) {
        super(parentShell);
        this.logView = logView;
        this.provider = (TreeViewer) provider;
        labelProvider = (LogViewLabelProvider) this.provider.getLabelProvider();
        labelProvider.connect(this);
        this.entry = (AbstractEntry) selection;
        this.comparator = comparator;
        this.memento = memento;
        setShellStyle(SWT.MODELESS | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.CLOSE | SWT.BORDER | SWT.TITLE);
        clipboard = new Clipboard(parentShell.getDisplay());
        initialize();
        collator = Collator.getInstance();
        readConfiguration();
        isLastChild = false;
        isAtEndOfLog = false;
        stackFilterPatterns = getFilters();
    }

    /*
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IHelpContextIds.LOG_EVENTDETAILS);
    }

    private void initialize() {
        parentEntry = (AbstractEntry) entry.getParent(entry);
        if (isChild(entry)) {
            setEntryChildren(parentEntry);
        } else {
            setEntryChildren();
        }
        resetChildIndex();
        isLastChild = false;
        isAtEndOfLog = false;
    }

    private void resetChildIndex() {
        if (entryChildren == null)
            return;
        LogEntry thisEntry = (LogEntry) entry;
        for (int i = 0; i < entryChildren.length; i++) {
            if (entryChildren[i] instanceof LogEntry) {
                LogEntry logEntry = (LogEntry) entryChildren[i];
                if (logEntry == thisEntry) {
                    childIndex = i;
                    return;
                }
            }
        }
        childIndex = 0;
    }

    private boolean isChild(AbstractEntry entry) {
        return entry.getParent(entry) != null;
    }

    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public int open() {
        isOpen = true;
        if (sashWeights == null) {
            int a, b, c;
            int height = getSashForm().getClientArea().height;
            if (height < 250) {
                a = b = c = height / 3;
            } else {
                // Details section needs about 100
                a = 100;
                // Text area gets 100
                c = 100;
                // Stack trace should take up majority of room
                b = height - a - c;
            }
            sashWeights = new int[] { a, b, c };
        }
        getSashForm().setWeights(sashWeights);
        return super.open();
    }

    @Override
    public boolean close() {
        if (clipboard != null) {
            clipboard.dispose();
            clipboard = null;
        }
        storeSettings();
        isOpen = false;
        labelProvider.disconnect(this);
        return super.close();
    }

    @Override
    public void create() {
        super.create();
        // dialog location
        if (dialogLocation != null)
            getShell().setLocation(dialogLocation);
        // dialog size
        if (dialogSize != null)
            getShell().setSize(dialogSize);
        else
            getShell().setSize(500, 550);
        applyDialogFont(buttonBar);
        getButton(IDialogConstants.OK_ID).setFocus();
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (IDialogConstants.OK_ID == buttonId)
            okPressed();
        else if (IDialogConstants.CANCEL_ID == buttonId)
            cancelPressed();
        else if (IDialogConstants.BACK_ID == buttonId)
            backPressed();
        else if (IDialogConstants.NEXT_ID == buttonId)
            nextPressed();
        else if (COPY_ID == buttonId)
            copyPressed();
    }

    protected void backPressed() {
        if (childIndex > 0) {
            if (isLastChild && (isChild(entry))) {
                setEntryChildren(parentEntry);
                isLastChild = false;
            }
            childIndex--;
            entry = entryChildren[childIndex];
        } else {
            if (parentEntry instanceof LogEntry) {
                entry = parentEntry;
                if (isChild(entry)) {
                    setEntryChildren((AbstractEntry) entry.getParent(entry));
                } else {
                    setEntryChildren();
                }
                resetChildIndex();
            }
        }
        setEntrySelectionInTable();
    }

    protected void nextPressed() {
        if (childIndex < entryChildren.length - 1) {
            childIndex++;
            entry = entryChildren[childIndex];
            isLastChild = childIndex == entryChildren.length - 1;
        } else if (isChild(entry) && isLastChild && !isAtEndOfLog) {
            findNextSelectedChild(entry);
        } else // at end of list but can branch into child elements - bug 58083
        {
            setEntryChildren(entry);
            entry = entryChildren[0];
            isAtEndOfLog = entryChildren.length == 0;
            isLastChild = entryChildren.length == 0;
        }
        setEntrySelectionInTable();
    }

    protected void copyPressed() {
        StringWriter writer = new StringWriter();
        PrintWriter pwriter = new PrintWriter(writer);
        entry.write(pwriter);
        pwriter.flush();
        String textVersion = writer.toString();
        try {
            pwriter.close();
            writer.close();
        } catch (IOException // do nothing
        e) {
        }
        // set the clipboard contents
        clipboard.setContents(new Object[] { textVersion }, new Transfer[] { TextTransfer.getInstance() });
    }

    public void setComparator(Comparator comparator) {
        this.comparator = comparator;
        updateProperties();
    }

    private void setComparator(byte sortType, final int sortOrder) {
        if (sortType == LogView.DATE) {
            comparator = new Comparator() {

                @Override
                public int compare(Object e1, Object e2) {
                    Date date1 = ((LogEntry) e1).getDate();
                    Date date2 = ((LogEntry) e2).getDate();
                    if (sortOrder == LogView.ASCENDING)
                        return date1.getTime() < date2.getTime() ? LogView.DESCENDING : LogView.ASCENDING;
                    return date1.getTime() > date2.getTime() ? LogView.DESCENDING : LogView.ASCENDING;
                }
            };
        } else if (sortType == LogView.PLUGIN) {
            comparator = new Comparator() {

                @Override
                public int compare(Object e1, Object e2) {
                    LogEntry entry1 = (LogEntry) e1;
                    LogEntry entry2 = (LogEntry) e2;
                    return collator.compare(entry1.getPluginId(), entry2.getPluginId()) * sortOrder;
                }
            };
        } else {
            comparator = new Comparator() {

                @Override
                public int compare(Object e1, Object e2) {
                    LogEntry entry1 = (LogEntry) e1;
                    LogEntry entry2 = (LogEntry) e2;
                    return collator.compare(entry1.getMessage(), entry2.getMessage()) * sortOrder;
                }
            };
        }
    }

    public void resetSelection(IAdaptable selectedEntry, byte sortType, int sortOrder) {
        setComparator(sortType, sortOrder);
        resetSelection(selectedEntry);
    }

    public void resetSelection(IAdaptable selectedEntry) {
        if (entry.equals(selectedEntry)) {
            updateProperties();
            return;
        }
        if (selectedEntry instanceof AbstractEntry) {
            entry = (AbstractEntry) selectedEntry;
            initialize();
            updateProperties();
        }
    }

    public void resetButtons() {
        backButton.setEnabled(false);
        nextButton.setEnabled(false);
    }

    private void setEntrySelectionInTable() {
        ISelection selection = new StructuredSelection(entry);
        provider.setSelection(selection);
    }

    public void updateProperties() {
        if (isChild(entry)) {
            parentEntry = (AbstractEntry) entry.getParent(entry);
            setEntryChildren(parentEntry);
            resetChildIndex();
            if (childIndex == entryChildren.length - 1)
                isLastChild = true;
        }
        if (entry instanceof LogEntry) {
            LogEntry logEntry = (LogEntry) entry;
            String strDate = dateFormat.format(logEntry.getDate());
            dateLabel.setText(strDate);
            plugInIdLabel.setText(logEntry.getPluginId());
            severityImageLabel.setImage(labelProvider.getColumnImage(entry, 0));
            severityLabel.setText(logEntry.getSeverityText());
            //$NON-NLS-1$
            msgText.setText(logEntry.getMessage() != null ? logEntry.getMessage() : "");
            String stack = logEntry.getStack();
            if (stack != null) {
                stack = filterStack(stack);
                stackTraceText.setText(stack);
            } else {
                stackTraceText.setText(Messages.EventDetailsDialog_noStack);
            }
            if (logEntry.getSession() != null) {
                String session = logEntry.getSession().getSessionData();
                if (session != null) {
                    sessionDataText.setText(session);
                }
            }
        } else {
            //$NON-NLS-1$
            dateLabel.setText("");
            severityImageLabel.setImage(null);
            //$NON-NLS-1$
            severityLabel.setText("");
            //$NON-NLS-1$
            msgText.setText("");
            //$NON-NLS-1$
            stackTraceText.setText("");
            //$NON-NLS-1$
            sessionDataText.setText("");
        }
        updateButtons();
    }

    private void updateButtons() {
        boolean isAtEnd = childIndex == entryChildren.length - 1;
        if (isChild(entry)) {
            boolean canGoToParent = (entry.getParent(entry) instanceof LogEntry);
            backButton.setEnabled((childIndex > 0) || canGoToParent);
            nextButton.setEnabled(nextChildExists(entry, parentEntry, entryChildren) || entry.hasChildren() || !isLastChild || !isAtEnd);
        } else {
            backButton.setEnabled(childIndex != 0);
            nextButton.setEnabled(!isAtEnd || entry.hasChildren());
        }
    }

    private void findNextSelectedChild(AbstractEntry originalEntry) {
        if (isChild(parentEntry)) {
            // we're at the end of the child list; find next parent
            // to select.  If the parent is a child at the end of the child
            // list, find its next parent entry to select, etc.
            entry = parentEntry;
            setEntryChildren((AbstractEntry) parentEntry.getParent(parentEntry));
            parentEntry = (AbstractEntry) parentEntry.getParent(parentEntry);
            resetChildIndex();
            isLastChild = childIndex == entryChildren.length - 1;
            if (isLastChild) {
                findNextSelectedChild(originalEntry);
            } else {
                nextPressed();
            }
        } else if (parentEntry instanceof LogEntry) {
            entry = parentEntry;
            setEntryChildren();
            resetChildIndex();
            isLastChild = childIndex == entryChildren.length - 1;
            if (isLastChild) {
                if (isChild(entry)) {
                    findNextSelectedChild(originalEntry);
                } else {
                    entry = originalEntry;
                    isAtEndOfLog = true;
                    nextPressed();
                }
            } else {
                nextPressed();
            }
        } else {
            entry = originalEntry;
            isAtEndOfLog = true;
            nextPressed();
        }
    }

    private boolean nextChildExists(AbstractEntry originalEntry, AbstractEntry originalParent, AbstractEntry[] originalEntries) {
        if (isChild(parentEntry)) {
            // we're at the end of the child list; find next parent
            // to select.  If the parent is a child at the end of the child
            // list, find its next parent entry to select, etc.
            entry = parentEntry;
            parentEntry = (AbstractEntry) entry.getParent(entry);
            setEntryChildren(parentEntry);
            resetChildIndex();
            if (childIndex == entryChildren.length - 1) {
                return nextChildExists(originalEntry, originalParent, originalEntries);
            }
            entry = originalEntry;
            parentEntry = originalParent;
            entryChildren = originalEntries;
            resetChildIndex();
            return true;
        } else if (parentEntry instanceof LogEntry) {
            entry = parentEntry;
            setEntryChildren();
            childIndex = -1;
            resetChildIndex();
            if ((childIndex != -1) && (childIndex < entryChildren.length - 1)) {
                entry = originalEntry;
                parentEntry = originalParent;
                entryChildren = originalEntries;
                resetChildIndex();
                return true;
            }
        }
        entry = originalEntry;
        parentEntry = originalParent;
        entryChildren = originalEntries;
        resetChildIndex();
        return false;
    }

    /**
	 * Sets entry children (Prev-Next navigable) to top-level elements
	 */
    private void setEntryChildren() {
        AbstractEntry[] children = getElements();
        if (comparator != null)
            Arrays.sort(children, comparator);
        entryChildren = new AbstractEntry[children.length];
        System.arraycopy(children, 0, entryChildren, 0, children.length);
    }

    /**
	 * Sets entry children (Prev-Next navigable) to children of given entry
	 */
    private void setEntryChildren(AbstractEntry entry) {
        Object[] children = entry.getChildren(entry);
        if (comparator != null)
            Arrays.sort(children, comparator);
        List result = new ArrayList();
        for (Object element : children) {
            if (element instanceof AbstractEntry) {
                result.add(element);
            }
        }
        entryChildren = (AbstractEntry[]) result.toArray(new AbstractEntry[result.size()]);
    }

    public SashForm getSashForm() {
        return sashForm;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        container.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_BOTH);
        container.setLayoutData(gd);
        createSashForm(container);
        createDetailsSection(getSashForm());
        createStackSection(getSashForm());
        createSessionSection(getSashForm());
        updateProperties();
        Dialog.applyDialogFont(container);
        return container;
    }

    private void createSashForm(Composite parent) {
        sashForm = new SashForm(parent, SWT.VERTICAL);
        GridLayout layout = new GridLayout();
        layout.marginHeight = layout.marginWidth = 0;
        sashForm.setLayout(layout);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        sashForm.setSashWidth(10);
    }

    private void createToolbarButtonBar(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 0;
        //layout.numColumns = 1;
        comp.setLayout(layout);
        comp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        ((GridData) comp.getLayoutData()).verticalAlignment = SWT.BOTTOM;
        Composite container = new Composite(comp, SWT.NONE);
        layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        //$NON-NLS-1$
        backButton = createButton(container, IDialogConstants.BACK_ID, "", false);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        backButton.setLayoutData(gd);
        backButton.setToolTipText(Messages.EventDetailsDialog_previous);
        backButton.setImage(SharedImages.getImage(SharedImages.DESC_PREV_EVENT));
        backButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {

            @Override
            public void getName(AccessibleEvent e) {
                e.result = Messages.EventDetailsDialog_previous;
            }
        });
        //$NON-NLS-1$
        copyButton = createButton(container, COPY_ID, "", false);
        gd = new GridData();
        copyButton.setLayoutData(gd);
        copyButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_COPY));
        copyButton.setToolTipText(Messages.EventDetailsDialog_copy);
        copyButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {

            @Override
            public void getName(AccessibleEvent e) {
                e.result = Messages.EventDetailsDialog_copy;
            }
        });
        //$NON-NLS-1$
        nextButton = createButton(container, IDialogConstants.NEXT_ID, "", false);
        gd = new GridData();
        nextButton.setLayoutData(gd);
        nextButton.setToolTipText(Messages.EventDetailsDialog_next);
        nextButton.setImage(SharedImages.getImage(SharedImages.DESC_NEXT_EVENT));
        nextButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {

            @Override
            public void getName(AccessibleEvent e) {
                e.result = Messages.EventDetailsDialog_next;
            }
        });
        Button button = new Button(container, SWT.NONE);
        button.setToolTipText(Messages.EventDetailsDialog_ShowFilterDialog);
        button.setImage(SharedImages.getImage(SharedImages.DESC_FILTER));
        gd = new GridData();
        gd.horizontalAlignment = SWT.RIGHT;
        button.setLayoutData(gd);
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FilterDialog dialog = new FilterDialog(getShell(), memento);
                dialog.create();
                dialog.getShell().setText(Messages.EventDetailsDialog_FilterDialog);
                if (dialog.open() == Window.OK) {
                    // update filters and currently displayed stack trace
                    stackFilterPatterns = getFilters();
                    logView.reloadLog();
                    initialize();
                }
                updateProperties();
            }
        });
        button.getAccessible().addAccessibleListener(new AccessibleAdapter() {

            @Override
            public void getName(AccessibleEvent e) {
                e.result = Messages.EventDetailsDialog_FilterDialog;
            }
        });
        // set numColumns at the end, after all createButton() calls, which change this value
        layout.numColumns = 2;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK button only by default
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    private void createDetailsSection(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 0;
        layout.numColumns = 2;
        container.setLayout(layout);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = 200;
        container.setLayoutData(data);
        createTextSection(container);
        createToolbarButtonBar(container);
    }

    private void createTextSection(Composite parent) {
        Composite textContainer = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginHeight = layout.marginWidth = 0;
        textContainer.setLayout(layout);
        textContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
        Label label = new Label(textContainer, SWT.NONE);
        label.setText(Messages.EventDetailsDialog_plugIn);
        plugInIdLabel = new Label(textContainer, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        plugInIdLabel.setLayoutData(gd);
        label = new Label(textContainer, SWT.NONE);
        label.setText(Messages.EventDetailsDialog_severity);
        severityImageLabel = new Label(textContainer, SWT.NONE);
        severityLabel = new Label(textContainer, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        severityLabel.setLayoutData(gd);
        label = new Label(textContainer, SWT.NONE);
        label.setText(Messages.EventDetailsDialog_date);
        dateLabel = new Label(textContainer, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        dateLabel.setLayoutData(gd);
        label = new Label(textContainer, SWT.NONE);
        label.setText(Messages.EventDetailsDialog_message);
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        label.setLayoutData(gd);
        msgText = new Text(textContainer, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
        msgText.setEditable(false);
        gd = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL);
        gd.horizontalSpan = 2;
        gd.grabExcessVerticalSpace = true;
        msgText.setLayoutData(gd);
    }

    private void createStackSection(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        container.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 200;
        container.setLayoutData(gd);
        Label label = new Label(container, SWT.NONE);
        label.setText(Messages.EventDetailsDialog_exception);
        gd = new GridData();
        gd.verticalAlignment = SWT.BOTTOM;
        label.setLayoutData(gd);
        stackTraceText = new Text(container, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = 2;
        stackTraceText.setLayoutData(gd);
        stackTraceText.setEditable(false);
    }

    private void createSessionSection(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        container.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 100;
        container.setLayoutData(gd);
        Label label = new Label(container, SWT.NONE);
        label.setText(Messages.EventDetailsDialog_session);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        label.setLayoutData(gd);
        sessionDataText = new Text(container, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        sessionDataText.setLayoutData(gd);
        sessionDataText.setEditable(false);
    }

    /**
	 * Loads filters from preferences.
	 * @return filters from preferences or empty array
	 *
	 * @since 3.4
	 */
    private String[] getFilters() {
        Boolean filterEnabled = memento.getBoolean(FILTER_ENABLED);
        String filtersString = memento.getString(FILTER_LIST);
        if ((filterEnabled == null) || (filterEnabled.booleanValue() == false) || filtersString == null) {
            return new String[0];
        }
        //$NON-NLS-1$
        StringTokenizer st = new StringTokenizer(filtersString, ";");
        List filters = new ArrayList();
        while (st.hasMoreElements()) {
            String filter = st.nextToken();
            filters.add(filter);
        }
        return (String[]) filters.toArray(new String[filters.size()]);
    }

    /**
	 * Filters stack trace.
	 * Every stack trace line is compared against all patterns.
	 * If line contains any of pattern strings, it's excluded from output.
	 *
	 * @returns filtered stack trace
	 * @since 3.4
	 */
    private String filterStack(String stack) {
        if (stackFilterPatterns.length == 0) {
            return stack;
        }
        //$NON-NLS-1$
        StringTokenizer st = new StringTokenizer(stack, "\n");
        StringBuffer result = new StringBuffer();
        while (st.hasMoreTokens()) {
            String stackElement = st.nextToken();
            boolean filtered = false;
            int i = 0;
            while ((!filtered) && (i < stackFilterPatterns.length)) {
                filtered = stackElement.indexOf(stackFilterPatterns[i]) >= 0;
                i++;
            }
            if (!filtered) {
                //$NON-NLS-1$
                result.append(stackElement).append(//$NON-NLS-1$
                "\n");
            }
        }
        return result.toString();
    }

    //--------------- configuration handling --------------
    /**
	 * Stores the current state in the dialog settings.
	 * @since 2.0
	 */
    private void storeSettings() {
        writeConfiguration();
    }

    /**
	 * Returns the dialog settings object used to share state
	 * between several event detail dialogs.
	 *
	 * @return the dialog settings to be used
	 */
    private IDialogSettings getDialogSettings() {
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings dialogSettings = settings.getSection(getClass().getName());
        if (dialogSettings == null)
            dialogSettings = settings.addNewSection(getClass().getName());
        return dialogSettings;
    }

    /**
	 * Initializes itself from the dialog settings with the same state
	 * as at the previous invocation.
	 */
    private void readConfiguration() {
        IDialogSettings s = getDialogSettings();
        try {
            //$NON-NLS-1$
            int x = s.getInt("x");
            //$NON-NLS-1$
            int y = s.getInt("y");
            dialogLocation = new Point(x, y);
            //$NON-NLS-1$
            x = s.getInt("width");
            //$NON-NLS-1$
            y = s.getInt("height");
            dialogSize = new Point(x, y);
            sashWeights = new int[3];
            //$NON-NLS-1$
            sashWeights[0] = s.getInt("sashWidth1");
            //$NON-NLS-1$
            sashWeights[1] = s.getInt("sashWidth2");
            //$NON-NLS-1$
            sashWeights[2] = s.getInt("sashWidth3");
        } catch (NumberFormatException e) {
            dialogLocation = null;
            dialogSize = null;
            sashWeights = null;
        }
    }

    private void writeConfiguration() {
        IDialogSettings s = getDialogSettings();
        Point location = getShell().getLocation();
        //$NON-NLS-1$
        s.put("x", location.x);
        //$NON-NLS-1$
        s.put("y", location.y);
        Point size = getShell().getSize();
        //$NON-NLS-1$
        s.put("width", size.x);
        //$NON-NLS-1$
        s.put("height", size.y);
        sashWeights = getSashForm().getWeights();
        //$NON-NLS-1$
        s.put("sashWidth1", sashWeights[0]);
        //$NON-NLS-1$
        s.put("sashWidth2", sashWeights[1]);
        //$NON-NLS-1$
        s.put("sashWidth3", sashWeights[2]);
    }

    /**
	 * Utility method to get all top level elements of the Log View
	 * @return top level elements of the Log View
	 */
    private AbstractEntry[] getElements() {
        return (AbstractEntry[]) ((ITreeContentProvider) provider.getContentProvider()).getElements(null);
    }
}
